package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;

/**
 * Request DTO for recording a fellowship multiplication
 */
public record RecordMultiplicationRequest(
    Long childFellowshipId,
    LocalDate multiplicationDate,
    String reason,
    Integer membersTransferred,
    String notes
) {
}
