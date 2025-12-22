package com.reuben.pastcare_spring.dtos;

import org.springframework.data.domain.Page;

/**
 * Response DTO for advanced search results.
 * Includes pagination info and search metadata.
 */
public record AdvancedSearchResponse(
    Page<MemberResponse> members,
    SearchMetadata metadata
) {

    /**
     * Metadata about the search execution.
     */
    public record SearchMetadata(
        int totalFiltersApplied,
        long executionTimeMs,
        String query // For debugging/logging
    ) {}
}
