package com.reuben.pastcare_spring.dtos;

import java.time.YearMonth;

/**
 * Response DTO for member anniversaries.
 * Dashboard Phase 1: Enhanced Widgets
 */
public record AnniversaryResponse(
    Long memberId,
    String firstName,
    String lastName,
    YearMonth memberSince,
    Integer yearsOfMembership
) {
}
