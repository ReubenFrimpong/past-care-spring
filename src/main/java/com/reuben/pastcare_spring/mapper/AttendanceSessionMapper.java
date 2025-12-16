package com.reuben.pastcare_spring.mapper;

import java.util.List;
import java.util.stream.Collectors;

import com.reuben.pastcare_spring.dtos.AttendanceResponse;
import com.reuben.pastcare_spring.dtos.AttendanceSessionResponse;
import com.reuben.pastcare_spring.enums.AttendanceStatus;
import com.reuben.pastcare_spring.models.AttendanceSession;

public class AttendanceSessionMapper {

  public static AttendanceSessionResponse toAttendanceSessionResponse(AttendanceSession session, boolean includeAttendances) {
    List<AttendanceResponse> attendanceResponses = null;

    if (includeAttendances && session.getAttendances() != null) {
      attendanceResponses = session.getAttendances().stream()
        .map(AttendanceMapper::toAttendanceResponse)
        .collect(Collectors.toList());
    }

    int totalMembers = session.getAttendances() != null ? session.getAttendances().size() : 0;
    int presentCount = session.getAttendances() != null ?
      (int) session.getAttendances().stream().filter(a -> a.getStatus() == AttendanceStatus.PRESENT).count() : 0;
    int absentCount = session.getAttendances() != null ?
      (int) session.getAttendances().stream().filter(a -> a.getStatus() == AttendanceStatus.ABSENT).count() : 0;
    int excusedCount = session.getAttendances() != null ?
      (int) session.getAttendances().stream().filter(a -> a.getStatus() == AttendanceStatus.EXCUSED).count() : 0;

    return new AttendanceSessionResponse(
      session.getId(),
      session.getSessionName(),
      session.getSessionDate(),
      session.getSessionTime(),
      session.getFellowship() != null ? session.getFellowship().getId() : null,
      session.getFellowship() != null ? session.getFellowship().getName() : null,
      session.getChurch().getId(),
      session.getChurch().getName(),
      session.getNotes(),
      session.getIsCompleted(),
      totalMembers,
      presentCount,
      absentCount,
      excusedCount,
      attendanceResponses,
      session.getCreatedAt(),
      session.getUpdatedAt()
    );
  }

  public static AttendanceSessionResponse toAttendanceSessionResponse(AttendanceSession session) {
    return toAttendanceSessionResponse(session, false);
  }
}
