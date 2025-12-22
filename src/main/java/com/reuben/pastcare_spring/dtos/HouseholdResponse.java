package com.reuben.pastcare_spring.dtos;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for household data
 */
public record HouseholdResponse(
    Long id,
    String householdName,
    MemberResponse householdHead,
    LocationResponse sharedLocation,
    List<MemberResponse> members,
    String notes,
    LocalDate establishedDate,
    String householdImageUrl,
    String householdEmail,
    String householdPhone,
    Integer memberCount,
    Instant createdAt,
    Instant updatedAt
) {
}
