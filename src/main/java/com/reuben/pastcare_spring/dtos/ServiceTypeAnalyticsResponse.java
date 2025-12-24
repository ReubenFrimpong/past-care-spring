package com.reuben.pastcare_spring.dtos;

import java.util.List;

/**
 * Phase 2: Attendance Analytics
 * Analytics breakdown by service type
 */
public record ServiceTypeAnalyticsResponse(
    String serviceType,
    Long totalSessions,
    Long totalAttendance,
    Double avgAttendancePerSession,
    Double attendanceRate,
    Long peakAttendance,
    Long lowestAttendance,
    List<String> topAttenders,
    List<TrendPoint> trends
) {
    public record TrendPoint(
        String period, // "2024-01", "Week 1", etc.
        Long attendance,
        Double rate
    ) {}
}
