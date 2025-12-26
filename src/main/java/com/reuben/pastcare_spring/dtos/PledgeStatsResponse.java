package com.reuben.pastcare_spring.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * DTO for pledge statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PledgeStatsResponse {

  private Long totalPledges;
  private Long activePledges;
  private Long completedPledges;
  private Long overduePledges;
  private BigDecimal totalPledgedAmount;
  private BigDecimal totalPaidAmount;
  private BigDecimal totalRemainingAmount;
  private Double averageCompletionRate;
  private Integer totalPayments;
}
