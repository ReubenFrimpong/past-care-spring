package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

/**
 * Request DTO for bulk updating multiple members
 * Supports updating fellowships, tags, status, and verification state
 */
public record MemberBulkUpdateRequest(
    @NotNull(message = "Member IDs are required")
    @NotEmpty(message = "At least one member ID is required")
    @Size(max = 100, message = "Cannot update more than 100 members at once")
    List<Long> memberIds,

    // Fields to update (all optional - only update if provided)
    UpdateAction<List<Long>> fellowshipIds,  // Add, remove, or replace fellowships
    UpdateAction<Set<String>> tags,           // Add, remove, or replace tags
    String status,                            // VISITOR, FIRST_TIMER, REGULAR, MEMBER, LEADER
    Boolean isVerified,                       // Mark as verified/unverified
    String maritalStatus                      // Update marital status
) {
    /**
     * Generic update action that specifies operation type and values
     * @param <T> Type of values (List<Long> for fellowships, Set<String> for tags)
     */
    public record UpdateAction<T>(
        ActionType action,  // ADD, REMOVE, REPLACE
        T values
    ) {}

    public enum ActionType {
        ADD,      // Add to existing values
        REMOVE,   // Remove from existing values
        REPLACE   // Replace all existing values
    }
}
