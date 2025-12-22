package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating or updating a household
 */
public record HouseholdRequest(
    @NotBlank(message = "Household name is required")
    @Size(max = 200, message = "Household name must not exceed 200 characters")
    String householdName,

    @NotNull(message = "Household head is required")
    Long householdHeadId,

    List<Long> memberIds,

    Long locationId,

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    String notes,

    LocalDate establishedDate,

    @Email(message = "Invalid email format")
    @Size(max = 200, message = "Email must not exceed 200 characters")
    String householdEmail,

    @Size(max = 50, message = "Phone number must not exceed 50 characters")
    String householdPhone,

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    String householdImageUrl
) {
}
