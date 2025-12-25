package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.FellowshipType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO for creating or updating a fellowship.
 */
public record FellowshipRequest(
  @NotBlank(message = "Fellowship name is required")
  String name,

  String description,

  String imageUrl,

  @NotNull(message = "Fellowship type is required")
  FellowshipType fellowshipType,

  Long leaderId,

  List<Long> coleaderIds,

  DayOfWeek meetingDay,

  LocalTime meetingTime,

  Long meetingLocationId,

  @Positive(message = "Max capacity must be positive")
  Integer maxCapacity,

  Boolean isActive,

  Boolean acceptingMembers
) {
}
