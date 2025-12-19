package com.reuben.pastcare_spring.dtos;

/**
 * Response DTO for location-based member statistics
 * Used for map visualization and geographic reports
 */
public record LocationStatsResponse(
  Long locationId,
  String displayName,
  String coordinates,
  String city,
  String suburb,
  String region,
  Long memberCount
) {
}
