package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating or updating a saved search.
 */
public record SavedSearchRequest(
    @NotBlank(message = "Search name is required")
    @Size(max = 200, message = "Search name must not exceed 200 characters")
    String searchName,

    @NotBlank(message = "Search criteria is required")
    String searchCriteria, // JSON string of AdvancedSearchRequest

    Boolean isPublic,

    Boolean isDynamic,

    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description
) {
    public SavedSearchRequest {
        // Set defaults if null
        if (isPublic == null) {
            isPublic = false;
        }
        if (isDynamic == null) {
            isDynamic = false;
        }
    }
}
