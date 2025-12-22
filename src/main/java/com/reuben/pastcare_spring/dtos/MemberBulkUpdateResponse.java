package com.reuben.pastcare_spring.dtos;

import java.util.List;

/**
 * Response DTO for bulk update operation
 * Contains counts and updated member details
 */
public record MemberBulkUpdateResponse(
    int totalMembers,      // Total members attempted to update
    int successCount,      // Successfully updated
    int failureCount,      // Failed to update
    List<UpdateError> errors,        // List of errors with member IDs
    List<MemberResponse> updatedMembers  // Updated member details
) {
    /**
     * Error details for a specific member update failure
     */
    public record UpdateError(
        Long memberId,
        String memberName,
        String errorMessage
    ) {}
}
