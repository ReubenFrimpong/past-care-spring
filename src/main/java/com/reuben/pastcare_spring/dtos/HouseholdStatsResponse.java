package com.reuben.pastcare_spring.dtos;

/**
 * Response DTO for household statistics
 */
public record HouseholdStatsResponse(
    long totalHouseholds,
    long totalMembers,
    long membersInHouseholds,
    double averageHouseholdSize
) {
}
