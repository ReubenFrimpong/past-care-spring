package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * DTO for campaign statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignStatsResponse {

  private Long totalCampaigns;
  private Long activeCampaigns;
  private Long completedCampaigns;
  private BigDecimal totalGoalAmount;
  private BigDecimal totalRaised;
  private BigDecimal totalPledged;
  private Integer totalDonations;
  private Integer totalPledges;
  private Double averageProgress;
}
