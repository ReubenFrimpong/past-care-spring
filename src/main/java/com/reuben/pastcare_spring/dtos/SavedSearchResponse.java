package com.reuben.pastcare_spring.dtos;

import java.time.LocalDateTime;

/**
 * Response DTO for saved search queries.
 */
public record SavedSearchResponse(
    Long id,
    String searchName,
    String searchCriteria,
    Boolean isPublic,
    Boolean isDynamic,
    String description,
    LocalDateTime lastExecuted,
    Long lastResultCount,
    String lastExecutedAgo, // Human-readable: "2 days ago"
    CreatorInfo createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Boolean canEdit,
    Boolean canDelete
) {
    public record CreatorInfo(
        Long id,
        String name,
        String email
    ) {}
}
