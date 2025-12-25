package com.reuben.pastcare_spring.dtos;

/**
 * Fellowship Module Phase 2: Analytics
 * Response DTO for fellowship comparison
 */
public record FellowshipComparisonResponse(
    Long fellowshipId,
    String fellowshipName,
    String fellowshipType,
    Integer memberCount,
    Double averageAttendanceRate, // If we have attendance tracking later
    Integer joinRequestsLast30Days,
    Integer approvalRate, // Percentage of approved requests
    String healthStatus,
    Integer rank // Ranking based on health/size/growth
) {
}
