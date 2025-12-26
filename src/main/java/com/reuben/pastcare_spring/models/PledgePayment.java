package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * Entity representing an individual payment towards a pledge
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
  name = "pledge_payment",
  indexes = {
    @Index(name = "idx_pledge_payment_church", columnList = "church_id"),
    @Index(name = "idx_pledge_payment_pledge", columnList = "pledge_id"),
    @Index(name = "idx_pledge_payment_donation", columnList = "donation_id"),
    @Index(name = "idx_pledge_payment_status", columnList = "status"),
    @Index(name = "idx_pledge_payment_due_date", columnList = "dueDate"),
    @Index(name = "idx_pledge_payment_church_status", columnList = "church_id,status")
  }
)
@Data
public class PledgePayment extends TenantBaseEntity {

  /**
   * Pledge this payment belongs to
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pledge_id", nullable = false)
  private Pledge pledge;

  /**
   * Actual donation record (when payment is made)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "donation_id")
  private Donation donation;

  /**
   * Expected payment amount
   */
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  /**
   * Date payment was received (null if not yet paid)
   */
  private LocalDate paymentDate;

  /**
   * Date payment is/was due
   */
  private LocalDate dueDate;

  /**
   * Payment status
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PledgePaymentStatus status = PledgePaymentStatus.PENDING;

  /**
   * Notes about this payment
   */
  @Column(columnDefinition = "TEXT")
  private String notes;

  /**
   * Check if payment is overdue
   */
  public boolean isOverdue() {
    if (dueDate == null || status == PledgePaymentStatus.PAID) {
      return false;
    }
    return LocalDate.now().isAfter(dueDate);
  }

  /**
   * Check if payment is paid
   */
  public boolean isPaid() {
    return status == PledgePaymentStatus.PAID;
  }

  /**
   * Get days overdue (negative if not due yet)
   */
  public long getDaysOverdue() {
    if (dueDate == null) {
      return 0;
    }
    return java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDate.now());
  }
}
