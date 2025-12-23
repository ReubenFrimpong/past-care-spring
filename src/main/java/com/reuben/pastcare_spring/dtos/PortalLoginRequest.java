package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for portal user login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PortalLoginRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private Long churchId; // Optional: can be inferred from subdomain/URL
}
