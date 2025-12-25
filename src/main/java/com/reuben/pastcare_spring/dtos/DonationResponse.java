package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.Donation;
import com.reuben.pastcare_spring.models.DonationType;
import com.reuben.pastcare_spring.models.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Giving Module Phase 1: Donation Recording
 * Response DTO for donation data
 */
public record DonationResponse(
    Long id,
    Long memberId,
    String memberName,
    BigDecimal amount,
    LocalDate donationDate,
    DonationType donationType,
    PaymentMethod paymentMethod,
    Boolean isAnonymous,
    String referenceNumber,
    String notes,
    String campaign,
    Boolean receiptIssued,
    String receiptNumber,
    String currency,
    Long recordedById,
    String recordedByName,
    Instant createdAt
) {
  /**
   * Create response from entity
   */
  public static DonationResponse fromEntity(Donation donation) {
    return new DonationResponse(
        donation.getId(),
        donation.getMember() != null ? donation.getMember().getId() : null,
        donation.getDonorName(),
        donation.getAmount(),
        donation.getDonationDate(),
        donation.getDonationType(),
        donation.getPaymentMethod(),
        donation.getIsAnonymous(),
        donation.getReferenceNumber(),
        donation.getNotes(),
        donation.getCampaign(),
        donation.getReceiptIssued(),
        donation.getReceiptNumber(),
        donation.getCurrency(),
        donation.getRecordedBy() != null ? donation.getRecordedBy().getId() : null,
        donation.getRecordedBy() != null ? donation.getRecordedBy().getName() : null,
        donation.getCreatedAt()
    );
  }
}
