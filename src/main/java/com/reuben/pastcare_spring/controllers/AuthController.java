package com.reuben.pastcare_spring.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.AuthLoginRequest;
import com.reuben.pastcare_spring.dtos.AuthRegistrationRequest;
import com.reuben.pastcare_spring.dtos.AuthResponse;
import com.reuben.pastcare_spring.dtos.AuthTokenData;
import com.reuben.pastcare_spring.dtos.UserChurchRegistrationRequest;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.services.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

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
