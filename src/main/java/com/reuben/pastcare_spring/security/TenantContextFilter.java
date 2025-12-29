package com.reuben.pastcare_spring.security;

import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filter that establishes tenant context for each request.
 * This filter:
 * 1. Extracts the JWT token from the Authorization header
 * 2. Validates the churchId in the JWT matches the user's actual church in the database
 * 3. Sets the tenant context (churchId, userId, role) in ThreadLocal for the request
 * 4. Clears the context after the request completes
 *
 * This is a CRITICAL SECURITY component that prevents cross-tenant data access.
 *
 * Order: HIGHEST_PRECEDENCE + 10 (runs after JwtAuthenticationFilter)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class TenantContextFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TenantContextFilter.class);

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public TenantContextFilter(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String token = extractToken(request);

            if (token != null && !isTokenExpired(token)) {
                // Extract claims from JWT
                Long userId = jwtUtil.extractUserId(token);
                Long jwtChurchId = jwtUtil.extractChurchId(token);
                String role = jwtUtil.extractRole(token);

                logger.debug("Processing request for userId={}, jwtChurchId={}, role={}",
                        userId, jwtChurchId, role);

                // CRITICAL SECURITY CHECK: Validate JWT churchId matches database
                Optional<User> userOpt = userRepository.findById(userId);

                if (userOpt.isEmpty()) {
                    logger.warn("SECURITY: User not found in database. userId={}", userId);
                    throw new TenantViolationException("User not found");
                }

                User user = userOpt.get();
                Long dbChurchId = user.getChurch() != null ? user.getChurch().getId() : null;

                // Security validation: JWT churchId must match database churchId
                // Exception: SUPERADMIN can have null churchId
                if (!"SUPERADMIN".equals(role)) {
                    if (jwtChurchId == null) {
                        logger.error("SECURITY VIOLATION: JWT missing churchId. " +
                                "userId={}, role={}", userId, role);
                        throw new TenantViolationException(
                                "Missing churchId in token");
                    }

                    if (!jwtChurchId.equals(dbChurchId)) {
                        logger.error("SECURITY VIOLATION: JWT churchId mismatch. " +
                                "userId={}, jwtChurchId={}, dbChurchId={}, ip={}",
                                userId, jwtChurchId, dbChurchId, request.getRemoteAddr());
                        throw new TenantViolationException(
                                "JWT churchId mismatch. Possible token tampering.");
                    }
                }

                // Set tenant context for this request
                if (dbChurchId != null) {
                    TenantContext.setCurrentChurchId(dbChurchId);
                    logger.debug("Tenant context set: churchId={}", dbChurchId);
                }

                TenantContext.setCurrentUserId(userId);
                TenantContext.setCurrentUserRole(role);

                // Set MDC for logging correlation
                MDC.put("churchId", String.valueOf(dbChurchId));
                MDC.put("userId", String.valueOf(userId));
                MDC.put("role", role);
                MDC.put("requestUri", request.getRequestURI());

                logger.debug("Tenant context established successfully");
            }

            // Continue filter chain
            filterChain.doFilter(request, response);

        } catch (TenantViolationException e) {
            logger.error("Tenant violation detected: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");

        } finally {
            // CRITICAL: Always clear tenant context after request completes
            // This prevents context leakage between requests
            TenantContext.clear();
            MDC.clear();
            logger.debug("Tenant context cleared");
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String token) {
        try {
            return jwtUtil.extractExpiration(token).before(new java.util.Date());
        } catch (Exception e) {
            logger.debug("Token validation failed: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Exception thrown when tenant security violations are detected
     */
    public static class TenantViolationException extends RuntimeException {
        public TenantViolationException(String message) {
            super(message);
        }
    }
}
