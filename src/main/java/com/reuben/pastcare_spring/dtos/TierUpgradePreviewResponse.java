package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for tier upgrade preview.
 *
 * <p>Contains complete financial breakdown of the upgrade
 * for display to user before payment confirmation.
 *
 * @since 2026-01-02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TierUpgradePreviewResponse {

    // ==================== CURRENT SUBSCRIPTION ====================

    /**
     * Current tier name (e.g., "TIER_2")
     */
    private String currentTier;

    /**
     * Current billing interval (e.g., "MONTHLY")
     */
    private String currentInterval;

    // ==================== NEW SUBSCRIPTION ====================

    /**
     * New tier name (e.g., "TIER_3")
     */
    private String newTier;

    /**
     * New billing interval (e.g., "ANNUAL")
     */
    private String newInterval;

    // ==================== TIME CALCULATIONS ====================

    /**
     * Days remaining in current billing period
     */
    private int daysRemaining;

    /**
     * Days already used in current billing period
     */
    private int daysUsed;

    // ==================== PRICING (GHS - Primary Display) ====================

    /**
     * Current tier+interval price in GHS
     */
    private BigDecimal currentPriceGhs;

    /**
     * New tier+interval price in GHS
     */
    private BigDecimal newPriceGhs;

    /**
     * Credit for unused time on current tier (GHS)
     */
    private BigDecimal unusedCreditGhs;

    /**
     * Prorated charge for new tier for remaining days (GHS)
     */
    private BigDecimal proratedChargeGhs;

    /**
     * Net charge = prorated charge - unused credit (GHS)
     * This is the amount customer will pay
     */
    private BigDecimal netChargeGhs;

    // ==================== DATES ====================

    /**
     * Current next billing date (before upgrade)
     */
    private LocalDate currentNextBillingDate;

    /**
     * New next billing date (after upgrade)
     * - Same as current for tier-only upgrades
     * - Extended for interval changes
     */
    private LocalDate newNextBillingDate;

    // ==================== METADATA ====================

    /**
     * Type of change: TIER_UPGRADE, INTERVAL_CHANGE, or COMBINED
     */
    private String changeType;

    /**
     * Human-readable summary of the upgrade
     * Example: "Upgrade: TIER_2 → TIER_3 | Days: 15/30 remaining | Credit: GHS 60.00 | Charge: GHS 84.00 | Net: GHS 24.00"
     */
    private String summary;
}
