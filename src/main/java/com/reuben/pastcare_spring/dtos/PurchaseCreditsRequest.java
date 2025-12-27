package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseCreditsRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Minimum purchase amount is 1.00")
    private BigDecimal amount;

    private String paymentReference;
}
