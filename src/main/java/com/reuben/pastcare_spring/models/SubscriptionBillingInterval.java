package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing available billing intervals for subscriptions.
 *
 * <p>Defines intervals like MONTHLY, QUARTERLY, BIANNUAL, ANNUAL.
 * Used to determine billing frequency and calculate pricing.
 *
 * @since 2026-01-01
 */
@Entity
@Table(name = "subscription_billing_intervals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionBillingInterval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Internal interval identifier (e.g., "MONTHLY", "QUARTERLY").
     */
    @Column(nullable = false, unique = true, length = 20)
    private String intervalName;

    /**
     * User-friendly display name (e.g., "Quarterly (3 Months)").
     */
    @Column(nullable = false, length = 50)
    private String displayName;

    /**
     * Number of months this interval covers.
     * 1 = monthly, 3 = quarterly, 6 = biannual, 12 = annual
     */
    @Column(nullable = false)
    private Integer months;

    /**
     * Display order for showing intervals on frontend (1 = first).
     */
    @Column(nullable = false)
    private Integer displayOrder;

    /**
     * Whether this interval is active and available for selection.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Calculate the annualized monthly rate for this interval.
     * E.g., quarterly = 3 months, so annualized monthly = 12/3 = 4 intervals per year
     *
     * @return Number of intervals per year
     */
    public int getIntervalsPerYear() {
        if (months == null || months == 0) {
            return 1;
        }
        return 12 / months;
    }

    /**
     * Check if this is a monthly interval.
     *
     * @return true if monthly, false otherwise
     */
    public boolean isMonthly() {
        return months != null && months == 1;
    }

    /**
     * Get a user-friendly description of savings for this interval.
     *
     * @param discountPct Discount percentage for this interval
     * @return Description like "Save 8% with quarterly billing"
     */
    public String getSavingsDescription(double discountPct) {
        if (isMonthly() || discountPct <= 0) {
            return "Standard monthly billing";
        }
        return String.format("Save %.0f%% with %s billing",
                discountPct, displayName.toLowerCase());
    }

    @Override
    public String toString() {
        return "SubscriptionBillingInterval{" +
                "id=" + id +
                ", intervalName='" + intervalName + '\'' +
                ", displayName='" + displayName + '\'' +
                ", months=" + months +
                ", isActive=" + isActive +
                '}';
    }
}
