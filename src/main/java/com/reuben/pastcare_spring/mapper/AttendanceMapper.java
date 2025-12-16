package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.dtos.AttendanceResponse;
import com.reuben.pastcare_spring.models.Attendance;

public class AttendanceMapper {

  public static AttendanceResponse toAttendanceResponse(Attendance attendance) {
    return new AttendanceResponse(
      attendance.getId(),
      attendance.getMember().getId(),
      attendance.getMember().getFirstName() + " " + attendance.getMember().getLastName(),
      attendance.getAttendanceSession().getId(),
      attendance.getAttendanceSession().getSessionName(),
      attendance.getStatus(),
      attendance.getRemarks(),
      attendance.getCreatedAt(),
      attendance.getUpdatedAt()
    );
  }
}
