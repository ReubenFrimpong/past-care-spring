package com.reuben.pastcare_spring.security;

import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Filter to enforce subscription access control.
 * Blocks access to protected endpoints if subscription is not active.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionFilter extends OncePerRequestFilter {

    private final ChurchSubscriptionRepository subscriptionRepository;

    // Endpoints that are always accessible regardless of subscription status
    private static final List<String> EXEMPTED_ENDPOINTS = Arrays.asList(
        "/api/auth/",
        "/api/billing/",
        "/api/churches/*/subscription",
        "/api/health",
        "/swagger",
        "/v3/api-docs",
        "/actuator"
    );

    // Endpoints that require active subscription
    private static final List<String> PROTECTED_ENDPOINTS = Arrays.asList(
        "/api/dashboard/",
        "/api/members/",
        "/api/attendance/",
        "/api/events/",
        "/api/donations/",
        "/api/fellowships/",
        "/api/users/",
        "/api/reports/"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // Skip filter for exempted endpoints
        if (isExemptedEndpoint(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if endpoint requires subscription
        if (!isProtectedEndpoint(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if user is SUPERADMIN (they don't have church ID and bypass subscription checks)
        if (isSuperAdmin(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract church ID from authenticated user
        Long churchId = extractChurchId(authentication);
        if (churchId == null) {
            log.warn("[SubscriptionFilter] No church ID found for authenticated user (not SUPERADMIN)");
            filterChain.doFilter(request, response);
            return;
        }

        // Check subscription status
        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
                .orElse(null);

        if (subscription == null) {
            log.warn("[SubscriptionFilter] No subscription found for church: {}", churchId);
            sendSubscriptionRequiredError(response, "NO_SUBSCRIPTION");
            return;
        }

        // Allow access if subscription is active, in grace period, or has promotional credits
        if (subscription.isActive() || subscription.isInGracePeriod() || subscription.hasPromotionalCredits()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Block access for suspended, canceled, or expired subscriptions
        log.warn("[SubscriptionFilter] Access denied for church {} - subscription status: {}",
                churchId, subscription.getStatus());
        sendSubscriptionRequiredError(response, subscription.getStatus());
    }

    /**
     * Check if endpoint is exempted from subscription check
     */
    private boolean isExemptedEndpoint(String uri) {
        return EXEMPTED_ENDPOINTS.stream()
                .anyMatch(pattern -> uri.contains(pattern.replace("*", "")));
    }

    /**
     * Check if endpoint requires active subscription
     */
    private boolean isProtectedEndpoint(String uri) {
        return PROTECTED_ENDPOINTS.stream()
                .anyMatch(uri::startsWith);
    }

    /**
     * Check if user is SUPERADMIN
     */
    private boolean isSuperAdmin(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            return userPrincipal.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_SUPERADMIN"));
        }
        return false;
    }

    /**
     * Extract church ID from authenticated user
     */
    private Long extractChurchId(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getChurchId();
        }
        return null;
    }

    /**
     * Send subscription required error response
     */
    private void sendSubscriptionRequiredError(HttpServletResponse response, String status) throws IOException {
        response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED); // 402
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String message = switch (status) {
            case "SUSPENDED" -> "Your subscription has been suspended. Please update your payment to restore access.";
            case "CANCELED" -> "Your subscription has been canceled. Please reactivate to continue.";
            case "PAST_DUE" -> "Your payment is overdue. Please update your payment to continue.";
            case "NO_SUBSCRIPTION" -> "No active subscription found. Please subscribe to access this feature.";
            default -> "Active subscription required to access this feature.";
        };

        String jsonResponse = String.format(
                "{\"error\":\"SUBSCRIPTION_REQUIRED\",\"message\":\"%s\",\"status\":\"%s\",\"requiredAction\":\"RENEW_SUBSCRIPTION\"}",
                message, status
        );

        response.getWriter().write(jsonResponse);
    }
}
