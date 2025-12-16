package com.reuben.pastcare_spring.dtos;

/**
 * Recent activity item for dashboard.
 */
public record RecentActivityResponse(
    Long id,
    String activityType,  // "New Member", "Donation Received", "Attendance Updated"
    String title,
    String description
) {
}
