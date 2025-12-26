package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CampaignStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * DTO for creating/updating a campaign
 */
@Data
public class CampaignRequest {

  @NotBlank(message = "Campaign name is required")
  private String name;

  private String description;

  @NotNull(message = "Goal amount is required")
  @Positive(message = "Goal amount must be positive")
  private BigDecimal goalAmount;

  private String currency = "GHS";

  @NotNull(message = "Start date is required")
  private LocalDate startDate;

  private LocalDate endDate;

  private CampaignStatus status = CampaignStatus.ACTIVE;

  private Boolean isPublic = true;

  private Boolean showThermometer = true;

  private Boolean showDonorList = true;

  private Boolean featured = false;
}
