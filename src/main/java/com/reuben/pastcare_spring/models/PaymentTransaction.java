package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity to track all payment transactions (attempts, successes, failures)
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payment_transactions", indexes = {
    @Index(name = "idx_payment_member", columnList = "member_id"),
    @Index(name = "idx_payment_status", columnList = "status"),
    @Index(name = "idx_payment_reference", columnList = "payment_reference"),
    @Index(name = "idx_payment_recurring", columnList = "recurring_donation_id")
})
@Data
public class PaymentTransaction extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id")
    private Donation donation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_donation_id")
    private RecurringDonation recurringDonation;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "GHS";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DonationType donationType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentTransactionStatus status;

    // Paystack fields
    @Column(unique = true, length = 255)
    private String paymentReference;

    @Column(length = 255)
    private String paystackReference;

    @Column(length = 100)
    private String paystackTransactionId;

    @Column(length = 255)
    private String paystackAuthorizationCode;

    // Card details
    @Column(length = 20)
    private String cardLast4;

    @Column(length = 50)
    private String cardBrand;

    @Column(length = 255)
    private String cardBin;

    // Gateway response
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    @Column(columnDefinition = "TEXT")
    private String gatewayMessage;

    // Timestamps
    @Column
    private LocalDateTime paidAt;

    @Column
    private LocalDateTime failedAt;

    // Retry tracking
    @Column(nullable = false)
    private Integer retryCount = 0;

    @Column
    private LocalDateTime nextRetryAt;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    // Customer details
    @Column(length = 255)
    private String customerEmail;

    @Column(length = 50)
    private String customerPhone;

    @Column(length = 200)
    private String campaign;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
