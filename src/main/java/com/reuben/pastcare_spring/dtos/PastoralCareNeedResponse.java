package com.reuben.pastcare_spring.dtos;

/**
 * Pastoral care need item for dashboard.
 */
public record PastoralCareNeedResponse(
    Long id,
    String memberName,
    String description,
    String priority  // "Urgent", "Today", "This Week"
) {
}
