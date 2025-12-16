package com.reuben.pastcare_spring.dtos;

/**
 * Internal DTO for passing token data from AuthService to AuthController.
 * This is not exposed to clients - tokens are set in HttpOnly cookies.
 */
public record AuthTokenData(
    String accessToken,
    String refreshToken,
    UserResponse user
) {
}
