package com.reuben.pastcare_spring.dtos;

import lombok.*;

import java.util.Map;

/**
 * Response DTO for Event statistics.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventStatsResponse {

    // Overall statistics
    private Long totalEvents;
    private Long upcomingEvents;
    private Long ongoingEvents;
    private Long pastEvents;
    private Long cancelledEvents;

    // Registration statistics
    private Long eventsRequiringRegistration;
    private Long eventsWithOpenRegistration;
    private Long totalRegistrations;
    private Long pendingApprovals;

    // Event type breakdown
    private Map<String, Long> eventsByType;

    // Visibility breakdown
    private Map<String, Long> eventsByVisibility;

    // Location type breakdown
    private Map<String, Long> eventsByLocationType;

    // Monthly event count (for charts)
    private Map<String, Long> eventsByMonth;

    // Attendance statistics
    private Long totalAttendance;
    private Double averageAttendanceRate;

    // Popular tags
    private Map<String, Long> popularTags;

    // Organizer statistics
    private Long totalOrganizers;
    private Long eventsWithMultipleOrganizers;
}
