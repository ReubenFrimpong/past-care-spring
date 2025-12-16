package com.reuben.pastcare_spring.dtos;

/**
 * Dashboard statistics summary.
 * Provides overview metrics for church management dashboard.
 */
public record DashboardStatsResponse(
    int activeMembers,
    int needPrayer,
    int eventsThisWeek,
    String attendanceRate
) {
}
