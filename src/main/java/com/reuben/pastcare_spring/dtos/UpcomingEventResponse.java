package com.reuben.pastcare_spring.dtos;

import java.time.LocalDateTime;

/**
 * Upcoming event item for dashboard.
 */
public record UpcomingEventResponse(
    Long id,
    String title,
    String description,
    LocalDateTime eventDateTime,
    String badge  // "Tomorrow", "This Week", etc.
) {
}
