package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing a recurring donation setup
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "recurring_donations", indexes = {
    @Index(name = "idx_recurring_member", columnList = "member_id"),
    @Index(name = "idx_recurring_status", columnList = "status"),
    @Index(name = "idx_recurring_next_charge", columnList = "next_charge_date")
})
@Data
public class RecurringDonation extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DonationType donationType = DonationType.TITHE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecurringFrequency frequency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecurringDonationStatus status = RecurringDonationStatus.ACTIVE;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalDate nextChargeDate;

    @Column
    private LocalDateTime lastChargeDate;

    @Column(length = 3)
    private String currency = "GHS";

    // Paystack-specific fields
    @Column(length = 255)
    private String paystackAuthorizationCode;

    @Column(length = 100)
    private String paystackCustomerCode;

    @Column(length = 100)
    private String paystackPlanCode;

    @Column(length = 20)
    private String cardLast4;

    @Column(length = 50)
    private String cardBrand;

    @Column(length = 255)
    private String cardBin;

    // Campaign association (optional)
    @Column(length = 200)
    private String campaign;

    // Retry tracking
    @Column(nullable = false)
    private Integer consecutiveFailures = 0;

    @Column
    private LocalDateTime lastFailureDate;

    @Column(columnDefinition = "TEXT")
    private String lastFailureReason;

    // Statistics
    @Column(nullable = false)
    private Integer totalPayments = 0;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalAmountPaid = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
