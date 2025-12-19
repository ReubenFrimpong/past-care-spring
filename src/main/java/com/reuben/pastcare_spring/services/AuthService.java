package com.reuben.pastcare_spring.services;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reuben.pastcare_spring.dtos.AuthLoginRequest;
import com.reuben.pastcare_spring.dtos.AuthRegistrationRequest;
import com.reuben.pastcare_spring.dtos.AuthTokenData;
import com.reuben.pastcare_spring.dtos.UserChurchRegistrationRequest;
import com.reuben.pastcare_spring.dtos.UserResponse;
import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.exceptions.AccountLockedException;
import com.reuben.pastcare_spring.exceptions.DuplicateChurchException;
import com.reuben.pastcare_spring.exceptions.DuplicateUserException;
import com.reuben.pastcare_spring.exceptions.TooManyRequestsException;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.RefreshToken;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.security.JwtUtil;



@Service
public class AuthService {
  @Autowired
  UserRepository userRepository;

  @Autowired
  ChurchRepository churchRepository;

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

  @Autowired
  private BCryptPasswordEncoder passwordEncoder;


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
      LocalDateTime lockedUntil = bruteForceProtectionService.getAccountLockedUntil(email);
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

      // Validate church association (except for SUPERADMIN)
      user.validateChurchAssociation();

      // Generate JWT with tenant claims (churchId can be null for SUPERADMIN)
      Long churchId = user.getChurch() != null ? user.getChurch().getId() : null;
      String accessToken = jwtUtil.generateToken(
          userDetails,
          user.getId(),
          churchId,
          user.getRole().name(),
          request.rememberMe()
      );

      // Create refresh token (church can be null for SUPERADMIN)
      RefreshToken refreshToken = refreshTokenService.createRefreshToken(
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
    user.setPassword(passwordEncoder.encode(authRequest.password()));
    return userRepository.save(user);
  }

  /**
   * Register a new church along with the first admin user.
   * This is a transactional operation - both church and user are created together.
   *
   * @param request Contains both church and user registration data
   * @return AuthTokenData with access token, refresh token, and user info
   * @throws DuplicateChurchException if a church with the same name exists
   * @throws DuplicateUserException if a user with the same email exists
   */
  @Transactional
  public AuthTokenData registerNewChurch(UserChurchRegistrationRequest request, HttpServletRequest httpRequest) {
    String churchName = request.church().name();
    String userEmail = request.user().email();

    // Check for duplicate church name
    if (churchRepository.existsByNameIgnoreCase(churchName)) {
      throw new DuplicateChurchException(
          "A church with the name '" + churchName + "' already exists. Please choose a different name.");
    }

    // Check for duplicate user email
    if (userRepository.findByEmail(userEmail).isPresent()) {
      throw new DuplicateUserException(
          "An account with the email '" + userEmail + "' already exists. Please use a different email or login.");
    }

    // Create the church
    Church church = new Church();
    church.setName(request.church().name());
    church.setAddress(request.church().address());
    church.setPhoneNumber(request.church().phoneNumber());
    church.setEmail(request.church().email());
    church.setWebsite(request.church().website());
    church = churchRepository.save(church);

    // Create the first admin user for this church
    User user = new User();
    user.setName(request.user().name());
    user.setEmail(request.user().email());
    user.setPassword(passwordEncoder.encode(request.user().password()));
    user.setPhoneNumber(request.user().phoneNumber());
    user.setRole(Role.ADMIN); // First user is always an admin
    user.setChurch(church);
    user = userRepository.save(user);

    // Generate tokens for automatic login
    UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
    String accessToken = jwtUtil.generateToken(
        userDetails,
        user.getId(),
        church.getId(),
        user.getRole().name(),
        false
    );

    String ipAddress = bruteForceProtectionService.getClientIp(httpRequest);
    String userAgent = httpRequest.getHeader("User-Agent");

    com.reuben.pastcare_spring.models.RefreshToken refreshToken = refreshTokenService.createRefreshToken(
        user,
        church,
        ipAddress,
        userAgent
    );

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

    // Generate new access token (churchId can be null for SUPERADMIN)
    Long churchId = user.getChurch() != null ? user.getChurch().getId() : null;
    String newAccessToken = jwtUtil.generateToken(
        userDetails,
        user.getId(),
        churchId,
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
