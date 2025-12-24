package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;

/**
 * Phase 2: Attendance Analytics
 * Individual member engagement metrics
 */
public record MemberEngagementResponse(
    Long memberId,
    String memberName,
    Long totalSessionsAvailable,
    Long sessionsAttended,
    Double attendanceRate,
    Long consecutiveAttendance,
    Long timesLate,
    Double lateRate,
    LocalDate lastAttendance,
    Long daysSinceLastAttendance,
    String engagementLevel, // HIGH, MEDIUM, LOW, AT_RISK
    String preferredCheckInMethod
) {
    /**
     * Calculate engagement level based on attendance rate
     */
    public static String calculateEngagementLevel(Double attendanceRate, Long daysSinceLastAttendance) {
        if (daysSinceLastAttendance != null && daysSinceLastAttendance > 30) {
            return "AT_RISK";
        }
        if (attendanceRate >= 80.0) {
            return "HIGH";
        } else if (attendanceRate >= 50.0) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }
}
