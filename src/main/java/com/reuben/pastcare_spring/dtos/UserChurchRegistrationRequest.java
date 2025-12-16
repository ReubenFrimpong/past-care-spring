package com.reuben.pastcare_spring.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for registering a new church along with the first admin user.
 * This is used when a new organization signs up for the platform.
 * 
 * The user will automatically become ADMIN of the newly created church.
 */
public record UserChurchRegistrationRequest(
    @NotNull(message = "User data is required")
    @Valid
    UserRegistrationData user,
    
    @NotNull(message = "Church data is required")
    @Valid
    ChurchRegistrationRequest church
) {
}
