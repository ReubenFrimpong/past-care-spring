package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for church registration during user signup.
 * Contains all the necessary information to create a new church (tenant).
 */
public record ChurchRegistrationRequest(
    @NotBlank(message = "Church name is required")
    String name,
    
    String address,
    
    String phoneNumber,
    
    @Email(message = "Church email must be valid")
    String email,
    
    String website
) {
}
