package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;

/**
 * Phase 2: Attendance Analytics
 * Summary statistics for attendance overview
 */
public record AttendanceSummaryResponse(
    // Period
    LocalDate startDate,
    LocalDate endDate,

    // Session Statistics
    Long totalSessions,
    Long completedSessions,
    Long upcomingSessions,

    // Member Statistics
    Long totalMembers,
    Long activeMembers, // Attended at least once in period
    Long uniqueAttendees,

    // Attendance Metrics
    Long totalAttendanceRecords,
    Long presentCount,
    Long absentCount,
    Long excusedCount,
    Double overallAttendanceRate,

    // Comparison with Previous Period
    Double attendanceRateChange, // Percentage change from previous period
    Long memberCountChange,

    // Check-in Insights
    Long qrCodeCheckIns,
    Long geofenceCheckIns,
    Long manualCheckIns,
    Long lateCheckIns,
    Double lateCheckInRate,

    // Visitor Insights
    Long totalVisitors,
    Long newVisitors,
    Long returningVisitors
) {}
