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
     */
    @GetMapping("/subscription")
    @Operation(summary = "Get current subscription")
    @RequirePermission(Permission.SUBSCRIPTION_VIEW)
    public ResponseEntity<ChurchSubscription> getCurrentSubscription() {
        Long churchId = TenantContext.getCurrentChurchId();
        ChurchSubscription subscription = billingService.getChurchSubscription(churchId);
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
     */
    @PostMapping("/subscribe")
    @Operation(summary = "Initialize subscription upgrade payment")
    @RequirePermission(Permission.SUBSCRIPTION_MANAGE)
    public ResponseEntity<PaymentInitializationResponse> initializeSubscription(
            @RequestBody SubscriptionUpgradeRequest request) {
        Long churchId = TenantContext.getCurrentChurchId();

        PaymentInitializationResponse response = billingService.initializeSubscriptionPayment(
            churchId,
            request.getPlanId(),
            request.getEmail(),
            request.getCallbackUrl()
        );

        log.info("Subscription payment initialized for church {}: plan {}", churchId, request.getPlanId());
        return ResponseEntity.ok(response);
    }

    /**
     * Verify payment and activate subscription.
     */
    @PostMapping("/verify/{reference}")
    @Operation(summary = "Verify payment and activate subscription")
    @RequirePermission(Permission.SUBSCRIPTION_MANAGE)
    public ResponseEntity<Payment> verifyAndActivate(@PathVariable String reference) {
        Payment payment = billingService.verifyAndActivateSubscription(reference);
        log.info("Subscription activated for church {} via payment {}", payment.getChurchId(), reference);
        return ResponseEntity.ok(payment);
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
        Long churchId = TenantContext.getCurrentChurchId();
        ChurchSubscription subscription = billingService.getChurchSubscription(churchId);

        SubscriptionStatusResponse response = SubscriptionStatusResponse.builder()
            .isActive(subscription.isActive())
            .isTrialing(subscription.isTrialing())
            .isPastDue(subscription.isPastDue())
            .isCanceled(subscription.isCanceled())
            .isSuspended(subscription.isSuspended())
            .isInGracePeriod(subscription.isInGracePeriod())
            .planName(subscription.getPlan().getName())
            .planDisplayName(subscription.getPlan().getDisplayName())
            .status(subscription.getStatus())
            .trialEndDate(subscription.getTrialEndDate())
            .nextBillingDate(subscription.getNextBillingDate())
            .currentPeriodEnd(subscription.getCurrentPeriodEnd())
            .build();

        return ResponseEntity.ok(response);
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
     * Request DTO for subscription upgrade.
     */
    @lombok.Data
    public static class SubscriptionUpgradeRequest {
        private Long planId;
        private String email;
        private String callbackUrl;
    }

    /**
     * Response DTO for subscription status.
     */
    @lombok.Data
    @lombok.Builder
    public static class SubscriptionStatusResponse {
        private boolean isActive;
        private boolean isTrialing;
        private boolean isPastDue;
        private boolean isCanceled;
        private boolean isSuspended;
        private boolean isInGracePeriod;
        private String planName;
        private String planDisplayName;
        private String status;
        private java.time.LocalDate trialEndDate;
        private java.time.LocalDate nextBillingDate;
        private java.time.LocalDate currentPeriodEnd;
    }
}
