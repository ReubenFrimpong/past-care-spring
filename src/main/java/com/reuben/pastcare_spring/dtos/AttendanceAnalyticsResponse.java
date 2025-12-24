package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;
import java.util.List;

/**
 * Phase 2: Attendance Analytics
 * Comprehensive analytics response for attendance overview
 */
public record AttendanceAnalyticsResponse(
    // Overall Statistics
    Long totalSessions,
    Long totalAttendanceRecords,
    Long uniqueMembersAttended,
    Double overallAttendanceRate,

    // Trend Data
    List<AttendanceTrendPoint> trends,

    // Service Type Breakdown
    List<ServiceTypeStats> serviceTypeStats,

    // Check-in Method Distribution
    List<CheckInMethodStats> checkInMethodStats,

    // Late Arrival Insights
    LateArrivalStats lateArrivalStats,

    // Visitor Metrics
    VisitorMetrics visitorMetrics,

    // Date Range
    LocalDate startDate,
    LocalDate endDate
) {
    // Nested records for structured data

    public record AttendanceTrendPoint(
        LocalDate date,
        Long totalMembers,
        Long presentCount,
        Double attendanceRate,
        String serviceType
    ) {}

    public record ServiceTypeStats(
        String serviceType,
        Long sessionCount,
        Long totalAttendance,
        Double avgAttendance,
        Double attendanceRate
    ) {}

    public record CheckInMethodStats(
        String checkInMethod,
        Long count,
        Double percentage
    ) {}

    public record LateArrivalStats(
        Long totalLateArrivals,
        Double lateArrivalRate,
        Double avgMinutesLate,
        Long maxMinutesLate,
        List<String> frequentlyLateMembers
    ) {}

    public record VisitorMetrics(
        Long totalVisitors,
        Long returningVisitors,
        Long convertedToMembers,
        Double conversionRate,
        Double returnRate
    ) {}
}
