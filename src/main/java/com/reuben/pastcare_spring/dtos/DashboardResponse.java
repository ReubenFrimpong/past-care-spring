package com.reuben.pastcare_spring.dtos;

import java.util.List;

/**
 * Complete dashboard data response.
 * Aggregates all dashboard sections into single response.
 */
public record DashboardResponse(
    String userName,
    DashboardStatsResponse stats,
    List<PastoralCareNeedResponse> pastoralCareNeeds,
    List<UpcomingEventResponse> upcomingEvents,
    List<RecentActivityResponse> recentActivities
) {
}
