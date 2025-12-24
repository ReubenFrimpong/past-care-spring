package com.reuben.pastcare_spring.dtos;

import java.time.LocalDateTime;

/**
 * Response DTO for QR code generation.
 *
 * Phase 1: Enhanced Attendance Tracking - QR Code System
 */
public record QRCodeResponse(
    Long sessionId,
    String sessionName,
    String qrCodeData,
    String qrCodeImageBase64,
    LocalDateTime expiresAt,
    String message
) {
}
