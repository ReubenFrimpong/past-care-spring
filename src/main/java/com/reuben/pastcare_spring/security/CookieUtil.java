package com.reuben.pastcare_spring.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

/**
 * Utility class for managing secure HttpOnly cookies.
 * Provides methods for setting and retrieving access and refresh tokens.
 */
@Component
public class CookieUtil {

  public static final String ACCESS_TOKEN_COOKIE = "access_token";
  public static final String REFRESH_TOKEN_COOKIE = "refresh_token";

  @Value("${jwt.cookie.domain:localhost}")
  private String cookieDomain;

  @Value("${jwt.cookie.secure:false}")
  private boolean secureCookie;

  @Value("${jwt.cookie.same-site:Lax}")
  private String sameSite;

  /**
   * Create an HttpOnly cookie for the access token.
   * Short-lived (15 minutes by default).
   */
  public Cookie createAccessTokenCookie(String token, int maxAgeSeconds) {
    Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, token);
    cookie.setHttpOnly(true);
    cookie.setSecure(secureCookie);
    cookie.setPath("/");
    cookie.setMaxAge(maxAgeSeconds);

    if (!"localhost".equals(cookieDomain)) {
      cookie.setDomain(cookieDomain);
    }

    return cookie;
  }

  /**
   * Create an HttpOnly cookie for the refresh token.
   * Long-lived (30 days by default).
   */
  public Cookie createRefreshTokenCookie(String token, int maxAgeSeconds) {
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE, token);
    cookie.setHttpOnly(true);
    cookie.setSecure(secureCookie);
    cookie.setPath("/api/auth/refresh"); // Only sent to refresh endpoint
    cookie.setMaxAge(maxAgeSeconds);

    if (!"localhost".equals(cookieDomain)) {
      cookie.setDomain(cookieDomain);
    }

    return cookie;
  }

  /**
   * Delete a cookie by setting its max age to 0.
   */
  public Cookie deleteCookie(String name) {
    Cookie cookie = new Cookie(name, null);
    cookie.setHttpOnly(true);
    cookie.setSecure(secureCookie);
    cookie.setPath("/");
    cookie.setMaxAge(0);

    if (!"localhost".equals(cookieDomain)) {
      cookie.setDomain(cookieDomain);
    }

    return cookie;
  }

  /**
   * Get cookie value from request.
   */
  public Optional<String> getCookieValue(HttpServletRequest request, String cookieName) {
    if (request.getCookies() == null) {
      return Optional.empty();
    }

    return Arrays.stream(request.getCookies())
        .filter(cookie -> cookieName.equals(cookie.getName()))
        .map(Cookie::getValue)
        .findFirst();
  }

  /**
   * Get access token from cookie.
   */
  public Optional<String> getAccessToken(HttpServletRequest request) {
    return getCookieValue(request, ACCESS_TOKEN_COOKIE);
  }

  /**
   * Get refresh token from cookie.
   */
  public Optional<String> getRefreshToken(HttpServletRequest request) {
    return getCookieValue(request, REFRESH_TOKEN_COOKIE);
  }

  /**
   * Clear all auth cookies.
   */
  public void clearAuthCookies(HttpServletResponse response) {
    response.addCookie(deleteCookie(ACCESS_TOKEN_COOKIE));
    response.addCookie(deleteCookie(REFRESH_TOKEN_COOKIE));
  }
}
