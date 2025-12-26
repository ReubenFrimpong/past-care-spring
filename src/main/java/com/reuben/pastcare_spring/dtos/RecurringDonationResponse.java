package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for recurring donations
 */
@Data
public class RecurringDonationResponse {

    private Long id;
    private Long memberId;
    private String memberName;
    private BigDecimal amount;
    private DonationType donationType;
    private RecurringFrequency frequency;
    private RecurringDonationStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextChargeDate;
    private LocalDateTime lastChargeDate;
    private String currency;
    private String campaign;
    private Integer consecutiveFailures;
    private LocalDateTime lastFailureDate;
    private String lastFailureReason;
    private Integer totalPayments;
    private BigDecimal totalAmountPaid;
    private String cardLast4;
    private String cardBrand;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Convert entity to response DTO
     */
    public static RecurringDonationResponse fromEntity(RecurringDonation entity) {
        RecurringDonationResponse response = new RecurringDonationResponse();
        response.setId(entity.getId());
        response.setMemberId(entity.getMember().getId());
        response.setMemberName(entity.getMember().getFirstName() + " " + entity.getMember().getLastName());
        response.setAmount(entity.getAmount());
        response.setDonationType(entity.getDonationType());
        response.setFrequency(entity.getFrequency());
        response.setStatus(entity.getStatus());
        response.setStartDate(entity.getStartDate());
        response.setEndDate(entity.getEndDate());
        response.setNextChargeDate(entity.getNextChargeDate());
        response.setLastChargeDate(entity.getLastChargeDate());
        response.setCurrency(entity.getCurrency());
        response.setCampaign(entity.getCampaign());
        response.setConsecutiveFailures(entity.getConsecutiveFailures());
        response.setLastFailureDate(entity.getLastFailureDate());
        response.setLastFailureReason(entity.getLastFailureReason());
        response.setTotalPayments(entity.getTotalPayments());
        response.setTotalAmountPaid(entity.getTotalAmountPaid());
        response.setCardLast4(entity.getCardLast4());
        response.setCardBrand(entity.getCardBrand());
        response.setNotes(entity.getNotes());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}
