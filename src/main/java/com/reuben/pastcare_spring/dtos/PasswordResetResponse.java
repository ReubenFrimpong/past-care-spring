package com.reuben.pastcare_spring.dtos;

public record PasswordResetResponse(
    String message,
    boolean success
) {}
