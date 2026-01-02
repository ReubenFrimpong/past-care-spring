package com.reuben.pastcare_spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Immutable value object containing all proration calculation results for tier upgrades.
 *
 * <p>This object encapsulates the complete financial breakdown of a subscription tier upgrade,
 * including credits for unused time, prorated charges for the new tier, and net amounts due.
 *
 * <p>Instances are immutable and thread-safe.
 */
@Getter
@AllArgsConstructor
public class ProrationCalculation {

    // ==================== TIME CALCULATIONS ====================

    /**
     * Days remaining in the current billing period
     */
    private final int daysRemaining;

    /**
     * Days already used in the current billing period
     */
    private final int daysUsed;

    /**
     * Total days in the billing period (30, 90, 180, or 360)
     */
    private final int totalDaysInPeriod;

    // ==================== OLD TIER/INTERVAL PRICING ====================

    /**
     * Price of old tier+interval in USD (reference currency)
     */
    private final BigDecimal oldPriceUsd;

    /**
     * Price of old tier+interval in GHS (payment currency)
     */
    private final BigDecimal oldPriceGhs;

    // ==================== NEW TIER/INTERVAL PRICING ====================

    /**
     * Price of new tier+interval in USD (reference currency)
     */
    private final BigDecimal newPriceUsd;

    /**
     * Price of new tier+interval in GHS (payment currency)
     */
    private final BigDecimal newPriceGhs;

    // ==================== PRORATION AMOUNTS (USD - Reference Currency) ====================

    /**
     * Credit for unused portion of old tier+interval (USD)
     * Formula: (old daily rate) × (days remaining)
     */
    private final BigDecimal unusedCreditUsd;

    /**
     * Prorated charge for new tier+interval for remaining days (USD)
     * Formula: (new daily rate) × (days remaining) OR full price for interval changes
     */
    private final BigDecimal proratedChargeUsd;

    /**
     * Net charge = prorated charge - unused credit (USD)
     * This is the amount that will be charged to the customer
     */
    private final BigDecimal netChargeUsd;

    // ==================== PRORATION AMOUNTS (GHS - Payment Currency) ====================

    /**
     * Credit for unused portion of old tier+interval (GHS)
     * This is what the customer gets back from their current subscription
     */
    private final BigDecimal unusedCreditGhs;

    /**
     * Prorated charge for new tier+interval for remaining days (GHS)
     * This is the charge for the new tier for the remaining period
     */
    private final BigDecimal proratedChargeGhs;

    /**
     * Net charge = prorated charge - unused credit (GHS)
     * This is the actual amount customer pays via Paystack
     */
    private final BigDecimal netChargeGhs;

    // ==================== DATES ====================

    /**
     * Current next billing date (before upgrade)
     */
    private final LocalDate currentNextBillingDate;

    /**
     * New next billing date (after upgrade)
     * - SAME as current for tier-only upgrades
     * - EXTENDED for interval changes
     */
    private final LocalDate newNextBillingDate;

    // ==================== METADATA ====================

    /**
     * Type of change: TIER_UPGRADE, INTERVAL_CHANGE, or COMBINED
     */
    private final String changeType;

    /**
     * Old tier name (e.g., "TIER_2")
     */
    private final String oldTierName;

    /**
     * New tier name (e.g., "TIER_3")
     */
    private final String newTierName;

    /**
     * Old billing interval name (e.g., "MONTHLY")
     */
    private final String oldIntervalName;

    /**
     * New billing interval name (e.g., "ANNUAL")
     */
    private final String newIntervalName;

    // ==================== BUSINESS METHODS ====================

    /**
     * Get formatted summary of the upgrade calculation.
     *
     * <p>Example: "Upgrade: TIER_2 → TIER_3 | Days: 15/30 remaining | Credit: GHS 5.00 | Charge: GHS 7.00 | Net: GHS 2.00"
     *
     * @return human-readable summary
     */
    public String getFormattedSummary() {
        return String.format(
            "Upgrade: %s → %s | Days: %d/%d remaining | Credit: GHS %.2f | Charge: GHS %.2f | Net: GHS %.2f",
            oldTierName,
            newTierName,
            daysRemaining,
            totalDaysInPeriod,
            unusedCreditGhs,
            proratedChargeGhs,
            netChargeGhs
        );
    }

    /**
     * Check if this is a tier-only upgrade (no billing interval change).
     *
     * @return true if only tier is changing
     */
    public boolean isTierOnlyUpgrade() {
        return "TIER_UPGRADE".equals(changeType);
    }

    /**
     * Check if this involves a billing interval change.
     *
     * @return true if interval is changing (with or without tier change)
     */
    public boolean isIntervalChange() {
        return "INTERVAL_CHANGE".equals(changeType) || "COMBINED".equals(changeType);
    }

    /**
     * Check if this is a combined tier + interval upgrade.
     *
     * @return true if both tier and interval are changing
     */
    public boolean isCombinedUpgrade() {
        return "COMBINED".equals(changeType);
    }

    /**
     * Check if the next billing date will change.
     *
     * @return true if billing date extends
     */
    public boolean billingDateChanges() {
        return !currentNextBillingDate.equals(newNextBillingDate);
    }

    /**
     * Get the number of additional days added to billing period.
     *
     * @return days added (0 for tier-only upgrades)
     */
    public long getAdditionalDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(currentNextBillingDate, newNextBillingDate);
    }

    /**
     * Get percentage of billing period remaining.
     *
     * @return percentage (0-100)
     */
    public double getPercentageRemaining() {
        if (totalDaysInPeriod == 0) return 0;
        return (double) daysRemaining / totalDaysInPeriod * 100;
    }

    /**
     * Validate that the calculation is mathematically sound.
     *
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        // Net charge cannot be negative
        if (netChargeGhs.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException(
                String.format("Net charge cannot be negative: GHS %.2f", netChargeGhs)
            );
        }

        // Days must be positive
        if (daysRemaining < 0 || daysUsed < 0 || totalDaysInPeriod <= 0) {
            throw new IllegalStateException(
                String.format("Invalid days: remaining=%d, used=%d, total=%d",
                    daysRemaining, daysUsed, totalDaysInPeriod)
            );
        }

        // Days must sum correctly
        if (daysRemaining + daysUsed != totalDaysInPeriod) {
            throw new IllegalStateException(
                String.format("Days don't sum correctly: %d + %d ≠ %d",
                    daysRemaining, daysUsed, totalDaysInPeriod)
            );
        }

        // Change type must be valid
        if (!isTierOnlyUpgrade() && !isIntervalChange() && !isCombinedUpgrade()) {
            throw new IllegalStateException("Invalid change type: " + changeType);
        }

        // Prices must be non-negative
        if (oldPriceGhs.compareTo(BigDecimal.ZERO) < 0 ||
            newPriceGhs.compareTo(BigDecimal.ZERO) < 0 ||
            unusedCreditGhs.compareTo(BigDecimal.ZERO) < 0 ||
            proratedChargeGhs.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Prices cannot be negative");
        }

        // For tier-only upgrades, billing date should not change
        if (isTierOnlyUpgrade() && billingDateChanges()) {
            throw new IllegalStateException(
                "Tier-only upgrade should not change billing date"
            );
        }

        // For interval changes, billing date should extend
        if (isIntervalChange() && !billingDateChanges()) {
            throw new IllegalStateException(
                "Interval change must extend billing date"
            );
        }
    }

    @Override
    public String toString() {
        return "ProrationCalculation{" +
            "changeType='" + changeType + '\'' +
            ", oldTier='" + oldTierName + '\'' +
            ", newTier='" + newTierName + '\'' +
            ", oldInterval='" + oldIntervalName + '\'' +
            ", newInterval='" + newIntervalName + '\'' +
            ", daysRemaining=" + daysRemaining +
            ", daysUsed=" + daysUsed +
            ", netChargeGhs=" + netChargeGhs +
            ", currentNextBillingDate=" + currentNextBillingDate +
            ", newNextBillingDate=" + newNextBillingDate +
            '}';
    }
}
