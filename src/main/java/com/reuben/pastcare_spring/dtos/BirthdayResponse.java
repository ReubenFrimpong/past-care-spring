package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;

/**
 * Response DTO for member birthdays.
 * Dashboard Phase 1: Enhanced Widgets
 */
public record BirthdayResponse(
    Long memberId,
    String firstName,
    String lastName,
    LocalDate dateOfBirth,
    Integer age,
    String daysUntil // e.g., "Today", "Tomorrow", "In 3 days"
) {
}
