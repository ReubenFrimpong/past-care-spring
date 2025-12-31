package com.reuben.pastcare_spring.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.reuben.pastcare_spring.dtos.PaymentInitializationRequest;
import com.reuben.pastcare_spring.dtos.PaymentInitializationResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Service for managing storage addon purchases, billing, and renewals.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Prorated addon purchase (mid-cycle)</li>
 *   <li>Addon activation after payment verification</li>
 *   <li>Storage limit cache updates</li>
 *   <li>Addon cancellation</li>
 *   <li>Renewal date synchronization with base subscription</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorageAddonBillingService {

    private final ChurchStorageAddonRepository churchStorageAddonRepository;
    private final StorageAddonRepository storageAddonRepository;
    private final ChurchRepository churchRepository;
    private final ChurchSubscriptionRepository churchSubscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final PaystackService paystackService;

    /**
     * Initiate prorated storage addon purchase.
     *
     * <p>Flow:
     * <ol>
     *   <li>Validate subscription is active or in grace period</li>
     *   <li>Check addon not already purchased (prevent duplicates)</li>
     *   <li>Calculate prorated charge based on days remaining</li>
     *   <li>Create Payment record with reference ADDON-{UUID}</li>
     *   <li>Initialize Paystack payment</li>
     *   <li>Return authorization URL for redirect</li>
     * </ol>
     *
     * @param churchId Church purchasing addon
     * @param storageAddonId Addon to purchase
     * @param email User's email for Paystack
     * @param callbackUrl Frontend callback URL after payment
     * @return Paystack payment initialization response with authorization_url
     * @throws IllegalStateException if subscription inactive or addon already purchased
     */
    @Transactional
    public PaymentInitializationResponse purchaseStorageAddon(
            Long churchId,
            Long storageAddonId,
            String email,
            String callbackUrl
    ) {
        log.info("Initiating storage addon purchase: churchId={}, addonId={}", churchId, storageAddonId);

        // 1. Validate subscription status
        ChurchSubscription subscription = churchSubscriptionRepository
                .findByChurchId(churchId)
                .orElseThrow(() -> new IllegalStateException(
                        "No subscription found for church ID: " + churchId));

        if (!subscription.isActive() && !subscription.isInGracePeriod()) {
            throw new IllegalStateException(
                    "Subscription must be active or in grace period to purchase addons. " +
                    "Current status: " + subscription.getStatus());
        }

        // 2. Check for duplicate purchase
        StorageAddon addon = storageAddonRepository.findById(storageAddonId)
                .orElseThrow(() -> new IllegalStateException(
                        "Storage addon not found: " + storageAddonId));

        boolean alreadyPurchased = churchStorageAddonRepository
                .existsByChurchIdAndStorageAddonIdAndStatus(churchId, storageAddonId, "ACTIVE");

        if (alreadyPurchased) {
            throw new IllegalStateException(
                    "Church has already purchased this addon: " + addon.getName());
        }

        // 3. Calculate prorated charge
        ProrationResult proration = calculateProration(
                addon.getPrice(),
                subscription.getNextBillingDate()
        );

        // 4. Create Payment record
        String reference = "ADDON-" + UUID.randomUUID().toString();

        Payment payment = Payment.builder()
                .churchId(churchId)
                .amount(proration.getProratedAmount())
                .currency("GHS")
                .status("PENDING")
                .paystackReference(reference)
                .paymentType(proration.isProrated() ? "STORAGE_ADDON_PRORATED" : "STORAGE_ADDON")
                .description(String.format(
                        "Storage Addon: %s (%d GB) - %s",
                        addon.getName(),
                        addon.getStorageGb(),
                        proration.isProrated()
                                ? String.format("Prorated for %d days", proration.getDaysRemaining())
                                : "Full month"
                ))
                .metadata(String.format(
                        "{\"storage_addon_id\": %d, \"is_prorated\": %b, \"prorated_days\": %d, \"original_amount\": %.2f}",
                        storageAddonId,
                        proration.isProrated(),
                        proration.getDaysRemaining(),
                        addon.getPrice().doubleValue()
                ))
                .build();

        payment = paymentRepository.save(payment);
        log.info("Created payment record: reference={}, amount={}", reference, proration.getProratedAmount());

        // 5. Initialize Paystack payment
        PaymentInitializationRequest paystackRequest = new PaymentInitializationRequest();
        paystackRequest.setEmail(email);
        paystackRequest.setAmount(proration.getProratedAmount());
        paystackRequest.setReference(reference);
        paystackRequest.setCurrency("GHS");
        paystackRequest.setCallbackUrl(callbackUrl);
        // Note: donationType and memberId are not required for addon purchases

        PaymentInitializationResponse paystackResponse = paystackService.initializePayment(paystackRequest);

        log.info("Paystack payment initialized for addon purchase: reference={}", reference);
        return paystackResponse;
    }

    /**
     * Verify payment and activate addon (called from webhook).
     *
     * <p>Flow:
     * <ol>
     *   <li>Verify payment with Paystack</li>
     *   <li>Mark payment as successful</li>
     *   <li>Create ChurchStorageAddon record (status=ACTIVE)</li>
     *   <li>Sync next_renewal_date with base subscription</li>
     *   <li>Update church total_storage_limit_mb</li>
     * </ol>
     *
     * @param reference Paystack reference (ADDON-{UUID})
     */
    @Transactional
    public void verifyAndActivateAddon(String reference) {
        log.info("Verifying and activating addon: reference={}", reference);

        // 1. Find payment record
        Payment payment = paymentRepository.findByPaystackReference(reference)
                .orElseThrow(() -> new IllegalStateException(
                        "Payment not found: " + reference));

        if (payment.isSuccessful()) {
            log.warn("Payment already processed: reference={}", reference);
            return;
        }

        // 2. Verify with Paystack
        JsonNode verificationResult = paystackService.verifyPayment(reference);
        String status = verificationResult.get("data").get("status").asText();

        if (!"success".equals(status)) {
            payment.markAsFailed("Paystack verification failed: " + status);
            paymentRepository.save(payment);
            throw new IllegalStateException("Payment verification failed: " + status);
        }

        // 3. Mark payment successful
        payment.markAsSuccessful();
        paymentRepository.save(payment);

        // 4. Extract addon details from metadata
        String metadata = payment.getMetadata();
        Long storageAddonId = extractStorageAddonIdFromMetadata(metadata);
        boolean isProrated = extractBooleanFromMetadata(metadata, "is_prorated");
        Integer proratedDays = extractIntegerFromMetadata(metadata, "prorated_days");
        BigDecimal originalAmount = extractBigDecimalFromMetadata(metadata, "original_amount");

        StorageAddon addon = storageAddonRepository.findById(storageAddonId)
                .orElseThrow(() -> new IllegalStateException(
                        "Storage addon not found: " + storageAddonId));

        // 5. Get subscription for renewal date sync
        ChurchSubscription subscription = churchSubscriptionRepository
                .findByChurchId(payment.getChurchId())
                .orElseThrow(() -> new IllegalStateException(
                        "Subscription not found for church: " + payment.getChurchId()));

        // 6. Create ChurchStorageAddon record
        LocalDate today = LocalDate.now();
        ChurchStorageAddon churchAddon = ChurchStorageAddon.builder()
                .churchId(payment.getChurchId())
                .storageAddon(addon)
                .purchasedAt(LocalDateTime.now())
                .purchasePrice(originalAmount != null ? originalAmount : addon.getPrice())
                .purchaseReference(reference)
                .isProrated(isProrated)
                .proratedAmount(isProrated ? payment.getAmount() : null)
                .proratedDays(isProrated ? proratedDays : null)
                .currentPeriodStart(today)
                .currentPeriodEnd(subscription.getNextBillingDate().minusDays(1))
                .nextRenewalDate(subscription.getNextBillingDate()) // CRITICAL: sync with base
                .status("ACTIVE")
                .build();

        churchAddon = churchStorageAddonRepository.save(churchAddon);
        log.info("Activated storage addon: churchId={}, addon={}, capacity={}GB",
                payment.getChurchId(), addon.getName(), addon.getStorageGb());

        // 7. Update church storage limit cache
        updateChurchStorageLimit(payment.getChurchId());
    }

    /**
     * Calculate prorated charge for mid-cycle addon purchase.
     *
     * @param monthlyPrice Full monthly price of addon
     * @param nextBillingDate Next billing date of base subscription
     * @return Proration result with amount and days
     */
    public ProrationResult calculateProration(BigDecimal monthlyPrice, LocalDate nextBillingDate) {
        LocalDate today = LocalDate.now();

        // If next billing date is today or in the past, charge full month
        if (!today.isBefore(nextBillingDate)) {
            return new ProrationResult(monthlyPrice, 30, false);
        }

        // Calculate days remaining
        long daysRemaining = ChronoUnit.DAYS.between(today, nextBillingDate);

        // If less than 3 days remaining, charge for next full period
        if (daysRemaining < 3) {
            return new ProrationResult(monthlyPrice, 30, false);
        }

        // Calculate prorated amount (daily rate × days remaining)
        BigDecimal dailyRate = monthlyPrice.divide(BigDecimal.valueOf(30), 4, RoundingMode.HALF_UP);
        BigDecimal proratedAmount = dailyRate.multiply(BigDecimal.valueOf(daysRemaining))
                .setScale(2, RoundingMode.HALF_UP);

        return new ProrationResult(proratedAmount, (int) daysRemaining, true);
    }

    /**
     * Update church's total storage limit cache (base + active addons).
     *
     * <p>Formula: total_storage_limit_mb = 2048 (base) + SUM(storage_gb * 1024) WHERE status='ACTIVE'
     *
     * @param churchId Church to update
     */
    @Transactional
    public void updateChurchStorageLimit(Long churchId) {
        log.debug("Updating storage limit cache for church: {}", churchId);

        // Calculate total addon storage in MB
        Long addonStorageMb = churchStorageAddonRepository.sumActiveStorageMbByChurchId(churchId);

        // Total = base (2048 MB) + addons
        long totalLimitMb = 2048L + (addonStorageMb != null ? addonStorageMb : 0L);

        // Update church
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalStateException("Church not found: " + churchId));

        church.setTotalStorageLimitMb(totalLimitMb);
        church.setStorageLimitUpdatedAt(LocalDateTime.now());
        churchRepository.save(church);

        log.info("Updated storage limit for church {}: {}MB (base 2048 + addons {})",
                churchId, totalLimitMb, addonStorageMb);
    }

    /**
     * Cancel addon (remains active until period end, no refund).
     *
     * @param churchId Church ID
     * @param addonId Storage addon ID
     * @param reason Cancellation reason
     */
    @Transactional
    public void cancelAddon(Long churchId, Long addonId, String reason) {
        log.info("Canceling addon: churchId={}, addonId={}, reason={}", churchId, addonId, reason);

        ChurchStorageAddon churchAddon = churchStorageAddonRepository
                .findByChurchIdAndStorageAddonIdAndStatus(churchId, addonId, "ACTIVE")
                .orElseThrow(() -> new IllegalStateException(
                        "Active addon not found for church " + churchId + " and addon " + addonId));

        churchAddon.markAsCanceled(reason);
        churchStorageAddonRepository.save(churchAddon);

        log.info("Addon canceled (active until {}): churchId={}, addon={}",
                churchAddon.getCurrentPeriodEnd(), churchId, churchAddon.getDisplayName());

        // Note: Storage limit NOT updated yet - addon remains active until period end
    }

    /**
     * Suspend addon (when subscription is suspended).
     *
     * @param churchAddon Addon to suspend
     */
    @Transactional
    public void suspendAddon(ChurchStorageAddon churchAddon) {
        churchAddon.markAsSuspended();
        churchStorageAddonRepository.save(churchAddon);
        log.info("Suspended addon: churchId={}, addon={}",
                churchAddon.getChurchId(), churchAddon.getDisplayName());
    }

    /**
     * Reactivate addon (when subscription is manually activated).
     *
     * @param churchAddon Addon to reactivate
     * @param newRenewalDate New renewal date (sync with subscription)
     */
    @Transactional
    public void reactivateAddon(ChurchStorageAddon churchAddon, LocalDate newRenewalDate) {
        churchAddon.reactivate();
        churchAddon.setNextRenewalDate(newRenewalDate);
        churchStorageAddonRepository.save(churchAddon);
        log.info("Reactivated addon: churchId={}, addon={}",
                churchAddon.getChurchId(), churchAddon.getDisplayName());
    }

    /**
     * Helper: Extract storage addon ID from payment metadata JSON.
     */
    private Long extractStorageAddonIdFromMetadata(String metadata) {
        try {
            String pattern = "\"storage_addon_id\":\\s*(\\d+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(metadata);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
        } catch (Exception e) {
            log.error("Failed to extract storage_addon_id from metadata: {}", metadata, e);
        }
        throw new IllegalStateException("Could not extract storage_addon_id from payment metadata");
    }

    /**
     * Helper: Extract boolean from payment metadata JSON.
     */
    private boolean extractBooleanFromMetadata(String metadata, String field) {
        try {
            String pattern = "\"" + field + "\":\\s*(true|false)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(metadata);
            if (m.find()) {
                return Boolean.parseBoolean(m.group(1));
            }
        } catch (Exception e) {
            log.error("Failed to extract {} from metadata: {}", field, metadata, e);
        }
        return false;
    }

    /**
     * Helper: Extract integer from payment metadata JSON.
     */
    private Integer extractIntegerFromMetadata(String metadata, String field) {
        try {
            String pattern = "\"" + field + "\":\\s*(\\d+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(metadata);
            if (m.find()) {
                return Integer.parseInt(m.group(1));
            }
        } catch (Exception e) {
            log.error("Failed to extract {} from metadata: {}", field, metadata, e);
        }
        return null;
    }

    /**
     * Helper: Extract BigDecimal from payment metadata JSON.
     */
    private BigDecimal extractBigDecimalFromMetadata(String metadata, String field) {
        try {
            String pattern = "\"" + field + "\":\\s*([\\d.]+)";
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(metadata);
            if (m.find()) {
                return new BigDecimal(m.group(1));
            }
        } catch (Exception e) {
            log.error("Failed to extract {} from metadata: {}", field, metadata, e);
        }
        return null;
    }

    /**
     * Proration calculation result.
     */
    public static class ProrationResult {
        private final BigDecimal proratedAmount;
        private final int daysRemaining;
        private final boolean prorated;

        public ProrationResult(BigDecimal proratedAmount, int daysRemaining, boolean prorated) {
            this.proratedAmount = proratedAmount;
            this.daysRemaining = daysRemaining;
            this.prorated = prorated;
        }

        public BigDecimal getProratedAmount() {
            return proratedAmount;
        }

        public int getDaysRemaining() {
            return daysRemaining;
        }

        public boolean isProrated() {
            return prorated;
        }
    }
}
