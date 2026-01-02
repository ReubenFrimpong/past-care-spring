package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing the complete audit trail of subscription tier changes.
 * Tracks all financial details, proration calculations, and payment status.
 */
@Entity
@Table(name = "tier_change_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TierChangeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "church_id", nullable = false)
    private Church church;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private ChurchSubscription subscription;

    // Tier change details
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "old_tier_id", nullable = false)
    private CongregationPricingTier oldTier;

    @Column(name = "old_tier_name", nullable = false, length = 50)
    private String oldTierName; // Denormalized for history preservation

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "new_tier_id", nullable = false)
    private CongregationPricingTier newTier;

    @Column(name = "new_tier_name", nullable = false, length = 50)
    private String newTierName; // Denormalized for history preservation

    // Billing interval change (nullable if tier-only upgrade)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "old_interval_id")
    private SubscriptionBillingInterval oldInterval;

    @Column(name = "old_interval_name", length = 50)
    private String oldIntervalName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "new_interval_id")
    private SubscriptionBillingInterval newInterval;

    @Column(name = "new_interval_name", length = 50)
    private String newIntervalName;

    // Financial fields - days calculation
    @Column(name = "days_remaining", nullable = false)
    private Integer daysRemaining;

    @Column(name = "days_used", nullable = false)
    private Integer daysUsed;

    // Financial fields - USD (reference currency)
    @Column(name = "old_price_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal oldPriceUsd;

    @Column(name = "new_price_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal newPriceUsd;

    @Column(name = "unused_credit_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal unusedCreditUsd;

    @Column(name = "prorated_charge_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal proratedChargeUsd;

    @Column(name = "net_charge_usd", nullable = false, precision = 10, scale = 2)
    private BigDecimal netChargeUsd;

    // Financial fields - GHS (payment currency)
    @Column(name = "old_price_ghs", nullable = false, precision = 10, scale = 2)
    private BigDecimal oldPriceGhs;

    @Column(name = "new_price_ghs", nullable = false, precision = 10, scale = 2)
    private BigDecimal newPriceGhs;

    @Column(name = "unused_credit_ghs", nullable = false, precision = 10, scale = 2)
    private BigDecimal unusedCreditGhs;

    @Column(name = "prorated_charge_ghs", nullable = false, precision = 10, scale = 2)
    private BigDecimal proratedChargeGhs;

    @Column(name = "net_charge_ghs", nullable = false, precision = 10, scale = 2)
    private BigDecimal netChargeGhs;

    // Payment tracking
    @Column(name = "payment_reference", unique = true, nullable = false)
    private String paymentReference;

    @Column(name = "payment_status", nullable = false, length = 50)
    private String paymentStatus; // PENDING, COMPLETED, FAILED

    @Column(name = "paystack_authorization_code")
    private String paystackAuthorizationCode;

    // Dates
    @Column(name = "old_next_billing_date", nullable = false)
    private LocalDate oldNextBillingDate;

    @Column(name = "new_next_billing_date", nullable = false)
    private LocalDate newNextBillingDate;

    @Column(name = "change_requested_at", nullable = false)
    private LocalDateTime changeRequestedAt;

    @Column(name = "payment_completed_at")
    private LocalDateTime paymentCompletedAt;

    // Metadata
    @Column(name = "change_type", nullable = false, length = 50)
    private String changeType; // TIER_UPGRADE, INTERVAL_CHANGE, COMBINED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiated_by_user_id")
    private User initiatedBy;

    @Column(name = "reason")
    private String reason;

    @PrePersist
    protected void onCreate() {
        if (changeRequestedAt == null) {
            changeRequestedAt = LocalDateTime.now();
        }
    }

    // Business methods

    /**
     * Mark this tier change as successfully completed
     */
    public void markAsCompleted(String authorizationCode) {
        this.paymentStatus = "COMPLETED";
        this.paystackAuthorizationCode = authorizationCode;
        this.paymentCompletedAt = LocalDateTime.now();
    }

    /**
     * Mark this tier change as failed
     */
    public void markAsFailed() {
        this.paymentStatus = "FAILED";
    }

    /**
     * Check if this tier change is completed
     */
    public boolean isCompleted() {
        return "COMPLETED".equals(paymentStatus);
    }

    /**
     * Check if this is a tier-only upgrade (no interval change)
     */
    public boolean isTierOnlyUpgrade() {
        return "TIER_UPGRADE".equals(changeType);
    }

    /**
     * Check if this involves an interval change
     */
    public boolean isIntervalChange() {
        return "INTERVAL_CHANGE".equals(changeType) || "COMBINED".equals(changeType);
    }
}
