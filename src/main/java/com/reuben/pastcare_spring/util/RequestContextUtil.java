package com.reuben.pastcare_spring.util;

import com.reuben.pastcare_spring.security.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Utility class for extracting context information from HTTP requests.
 * Provides methods to extract church ID and user ID from JWT tokens
 * found in cookies or authorization headers.
 */
@Component
public class RequestContextUtil {

    private final JwtUtil jwtUtil;

    public RequestContextUtil(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * Extract church ID from JWT token in the request.
     * Checks cookies first for "access_token", then falls back to Authorization header.
     *
     * @param request The HTTP servlet request
     * @return The church ID from the JWT token
     * @throws IllegalStateException if no valid JWT token is found
     */
    public Long extractChurchId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            return jwtUtil.extractChurchId(token);
        }
        throw new IllegalStateException("No valid JWT token found in request");
    }

    /**
     * Extract user ID from JWT token in the request.
     * Checks cookies first for "access_token", then falls back to Authorization header.
     *
     * @param request The HTTP servlet request
     * @return The user ID from the JWT token
     * @throws IllegalStateException if no valid JWT token is found
     */
    public Long extractUserId(HttpServletRequest request) {
        String token = extractToken(request);
        if (token != null) {
            return jwtUtil.extractUserId(token);
        }
        throw new IllegalStateException("No valid JWT token found in request");
    }

    /**
     * Extract JWT token from the request.
     * First checks cookies for "access_token" cookie.
     * If not found, checks Authorization header for "Bearer " token.
     *
     * @param request The HTTP servlet request
     * @return The JWT token string, or null if not found
     */
    private String extractToken(HttpServletRequest request) {
        // Try to extract from cookie first
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }

        // Fall back to Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove "Bearer " prefix
        }

        return null;
    }
}
