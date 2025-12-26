package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.DonationType;
import com.reuben.pastcare_spring.models.RecurringFrequency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for creating/updating recurring donations
 */
@Data
public class RecurringDonationRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Donation type is required")
    private DonationType donationType;

    @NotNull(message = "Frequency is required")
    private RecurringFrequency frequency;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private String currency = "GHS";

    private String campaign;

    private String notes;

    // Paystack payment details (from frontend after payment authorization)
    private String paystackAuthorizationCode;
    private String paystackCustomerCode;
    private String paystackPlanCode;
    private String cardLast4;
    private String cardBrand;
    private String cardBin;
}
