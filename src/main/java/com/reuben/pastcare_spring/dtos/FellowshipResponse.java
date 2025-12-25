package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.FellowshipType;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

/**
 * DTO for fellowship responses.
 */
public record FellowshipResponse(
  Long id,
  String name,
  String description,
  String imageUrl,
  FellowshipType fellowshipType,
  UserSummary leader,
  java.util.List<UserSummary> coleaders,
  DayOfWeek meetingDay,
  LocalTime meetingTime,
  LocationResponse meetingLocation,
  Integer maxCapacity,
  Boolean isActive,
  Boolean acceptingMembers,
  Integer memberCount,
  Instant createdAt,
  Instant updatedAt
) {

  /**
   * Simple user summary for leader/coleader display
   */
  public record UserSummary(
    Long id,
    String name,
    String email
  ) {
  }

  /**
   * Convert Fellowship entity to FellowshipResponse DTO
   */
  public static FellowshipResponse fromEntity(Fellowship fellowship) {
    UserSummary leaderSummary = null;
    if (fellowship.getLeader() != null) {
      leaderSummary = new UserSummary(
        fellowship.getLeader().getId(),
        fellowship.getLeader().getName(),
        fellowship.getLeader().getEmail()
      );
    }

    java.util.List<UserSummary> coleaderSummaries = fellowship.getColeaders() == null
      ? java.util.List.of()
      : fellowship.getColeaders().stream()
        .map(user -> new UserSummary(
          user.getId(),
          user.getName(),
          user.getEmail()
        ))
        .toList();

    LocationResponse locationResponse = null;
    if (fellowship.getMeetingLocation() != null) {
      var loc = fellowship.getMeetingLocation();
      locationResponse = new LocationResponse(
        loc.getId(),
        loc.getCoordinates(),
        loc.getRegion(),
        loc.getDistrict(),
        loc.getCity(),
        loc.getSuburb(),
        loc.getDisplayName(),
        loc.getShortName()
      );
    }

    int memberCount = fellowship.getMembers() == null ? 0 : fellowship.getMembers().size();

    return new FellowshipResponse(
      fellowship.getId(),
      fellowship.getName(),
      fellowship.getDescription(),
      fellowship.getImageUrl(),
      fellowship.getFellowshipType(),
      leaderSummary,
      coleaderSummaries,
      fellowship.getMeetingDay(),
      fellowship.getMeetingTime(),
      locationResponse,
      fellowship.getMaxCapacity(),
      fellowship.getIsActive(),
      fellowship.getAcceptingMembers(),
      memberCount,
      fellowship.getCreatedAt(),
      fellowship.getUpdatedAt()
    );
  }
}
