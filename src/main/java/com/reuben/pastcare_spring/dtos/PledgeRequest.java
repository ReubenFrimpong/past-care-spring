package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.PledgeFrequency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * DTO for creating/updating a pledge
 */
@Data
public class PledgeRequest {

  @NotNull(message = "Member ID is required")
  private Long memberId;

  private Long campaignId;

  @NotNull(message = "Total amount is required")
  @Positive(message = "Total amount must be positive")
  private BigDecimal totalAmount;

  private String currency = "GHS";

  @NotNull(message = "Frequency is required")
  private PledgeFrequency frequency;

  private Integer installments;

  @NotNull(message = "Pledge date is required")
  private LocalDate pledgeDate;

  @NotNull(message = "Start date is required")
  private LocalDate startDate;

  private LocalDate endDate;

  private String notes;

  private Boolean sendReminders = true;

  private Integer reminderDaysBefore = 7;
}
