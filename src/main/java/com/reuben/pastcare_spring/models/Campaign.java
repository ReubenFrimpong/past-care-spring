package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * Entity representing a fundraising campaign (e.g., building fund, missions trip)
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
  name = "campaign",
  indexes = {
    @Index(name = "idx_campaign_church", columnList = "church_id"),
    @Index(name = "idx_campaign_status", columnList = "status"),
    @Index(name = "idx_campaign_dates", columnList = "startDate,endDate"),
    @Index(name = "idx_campaign_church_status", columnList = "church_id,status")
  }
)
@Data
public class Campaign extends TenantBaseEntity {

  /**
   * Campaign name
   */
  @Column(nullable = false, length = 200)
  private String name;

  /**
   * Campaign description
   */
  @Column(columnDefinition = "TEXT")
  private String description;

  /**
   * Target goal amount
   */
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal goalAmount;

  /**
   * Currency code (defaults to church's currency)
   */
  @Column(length = 3)
  private String currency = "GHS"; // Ghana Cedis default

  /**
   * Campaign start date
   */
  @Column(nullable = false)
  private LocalDate startDate;

  /**
   * Campaign end date (optional - ongoing campaigns)
   */
  private LocalDate endDate;

  /**
   * Campaign status
   */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private CampaignStatus status = CampaignStatus.ACTIVE;

  /**
   * Whether campaign should be visible in member portal
   */
  @Column(nullable = false)
  private Boolean isPublic = true;

  /**
   * Current amount raised (updated via donations)
   */
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal currentAmount = BigDecimal.ZERO;

  /**
   * Total pledged amount
   */
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal totalPledges = BigDecimal.ZERO;

  /**
   * Total number of donations received
   */
  @Column(nullable = false)
  private Integer totalDonations = 0;

  /**
   * Total number of pledges made
   */
  @Column(nullable = false)
  private Integer totalPledgesCount = 0;

  /**
   * Whether to show progress thermometer
   */
  @Column(nullable = false)
  private Boolean showThermometer = true;

  /**
   * Whether to show donor list
   */
  @Column(nullable = false)
  private Boolean showDonorList = true;

  /**
   * Whether campaign is featured on dashboard
   */
  @Column(nullable = false)
  private Boolean featured = false;

  /**
   * User who created this campaign
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_id")
  private User createdBy;

  /**
   * Calculate progress percentage
   */
  public Double getProgressPercentage() {
    if (goalAmount == null || goalAmount.compareTo(BigDecimal.ZERO) == 0) {
      return 0.0;
    }
    return currentAmount.divide(goalAmount, 4, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100))
        .doubleValue();
  }

  /**
   * Get remaining amount to goal
   */
  public BigDecimal getRemainingAmount() {
    return goalAmount.subtract(currentAmount);
  }

  /**
   * Check if campaign is active
   */
  public boolean isActive() {
    return status == CampaignStatus.ACTIVE;
  }

  /**
   * Check if campaign has ended
   */
  public boolean hasEnded() {
    if (endDate == null) {
      return false; // Ongoing campaign
    }
    return LocalDate.now().isAfter(endDate);
  }

  /**
   * Check if goal has been reached
   */
  public boolean isGoalReached() {
    return currentAmount.compareTo(goalAmount) >= 0;
  }
}
