package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;

/**
 * Response DTO for fellowship retention metrics
 */
public record FellowshipRetentionResponse(
    Long fellowshipId,
    String fellowshipName,
    LocalDate periodStart,
    LocalDate periodEnd,
    Integer membersAtStart,
    Integer newJoins,
    Integer leftFellowship,
    Integer membersAtEnd,
    Double retentionRate,      // Percentage of members retained (0-100)
    Double churnRate,           // Percentage of members who left (0-100)
    Integer transfersIn,
    Integer transfersOut,
    Integer inactiveMembers,
    Integer reactivatedMembers,
    String healthIndicator      // EXCELLENT, GOOD, CONCERNING, CRITICAL
) {
}
