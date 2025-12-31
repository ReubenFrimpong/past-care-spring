package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AttendanceSessionRequest(
  @NotBlank(message = "Session name is required")
  String sessionName,

  @NotNull(message = "Session date is required")
  LocalDate sessionDate,

  LocalTime sessionTime,

  Long fellowshipId,

  Long eventId,

  @NotNull(message = "Church ID is required")
  Long churchId,

  String notes
) {}
