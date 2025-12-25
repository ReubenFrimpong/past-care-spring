package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.DonationType;
import com.reuben.pastcare_spring.models.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Giving Module Phase 1: Donation Recording
 * Request DTO for creating/updating donations
 */
public record DonationRequest(
    Long memberId, // Nullable for anonymous donations

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    BigDecimal amount,

    @NotNull(message = "Donation date is required")
    LocalDate donationDate,

    @NotNull(message = "Donation type is required")
    DonationType donationType,

    @NotNull(message = "Payment method is required")
    PaymentMethod paymentMethod,

    Boolean isAnonymous,
    String referenceNumber,
    String notes,
    String campaign,
    String currency // Optional, defaults to church currency
) {
  /**
   * Constructor with defaults
   */
  public DonationRequest {
    if (isAnonymous == null) {
      isAnonymous = false;
    }
    if (currency == null || currency.isBlank()) {
      currency = "GHS";
    }
  }
}
