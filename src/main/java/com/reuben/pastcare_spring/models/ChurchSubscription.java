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
     * Auto-renew enabled (default true)
     */
    @Column(name = "auto_renew", nullable = false)
    @Builder.Default
    private Boolean autoRenew = true;

    /**
     * Grace period days after payment failure (default 7)
     */
    @Column(name = "grace_period_days")
    @Builder.Default
    private Integer gracePeriodDays = 7;

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
}
