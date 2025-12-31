package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Church subscription entity tracking subscription status per church.
 *
 * <p>Manages:
 * <ul>
 *   <li>Current subscription plan</li>
 *   <li>Subscription status (active, past_due, canceled, trialing)</li>
 *   <li>Billing dates and trial period</li>
 *   <li>Paystack subscription details</li>
 * </ul>
 */
@Entity
@Table(name = "church_subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChurchSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Church ID (tenant)
     */
    @Column(name = "church_id", nullable = false, unique = true)
    private Long churchId;

    /**
     * Current subscription plan
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    /**
     * Subscription status: ACTIVE, PAST_DUE, CANCELED, SUSPENDED
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "ACTIVE";

    /**
     * Next billing date
     */
    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;

    /**
     * Current period start date
     */
    @Column(name = "current_period_start")
    private LocalDate currentPeriodStart;

    /**
     * Current period end date
     */
    @Column(name = "current_period_end")
    private LocalDate currentPeriodEnd;

    /**
     * Date when subscription was canceled (if applicable)
     */
    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    /**
     * Date when subscription ends (for canceled subscriptions with remaining time)
     */
    @Column(name = "ends_at")
    private LocalDate endsAt;

    /**
     * Paystack customer code
     */
    @Column(name = "paystack_customer_code", length = 100)
    private String paystackCustomerCode;

    /**
     * Paystack subscription code
     */
    @Column(name = "paystack_subscription_code", length = 100)
    private String paystackSubscriptionCode;

    /**
     * Paystack email token for customer
     */
    @Column(name = "paystack_email_token", length = 100)
    private String paystackEmailToken;

    /**
     * Paystack authorization code (for recurring payments)
     */
    @Column(name = "paystack_authorization_code", length = 100)
    private String paystackAuthorizationCode;

    /**
     * Payment method type: CARD, BANK_TRANSFER, MOBILE_MONEY
     */
    @Column(name = "payment_method_type", length = 50)
    private String paymentMethodType;

    /**
     * Last 4 digits of card (if card payment)
     */
    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    /**
     * Card brand: VISA, MASTERCARD, etc.
     */
    @Column(name = "card_brand", length = 50)
    private String cardBrand;

    /**
     * Billing period: MONTHLY, QUARTERLY (3 months), BIANNUAL (6 months), YEARLY
     */
    @Column(name = "billing_period", length = 20)
    @Builder.Default
    private String billingPeriod = "MONTHLY";

    /**
     * Number of months in the billing period (1, 3, 6, or 12)
     */
    @Column(name = "billing_period_months")
    @Builder.Default
    private Integer billingPeriodMonths = 1;

    /**
     * Auto-renew enabled (default true)
     */
    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private Boolean autoRenew = true;

    /**
     * Grace period days after payment failure (default 0 - no automatic grace period)
     * Grace period must be explicitly granted by SUPERADMIN
     */
    @Column(name = "grace_period_days")
    @Builder.Default
    private Integer gracePeriodDays = 0;

    /**
     * Number of failed payment attempts
     */
    @Column(name = "failed_payment_attempts")
    @Builder.Default
    private Integer failedPaymentAttempts = 0;

    /**
     * Number of free months remaining (promotional credit)
     */
    @Column(name = "free_months_remaining")
    @Builder.Default
    private Integer freeMonthsRemaining = 0;

    /**
     * Note about the promotional credit (e.g., "Holiday promotion", "Referral bonus")
     */
    @Column(name = "promotional_note", length = 255)
    private String promotionalNote;

    /**
     * User ID who granted the promotional credit
     */
    @Column(name = "promotional_granted_by")
    private Long promotionalGrantedBy;

    /**
     * When the promotional credit was granted
     */
    @Column(name = "promotional_granted_at")
    private LocalDateTime promotionalGrantedAt;

    /**
     * When subscription was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When subscription was last updated
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Timestamp when subscription was suspended (triggers 30-day deletion countdown)
     */
    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    /**
     * Date when church data will be permanently deleted
     * Calculated as: suspended_at + 30 days + retention_extension_days
     */
    @Column(name = "data_retention_end_date")
    private LocalDate dataRetentionEndDate;

    /**
     * Number of days SUPERADMIN has extended the retention period beyond 30 days
     */
    @Column(name = "retention_extension_days")
    @Builder.Default
    private Integer retentionExtensionDays = 0;

    /**
     * Timestamp when the 7-day deletion warning email was sent
     */
    @Column(name = "deletion_warning_sent_at")
    private LocalDateTime deletionWarningSentAt;

    /**
     * SUPERADMIN note explaining why retention period was extended
     */
    @Column(name = "retention_extension_note", length = 500)
    private String retentionExtensionNote;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if subscription is active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Check if subscription is past due
     */
    public boolean isPastDue() {
        return "PAST_DUE".equals(status);
    }

    /**
     * Check if subscription is canceled
     */
    public boolean isCanceled() {
        return "CANCELED".equals(status);
    }

    /**
     * Check if subscription is suspended
     */
    public boolean isSuspended() {
        return "SUSPENDED".equals(status);
    }

    /**
     * Check if in grace period (past due but within grace period days)
     */
    public boolean isInGracePeriod() {
        if (!isPastDue()) return false;
        if (nextBillingDate == null) return false;

        LocalDate gracePeriodEnd = nextBillingDate.plusDays(gracePeriodDays);
        return LocalDate.now().isBefore(gracePeriodEnd);
    }

    /**
     * Check if subscription should be suspended (past grace period)
     */
    public boolean shouldSuspend() {
        if (!isPastDue()) return false;
        return !isInGracePeriod();
    }

    /**
     * Get days until next billing
     */
    public long getDaysUntilNextBilling() {
        if (nextBillingDate == null) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextBillingDate);
    }

    /**
     * Check if subscription has promotional credits (free months)
     */
    public boolean hasPromotionalCredits() {
        return freeMonthsRemaining != null && freeMonthsRemaining > 0;
    }

    /**
     * Use one promotional credit (decrement free months)
     */
    public void usePromotionalCredit() {
        if (hasPromotionalCredits()) {
            freeMonthsRemaining--;
        }
    }

    /**
     * Grant promotional credits (free months)
     */
    public void grantPromotionalCredits(int months, String note, Long grantedBy) {
        this.freeMonthsRemaining = (this.freeMonthsRemaining == null ? 0 : this.freeMonthsRemaining) + months;
        this.promotionalNote = note;
        this.promotionalGrantedBy = grantedBy;
        this.promotionalGrantedAt = LocalDateTime.now();
    }

    // ==================== DATA RETENTION METHODS ====================

    /**
     * Calculate data deletion date: suspension date + 90 days + extension days
     * Returns null if subscription is not suspended
     */
    public LocalDate getCalculatedDeletionDate() {
        if (suspendedAt == null) return null;
        int totalRetentionDays = 90 + (retentionExtensionDays != null ? retentionExtensionDays : 0);
        return suspendedAt.toLocalDate().plusDays(totalRetentionDays);
    }

    /**
     * Check if church data is eligible for deletion
     * True if: subscription is suspended AND deletion date has passed AND warning was sent 7+ days ago
     */
    public boolean isEligibleForDeletion() {
        if (!isSuspended()) return false;
        if (dataRetentionEndDate == null) return false;
        if (LocalDate.now().isBefore(dataRetentionEndDate) || LocalDate.now().isEqual(dataRetentionEndDate)) {
            return false;
        }

        // Must have sent warning at least 7 days ago
        if (deletionWarningSentAt == null) return false;
        return deletionWarningSentAt.isBefore(LocalDateTime.now().minusDays(7));
    }

    /**
     * Check if church needs a deletion warning email
     * True if: deletion date is 7 days away AND warning hasn't been sent yet
     */
    public boolean needsDeletionWarning() {
        if (!isSuspended()) return false;
        if (dataRetentionEndDate == null) return false;
        if (deletionWarningSentAt != null) return false; // Already sent

        // Send warning 7 days before deletion
        LocalDate warningDate = dataRetentionEndDate.minusDays(7);
        return LocalDate.now().isAfter(warningDate) || LocalDate.now().isEqual(warningDate);
    }

    /**
     * Get days until data deletion
     * Returns negative number if deletion date has passed
     */
    public long getDaysUntilDeletion() {
        if (dataRetentionEndDate == null) return -1;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), dataRetentionEndDate);
    }

    /**
     * Mark subscription as suspended and set deletion timer (90 days)
     */
    public void markAsSuspended() {
        this.status = "SUSPENDED";
        this.suspendedAt = LocalDateTime.now();
        this.dataRetentionEndDate = LocalDate.now().plusDays(90 + (retentionExtensionDays != null ? retentionExtensionDays : 0));
    }

    /**
     * Extend data retention period (SUPERADMIN action)
     */
    public void extendRetentionPeriod(int additionalDays, String note) {
        this.retentionExtensionDays = (this.retentionExtensionDays != null ? this.retentionExtensionDays : 0) + additionalDays;
        this.retentionExtensionNote = note;

        // Recalculate deletion date (90 days base + extensions)
        if (suspendedAt != null) {
            this.dataRetentionEndDate = suspendedAt.toLocalDate().plusDays(90 + this.retentionExtensionDays);
        }
    }

    /**
     * Cancel deletion and reset retention timer (when subscription is reactivated)
     */
    public void cancelDeletion() {
        this.suspendedAt = null;
        this.dataRetentionEndDate = null;
        this.deletionWarningSentAt = null;
        this.retentionExtensionDays = 0;
        this.retentionExtensionNote = null;
    }

    /**
     * Mark deletion warning as sent
     */
    public void markDeletionWarningSent() {
        this.deletionWarningSentAt = LocalDateTime.now();
    }
}
