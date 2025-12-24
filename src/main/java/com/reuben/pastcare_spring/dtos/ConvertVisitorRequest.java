package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for converting a visitor to a member.
 * Phase 1: Enhanced Attendance Tracking - Visitor Management
 *
 * @param visitorId The visitor ID to convert
 * @param memberRequest The member registration data
 * @param conversionDate Date of conversion (defaults to today if null)
 * @param retainVisitorHistory Whether to retain visitor attendance history
 */
public record ConvertVisitorRequest(
    @NotNull(message = "Visitor ID is required")
    Long visitorId,

    @NotNull(message = "Member data is required")
    MemberRequest memberRequest,

    LocalDate conversionDate,

    Boolean retainVisitorHistory
) {
  public ConvertVisitorRequest {
    // Default values
    if (conversionDate == null) {
      conversionDate = LocalDate.now();
    }
    if (retainVisitorHistory == null) {
      retainVisitorHistory = true;
    }
  }
}
