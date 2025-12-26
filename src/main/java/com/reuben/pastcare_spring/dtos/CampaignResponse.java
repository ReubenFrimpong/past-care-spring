package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.Campaign;
import com.reuben.pastcare_spring.models.CampaignStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * DTO for campaign response data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampaignResponse {

  private Long id;
  private String name;
  private String description;
  private BigDecimal goalAmount;
  private String currency;
  private LocalDate startDate;
  private LocalDate endDate;
  private CampaignStatus status;
  private Boolean isPublic;
  private BigDecimal currentAmount;
  private BigDecimal totalPledges;
  private Integer totalDonations;
  private Integer totalPledgesCount;
  private Boolean showThermometer;
  private Boolean showDonorList;
  private Boolean featured;
  private Double progressPercentage;
  private BigDecimal remainingAmount;
  private Boolean isGoalReached;
  private Boolean hasEnded;

  /**
   * Convert Campaign entity to DTO
   */
  public static CampaignResponse fromEntity(Campaign campaign) {
    CampaignResponse response = new CampaignResponse();
    response.setId(campaign.getId());
    response.setName(campaign.getName());
    response.setDescription(campaign.getDescription());
    response.setGoalAmount(campaign.getGoalAmount());
    response.setCurrency(campaign.getCurrency());
    response.setStartDate(campaign.getStartDate());
    response.setEndDate(campaign.getEndDate());
    response.setStatus(campaign.getStatus());
    response.setIsPublic(campaign.getIsPublic());
    response.setCurrentAmount(campaign.getCurrentAmount());
    response.setTotalPledges(campaign.getTotalPledges());
    response.setTotalDonations(campaign.getTotalDonations());
    response.setTotalPledgesCount(campaign.getTotalPledgesCount());
    response.setShowThermometer(campaign.getShowThermometer());
    response.setShowDonorList(campaign.getShowDonorList());
    response.setFeatured(campaign.getFeatured());
    response.setProgressPercentage(campaign.getProgressPercentage());
    response.setRemainingAmount(campaign.getRemainingAmount());
    response.setIsGoalReached(campaign.isGoalReached());
    response.setHasEnded(campaign.hasEnded());
    return response;
  }
}
