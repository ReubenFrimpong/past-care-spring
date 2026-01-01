package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a pricing tier based on congregation size.
 *
 * <p>Replaces storage-based pricing with member-count-based pricing.
 * Each tier covers a range of congregation sizes and has different prices
 * for various billing intervals (monthly, quarterly, biannual, annual).
 *
 * @since 2026-01-01
 */
@Entity
@Table(name = "congregation_pricing_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CongregationPricingTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Internal tier identifier (e.g., "TIER_1", "TIER_2").
     */
    @Column(nullable = false, unique = true, length = 50)
    private String tierName;

    /**
     * User-friendly display name (e.g., "Small Church (1-200)").
     */
    @Column(nullable = false, length = 100)
    private String displayName;

    /**
     * Detailed description of this tier.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Minimum number of members for this tier (inclusive).
     */
    @Column(nullable = false)
    private Integer minMembers;

    /**
     * Maximum number of members for this tier (inclusive).
     * NULL indicates unlimited (for highest tier).
     */
    @Column
    private Integer maxMembers;

    /**
     * Price per month in base currency (USD).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPriceUsd;

    /**
     * Price for 3 months in base currency (USD).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quarterlyPriceUsd;

    /**
     * Price for 6 months in base currency (USD).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal biannualPriceUsd;

    /**
     * Price for 12 months in base currency (USD).
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal annualPriceUsd;

    /**
     * Discount percentage for quarterly vs monthly billing.
     * E.g., 8.00 means 8% discount.
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal quarterlyDiscountPct;

    /**
     * Discount percentage for biannual vs monthly billing.
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal biannualDiscountPct;

    /**
     * Discount percentage for annual vs monthly billing.
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal annualDiscountPct;

    /**
     * Features included in this tier (JSON array of strings).
     */
    @Column(columnDefinition = "TEXT")
    private String features;

    /**
     * Whether this tier is active and available for new signups.
     */
    @Column(nullable = false)
    private Boolean isActive = true;

    /**
     * Display order for showing tiers on frontend (1 = first).
     */
    @Column(nullable = false)
    private Integer displayOrder;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Check if a given member count falls within this tier's range.
     *
     * @param memberCount Number of members
     * @return true if member count is in range, false otherwise
     */
    public boolean isInRange(int memberCount) {
        if (memberCount < minMembers) {
            return false;
        }
        // maxMembers == null means unlimited
        return maxMembers == null || memberCount <= maxMembers;
    }

    /**
     * Get price for a specific billing interval.
     *
     * @param intervalName Interval name (MONTHLY, QUARTERLY, BIANNUAL, ANNUAL)
     * @return Price for that interval in USD
     * @throws IllegalArgumentException if interval name is invalid
     */
    public BigDecimal getPriceForInterval(String intervalName) {
        if (intervalName == null) {
            throw new IllegalArgumentException("Interval name cannot be null");
        }

        return switch (intervalName.toUpperCase()) {
            case "MONTHLY" -> monthlyPriceUsd;
            case "QUARTERLY" -> quarterlyPriceUsd;
            case "BIANNUAL" -> biannualPriceUsd;
            case "ANNUAL" -> annualPriceUsd;
            default -> throw new IllegalArgumentException("Invalid interval name: " + intervalName);
        };
    }

    /**
     * Get discount percentage for a specific billing interval.
     *
     * @param intervalName Interval name (QUARTERLY, BIANNUAL, ANNUAL)
     * @return Discount percentage, or 0 for MONTHLY
     */
    public BigDecimal getDiscountForInterval(String intervalName) {
        if (intervalName == null) {
            return BigDecimal.ZERO;
        }

        return switch (intervalName.toUpperCase()) {
            case "MONTHLY" -> BigDecimal.ZERO;
            case "QUARTERLY" -> quarterlyDiscountPct != null ? quarterlyDiscountPct : BigDecimal.ZERO;
            case "BIANNUAL" -> biannualDiscountPct != null ? biannualDiscountPct : BigDecimal.ZERO;
            case "ANNUAL" -> annualDiscountPct != null ? annualDiscountPct : BigDecimal.ZERO;
            default -> BigDecimal.ZERO;
        };
    }

    /**
     * Calculate total savings for a billing interval compared to monthly.
     *
     * @param intervalName Interval name
     * @return Amount saved in USD
     */
    public BigDecimal calculateSavings(String intervalName) {
        if (intervalName == null || "MONTHLY".equalsIgnoreCase(intervalName)) {
            return BigDecimal.ZERO;
        }

        int months = switch (intervalName.toUpperCase()) {
            case "QUARTERLY" -> 3;
            case "BIANNUAL" -> 6;
            case "ANNUAL" -> 12;
            default -> 1;
        };

        BigDecimal monthlyTotal = monthlyPriceUsd.multiply(BigDecimal.valueOf(months));
        BigDecimal intervalPrice = getPriceForInterval(intervalName);

        return monthlyTotal.subtract(intervalPrice);
    }

    @Override
    public String toString() {
        return "CongregationPricingTier{" +
                "id=" + id +
                ", tierName='" + tierName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", minMembers=" + minMembers +
                ", maxMembers=" + maxMembers +
                ", monthlyPriceUsd=" + monthlyPriceUsd +
                ", isActive=" + isActive +
                '}';
    }
}
