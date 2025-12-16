package com.reuben.pastcare_spring.dtos;

/**
 * Authentication response DTO.
 * With HttpOnly cookie-based authentication, tokens are not included in the response body.
 * Only user information is returned for client-side display purposes.
 */
public record AuthResponse(
    UserResponse user
) {
}
