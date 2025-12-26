package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * Entity representing a member's pledge commitment
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
  name = "pledge",
  indexes = {
    @Index(name = "idx_pledge_church", columnList = "church_id"),
    @Index(name = "idx_pledge_member", columnList = "member_id"),
    @Index(name = "idx_pledge_campaign", columnList = "campaign_id"),
    @Index(name = "idx_pledge_status", columnList = "status"),
    @Index(name = "idx_pledge_next_payment", columnList = "nextPaymentDate"),
    @Index(name = "idx_pledge_church_status", columnList = "church_id,status")
  }
)
@Data
public class Pledge extends TenantBaseEntity {

  /**
   * Member who made the pledge
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  /**
   * Campaign this pledge is for (optional)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id")
  private Campaign campaign;

  /**
   * Total pledged amount
   */
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal totalAmount;

  /**
   * Currency code (defaults to church's currency)
   */
  @Column(length = 3)
  private String currency = "GHS"; // Ghana Cedis default

  /**
   * Payment frequency
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PledgeFrequency frequency;

  /**
   * Number of installments (null for one-time pledges)
   */
  private Integer installments;

  /**
   * Date pledge was made
   */
  @Column(nullable = false)
  private LocalDate pledgeDate;

  /**
   * Date first payment is expected
   */
  @Column(nullable = false)
  private LocalDate startDate;

  /**
   * Date pledge is expected to be completed (optional)
   */
  private LocalDate endDate;

  /**
   * Pledge status
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private PledgeStatus status = PledgeStatus.ACTIVE;

  /**
   * Amount paid so far
   */
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amountPaid = BigDecimal.ZERO;

  /**
   * Remaining amount to be paid
   */
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amountRemaining;

  /**
   * Number of payments made
   */
  @Column(nullable = false)
  private Integer paymentsMade = 0;

  /**
   * Date of last payment
   */
  private LocalDate lastPaymentDate;

  /**
   * Date next payment is due
   */
  private LocalDate nextPaymentDate;

  /**
   * Notes about this pledge
   */
  @Column(columnDefinition = "TEXT")
  private String notes;

  /**
   * Whether to send payment reminders
   */
  @Column(nullable = false)
  private Boolean sendReminders = true;

  /**
   * Days before payment due to send reminder
   */
  private Integer reminderDaysBefore = 7;

  /**
   * Calculate progress percentage
   */
  public Double getProgressPercentage() {
    if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) == 0) {
      return 0.0;
    }
    return amountPaid.divide(totalAmount, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .doubleValue();
  }

  /**
   * Check if pledge is fully paid
   */
  public boolean isFullyPaid() {
    return amountRemaining.compareTo(BigDecimal.ZERO) == 0;
  }

  /**
   * Check if pledge is active
   */
  public boolean isActive() {
    return status == PledgeStatus.ACTIVE;
  }

  /**
   * Check if payment is overdue
   */
  public boolean isOverdue() {
    if (nextPaymentDate == null || status != PledgeStatus.ACTIVE) {
      return false;
    }
    return LocalDate.now().isAfter(nextPaymentDate);
  }

  /**
   * Get member name
   */
  public String getMemberName() {
    return member != null ? member.getFirstName() + " " + member.getLastName() : "Unknown";
  }

  /**
   * Get campaign name (if applicable)
   */
  public String getCampaignName() {
    return campaign != null ? campaign.getName() : null;
  }

  /**
   * Initialize amountRemaining if not set
   */
  @PrePersist
  @PreUpdate
  public void updateAmountRemaining() {
    if (totalAmount != null && amountPaid != null) {
      this.amountRemaining = totalAmount.subtract(amountPaid);
    }
  }
}
