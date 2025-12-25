package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;

/**
 * Response DTO for irregular attenders.
 * Dashboard Phase 1: Enhanced Widgets
 */
public record IrregularAttenderResponse(
    Long memberId,
    String firstName,
    String lastName,
    String phoneNumber,
    LocalDate lastAttendanceDate,
    Integer weeksAbsent
) {
}
