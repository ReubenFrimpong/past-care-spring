package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.DonationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request DTO for initializing a payment with Paystack
 */
@Data
public class PaymentInitializationRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Donation type is required")
    private DonationType donationType;

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email is required")
    private String email;

    private String currency = "GHS";

    private String campaign;

    private String callbackUrl;

    private String metadata;

    // For recurring donations
    private Boolean setupRecurring = false;
}
