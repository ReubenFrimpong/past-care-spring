package com.reuben.pastcare_spring.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.reuben.pastcare_spring.dtos.PaymentInitializationRequest;
import com.reuben.pastcare_spring.dtos.PaymentInitializationResponse;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.Payment;
import com.reuben.pastcare_spring.models.SubscriptionPlan;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import com.reuben.pastcare_spring.repositories.PaymentRepository;
import com.reuben.pastcare_spring.repositories.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing church subscriptions and billing.
 *
 * <p>Handles:
 * <ul>
 *   <li>Subscription creation and upgrades/downgrades</li>
 *   <li>Payment processing and verification</li>
 *   <li>Trial period management</li>
 *   <li>Subscription renewals and cancellations</li>
 *   <li>Payment history and invoicing</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final ChurchSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;
    private final PaymentRepository paymentRepository;
    private final PaystackService paystackService;

    /**
     * Get church subscription.
     */
    @Transactional(readOnly = true)
    public ChurchSubscription getChurchSubscription(Long churchId) {
        return subscriptionRepository.findByChurchId(churchId)
            .orElseThrow(() -> new RuntimeException("Subscription not found for church " + churchId));
    }

    /**
     * Get all available subscription plans.
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getAvailablePlans() {
        return planRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
    }

    /**
     * Get subscription plan by ID.
     */
    @Transactional(readOnly = true)
    public SubscriptionPlan getPlanById(Long planId) {
        return planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));
    }

    /**
     * Create initial subscription for a new church (14-day trial on STARTER plan).
     */
    @Transactional
    public ChurchSubscription createInitialSubscription(Long churchId) {
        // Check if subscription already exists
        if (subscriptionRepository.findByChurchId(churchId).isPresent()) {
            throw new RuntimeException("Subscription already exists for church " + churchId);
        }

        // Get free STARTER plan
        SubscriptionPlan starterPlan = planRepository.findByIsFreeTrueAndIsActiveTrue()
            .orElseThrow(() -> new RuntimeException("Free plan not found"));

        // Create subscription with 14-day trial
        ChurchSubscription subscription = ChurchSubscription.builder()
            .churchId(churchId)
            .plan(starterPlan)
            .status("TRIALING")
            .trialEndDate(LocalDate.now().plusDays(14))
            .autoRenew(true)
            .gracePeriodDays(7)
            .failedPaymentAttempts(0)
            .build();

        return subscriptionRepository.save(subscription);
    }

    /**
     * Initialize payment for subscription upgrade.
     */
    @Transactional
    public PaymentInitializationResponse initializeSubscriptionPayment(Long churchId, Long planId, String email, String callbackUrl) {
        ChurchSubscription subscription = getChurchSubscription(churchId);
        SubscriptionPlan newPlan = getPlanById(planId);

        // Validate upgrade (can't downgrade to free plan)
        if (newPlan.getIsFree()) {
            throw new RuntimeException("Cannot upgrade to free plan");
        }

        // Create pending payment record
        Payment payment = Payment.builder()
            .churchId(churchId)
            .plan(newPlan)
            .amount(newPlan.getPrice())
            .currency("USD")
            .status("PENDING")
            .paystackReference("SUB-" + UUID.randomUUID().toString())
            .paymentType("SUBSCRIPTION")
            .description("Subscription to " + newPlan.getDisplayName())
            .build();

        paymentRepository.save(payment);

        // Initialize payment with Paystack
        PaymentInitializationRequest request = new PaymentInitializationRequest();
        request.setEmail(email);
        request.setAmount(newPlan.getPrice());
        request.setCurrency("USD");
        request.setCallbackUrl(callbackUrl);
        request.setMemberId(null); // Not applicable for subscription
        request.setDonationType(null); // Not applicable
        request.setSetupRecurring(true);

        return paystackService.initializePayment(request);
    }

    /**
     * Verify payment and activate subscription.
     */
    @Transactional
    public Payment verifyAndActivateSubscription(String reference) {
        // Find payment record
        Payment payment = paymentRepository.findByPaystackReference(reference)
            .orElseThrow(() -> new RuntimeException("Payment not found: " + reference));

        // Verify with Paystack
        JsonNode verification = paystackService.verifyPayment(reference);

        if (!verification.get("status").asBoolean()) {
            String errorMsg = verification.has("message") ? verification.get("message").asText() : "Verification failed";
            payment.markAsFailed(errorMsg);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment verification failed: " + errorMsg);
        }

        // Extract payment data
        JsonNode data = verification.get("data");
        String authCode = data.has("authorization") && data.get("authorization").has("authorization_code") ?
            data.get("authorization").get("authorization_code").asText() : null;
        String cardLast4 = data.has("authorization") && data.get("authorization").has("last4") ?
            data.get("authorization").get("last4").asText() : null;
        String cardBrand = data.has("authorization") && data.get("authorization").has("brand") ?
            data.get("authorization").get("brand").asText() : null;

        // Update payment record
        payment.markAsSuccessful();
        payment.setPaystackTransactionId(data.get("reference").asText());
        payment.setPaystackAuthorizationCode(authCode);
        payment.setPaymentMethod("CARD");
        payment.setCardLast4(cardLast4);
        payment.setCardBrand(cardBrand);
        paymentRepository.save(payment);

        // Update subscription
        ChurchSubscription subscription = getChurchSubscription(payment.getChurchId());
        subscription.setPlan(payment.getPlan());
        subscription.setStatus("ACTIVE");
        subscription.setTrialEndDate(null); // End trial
        subscription.setCurrentPeriodStart(LocalDate.now());
        subscription.setCurrentPeriodEnd(LocalDate.now().plusMonths(1));
        subscription.setNextBillingDate(LocalDate.now().plusMonths(1));
        subscription.setPaymentMethodType("CARD");
        subscription.setCardLast4(cardLast4);
        subscription.setCardBrand(cardBrand);
        subscription.setPaystackAuthorizationCode(authCode);
        subscription.setFailedPaymentAttempts(0);
        subscriptionRepository.save(subscription);

        log.info("Subscription activated for church {}: {}", payment.getChurchId(), payment.getPlan().getName());

        return payment;
    }

    /**
     * Cancel subscription (will remain active until end of current period).
     */
    @Transactional
    public void cancelSubscription(Long churchId) {
        ChurchSubscription subscription = getChurchSubscription(churchId);

        subscription.setStatus("CANCELED");
        subscription.setCanceledAt(LocalDateTime.now());
        subscription.setEndsAt(subscription.getCurrentPeriodEnd());
        subscription.setAutoRenew(false);

        subscriptionRepository.save(subscription);

        log.info("Subscription canceled for church {}, will end on {}", churchId, subscription.getEndsAt());
    }

    /**
     * Reactivate a canceled subscription.
     */
    @Transactional
    public void reactivateSubscription(Long churchId) {
        ChurchSubscription subscription = getChurchSubscription(churchId);

        if (!"CANCELED".equals(subscription.getStatus())) {
            throw new RuntimeException("Subscription is not canceled");
        }

        subscription.setStatus("ACTIVE");
        subscription.setCanceledAt(null);
        subscription.setEndsAt(null);
        subscription.setAutoRenew(true);

        subscriptionRepository.save(subscription);

        log.info("Subscription reactivated for church {}", churchId);
    }

    /**
     * Downgrade to free plan.
     */
    @Transactional
    public void downgradeToFreePlan(Long churchId) {
        ChurchSubscription subscription = getChurchSubscription(churchId);

        SubscriptionPlan freePlan = planRepository.findByIsFreeTrueAndIsActiveTrue()
            .orElseThrow(() -> new RuntimeException("Free plan not found"));

        subscription.setPlan(freePlan);
        subscription.setStatus("ACTIVE");
        subscription.setNextBillingDate(null); // No billing for free plan
        subscription.setAutoRenew(false);

        subscriptionRepository.save(subscription);

        log.info("Church {} downgraded to free plan", churchId);
    }

    /**
     * Get payment history for a church.
     */
    @Transactional(readOnly = true)
    public List<Payment> getPaymentHistory(Long churchId) {
        return paymentRepository.findByChurchIdOrderByCreatedAtDesc(churchId);
    }

    /**
     * Get successful payments for a church.
     */
    @Transactional(readOnly = true)
    public List<Payment> getSuccessfulPayments(Long churchId) {
        return paymentRepository.findByChurchIdAndStatusOrderByPaymentDateDesc(churchId, "SUCCESS");
    }

    /**
     * Check if church is within trial period.
     */
    public boolean isInTrialPeriod(Long churchId) {
        ChurchSubscription subscription = getChurchSubscription(churchId);
        return subscription.isTrialing();
    }

    /**
     * Check if church has active subscription.
     */
    public boolean hasActiveSubscription(Long churchId) {
        ChurchSubscription subscription = getChurchSubscription(churchId);
        return subscription.isActive();
    }

    /**
     * Check if church has exceeded storage limit.
     */
    public boolean hasExceededStorageLimit(Long churchId, long usageMB) {
        ChurchSubscription subscription = getChurchSubscription(churchId);
        return usageMB > subscription.getPlan().getStorageLimitMb();
    }

    /**
     * Check if church has exceeded user limit.
     */
    public boolean hasExceededUserLimit(Long churchId, int userCount) {
        ChurchSubscription subscription = getChurchSubscription(churchId);
        int limit = subscription.getPlan().getUserLimit();
        return limit != -1 && userCount > limit; // -1 means unlimited
    }

    /**
     * Get subscription statistics.
     */
    @Transactional(readOnly = true)
    public SubscriptionStats getSubscriptionStats() {
        long activeSubscriptions = subscriptionRepository.countActiveSubscriptions();
        long trialingSubscriptions = subscriptionRepository.countByStatus("TRIALING");
        long canceledSubscriptions = subscriptionRepository.countByStatus("CANCELED");
        long pastDueSubscriptions = subscriptionRepository.countByStatus("PAST_DUE");

        BigDecimal totalRevenue = paymentRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long successfulPayments = paymentRepository.countByStatus("SUCCESS");
        long failedPayments = paymentRepository.countByStatus("FAILED");

        return SubscriptionStats.builder()
            .activeSubscriptions(activeSubscriptions)
            .trialingSubscriptions(trialingSubscriptions)
            .canceledSubscriptions(canceledSubscriptions)
            .pastDueSubscriptions(pastDueSubscriptions)
            .totalRevenue(totalRevenue)
            .successfulPayments(successfulPayments)
            .failedPayments(failedPayments)
            .build();
    }

    /**
     * Process subscription renewals (scheduled task).
     */
    @Transactional
    public void processSubscriptionRenewals() {
        LocalDate today = LocalDate.now();
        List<ChurchSubscription> dueForRenewal = subscriptionRepository
            .findByNextBillingDateBeforeAndAutoRenewTrue(today.plusDays(1));

        for (ChurchSubscription subscription : dueForRenewal) {
            try {
                // Check if church has promotional credits first
                if (subscription.hasPromotionalCredits()) {
                    processRenewalWithPromoCredits(subscription);
                    continue;
                }

                // TODO: Charge using stored authorization code
                // For now, mark as PAST_DUE and send reminder
                subscription.setStatus("PAST_DUE");
                subscription.setFailedPaymentAttempts(subscription.getFailedPaymentAttempts() + 1);
                subscriptionRepository.save(subscription);

                log.warn("Subscription renewal due for church {}", subscription.getChurchId());
            } catch (Exception e) {
                log.error("Error processing renewal for church {}", subscription.getChurchId(), e);
            }
        }
    }

    /**
     * Suspend subscriptions past grace period.
     */
    @Transactional
    public void suspendPastDueSubscriptions() {
        LocalDate gracePeriodCutoff = LocalDate.now().minusDays(7); // Default 7 days
        List<ChurchSubscription> pastDue = subscriptionRepository
            .findByStatusAndNextBillingDateBefore("PAST_DUE", gracePeriodCutoff);

        for (ChurchSubscription subscription : pastDue) {
            if (subscription.shouldSuspend()) {
                subscription.setStatus("SUSPENDED");
                subscriptionRepository.save(subscription);
                log.warn("Subscription suspended for church {} due to non-payment", subscription.getChurchId());
            }
        }
    }

    /**
     * Grant promotional credits (free months) to a church.
     *
     * @param churchId Church ID to grant credits to
     * @param months Number of free months to grant
     * @param note Reason for granting (e.g., "Holiday promotion", "Referral bonus")
     * @param grantedBy User ID who is granting the credit
     */
    @Transactional
    public void grantPromotionalCredits(Long churchId, int months, String note, Long grantedBy) {
        ChurchSubscription subscription = getChurchSubscription(churchId);

        subscription.grantPromotionalCredits(months, note, grantedBy);
        subscriptionRepository.save(subscription);

        log.info("Granted {} free month(s) to church {}. Reason: {}", months, churchId, note);
    }

    /**
     * Process renewal with promotional credits consideration.
     * If church has free months remaining, skip charging and use credit instead.
     *
     * @param subscription Subscription to renew
     * @return true if charged successfully or credit used, false if charge failed
     */
    private boolean processRenewalWithPromoCredits(ChurchSubscription subscription) {
        // Check if church has promotional credits
        if (subscription.hasPromotionalCredits()) {
            // Use promotional credit instead of charging
            subscription.usePromotionalCredit();
            subscription.setNextBillingDate(LocalDate.now().plusMonths(1));
            subscription.setCurrentPeriodStart(LocalDate.now());
            subscription.setCurrentPeriodEnd(LocalDate.now().plusMonths(1));
            subscription.setFailedPaymentAttempts(0);
            subscriptionRepository.save(subscription);

            log.info("Used promotional credit for church {}. {} free month(s) remaining",
                subscription.getChurchId(), subscription.getFreeMonthsRemaining());

            return true;
        }

        // No promotional credits, proceed with normal charging
        // TODO: Implement actual Paystack charge
        return false;
    }

    /**
     * Revoke remaining promotional credits from a church.
     *
     * @param churchId Church ID to revoke credits from
     * @param reason Reason for revoking
     */
    @Transactional
    public void revokePromotionalCredits(Long churchId, String reason) {
        ChurchSubscription subscription = getChurchSubscription(churchId);

        int revokedMonths = subscription.getFreeMonthsRemaining();
        subscription.setFreeMonthsRemaining(0);
        subscription.setPromotionalNote(reason);
        subscriptionRepository.save(subscription);

        log.info("Revoked {} free month(s) from church {}. Reason: {}", revokedMonths, churchId, reason);
    }

    /**
     * Get subscription with promotional credit details.
     *
     * @param churchId Church ID
     * @return Subscription with promotional credit info
     */
    @Transactional(readOnly = true)
    public PromotionalCreditInfo getPromotionalCreditInfo(Long churchId) {
        ChurchSubscription subscription = getChurchSubscription(churchId);

        return PromotionalCreditInfo.builder()
            .hasCredits(subscription.hasPromotionalCredits())
            .freeMonthsRemaining(subscription.getFreeMonthsRemaining())
            .promotionalNote(subscription.getPromotionalNote())
            .promotionalGrantedBy(subscription.getPromotionalGrantedBy())
            .promotionalGrantedAt(subscription.getPromotionalGrantedAt())
            .build();
    }

    /**
     * Promotional credit info DTO.
     */
    @lombok.Data
    @lombok.Builder
    public static class PromotionalCreditInfo {
        private boolean hasCredits;
        private Integer freeMonthsRemaining;
        private String promotionalNote;
        private Long promotionalGrantedBy;
        private LocalDateTime promotionalGrantedAt;
    }

    /**
     * Subscription statistics DTO.
     */
    @lombok.Data
    @lombok.Builder
    public static class SubscriptionStats {
        private long activeSubscriptions;
        private long trialingSubscriptions;
        private long canceledSubscriptions;
        private long pastDueSubscriptions;
        private BigDecimal totalRevenue;
        private long successfulPayments;
        private long failedPayments;
    }
}
