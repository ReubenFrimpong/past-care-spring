package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.CheckInMethod;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for member check-in.
 * Phase 1: Enhanced Attendance Tracking
 *
 * @param sessionId The attendance session ID
 * @param memberId The member ID (null for visitor check-in)
 * @param visitorId The visitor ID (null for member check-in)
 * @param checkInMethod The method used for check-in
 * @param qrCodeData QR code data (required for QR_CODE method)
 * @param latitude User's latitude (required for GEOFENCE method)
 * @param longitude User's longitude (required for GEOFENCE method)
 * @param deviceInfo Device information (optional)
 */
public record CheckInRequest(
    @NotNull(message = "Session ID is required")
    Long sessionId,

    Long memberId,

    Long visitorId,

    @NotNull(message = "Check-in method is required")
    CheckInMethod checkInMethod,

    String qrCodeData,

    Double latitude,

    Double longitude,

    String deviceInfo
) {
  /**
   * Validates that either memberId or visitorId is provided, but not both.
   */
  public void validate() {
    if (memberId == null && visitorId == null) {
      throw new IllegalArgumentException("Either memberId or visitorId must be provided");
    }
    if (memberId != null && visitorId != null) {
      throw new IllegalArgumentException("Cannot provide both memberId and visitorId");
    }

    // Validate QR code check-in
    if (checkInMethod == CheckInMethod.QR_CODE && (qrCodeData == null || qrCodeData.isBlank())) {
      throw new IllegalArgumentException("QR code data is required for QR_CODE check-in method");
    }

    // Validate geofence check-in
    if (checkInMethod == CheckInMethod.GEOFENCE) {
      if (latitude == null || longitude == null) {
        throw new IllegalArgumentException("Latitude and longitude are required for GEOFENCE check-in method");
      }
      if (latitude < -90 || latitude > 90) {
        throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
      }
      if (longitude < -180 || longitude > 180) {
        throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
      }
    }
  }
}
