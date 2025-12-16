package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.enums.AttendanceStatus;

import jakarta.validation.constraints.NotNull;

public record AttendanceRequest(
  @NotNull(message = "Member ID is required")
  Long memberId,

  @NotNull(message = "Attendance session ID is required")
  Long attendanceSessionId,

  @NotNull(message = "Status is required")
  AttendanceStatus status,

  String remarks
) {}
