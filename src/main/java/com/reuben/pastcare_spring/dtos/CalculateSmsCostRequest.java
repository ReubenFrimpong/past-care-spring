package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CalculateSmsCostRequest {

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Message is required")
    private String message;
}
