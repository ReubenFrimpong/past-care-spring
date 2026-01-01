package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.AuthLoginRequest;
import com.reuben.pastcare_spring.dtos.AuthRegistrationRequest;
import com.reuben.pastcare_spring.dtos.AuthResponse;
import com.reuben.pastcare_spring.dtos.AuthTokenData;
import com.reuben.pastcare_spring.dtos.UserChurchRegistrationRequest;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.models.PortalUser;
import com.reuben.pastcare_spring.services.AuthService;
import com.reuben.pastcare_spring.services.PortalUserService;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.repositories.PortalUserRepository;
import com.reuben.pastcare_spring.dtos.PortalLoginRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.reuben.pastcare_spring.security.CookieUtil;


@RestController
@RequestMapping("/api/auth")
public class AuthController {
  @Autowired
  private AuthService authService;

  @Autowired
  private PortalUserService portalUserService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private PortalUserRepository portalUserRepository;

  @Autowired
  private CookieUtil cookieUtil;

  @Value("${jwt.expiration:3600000}") // 1 hour in ms
  private Long accessTokenExpiration;

  @Value("${jwt.expiration.remember-me:2592000000}") // 30 days in ms
  private Long rememberMeExpiration;

  @Value("${jwt.refresh-token.expiration:2592000000}") // 30 days in ms
  private Long refreshTokenExpiration;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(
      @Valid @RequestBody AuthLoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  ) {
    // Get token data from auth service
    AuthTokenData tokenData = authService.login(request, httpRequest);

    // Set HttpOnly cookies for tokens
    // If rememberMe is true, use extended expiration for access token cookie to match JWT expiration
    long tokenExpiration = request.rememberMe() ? rememberMeExpiration : accessTokenExpiration;
    int accessTokenMaxAge = (int) (tokenExpiration / 1000); // Convert to seconds
    int refreshTokenMaxAge = (int) (refreshTokenExpiration / 1000);

    httpResponse.addCookie(cookieUtil.createAccessTokenCookie(
        tokenData.accessToken(),
        accessTokenMaxAge
    ));

    httpResponse.addCookie(cookieUtil.createRefreshTokenCookie(
        tokenData.refreshToken(),
        refreshTokenMaxAge
    ));

    // Return response with only user info (tokens are in HttpOnly cookies)
    return ResponseEntity.ok(new AuthResponse(tokenData.user()));
  }

  /**
   * Unified login endpoint - detects user type (admin/staff vs portal user) automatically.
   * Tries admin login first, then portal user login if not found.
   *
   * @param request Login credentials (email + password)
   * @param httpRequest HTTP request for IP tracking
   * @param httpResponse HTTP response for setting cookies
   * @return Unified response with user type indicator
   */
  @PostMapping("/unified-login")
  public ResponseEntity<Map<String, Object>> unifiedLogin(
      @Valid @RequestBody AuthLoginRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  ) {
    String email = request.email();

    // Try admin/staff login first (users table)
    if (userRepository.findByEmail(email).isPresent()) {
      try {
        // Get token data from auth service
        AuthTokenData tokenData = authService.login(request, httpRequest);

        // Set HttpOnly cookies for tokens
        long tokenExpiration = request.rememberMe() ? rememberMeExpiration : accessTokenExpiration;
        int accessTokenMaxAge = (int) (tokenExpiration / 1000);
        int refreshTokenMaxAge = (int) (refreshTokenExpiration / 1000);

        httpResponse.addCookie(cookieUtil.createAccessTokenCookie(
            tokenData.accessToken(),
            accessTokenMaxAge
        ));

        httpResponse.addCookie(cookieUtil.createRefreshTokenCookie(
            tokenData.refreshToken(),
            refreshTokenMaxAge
        ));

        // Return admin user response with type indicator
        Map<String, Object> response = new HashMap<>();
        response.put("userType", "ADMIN");
        response.put("user", tokenData.user());
        response.put("redirectTo", tokenData.user().role().toString().equals("SUPERADMIN")
            ? "/platform-admin"
            : "/dashboard");

        return ResponseEntity.ok(response);
      } catch (Exception e) {
        // If admin login fails, don't try portal - just throw error
        throw e;
      }
    }

    // Try portal user login (portal_users table)
    // Find all churches where this email exists as a portal user
    List<PortalUser> portalUsers = portalUserRepository.findByEmail(email);

    if (portalUsers.isEmpty()) {
      throw new RuntimeException("Invalid email or password");
    }

    // If user belongs to multiple churches, use the first active one
    // TODO: In production, implement church selection UI for multi-church users
    PortalUser portalUser = portalUsers.get(0);
    Long churchId = portalUser.getChurch().getId();

    // Create PortalLoginRequest from AuthLoginRequest
    PortalLoginRequest portalLoginRequest = new PortalLoginRequest();
    portalLoginRequest.setEmail(email);
    portalLoginRequest.setPassword(request.password());
    portalLoginRequest.setChurchId(churchId);

    // Authenticate portal user
    Map<String, Object> portalLoginResponse = portalUserService.loginPortalUser(churchId, portalLoginRequest);

    // Add type indicator and redirect path
    portalLoginResponse.put("userType", "PORTAL_USER");
    portalLoginResponse.put("redirectTo", "/portal/home");

    return ResponseEntity.ok(portalLoginResponse);
  }

  @PostMapping("/register")
  public ResponseEntity<User> createUser(@Valid @RequestBody AuthRegistrationRequest authRequest) {
    return ResponseEntity.ok(authService.register(authRequest));
  }

  /**
   * Register a new church along with the first admin user.
   * This endpoint creates both a church (tenant) and the first admin user in a single transaction.
   * The user is automatically logged in after successful registration.
   *
   * @param request Contains both church and user registration data
   * @return AuthResponse with user info (access and refresh tokens are set in HttpOnly cookies)
   */
  @PostMapping("/register/church")
  public ResponseEntity<AuthResponse> registerChurch(
      @Valid @RequestBody UserChurchRegistrationRequest request,
      HttpServletRequest httpRequest,
      HttpServletResponse httpResponse
  ) {
    // Register church and first admin user
    AuthTokenData tokenData = authService.registerNewChurch(request, httpRequest);

    // Set HttpOnly cookies for tokens
    int accessTokenMaxAge = (int) (accessTokenExpiration / 1000);
    int refreshTokenMaxAge = (int) (refreshTokenExpiration / 1000);

    httpResponse.addCookie(cookieUtil.createAccessTokenCookie(
        tokenData.accessToken(),
        accessTokenMaxAge
    ));

    httpResponse.addCookie(cookieUtil.createRefreshTokenCookie(
        tokenData.refreshToken(),
        refreshTokenMaxAge
    ));

    // Return response with only user info (tokens are in HttpOnly cookies)
    return ResponseEntity.ok(new AuthResponse(tokenData.user()));
  }

    @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
    // Get refresh token from cookie
    cookieUtil.getRefreshToken(request).ifPresent(token -> {
      authService.logout(token);
    });

    // Clear cookies
    cookieUtil.clearAuthCookies(response);

    return ResponseEntity.ok().build();
  }

    @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(
      HttpServletRequest request,
      HttpServletResponse response
  ) {
    String refreshToken = cookieUtil.getRefreshToken(request)
        .orElseThrow(() -> new RuntimeException("Refresh token not found"));

    // Get new token data
    AuthTokenData tokenData = authService.refreshAccessToken(refreshToken, request);

    // Set new access token in cookie
    int accessTokenMaxAge = (int) (accessTokenExpiration / 1000);
    response.addCookie(cookieUtil.createAccessTokenCookie(
        tokenData.accessToken(),
        accessTokenMaxAge
    ));

    // Return response with only user info (token is in HttpOnly cookie)
    return ResponseEntity.ok(new AuthResponse(tokenData.user()));
  }
}
