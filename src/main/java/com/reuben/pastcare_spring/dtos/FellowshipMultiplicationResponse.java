package com.reuben.pastcare_spring.dtos;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for fellowship multiplication events
 */
public record FellowshipMultiplicationResponse(
    Long id,
    Long parentFellowshipId,
    String parentFellowshipName,
    Long childFellowshipId,
    String childFellowshipName,
    LocalDate multiplicationDate,
    String reason,
    Integer membersTransferred,
    String notes,
    Instant createdAt
) {
}
