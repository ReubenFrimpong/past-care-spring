package com.reuben.pastcare_spring.dtos;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record BulkAttendanceRequest(
  @NotNull(message = "Attendance session ID is required")
  Long attendanceSessionId,

  @NotEmpty(message = "Attendance records cannot be empty")
  List<AttendanceRequest> attendances
) {}
