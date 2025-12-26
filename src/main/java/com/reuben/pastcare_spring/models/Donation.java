package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Giving Module Phase 1: Donation Recording
 * Entity representing a single donation/giving record
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
  name = "donation",
  indexes = {
    @Index(name = "idx_donation_member", columnList = "member_id"),
    @Index(name = "idx_donation_date", columnList = "donationDate"),
    @Index(name = "idx_donation_type", columnList = "donationType"),
    @Index(name = "idx_donation_payment_method", columnList = "paymentMethod"),
    @Index(name = "idx_donation_church_date", columnList = "church_id,donationDate")
  }
)
@Data
public class Donation extends TenantBaseEntity {

  /**
   * Member who made the donation (nullable for anonymous donations)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private Member member;

  /**
   * Amount donated
   */
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  /**
   * Date of donation
   */
  @Column(nullable = false)
  private LocalDate donationDate;

  /**
   * Type of donation (TITHE, OFFERING, etc.)
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private DonationType donationType;

  /**
   * Payment method used
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 30)
  private PaymentMethod paymentMethod;

  /**
   * Whether this is an anonymous donation
   */
  @Column(nullable = false)
  private Boolean isAnonymous = false;

  /**
   * Reference/transaction number (check number, transaction ID, etc.)
   */
  @Column(length = 100)
  private String referenceNumber;

  /**
   * Notes about this donation
   */
  @Column(columnDefinition = "TEXT")
  private String notes;

  /**
   * Campaign this donation is for (optional) - kept for backward compatibility
   */
  @Column(length = 100)
  private String campaign;

  /**
   * Campaign entity this donation is associated with (Phase 3)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id")
  private Campaign campaignEntity;

  /**
   * Pledge this donation is fulfilling (Phase 3)
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "pledge_id")
  private Pledge pledge;

  /**
   * Whether a receipt has been issued
   */
  @Column(nullable = false)
  private Boolean receiptIssued = false;

  /**
   * Receipt number (if issued)
   */
  @Column(length = 50)
  private String receiptNumber;

  /**
   * Currency code (defaults to church's currency)
   */
  @Column(length = 3)
  private String currency = "GHS"; // Ghana Cedis default

  /**
   * User who recorded this donation
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "recorded_by_id")
  private User recordedBy;

  /**
   * Helper method to get donor name (handles anonymous donations)
   */
  public String getDonorName() {
    if (isAnonymous) {
      return "Anonymous";
    }
    return member != null ? member.getFirstName() + " " + member.getLastName() : "Unknown";
  }

  /**
   * Helper method to check if donation is for a specific type
   */
  public boolean isType(DonationType type) {
    return this.donationType == type;
  }

  /**
   * Helper method to check if donation was made via a specific method
   */
  public boolean isPaidVia(PaymentMethod method) {
    return this.paymentMethod == method;
  }
}
