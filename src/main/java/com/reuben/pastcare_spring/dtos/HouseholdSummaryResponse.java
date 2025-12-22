package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;

/**
 * Summary response DTO for household lists (lighter than full HouseholdResponse)
 */
public record HouseholdSummaryResponse(
    Long id,
    String householdName,
    String householdHeadName,
    String locationName,
    Integer memberCount,
    String householdImageUrl,
    LocalDate establishedDate
) {
}
