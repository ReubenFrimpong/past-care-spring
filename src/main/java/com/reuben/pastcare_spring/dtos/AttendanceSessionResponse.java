package com.reuben.pastcare_spring.dtos;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record AttendanceSessionResponse(
  Long id,
  String sessionName,
  LocalDate sessionDate,
  LocalTime sessionTime,
  Long fellowshipId,
  String fellowshipName,
  Long eventId,
  String eventName,
  Long churchId,
  String churchName,
  String notes,
  Boolean isCompleted,
  Integer totalMembers,
  Integer presentCount,
  Integer absentCount,
  Integer excusedCount,
  List<AttendanceResponse> attendances,
  Instant createdAt,
  Instant updatedAt
) {}
