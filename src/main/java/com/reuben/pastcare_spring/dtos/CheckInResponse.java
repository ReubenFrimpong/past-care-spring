package com.reuben.pastcare_spring.dtos;

import java.time.LocalDateTime;

import com.reuben.pastcare_spring.enums.CheckInMethod;

/**
 * Response DTO for member check-in.
 * Phase 1: Enhanced Attendance Tracking
 *
 * @param attendanceId The created attendance record ID
 * @param sessionId The attendance session ID
 * @param sessionName The session name
 * @param memberId The member ID (null if visitor)
 * @param memberName The member name (null if visitor)
 * @param visitorId The visitor ID (null if member)
 * @param visitorName The visitor name (null if member)
 * @param checkInMethod The method used for check-in
 * @param checkInTime The time of check-in
 * @param isLate Whether the check-in was late
 * @param minutesLate Number of minutes late (null if not late)
 * @param message Success or warning message
 */
public record CheckInResponse(
    Long attendanceId,
    Long sessionId,
    String sessionName,
    Long memberId,
    String memberName,
    Long visitorId,
    String visitorName,
    CheckInMethod checkInMethod,
    LocalDateTime checkInTime,
    Boolean isLate,
    Integer minutesLate,
    String message
) {
}
