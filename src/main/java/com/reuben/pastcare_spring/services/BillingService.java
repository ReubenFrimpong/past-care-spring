package com.reuben.pastcare_spring.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.reuben.pastcare_spring.dtos.PaymentInitializationRequest;
import com.reuben.pastcare_spring.dtos.PaymentInitializationResponse;
import com.reuben.pastcare_spring.models.ChurchStorageAddon;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.Payment;
import com.reuben.pastcare_spring.models.SubscriptionPlan;
import com.reuben.pastcare_spring.repositories.ChurchStorageAddonRepository;
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
    private final com.reuben.pastcare_spring.repositories.ChurchRepository churchRepository;
    private final ChurchStorageAddonRepository churchStorageAddonRepository;
    private final StorageAddonBillingService storageAddonBillingService;

    /**
     * Get church subscription.
     */
    @Transactional(readOnly = true)
    public ChurchSubscription getChurchSubscription(Long churchId) {
        return subscriptionRepository.findByChurchId(churchId)
            .orElseThrow(() -> new RuntimeException("Subscription not found for church " + churchId));
    }

    /**
     * Get church subscription or return default "no subscription" status.
     * Returns an expired subscription if none exists.
     */
    @Transactional(readOnly = true)
    public ChurchSubscription getChurchSubscriptionOrDefault(Long churchId) {
        return subscriptionRepository.findByChurchId(churchId)
            .orElseGet(() -> createDefaultNoSubscriptionResponse(churchId));
    }

    /**
     * Create a default response when no subscription exists.
     * Treats it as an inactive subscription - payment required.
     */
    private ChurchSubscription createDefaultNoSubscriptionResponse(Long churchId) {
        // Get the STANDARD plan as reference (churches must pay to activate)
        SubscriptionPlan plan = planRepository.findByName("STANDARD")
            .orElseGet(() -> {
                // Create a minimal placeholder plan if STANDARD plan doesn't exist
                return SubscriptionPlan.builder()
                    .id(0L)
                    .name("STANDARD")
                    .displayName("Standard Plan")
                    .description("Subscribe to access all features")
                    .price(new java.math.BigDecimal("150.00"))
                    .billingInterval("MONTHLY")
                    .storageLimitMb(2048L) // 2GB
                    .userLimit(-1) // Unlimited
                    .isFree(false)
                    .isActive(true)
                    .build();
            });

        // Build a transient (non-persisted) subscription indicating no active subscription
        return ChurchSubscription.builder()
            .churchId(churchId)
            .plan(plan)
            .status("CANCELED") // No subscription = treated as canceled/inactive
            .currentPeriodStart(null)
            .currentPeriodEnd(null)
            .nextBillingDate(null)
            .autoRenew(false)
            .gracePeriodDays(0)
            .failedPaymentAttempts(0)
            .build();
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
     * Create initial subscription for a new church.
     * NOTE: There is NO free plan. Churches must subscribe via payment or partnership code.
     * This method creates a placeholder inactive subscription record.
     */
    @Transactional
    public ChurchSubscription createInitialSubscription(Long churchId) {
        // Check if subscription already exists
        if (subscriptionRepository.findByChurchId(churchId).isPresent()) {
            throw new RuntimeException("Subscription already exists for church " + churchId);
        }

        // Get the STANDARD plan as reference (churches must pay to activate)
        SubscriptionPlan standardPlan = planRepository.findByName("STANDARD")
            .orElseThrow(() -> new RuntimeException("STANDARD plan not found"));

        // Create inactive subscription - church must pay or use partnership code to activate
        ChurchSubscription subscription = ChurchSubscription.builder()
            .churchId(churchId)
            .plan(standardPlan)
            .status("CANCELED") // Inactive until payment is made
            .currentPeriodStart(null)
            .currentPeriodEnd(null)
            .nextBillingDate(null)
            .autoRenew(false)
            .gracePeriodDays(0) // No automatic grace period
            .failedPaymentAttempts(0)
            .build();

        log.info("Created inactive subscription for church {}. Payment required to activate.", churchId);
        return subscriptionRepository.save(subscription);
    }

    /**
     * Initialize payment for subscription upgrade.
     */
    @Transactional
    public PaymentInitializationResponse initializeSubscriptionPayment(
            Long churchId, Long planId, String email, String callbackUrl,
            String billingPeriod, Integer billingPeriodMonths) {

        ChurchSubscription subscription = getChurchSubscription(churchId);
        SubscriptionPlan newPlan = getPlanById(planId);

        // Validate upgrade (can't downgrade to free plan)
        if (newPlan.getIsFree()) {
            throw new RuntimeException("Cannot upgrade to free plan");
        }

        // Calculate amount based on billing period
        BigDecimal amount = calculatePeriodAmount(newPlan.getPrice(), billingPeriod, billingPeriodMonths);

        // Store billing period in subscription for later use
        subscription.setBillingPeriod(billingPeriod != null ? billingPeriod : "MONTHLY");
        subscription.setBillingPeriodMonths(billingPeriodMonths != null ? billingPeriodMonths : 1);
        subscriptionRepository.save(subscription);

        // Create pending payment record
        Payment payment = Payment.builder()
            .churchId(churchId)
            .plan(newPlan)
            .amount(amount)
            .currency("GHS")  // Ghana Cedis - Paystack does not support USD
            .status("PENDING")
            .paystackReference("SUB-" + UUID.randomUUID().toString())
            .paymentType("SUBSCRIPTION")
            .description("Subscription to " + newPlan.getDisplayName() +
                        " (" + (billingPeriod != null ? billingPeriod : "MONTHLY") + ")")
            .build();

        paymentRepository.save(payment);

        // Initialize payment with Paystack
        PaymentInitializationRequest request = new PaymentInitializationRequest();
        request.setEmail(email);
        request.setAmount(amount);
        request.setCurrency("GHS");  // Ghana Cedis - Paystack does not support USD
        request.setCallbackUrl(callbackUrl);
        request.setMemberId(null); // Not applicable for subscription
        request.setDonationType(null); // Not applicable
        request.setSetupRecurring(true);
        request.setReference(payment.getPaystackReference()); // Use the SUB- reference we created

        return paystackService.initializePayment(request);
    }

    /**
     * Calculate amount based on billing period.
     * No discounts - all periods charge the same per-month rate.
     */
    private BigDecimal calculatePeriodAmount(BigDecimal basePrice, String billingPeriod, Integer months) {
        if (billingPeriod == null || months == null || months == 1) {
            return basePrice; // Monthly
        }

        // No discounts - just multiply base price by number of months
        return basePrice.multiply(BigDecimal.valueOf(months));
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

        // Use the billing period that was stored when payment was initialized
        int periodMonths = subscription.getBillingPeriodMonths() != null ?
            subscription.getBillingPeriodMonths() : 1;

        subscription.setPlan(payment.getPlan());
        subscription.setStatus("ACTIVE");
        subscription.setCurrentPeriodStart(LocalDate.now());
        subscription.setCurrentPeriodEnd(LocalDate.now().plusMonths(periodMonths));
        subscription.setNextBillingDate(LocalDate.now().plusMonths(periodMonths));
        subscription.setPaymentMethodType("CARD");
        subscription.setCardLast4(cardLast4);
        subscription.setCardBrand(cardBrand);
        subscription.setPaystackAuthorizationCode(authCode);
        subscription.setAutoRenew(true); // Enable auto-renew for paid plans
        subscription.setFailedPaymentAttempts(0);
        subscriptionRepository.save(subscription);

        log.info("Subscription activated for church {}: plan {} for {} months",
            payment.getChurchId(), payment.getPlan().getName(), periodMonths);

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
     * Cancel subscription (no free plan available).
     * This is called when user requests "downgrade to free" but there is no free plan.
     * The subscription will be canceled and become inactive.
     */
    @Transactional
    public void downgradeToFreePlan(Long churchId) {
        ChurchSubscription subscription = getChurchSubscription(churchId);

        // Since there's no free plan, cancel the subscription
        subscription.setStatus("CANCELED");
        subscription.setCanceledAt(LocalDateTime.now());
        subscription.setEndsAt(subscription.getCurrentPeriodEnd());
        subscription.setNextBillingDate(null);
        subscription.setAutoRenew(false);

        subscriptionRepository.save(subscription);

        log.info("Church {} canceled subscription (no free plan available)", churchId);
    }

    /**
     * Manually activate subscription without payment verification (SUPERADMIN only).
     * Used for manual payment processing, payment failures, or administrative overrides.
     *
     * @param churchId Church ID
     * @param planId Plan ID to activate
     * @param durationMonths Duration in months (defaults to 1 if null)
     * @param reason Administrative reason for manual activation
     * @param category Category of manual activation (PAYMENT_CALLBACK_FAILED, ALTERNATIVE_PAYMENT, GRACE_PERIOD_EXTENSION, PROMOTIONAL, EMERGENCY_OVERRIDE)
     * @param adminUserId ID of the admin performing the action
     * @return Updated subscription
     */
    @Transactional
    public ChurchSubscription manuallyActivateSubscription(
            Long churchId,
            Long planId,
            Integer durationMonths,
            String reason,
            String category,
            Long adminUserId) {

        // Get or create subscription
        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
            .orElseGet(() -> {
                ChurchSubscription newSub = new ChurchSubscription();
                newSub.setChurchId(churchId);
                newSub.setGracePeriodDays(0); // No automatic grace period
                newSub.setFailedPaymentAttempts(0);
                return newSub;
            });

        // Get the plan
        SubscriptionPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new RuntimeException("Plan not found: " + planId));

        // Calculate period dates
        int months = durationMonths != null && durationMonths > 0 ? durationMonths : 1;
        LocalDate now = LocalDate.now();
        LocalDate periodEnd = now.plusMonths(months);

        // Update subscription
        subscription.setPlan(plan);
        subscription.setStatus("ACTIVE");
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(periodEnd);
        subscription.setNextBillingDate(periodEnd);
        subscription.setAutoRenew(false); // Manual activations don't auto-renew by default
        subscription.setFailedPaymentAttempts(0);
        subscription.setPaymentMethodType("MANUAL");

        subscriptionRepository.save(subscription);

        // Create a manual payment record for tracking
        String categoryLabel = category != null && !category.isEmpty() ? category : "UNSPECIFIED";
        String fullDescription = String.format("[%s] Manual subscription activation: %s", categoryLabel, reason);
        String metadata = String.format("{\"category\":\"%s\",\"adminUserId\":%d,\"reason\":\"%s\"}",
            categoryLabel, adminUserId, reason.replace("\"", "\\\""));

        Payment manualPayment = Payment.builder()
            .churchId(churchId)
            .plan(plan)
            .amount(plan.getPrice().multiply(BigDecimal.valueOf(months)))
            .currency("GHS")
            .status("SUCCESSFUL")
            .paystackReference("MANUAL-" + UUID.randomUUID().toString())
            .paymentType("SUBSCRIPTION_MANUAL")
            .paymentMethod("MANUAL")
            .description(fullDescription)
            .metadata(metadata)
            .paymentDate(LocalDateTime.now())
            .build();

        manualPayment.markAsSuccessful();
        paymentRepository.save(manualPayment);

        // CRITICAL: Reactivate suspended addons when subscription is manually activated
        List<ChurchStorageAddon> suspendedAddons = churchStorageAddonRepository
            .findSuspendedByChurchId(churchId);

        for (ChurchStorageAddon addon : suspendedAddons) {
            storageAddonBillingService.reactivateAddon(addon, periodEnd);
        }

        // Restore storage limit (add back addon capacity)
        if (!suspendedAddons.isEmpty()) {
            storageAddonBillingService.updateChurchStorageLimit(churchId);
        }

        log.info("SUPERADMIN {} manually activated subscription for church {}: plan {} for {} months. {} addon(s) reactivated. Category: {}, Reason: {}",
            adminUserId, churchId, plan.getName(), months, suspendedAddons.size(), categoryLabel, reason);

        return subscription;
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
        long canceledSubscriptions = subscriptionRepository.countByStatus("CANCELED");
        long pastDueSubscriptions = subscriptionRepository.countByStatus("PAST_DUE");
        long suspendedSubscriptions = subscriptionRepository.countByStatus("SUSPENDED");

        BigDecimal totalRevenue = paymentRepository.calculateTotalRevenue();
        if (totalRevenue == null) totalRevenue = BigDecimal.ZERO;

        long successfulPayments = paymentRepository.countByStatus("SUCCESS");
        long failedPayments = paymentRepository.countByStatus("FAILED");

        return SubscriptionStats.builder()
            .activeSubscriptions(activeSubscriptions)
            .canceledSubscriptions(canceledSubscriptions)
            .pastDueSubscriptions(pastDueSubscriptions)
            .suspendedSubscriptions(suspendedSubscriptions)
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

                // Get stored authorization code
                String authCode = subscription.getPaystackAuthorizationCode();
                if (authCode == null || authCode.isEmpty()) {
                    log.error("No authorization code for church {}", subscription.getChurchId());
                    subscription.setStatus("PAST_DUE");
                    subscription.setFailedPaymentAttempts(subscription.getFailedPaymentAttempts() + 1);
                    subscriptionRepository.save(subscription);
                    continue;
                }

                // Get church email
                String email = getChurchEmail(subscription.getChurchId());
                if (email == null || email.isEmpty()) {
                    log.error("No email found for church {}", subscription.getChurchId());
                    subscription.setStatus("PAST_DUE");
                    subscription.setFailedPaymentAttempts(subscription.getFailedPaymentAttempts() + 1);
                    subscriptionRepository.save(subscription);
                    continue;
                }

                // Calculate total renewal charge (base + active addons)
                BigDecimal baseAmount = subscription.getPlan().getPrice();
                List<ChurchStorageAddon> activeAddons = churchStorageAddonRepository
                    .findByChurchIdAndStatus(subscription.getChurchId(), "ACTIVE");

                BigDecimal addonTotal = activeAddons.stream()
                    .map(addon -> addon.getPurchasePrice()) // Use locked price
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalAmount = baseAmount.add(addonTotal);

                String description = activeAddons.isEmpty()
                    ? "Monthly renewal - " + subscription.getPlan().getDisplayName()
                    : String.format("Monthly renewal - %s + %d addon(s)",
                        subscription.getPlan().getDisplayName(), activeAddons.size());

                // Create payment record
                Payment payment = Payment.builder()
                    .churchId(subscription.getChurchId())
                    .plan(subscription.getPlan())
                    .amount(totalAmount) // Total: base + addons
                    .currency("GHS")  // Ghana Cedis - Paystack does not support USD
                    .status("PENDING")
                    .paystackReference("RENEWAL-" + UUID.randomUUID())
                    .paymentType("SUBSCRIPTION")
                    .description(description)
                    .build();
                paymentRepository.save(payment);

                // Charge using stored authorization code
                JsonNode result = paystackService.chargeAuthorization(
                    authCode,
                    totalAmount, // Charge total amount
                    email,
                    payment.getPaystackReference()
                );

                // Check result
                if (result.get("status").asBoolean()) {
                    // Success: Update subscription
                    payment.markAsSuccessful();
                    payment.setPaystackTransactionId(result.get("data").get("reference").asText());
                    paymentRepository.save(payment);

                    LocalDate newPeriodStart = LocalDate.now();
                    LocalDate newPeriodEnd = LocalDate.now().plusMonths(1);
                    LocalDate newBillingDate = LocalDate.now().plusMonths(1);

                    subscription.setNextBillingDate(newBillingDate);
                    subscription.setCurrentPeriodStart(newPeriodStart);
                    subscription.setCurrentPeriodEnd(newPeriodEnd);
                    subscription.setFailedPaymentAttempts(0);
                    subscriptionRepository.save(subscription);

                    // CRITICAL: Update ALL active addons to keep renewal dates synchronized
                    for (ChurchStorageAddon addon : activeAddons) {
                        addon.updateRenewalDates(newPeriodStart, newPeriodEnd, newBillingDate);
                        churchStorageAddonRepository.save(addon);
                    }

                    log.info("Subscription renewed successfully for church {} (base + {} addons)",
                        subscription.getChurchId(), activeAddons.size());
                } else {
                    // Failure: Mark as PAST_DUE
                    String errorMsg = result.has("message") ? result.get("message").asText() : "Charge failed";
                    payment.markAsFailed(errorMsg);
                    paymentRepository.save(payment);

                    subscription.setStatus("PAST_DUE");
                    subscription.setFailedPaymentAttempts(subscription.getFailedPaymentAttempts() + 1);
                    subscriptionRepository.save(subscription);

                    log.warn("Renewal failed for church {}: {}", subscription.getChurchId(), errorMsg);
                }
            } catch (Exception e) {
                log.error("Error processing renewal for church {}", subscription.getChurchId(), e);
            }
        }
    }

    /**
     * Suspend subscriptions past grace period.
     * Checks each subscription's individual grace period (if any).
     */
    @Transactional
    public void suspendPastDueSubscriptions() {
        // Get all PAST_DUE subscriptions
        List<ChurchSubscription> pastDue = subscriptionRepository.findByStatus("PAST_DUE");

        for (ChurchSubscription subscription : pastDue) {
            // Check if subscription should be suspended based on its individual grace period
            if (subscription.shouldSuspend()) {
                // Use markAsSuspended() which sets status and initializes retention tracking
                subscription.markAsSuspended();
                subscriptionRepository.save(subscription);

                // CRITICAL: Suspend ALL active addons when subscription is suspended
                List<ChurchStorageAddon> activeAddons = churchStorageAddonRepository
                    .findByChurchIdAndStatus(subscription.getChurchId(), "ACTIVE");

                for (ChurchStorageAddon addon : activeAddons) {
                    storageAddonBillingService.suspendAddon(addon);
                }

                // Update storage limit (remove addon capacity)
                if (!activeAddons.isEmpty()) {
                    storageAddonBillingService.updateChurchStorageLimit(subscription.getChurchId());
                }

                log.warn("Subscription suspended for church {} due to non-payment (grace period: {} days). {} addon(s) also suspended. Data retention until: {}",
                        subscription.getChurchId(), subscription.getGracePeriodDays(), activeAddons.size(), subscription.getDataRetentionEndDate());
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
     */
    private void processRenewalWithPromoCredits(ChurchSubscription subscription) {
        // Use promotional credit instead of charging
        subscription.usePromotionalCredit();

        LocalDate newPeriodStart = LocalDate.now();
        LocalDate newPeriodEnd = LocalDate.now().plusMonths(1);
        LocalDate newBillingDate = LocalDate.now().plusMonths(1);

        subscription.setNextBillingDate(newBillingDate);
        subscription.setCurrentPeriodStart(newPeriodStart);
        subscription.setCurrentPeriodEnd(newPeriodEnd);
        subscription.setFailedPaymentAttempts(0);
        subscriptionRepository.save(subscription);

        // FREE MONTH COVERS ADDONS TOO - update addon renewal dates
        List<ChurchStorageAddon> activeAddons = churchStorageAddonRepository
            .findByChurchIdAndStatus(subscription.getChurchId(), "ACTIVE");

        for (ChurchStorageAddon addon : activeAddons) {
            addon.updateRenewalDates(newPeriodStart, newPeriodEnd, newBillingDate);
            churchStorageAddonRepository.save(addon);
        }

        log.info("Used promotional credit for church {}. {} free month(s) remaining. Free month includes {} addon(s)",
            subscription.getChurchId(), subscription.getFreeMonthsRemaining(), activeAddons.size());
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
     * Get church email for billing purposes.
     *
     * @param churchId Church ID
     * @return Church email address
     */
    private String getChurchEmail(Long churchId) {
        return churchRepository.findById(churchId)
            .map(church -> church.getEmail())
            .orElse(null);
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
        private long canceledSubscriptions;
        private long pastDueSubscriptions;
        private long suspendedSubscriptions;
        private BigDecimal totalRevenue;
        private long successfulPayments;
        private long failedPayments;
    }

    // ==================== PLAN MANAGEMENT (SUPERADMIN) ====================

    /**
     * Get all plans including inactive ones (SUPERADMIN only).
     */
    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getAllPlans() {
        return planRepository.findAll();
    }

    /**
     * Create a new subscription plan (SUPERADMIN only).
     */
    @Transactional
    public SubscriptionPlan createPlan(com.reuben.pastcare_spring.controllers.BillingController.CreatePlanRequest request) {
        // Check if plan with same name already exists
        if (planRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Plan with name '" + request.getName() + "' already exists");
        }

        SubscriptionPlan plan = SubscriptionPlan.builder()
            .name(request.getName())
            .displayName(request.getDisplayName())
            .description(request.getDescription())
            .price(request.getPrice())
            .billingInterval(request.getBillingInterval())
            .storageLimitMb(request.getStorageLimitMb())
            .userLimit(request.getUserLimit())
            .isFree(request.getIsFree())
            .isActive(true)
            .features(request.getFeatures())
            .displayOrder(request.getDisplayOrder())
            .paystackPlanCode(request.getPaystackPlanCode())
            .build();

        return planRepository.save(plan);
    }

    /**
     * Update an existing subscription plan (SUPERADMIN only).
     */
    @Transactional
    public SubscriptionPlan updatePlan(Long planId, com.reuben.pastcare_spring.controllers.BillingController.UpdatePlanRequest request) {
        SubscriptionPlan plan = getPlanById(planId);

        if (request.getDisplayName() != null) {
            plan.setDisplayName(request.getDisplayName());
        }
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getStorageLimitMb() != null) {
            plan.setStorageLimitMb(request.getStorageLimitMb());
        }
        if (request.getUserLimit() != null) {
            plan.setUserLimit(request.getUserLimit());
        }
        if (request.getFeatures() != null) {
            plan.setFeatures(request.getFeatures());
        }
        if (request.getDisplayOrder() != null) {
            plan.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getPaystackPlanCode() != null) {
            plan.setPaystackPlanCode(request.getPaystackPlanCode());
        }

        return planRepository.save(plan);
    }

    /**
     * Deactivate a subscription plan (SUPERADMIN only).
     */
    @Transactional
    public void deactivatePlan(Long planId) {
        SubscriptionPlan plan = getPlanById(planId);

        // Don't allow deactivating the free plan
        if (plan.getIsFree()) {
            throw new RuntimeException("Cannot deactivate the free plan");
        }

        plan.setIsActive(false);
        planRepository.save(plan);
    }

    /**
     * Activate a subscription plan (SUPERADMIN only).
     */
    @Transactional
    public void activatePlan(Long planId) {
        SubscriptionPlan plan = getPlanById(planId);
        plan.setIsActive(true);
        planRepository.save(plan);
    }

    // ==================== Grace Period Management ====================

    /**
     * Grant or update grace period for a church subscription (SUPERADMIN only).
     *
     * @param churchId Church ID
     * @param gracePeriodDays Number of grace period days (1-30)
     * @param reason Reason for granting grace period
     * @param extend Whether to extend existing grace period or reset it
     * @return Updated subscription
     */
    @Transactional
    public ChurchSubscription grantGracePeriod(Long churchId, Integer gracePeriodDays, String reason, Boolean extend) {
        ChurchSubscription subscription = getChurchSubscription(churchId);

        if (gracePeriodDays == null || gracePeriodDays < 1 || gracePeriodDays > 30) {
            throw new IllegalArgumentException("Grace period days must be between 1 and 30");
        }

        if (extend && subscription.getGracePeriodDays() != null) {
            // Extend existing grace period
            subscription.setGracePeriodDays(subscription.getGracePeriodDays() + gracePeriodDays);
        } else {
            // Set new grace period
            subscription.setGracePeriodDays(gracePeriodDays);
        }

        // Add reason to promotional note (reusing existing field for grace period notes)
        subscription.setPromotionalNote("Grace Period: " + reason);

        log.info("Granted {} days grace period to church {} ({}). Reason: {}",
                gracePeriodDays, churchId, extend ? "extended" : "new", reason);

        return subscriptionRepository.save(subscription);
    }

    /**
     * Revoke grace period for a church subscription (SUPERADMIN only).
     * Resets grace period to 0 (no grace period).
     *
     * @param churchId Church ID
     * @return Updated subscription
     */
    @Transactional
    public ChurchSubscription revokeGracePeriod(Long churchId) {
        ChurchSubscription subscription = getChurchSubscription(churchId);

        // Reset to 0 (no grace period)
        subscription.setGracePeriodDays(0);
        subscription.setPromotionalNote(null);

        log.info("Revoked grace period for church {}, reset to 0 (no grace period)", churchId);

        return subscriptionRepository.save(subscription);
    }

    /**
     * Get grace period status for a church subscription.
     *
     * @param churchId Church ID
     * @return Grace period details
     */
    @Transactional(readOnly = true)
    public GracePeriodStatus getGracePeriodStatus(Long churchId) {
        ChurchSubscription subscription = getChurchSubscription(churchId);

        boolean inGracePeriod = subscription.isInGracePeriod();
        LocalDate gracePeriodEndDate = null;
        Long daysRemaining = 0L;

        if (inGracePeriod && subscription.getNextBillingDate() != null) {
            gracePeriodEndDate = subscription.getNextBillingDate()
                    .plusDays(subscription.getGracePeriodDays());
            daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.now(), gracePeriodEndDate);
        }

        return new GracePeriodStatus(
                subscription.getGracePeriodDays(),
                inGracePeriod,
                gracePeriodEndDate,
                daysRemaining,
                subscription.getPromotionalNote()
        );
    }

    /**
     * Get all subscriptions currently in grace period (SUPERADMIN only).
     *
     * @return List of subscriptions in grace period
     */
    @Transactional(readOnly = true)
    public List<ChurchSubscription> getSubscriptionsInGracePeriod() {
        List<ChurchSubscription> pastDueSubscriptions =
                subscriptionRepository.findByStatus("PAST_DUE");

        return pastDueSubscriptions.stream()
                .filter(ChurchSubscription::isInGracePeriod)
                .toList();
    }

    /**
     * Get all subscriptions that should be suspended (past grace period).
     *
     * @return List of subscriptions past grace period
     */
    @Transactional(readOnly = true)
    public List<ChurchSubscription> getSubscriptionsPastGracePeriod() {
        List<ChurchSubscription> pastDueSubscriptions =
                subscriptionRepository.findByStatus("PAST_DUE");

        return pastDueSubscriptions.stream()
                .filter(ChurchSubscription::shouldSuspend)
                .toList();
    }

    /**
     * Inner class to hold grace period status information.
     */
    public record GracePeriodStatus(
            Integer gracePeriodDays,
            Boolean inGracePeriod,
            LocalDate gracePeriodEndDate,
            Long daysRemaining,
            String reason
    ) {}
}
