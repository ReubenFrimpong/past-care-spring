package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment entity tracking all subscription payments.
 *
 * <p>Records:
 * <ul>
 *   <li>Payment transactions from Paystack</li>
 *   <li>Payment status (successful, failed, pending)</li>
 *   <li>Payment metadata (reference, amount, plan)</li>
 *   <li>Refunds and chargebacks</li>
 * </ul>
 */
@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Church ID (tenant)
     */
    @Column(name = "church_id", nullable = false)
    private Long churchId;

    /**
     * Subscription plan this payment is for
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;

    /**
     * Payment amount in USD
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Currency code: USD, NGN, etc.
     */
    @Column(name = "currency", nullable = false, length = 10)
    @Builder.Default
    private String currency = "USD";

    /**
     * Payment status: PENDING, SUCCESS, FAILED, REFUNDED, CHARGEBACK
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    /**
     * Paystack transaction reference
     */
    @Column(name = "paystack_reference", nullable = false, unique = true, length = 100)
    private String paystackReference;

    /**
     * Paystack transaction ID
     */
    @Column(name = "paystack_transaction_id", length = 100)
    private String paystackTransactionId;

    /**
     * Paystack authorization code (for recurring payments)
     */
    @Column(name = "paystack_authorization_code", length = 100)
    private String paystackAuthorizationCode;

    /**
     * Payment method: CARD, BANK_TRANSFER, MOBILE_MONEY, USSD
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * Card details (last 4 digits)
     */
    @Column(name = "card_last4", length = 4)
    private String cardLast4;

    /**
     * Card brand: VISA, MASTERCARD, VERVE, etc.
     */
    @Column(name = "card_brand", length = 50)
    private String cardBrand;

    /**
     * Card expiry (MM/YY)
     */
    @Column(name = "card_expiry", length = 5)
    private String cardExpiry;

    /**
     * Payment type: SUBSCRIPTION, ONE_TIME, UPGRADE, DOWNGRADE
     */
    @Column(name = "payment_type", length = 20)
    @Builder.Default
    private String paymentType = "SUBSCRIPTION";

    /**
     * Payment description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Payment metadata (JSON)
     */
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    /**
     * Invoice number (for invoices)
     */
    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    /**
     * Payment date (when payment was made/completed)
     */
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    /**
     * Refund amount (if refunded)
     */
    @Column(name = "refund_amount", precision = 10, scale = 2)
    private BigDecimal refundAmount;

    /**
     * Refund date
     */
    @Column(name = "refund_date")
    private LocalDateTime refundDate;

    /**
     * Refund reason
     */
    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    /**
     * Failure reason (if payment failed)
     */
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    /**
     * IP address of payment initiator
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * User agent of payment initiator
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    /**
     * When payment record was created
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * When payment record was last updated
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
     * Check if payment is successful
     */
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }

    /**
     * Check if payment is pending
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /**
     * Check if payment failed
     */
    public boolean isFailed() {
        return "FAILED".equals(status);
    }

    /**
     * Check if payment was refunded
     */
    public boolean isRefunded() {
        return "REFUNDED".equals(status);
    }

    /**
     * Mark payment as successful
     */
    public void markAsSuccessful() {
        this.status = "SUCCESS";
        this.paymentDate = LocalDateTime.now();
    }

    /**
     * Mark payment as failed
     */
    public void markAsFailed(String reason) {
        this.status = "FAILED";
        this.failureReason = reason;
    }

    /**
     * Refund payment
     */
    public void refund(BigDecimal amount, String reason) {
        this.status = "REFUNDED";
        this.refundAmount = amount;
        this.refundDate = LocalDateTime.now();
        this.refundReason = reason;
    }
}
