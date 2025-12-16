package com.reuben.pastcare_spring.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.reuben.pastcare_spring.dtos.AuthLoginRequest;
import com.reuben.pastcare_spring.dtos.AuthRegistrationRequest;
import com.reuben.pastcare_spring.dtos.AuthTokenData;
import com.reuben.pastcare_spring.dtos.UserResponse;
import com.reuben.pastcare_spring.exceptions.AccountLockedException;
import com.reuben.pastcare_spring.exceptions.TooManyRequestsException;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.security.JwtUtil;



@Service
public class AuthService {
  @Autowired
  UserRepository userRepository;

  @Autowired
  private AuthenticationManager authenticationManager;

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private BruteForceProtectionService bruteForceProtectionService;

  @Autowired
  private RefreshTokenService refreshTokenService;


  public AuthTokenData login(AuthLoginRequest request, HttpServletRequest httpRequest) {
    String email = request.email();
    String ipAddress = bruteForceProtectionService.getClientIp(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    // Check if IP is blocked
    if (bruteForceProtectionService.isIpBlocked(ipAddress)) {
      throw new TooManyRequestsException("Too many failed login attempts from this IP address. Please try again later.");
    }

    // Check if account is locked
    if (bruteForceProtectionService.isAccountLocked(email)) {
      java.time.LocalDateTime lockedUntil = bruteForceProtectionService.getAccountLockedUntil(email);
      throw new AccountLockedException(
          "Your account has been temporarily locked due to multiple failed login attempts.",
          lockedUntil
      );
    }

    try {
      // Attempt authentication
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(email, request.password())
      );

      // Authentication successful
      final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
      User user = userRepository.findByEmail(email).orElseThrow(() ->
          new RuntimeException("User not found after successful authentication"));

      // Validate user has a church (tenant)
      if (user.getChurch() == null) {
        throw new RuntimeException("User must be associated with a church");
      }

      // Generate JWT with tenant claims
      String accessToken = jwtUtil.generateToken(
          userDetails,
          user.getId(),
          user.getChurch().getId(),
          user.getRole().name(),
          request.rememberMe()
      );

      // Create refresh token
      com.reuben.pastcare_spring.models.RefreshToken refreshToken = refreshTokenService.createRefreshToken(
          user,
          user.getChurch(),
          ipAddress,
          userAgent
      );

      // Record successful login
      bruteForceProtectionService.recordLoginAttempt(email, ipAddress, userAgent, true);

      return new AuthTokenData(
          accessToken,
          refreshToken.getToken(),
          new UserResponse(
              user.getId(),
              user.getName(),
              user.getEmail(),
              user.getPhoneNumber(),
              user.getTitle(),
              user.getChurch(),
              user.getFellowships(),
              user.getPrimaryService(),
              user.getRole()
          )
      );

    } catch (AuthenticationException e) {
      // Record failed login attempt
      bruteForceProtectionService.recordLoginAttempt(email, ipAddress, userAgent, false);

      // Re-throw the exception to be handled by global exception handler
      throw e;
    }
  }

  public User register(AuthRegistrationRequest authRequest) {
    User user = new User();
    user.setName(authRequest.name());
    user.setEmail(authRequest.email());
    user.setPassword(authRequest.password());
    user.setRole(authRequest.role());
    user.setPhoneNumber(authRequest.phoneNumber());
    user.setPassword(new BCryptPasswordEncoder().encode(authRequest.password()));
    return userRepository.save(user);
  }

  public void logout(String refreshToken) {
    refreshTokenService.revokeToken(refreshToken);
  }

  public AuthTokenData refreshAccessToken(String refreshTokenValue, HttpServletRequest request) {
    // Find and validate refresh token
    com.reuben.pastcare_spring.models.RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenValue)
        .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

    // Update last used
    refreshTokenService.updateLastUsed(refreshToken);

    User user = refreshToken.getUser();
    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());

    // Generate new access token
    String newAccessToken = jwtUtil.generateToken(
        userDetails,
        user.getId(),
        user.getChurch().getId(),
        user.getRole().name(),
        false // Refresh token already handles remember me
    );

    return new AuthTokenData(
        newAccessToken,
        null, // Don't rotate refresh token on every access token refresh
        new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getTitle(),
            user.getChurch(),
            user.getFellowships(),
            user.getPrimaryService(),
            user.getRole()
        )
    );
  }
}
