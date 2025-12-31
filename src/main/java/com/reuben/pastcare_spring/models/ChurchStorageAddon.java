package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Junction entity tracking storage addon purchases by churches.
 *
 * <p>Key features:
 * <ul>
 *   <li>Price locking: purchase_price locked at time of purchase</li>
 *   <li>Prorated billing: first month charged proportionally based on days remaining</li>
 *   <li>Renewal synchronization: next_renewal_date MUST match base subscription</li>
 *   <li>Status management: ACTIVE, CANCELED (active until period end), SUSPENDED</li>
 *   <li>Complete audit trail</li>
 * </ul>
 */
@Entity
@Table(name = "church_storage_addons",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_church_addon",
                columnNames = {"church_id", "storage_addon_id"}
        ))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChurchStorageAddon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Church that purchased the addon (tenant)
     */
    @Column(name = "church_id", nullable = false)
    private Long churchId;

    /**
     * Storage addon that was purchased (EAGER to avoid N+1)
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "storage_addon_id", nullable = false)
    private StorageAddon storageAddon;

    /**
     * Purchase details
     */
    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;

    /**
     * Price locked at purchase time (prevents future price changes from affecting renewals)
     */
    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    /**
     * Paystack payment reference: ADDON-{UUID}
     */
    @Column(name = "purchase_reference", unique = true, length = 100)
    private String purchaseReference;

    /**
     * Prorating (first month only)
     */
    @Column(name = "is_prorated", nullable = false)
    @Builder.Default
    private Boolean isProrated = false;

    /**
     * Actual charged amount if prorated (less than purchase_price)
     */
    @Column(name = "prorated_amount", precision = 10, scale = 2)
    private BigDecimal proratedAmount;

    /**
     * Days remaining in billing period when purchased
     */
    @Column(name = "prorated_days")
    private Integer proratedDays;

    /**
     * Current billing period
     */
    @Column(name = "current_period_start", nullable = false)
    private LocalDate currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private LocalDate currentPeriodEnd;

    /**
     * CRITICAL: Must stay synchronized with base subscription's next_billing_date
     */
    @Column(name = "next_renewal_date", nullable = false)
    private LocalDate nextRenewalDate;

    /**
     * Status: ACTIVE, CANCELED, SUSPENDED
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * Cancellation details
     */
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    /**
     * Suspension details
     */
    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    /**
     * Audit timestamps
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (purchasedAt == null) {
            purchasedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Business logic methods
     */

    /**
     * Check if addon is currently active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Check if addon is canceled (but still active until period end)
     */
    public boolean isCanceled() {
        return "CANCELED".equals(status);
    }

    /**
     * Check if addon is suspended
     */
    public boolean isSuspended() {
        return "SUSPENDED".equals(status);
    }

    /**
     * Mark addon as canceled (remains active until period end, no refund)
     */
    public void markAsCanceled(String reason) {
        this.status = "CANCELED";
        this.canceledAt = LocalDateTime.now();
        this.cancellationReason = reason;
    }

    /**
     * Mark addon as suspended (when base subscription is suspended)
     */
    public void markAsSuspended() {
        this.status = "SUSPENDED";
        this.suspendedAt = LocalDateTime.now();
    }

    /**
     * Reactivate addon (when subscription is manually activated by SUPERADMIN)
     */
    public void reactivate() {
        this.status = "ACTIVE";
        this.suspendedAt = null;
        this.canceledAt = null;
        this.cancellationReason = null;
    }

    /**
     * Update renewal dates (when subscription renews successfully)
     */
    public void updateRenewalDates(LocalDate newPeriodStart, LocalDate newPeriodEnd, LocalDate newRenewalDate) {
        this.currentPeriodStart = newPeriodStart;
        this.currentPeriodEnd = newPeriodEnd;
        this.nextRenewalDate = newRenewalDate;
    }

    /**
     * Get storage capacity in MB (converted from GB)
     */
    public long getStorageCapacityMb() {
        return storageAddon.getStorageGb() * 1024L;
    }

    /**
     * Get display name for UI
     */
    public String getDisplayName() {
        return storageAddon != null ? storageAddon.getName() : "Unknown Addon";
    }

    /**
     * Check if addon is in grace period (canceled but still active)
     */
    public boolean isInGracePeriod() {
        if (!isCanceled()) {
            return false;
        }
        LocalDate today = LocalDate.now();
        return !today.isAfter(currentPeriodEnd);
    }

    /**
     * Calculate amount for next renewal (uses locked purchase price)
     */
    public BigDecimal calculateRenewalAmount(int billingPeriodMonths) {
        return purchasePrice.multiply(BigDecimal.valueOf(billingPeriodMonths));
    }
}
