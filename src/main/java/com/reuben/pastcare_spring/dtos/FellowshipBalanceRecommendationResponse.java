package com.reuben.pastcare_spring.dtos;

import java.util.List;

/**
 * Response DTO for fellowship balance recommendations
 */
public record FellowshipBalanceRecommendationResponse(
    Long fellowshipId,
    String fellowshipName,
    String recommendationType,  // SPLIT, MERGE, REDISTRIBUTE, NEW_FELLOWSHIP
    String priority,             // HIGH, MEDIUM, LOW
    String reason,
    Integer currentSize,
    Integer optimalSize,
    String demographicImbalance,
    List<String> suggestedActions
) {
}
