package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * DTO for recording a pledge payment
 */
@Data
public class PledgePaymentRequest {

  @NotNull(message = "Pledge ID is required")
  private Long pledgeId;

  @NotNull(message = "Amount is required")
  @Positive(message = "Amount must be positive")
  private BigDecimal amount;

  @NotNull(message = "Payment date is required")
  private LocalDate paymentDate;

  private String notes;

  /**
   * Associated donation ID (if payment was made via donation record)
   */
  private Long donationId;
}
