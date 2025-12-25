package com.reuben.pastcare_spring.dtos;

import java.util.List;

/**
 * Response DTO for member segmentation based on attendance
 */
public record MemberSegmentationResponse(
    List<MemberSegment> activeMembers,
    List<MemberSegment> irregularMembers,
    List<MemberSegment> inactiveMembers,
    List<MemberSegment> atRiskMembers,
    SegmentStatistics statistics
) {
    public record MemberSegment(
        Long memberId,
        String fullName,
        String segment, // ACTIVE, IRREGULAR, INACTIVE, AT_RISK
        Double attendanceRate,
        Integer consecutiveAbsences,
        Integer totalSessions,
        Integer sessionsAttended,
        String lastAttendanceDate,
        String recommendation
    ) {}

    public record SegmentStatistics(
        Integer totalMembers,
        Integer activeCount,
        Integer irregularCount,
        Integer inactiveCount,
        Integer atRiskCount,
        Double averageAttendanceRate
    ) {}
}
