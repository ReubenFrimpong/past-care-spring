package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.PaymentInitializationResponse;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.Payment;
import com.reuben.pastcare_spring.models.SubscriptionPlan;
import com.reuben.pastcare_spring.security.TenantContext;
import com.reuben.pastcare_spring.services.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for managing church subscriptions and billing.
 *
 * <p>Endpoints for:
 * <ul>
 *   <li>Viewing current subscription and available plans</li>
 *   <li>Upgrading/downgrading subscriptions</li>
 *   <li>Managing payment methods</li>
 *   <li>Viewing payment history</li>
 *   <li>Canceling subscriptions</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing", description = "Subscription and billing management")
public class BillingController {

    private final BillingService billingService;

    /**
     * Get current subscription for the church.
     * Returns a default "no subscription" response if church has no subscription.
     */
    @GetMapping("/subscription")
    @Operation(summary = "Get current subscription")
    @RequirePermission(Permission.SUBSCRIPTION_VIEW)
    public ResponseEntity<ChurchSubscription> getCurrentSubscription() {
        Long churchId = TenantContext.getCurrentChurchId();
        // Use getChurchSubscriptionOrDefault to handle missing subscriptions gracefully
        ChurchSubscription subscription = billingService.getChurchSubscriptionOrDefault(churchId);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Get all available subscription plans.
     */
    @GetMapping("/plans")
    @Operation(summary = "Get available subscription plans")
    public ResponseEntity<List<SubscriptionPlan>> getAvailablePlans() {
        List<SubscriptionPlan> plans = billingService.getAvailablePlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Get a specific plan by ID.
     */
    @GetMapping("/plans/{planId}")
    @Operation(summary = "Get subscription plan by ID")
    public ResponseEntity<SubscriptionPlan> getPlanById(@PathVariable Long planId) {
        SubscriptionPlan plan = billingService.getPlanById(planId);
        return ResponseEntity.ok(plan);
    }

    /**
     * Initialize payment for subscription upgrade.
     * Supports both legacy planId and new congregation pricing tierId.
     */
    @PostMapping("/subscribe")
    @Operation(summary = "Initialize subscription upgrade payment")
    @RequirePermission(Permission.SUBSCRIPTION_MANAGE)
    public ResponseEntity<PaymentInitializationResponse> initializeSubscription(
            @RequestBody SubscriptionUpgradeRequest request) {
        Long churchId = TenantContext.getCurrentChurchId();

        // Determine billing interval - prefer billingInterval, fallback to billingPeriod
        String interval = request.getBillingInterval() != null ?
            request.getBillingInterval() : request.getBillingPeriod();

        PaymentInitializationResponse response;

        // Check if using new congregation pricing (tierId) or legacy (planId)
        if (request.getTierId() != null) {
            // New congregation pricing flow
            response = billingService.initializeCongregationTierPayment(
                churchId,
                request.getTierId(),
                request.getTierName(),
                request.getEmail(),
                request.getCallbackUrl(),
                interval,
                request.getAmountGhs(),
                request.getAmountUsd()
            );
            log.info("Congregation tier payment initialized for church {}: tier {} ({})",
                churchId, request.getTierId(), request.getTierName());
        } else {
            // Legacy subscription plan flow
            response = billingService.initializeSubscriptionPayment(
                churchId,
                request.getPlanId(),
                request.getEmail(),
                request.getCallbackUrl(),
                request.getBillingPeriod(),
                request.getBillingPeriodMonths()
            );
            log.info("Subscription payment initialized for church {}: plan {}", churchId, request.getPlanId());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Verify payment and activate subscription (authenticated endpoint).
     * Use this when the user is logged in.
     */
    @PostMapping("/verify/{reference}")
    @Operation(summary = "Verify payment and activate subscription (authenticated)")
    @RequirePermission(Permission.SUBSCRIPTION_MANAGE)
    public ResponseEntity<Payment> verifyAndActivate(@PathVariable String reference) {
        Payment payment = billingService.verifyAndActivateSubscription(reference);
        log.info("Subscription activated for church {} via payment {}", payment.getChurchId(), reference);
        return ResponseEntity.ok(payment);
    }

    /**
     * Public endpoint to verify payment status without authentication.
     * This is used when the user returns from Paystack payment page.
     * Returns payment status and church info for frontend to handle redirect.
     */
    @PostMapping("/public/verify/{reference}")
    @Operation(summary = "Verify payment status (public, no auth required)")
    public ResponseEntity<Map<String, Object>> verifyPaymentPublic(@PathVariable String reference) {
        try {
            Payment payment = billingService.verifyAndActivateSubscription(reference);
            log.info("Subscription activated for church {} via public verification: {}", payment.getChurchId(), reference);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Payment verified and subscription activated",
                "churchId", payment.getChurchId(),
                "planName", payment.getPlan().getDisplayName(),
                "amount", payment.getAmount(),
                "status", payment.getStatus()
            ));
        } catch (Exception e) {
            log.error("Error verifying payment {}: {}", reference, e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Cancel subscription (remains active until end of period).
     */
    @PostMapping("/cancel")
    @Operation(summary = "Cancel subscription")
    @RequirePermission(Permission.SUBSCRIPTION_MANAGE)
    public ResponseEntity<Map<String, String>> cancelSubscription() {
        Long churchId = TenantContext.getCurrentChurchId();
        billingService.cancelSubscription(churchId);
        log.info("Subscription canceled for church {}", churchId);
        return ResponseEntity.ok(Map.of(
            "message", "Subscription canceled successfully. Access will remain until the end of the current billing period."
        ));
    }

    /**
     * Reactivate a canceled subscription.
     */
    @PostMapping("/reactivate")
    @Operation(summary = "Reactivate canceled subscription")
    @RequirePermission(Permission.SUBSCRIPTION_MANAGE)
    public ResponseEntity<Map<String, String>> reactivateSubscription() {
        Long churchId = TenantContext.getCurrentChurchId();
        billingService.reactivateSubscription(churchId);
        log.info("Subscription reactivated for church {}", churchId);
        return ResponseEntity.ok(Map.of("message", "Subscription reactivated successfully"));
    }

    /**
     * Downgrade to free plan.
     */
    @PostMapping("/downgrade-to-free")
    @Operation(summary = "Downgrade to free plan")
    @RequirePermission(Permission.SUBSCRIPTION_MANAGE)
    public ResponseEntity<Map<String, String>> downgradeToFree() {
        Long churchId = TenantContext.getCurrentChurchId();
        billingService.downgradeToFreePlan(churchId);
        log.info("Church {} downgraded to free plan", churchId);
        return ResponseEntity.ok(Map.of("message", "Downgraded to free plan successfully"));
    }

    /**
     * Get payment history for the church.
     */
    @GetMapping("/payments")
    @Operation(summary = "Get payment history")
    @RequirePermission(Permission.SUBSCRIPTION_VIEW)
    public ResponseEntity<List<Payment>> getPaymentHistory() {
        Long churchId = TenantContext.getCurrentChurchId();
        List<Payment> payments = billingService.getPaymentHistory(churchId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get successful payments only.
     */
    @GetMapping("/payments/successful")
    @Operation(summary = "Get successful payments")
    @RequirePermission(Permission.SUBSCRIPTION_VIEW)
    public ResponseEntity<List<Payment>> getSuccessfulPayments() {
        Long churchId = TenantContext.getCurrentChurchId();
        List<Payment> payments = billingService.getSuccessfulPayments(churchId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Check subscription status.
     */
    @GetMapping("/status")
    @Operation(summary = "Check subscription status")
    public ResponseEntity<SubscriptionStatusResponse> getSubscriptionStatus() {
        try {
            Long churchId = TenantContext.getCurrentChurchId();
            if (churchId == null) {
                throw new RuntimeException("Church ID not found in context");
            }

            // Use getChurchSubscriptionOrDefault to handle missing subscriptions gracefully
            ChurchSubscription subscription = billingService.getChurchSubscriptionOrDefault(churchId);

            SubscriptionStatusResponse response = SubscriptionStatusResponse.builder()
                .isActive(subscription.isActive())
                .isPastDue(subscription.isPastDue())
                .isCanceled(subscription.isCanceled())
                .isSuspended(subscription.isSuspended())
                .isInGracePeriod(subscription.isInGracePeriod())
                .planName(subscription.getPlan() != null ? subscription.getPlan().getName() : null)
                .planDisplayName(subscription.getPlan() != null ? subscription.getPlan().getDisplayName() : "No Subscription")
                .status(subscription.getStatus())
                .nextBillingDate(subscription.getNextBillingDate())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .hasPromotionalCredits(subscription.hasPromotionalCredits())
                // Data deletion countdown for suspended subscriptions
                .dataRetentionEndDate(subscription.getDataRetentionEndDate())
                .daysUntilDeletion(subscription.isSuspended() ? subscription.getDaysUntilDeletion() : null)
                .deletionWarningReceived(subscription.getDeletionWarningSentAt() != null)
                .deletionWarningSentAt(subscription.getDeletionWarningSentAt())
                .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting subscription status", e);
            throw e;
        }
    }

    /**
     * Get subscription statistics (SUPERADMIN only).
     */
    @GetMapping("/stats")
    @Operation(summary = "Get subscription statistics (SUPERADMIN only)")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<BillingService.SubscriptionStats> getSubscriptionStats() {
        BillingService.SubscriptionStats stats = billingService.getSubscriptionStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Grant promotional credits (free months) to a church (SUPERADMIN only).
     */
    @PostMapping("/promotional-credits/grant")
    @Operation(summary = "Grant promotional credits to a church (SUPERADMIN only)")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<Map<String, String>> grantPromotionalCredits(
            @RequestBody GrantPromotionalCreditsRequest request) {

        Long grantedBy = TenantContext.getCurrentUserId();

        billingService.grantPromotionalCredits(
            request.getChurchId(),
            request.getMonths(),
            request.getNote(),
            grantedBy
        );

        log.info("SUPERADMIN granted {} free month(s) to church {}",
            request.getMonths(), request.getChurchId());

        return ResponseEntity.ok(Map.of(
            "message", String.format("Granted %d free month(s) to church %d",
                request.getMonths(), request.getChurchId())
        ));
    }

    /**
     * Revoke promotional credits from a church (SUPERADMIN only).
     */
    @PostMapping("/promotional-credits/revoke")
    @Operation(summary = "Revoke promotional credits from a church (SUPERADMIN only)")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<Map<String, String>> revokePromotionalCredits(
            @RequestBody RevokePromotionalCreditsRequest request) {

        billingService.revokePromotionalCredits(
            request.getChurchId(),
            request.getReason()
        );

        log.info("SUPERADMIN revoked promotional credits from church {}", request.getChurchId());

        return ResponseEntity.ok(Map.of("message", "Promotional credits revoked successfully"));
    }

    /**
     * Get promotional credit info for current church.
     */
    @GetMapping("/promotional-credits")
    @Operation(summary = "Get promotional credit info")
    @RequirePermission(Permission.SUBSCRIPTION_VIEW)
    public ResponseEntity<BillingService.PromotionalCreditInfo> getPromotionalCreditInfo() {
        Long churchId = TenantContext.getCurrentChurchId();
        BillingService.PromotionalCreditInfo info = billingService.getPromotionalCreditInfo(churchId);
        return ResponseEntity.ok(info);
    }

    /**
     * Get promotional credit info for specific church (SUPERADMIN only).
     */
    @GetMapping("/promotional-credits/{churchId}")
    @Operation(summary = "Get promotional credit info for specific church (SUPERADMIN only)")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<BillingService.PromotionalCreditInfo> getPromotionalCreditInfoForChurch(
            @PathVariable Long churchId) {
        BillingService.PromotionalCreditInfo info = billingService.getPromotionalCreditInfo(churchId);
        return ResponseEntity.ok(info);
    }

    // ==================== SUPERADMIN PLAN MANAGEMENT ====================

    /**
     * Create a new subscription plan (SUPERADMIN only).
     */
    @PostMapping("/admin/plans")
    @Operation(summary = "Create subscription plan (SUPERADMIN only)")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<SubscriptionPlan> createPlan(@RequestBody CreatePlanRequest request) {
        SubscriptionPlan plan = billingService.createPlan(request);
        log.info("SUPERADMIN created new plan: {}", plan.getName());
        return ResponseEntity.ok(plan);
    }

    /**
     * Update an existing subscription plan (SUPERADMIN only).
     */
    @PutMapping("/admin/plans/{planId}")
    @Operation(summary = "Update subscription plan (SUPERADMIN only)")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<SubscriptionPlan> updatePlan(
            @PathVariable Long planId,
            @RequestBody UpdatePlanRequest request) {
        SubscriptionPlan plan = billingService.updatePlan(planId, request);
        log.info("SUPERADMIN updated plan: {}", plan.getName());
        return ResponseEntity.ok(plan);
    }

    /**
     * Delete/deactivate a subscription plan (SUPERADMIN only).
     */
    @DeleteMapping("/admin/plans/{planId}")
    @Operation(summary = "Deactivate subscription plan (SUPERADMIN only)")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<Map<String, String>> deactivatePlan(@PathVariable Long planId) {
        billingService.deactivatePlan(planId);
        log.info("SUPERADMIN deactivated plan ID: {}", planId);
        return ResponseEntity.ok(Map.of("message", "Plan deactivated successfully"));
    }

    /**
     * Activate a subscription plan (SUPERADMIN only).
     */
    @PostMapping("/admin/plans/{planId}/activate")
    @Operation(summary = "Activate subscription plan (SUPERADMIN only)")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<Map<String, String>> activatePlan(@PathVariable Long planId) {
        billingService.activatePlan(planId);
        log.info("SUPERADMIN activated plan ID: {}", planId);
        return ResponseEntity.ok(Map.of("message", "Plan activated successfully"));
    }

    /**
     * Get all plans including inactive (SUPERADMIN only).
     */
    @GetMapping("/admin/plans")
    @Operation(summary = "Get all subscription plans including inactive (SUPERADMIN only)")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<List<SubscriptionPlan>> getAllPlans() {
        List<SubscriptionPlan> plans = billingService.getAllPlans();
        return ResponseEntity.ok(plans);
    }

    /**
     * Request DTO for subscription upgrade.
     * Supports both legacy planId and new congregation pricing tierId.
     */
    @lombok.Data
    public static class SubscriptionUpgradeRequest {
        private Long planId;        // Legacy: subscription_plans.id
        private Long tierId;        // New: congregation_pricing_tiers.id
        private String tierName;    // New: congregation_pricing_tiers.tier_name
        private String email;
        private String callbackUrl;
        private String billingPeriod; // MONTHLY, QUARTERLY, BIANNUAL, YEARLY
        private Integer billingPeriodMonths; // 1, 3, 6, or 12
        private java.math.BigDecimal amountGhs;  // Price in GHS (calculated by frontend)
        private java.math.BigDecimal amountUsd;  // Price in USD (from tier)
        private String billingInterval; // New: same as billingPeriod
    }

    /**
     * Request DTO for granting promotional credits.
     */
    @lombok.Data
    public static class GrantPromotionalCreditsRequest {
        private Long churchId;
        private int months;
        private String note;
    }

    /**
     * Request DTO for revoking promotional credits.
     */
    @lombok.Data
    public static class RevokePromotionalCreditsRequest {
        private Long churchId;
        private String reason;
    }

    /**
     * Response DTO for subscription status.
     *
     * CRITICAL: Field names MUST match frontend TypeScript interface exactly.
     * Lombok @Data generates getters like isActive() for boolean fields, which Jackson
     * serializes as "active" (removing "is" prefix). We use @JsonProperty to override this.
     */
    @lombok.Data
    @lombok.Builder
    public static class SubscriptionStatusResponse {
        @com.fasterxml.jackson.annotation.JsonProperty("isActive")
        private boolean isActive;

        @com.fasterxml.jackson.annotation.JsonProperty("isPastDue")
        private boolean isPastDue;

        @com.fasterxml.jackson.annotation.JsonProperty("isCanceled")
        private boolean isCanceled;

        @com.fasterxml.jackson.annotation.JsonProperty("isSuspended")
        private boolean isSuspended;

        @com.fasterxml.jackson.annotation.JsonProperty("isInGracePeriod")
        private boolean isInGracePeriod;

        private String planName;
        private String planDisplayName;
        private String status;
        private java.time.LocalDate nextBillingDate;
        private java.time.LocalDate currentPeriodEnd;

        @com.fasterxml.jackson.annotation.JsonProperty("hasPromotionalCredits")
        private boolean hasPromotionalCredits;

        // Data deletion countdown (for suspended subscriptions)
        private java.time.LocalDate dataRetentionEndDate;
        private Long daysUntilDeletion;

        @com.fasterxml.jackson.annotation.JsonProperty("deletionWarningReceived")
        private boolean deletionWarningReceived;

        private java.time.LocalDateTime deletionWarningSentAt;
    }

    /**
     * Request DTO for creating a new subscription plan.
     */
    @lombok.Data
    public static class CreatePlanRequest {
        private String name;
        private String displayName;
        private String description;
        private java.math.BigDecimal price;
        private String billingInterval = "MONTHLY";
        private Long storageLimitMb;
        private Integer userLimit;
        private Boolean isFree = false;
        private String features;
        private Integer displayOrder;
        private String paystackPlanCode;
    }

    /**
     * Request DTO for updating a subscription plan.
     */
    @lombok.Data
    public static class UpdatePlanRequest {
        private String displayName;
        private String description;
        private java.math.BigDecimal price;
        private Long storageLimitMb;
        private Integer userLimit;
        private String features;
        private Integer displayOrder;
        private String paystackPlanCode;
    }

    // ==================== Grace Period Management (SUPERADMIN) ====================

    /**
     * Grant or extend grace period for a church subscription.
     * SUPERADMIN only.
     *
     * @param request Grace period request
     * @return Grace period response with updated details
     */
    @PostMapping("/platform/grace-period/grant")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> grantGracePeriod(@RequestBody @jakarta.validation.Valid com.reuben.pastcare_spring.dto.GracePeriodRequest request) {
        try {
            ChurchSubscription subscription = billingService.grantGracePeriod(
                    request.getChurchId(),
                    request.getGracePeriodDays(),
                    request.getReason(),
                    request.getExtend()
            );

            BillingService.GracePeriodStatus status = billingService.getGracePeriodStatus(request.getChurchId());

            com.reuben.pastcare_spring.dto.GracePeriodResponse response = com.reuben.pastcare_spring.dto.GracePeriodResponse.builder()
                    .churchId(subscription.getChurchId())
                    .churchName(getChurchName(subscription.getChurchId()))
                    .subscriptionStatus(subscription.getStatus())
                    .gracePeriodDays(status.gracePeriodDays())
                    .inGracePeriod(status.inGracePeriod())
                    .gracePeriodEndDate(status.gracePeriodEndDate())
                    .daysRemainingInGracePeriod(status.daysRemaining())
                    .nextBillingDate(subscription.getNextBillingDate())
                    .gracePeriodReason(status.reason())
                    .updatedAt(subscription.getUpdatedAt())
                    .message(request.getExtend() ?
                            "Grace period extended successfully" :
                            "Grace period granted successfully")
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Revoke grace period for a church subscription.
     * SUPERADMIN only.
     *
     * @param churchId Church ID
     * @return Success message
     */
    @DeleteMapping("/platform/grace-period/{churchId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> revokeGracePeriod(@PathVariable Long churchId) {
        try {
            ChurchSubscription subscription = billingService.revokeGracePeriod(churchId);

            return ResponseEntity.ok(java.util.Map.of(
                    "message", "Grace period revoked successfully. Reset to default 7 days.",
                    "churchId", subscription.getChurchId(),
                    "gracePeriodDays", subscription.getGracePeriodDays()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get grace period status for a church subscription.
     * SUPERADMIN only.
     *
     * @param churchId Church ID
     * @return Grace period status details
     */
    @GetMapping("/platform/grace-period/{churchId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> getGracePeriodStatus(@PathVariable Long churchId) {
        try {
            ChurchSubscription subscription = billingService.getChurchSubscription(churchId);
            BillingService.GracePeriodStatus status = billingService.getGracePeriodStatus(churchId);

            com.reuben.pastcare_spring.dto.GracePeriodResponse response = com.reuben.pastcare_spring.dto.GracePeriodResponse.builder()
                    .churchId(subscription.getChurchId())
                    .churchName(getChurchName(subscription.getChurchId()))
                    .subscriptionStatus(subscription.getStatus())
                    .gracePeriodDays(status.gracePeriodDays())
                    .inGracePeriod(status.inGracePeriod())
                    .gracePeriodEndDate(status.gracePeriodEndDate())
                    .daysRemainingInGracePeriod(status.daysRemaining())
                    .nextBillingDate(subscription.getNextBillingDate())
                    .gracePeriodReason(status.reason())
                    .updatedAt(subscription.getUpdatedAt())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all subscriptions currently in grace period.
     * SUPERADMIN only.
     *
     * @return List of subscriptions in grace period
     */
    @GetMapping("/platform/grace-period/active")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> getSubscriptionsInGracePeriod() {
        try {
            List<ChurchSubscription> subscriptions = billingService.getSubscriptionsInGracePeriod();

            List<com.reuben.pastcare_spring.dto.GracePeriodResponse> responses = subscriptions.stream()
                    .map(sub -> {
                        BillingService.GracePeriodStatus status = billingService.getGracePeriodStatus(sub.getChurchId());
                        return com.reuben.pastcare_spring.dto.GracePeriodResponse.builder()
                                .churchId(sub.getChurchId())
                                .churchName(getChurchName(sub.getChurchId()))
                                .subscriptionStatus(sub.getStatus())
                                .gracePeriodDays(status.gracePeriodDays())
                                .inGracePeriod(status.inGracePeriod())
                                .gracePeriodEndDate(status.gracePeriodEndDate())
                                .daysRemainingInGracePeriod(status.daysRemaining())
                                .nextBillingDate(sub.getNextBillingDate())
                                .gracePeriodReason(status.reason())
                                .updatedAt(sub.getUpdatedAt())
                                .build();
                    })
                    .toList();

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(java.util.Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get church name by ID (helper method).
     */
    private String getChurchName(Long churchId) {
        return billingService.getChurchSubscription(churchId)
                .getChurchId()
                .toString(); // TODO: Fetch actual church name from Church entity
    }

    // ==================== MANUAL SUBSCRIPTION ACTIVATION ====================

    /**
     * Manually activate subscription without payment verification (SUPERADMIN only).
     * Used for handling failed payments, manual payment methods, or administrative overrides.
     *
     * @param request Manual activation request
     * @return Updated subscription details
     */
    @PostMapping("/platform/subscription/manual-activate")
    @Operation(summary = "Manually activate subscription (SUPERADMIN only)")
    @RequirePermission(Permission.PLATFORM_ACCESS)
    public ResponseEntity<?> manuallyActivateSubscription(@RequestBody ManualActivationRequest request) {
        try {
            Long adminUserId = TenantContext.getCurrentUserId();

            ChurchSubscription subscription = billingService.manuallyActivateSubscription(
                request.getChurchId(),
                request.getPlanId(),
                request.getDurationMonths(),
                request.getReason(),
                request.getCategory(),
                adminUserId
            );

            log.info("SUPERADMIN {} manually activated subscription for church {} (Category: {})",
                adminUserId, request.getChurchId(), request.getCategory());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Subscription manually activated successfully",
                "subscription", subscription
            ));
        } catch (Exception e) {
            log.error("Error manually activating subscription: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Request DTO for manual subscription activation.
     */
    @lombok.Data
    public static class ManualActivationRequest {
        private Long churchId;
        private Long planId;
        private Integer durationMonths; // Duration in months (defaults to 1)
        private String reason; // Administrative reason for manual activation
        private String category; // Category: PAYMENT_CALLBACK_FAILED, ALTERNATIVE_PAYMENT, GRACE_PERIOD_EXTENSION, PROMOTIONAL, EMERGENCY_OVERRIDE
    }
}
