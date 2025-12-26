package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.Pledge;
import com.reuben.pastcare_spring.models.PledgeFrequency;
import com.reuben.pastcare_spring.models.PledgeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * DTO for pledge response data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PledgeResponse {

  private Long id;
  private Long memberId;
  private String memberName;
  private Long campaignId;
  private String campaignName;
  private BigDecimal totalAmount;
  private String currency;
  private PledgeFrequency frequency;
  private Integer installments;
  private LocalDate pledgeDate;
  private LocalDate startDate;
  private LocalDate endDate;
  private PledgeStatus status;
  private BigDecimal amountPaid;
  private BigDecimal amountRemaining;
  private Integer paymentsMade;
  private LocalDate lastPaymentDate;
  private LocalDate nextPaymentDate;
  private String notes;
  private Boolean sendReminders;
  private Integer reminderDaysBefore;
  private Double progressPercentage;
  private Boolean isFullyPaid;
  private Boolean isOverdue;

  /**
   * Convert Pledge entity to DTO
   */
  public static PledgeResponse fromEntity(Pledge pledge) {
    PledgeResponse response = new PledgeResponse();
    response.setId(pledge.getId());
    response.setMemberId(pledge.getMember() != null ? pledge.getMember().getId() : null);
    response.setMemberName(pledge.getMemberName());
    response.setCampaignId(pledge.getCampaign() != null ? pledge.getCampaign().getId() : null);
    response.setCampaignName(pledge.getCampaignName());
    response.setTotalAmount(pledge.getTotalAmount());
    response.setCurrency(pledge.getCurrency());
    response.setFrequency(pledge.getFrequency());
    response.setInstallments(pledge.getInstallments());
    response.setPledgeDate(pledge.getPledgeDate());
    response.setStartDate(pledge.getStartDate());
    response.setEndDate(pledge.getEndDate());
    response.setStatus(pledge.getStatus());
    response.setAmountPaid(pledge.getAmountPaid());
    response.setAmountRemaining(pledge.getAmountRemaining());
    response.setPaymentsMade(pledge.getPaymentsMade());
    response.setLastPaymentDate(pledge.getLastPaymentDate());
    response.setNextPaymentDate(pledge.getNextPaymentDate());
    response.setNotes(pledge.getNotes());
    response.setSendReminders(pledge.getSendReminders());
    response.setReminderDaysBefore(pledge.getReminderDaysBefore());
    response.setProgressPercentage(pledge.getProgressPercentage());
    response.setIsFullyPaid(pledge.isFullyPaid());
    response.setIsOverdue(pledge.isOverdue());
    return response;
  }
}
