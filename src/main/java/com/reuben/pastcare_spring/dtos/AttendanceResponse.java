package com.reuben.pastcare_spring.dtos;

import java.time.Instant;

import com.reuben.pastcare_spring.enums.AttendanceStatus;

public record AttendanceResponse(
  Long id,
  Long memberId,
  String memberName,
  Long attendanceSessionId,
  String sessionName,
  AttendanceStatus status,
  String remarks,
  Instant createdAt,
  Instant updatedAt
) {}
