package com.reuben.pastcare_spring.dtos;

/**
 * Response DTO for Location data
 */
public record LocationResponse(
  Long id,
  String coordinates,
  String region,
  String district,
  String city,
  String suburb,
  String displayName,
  String shortName
) {
}
