package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CommunicationPriority;
import com.reuben.pastcare_spring.models.ConfidentialNoteCategory;
import com.reuben.pastcare_spring.models.FollowUpStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Request DTO for creating or updating a confidential note.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public record ConfidentialNoteRequest(
    @NotNull(message = "Member ID is required")
    Long memberId,

    @NotNull(message = "Category is required")
    ConfidentialNoteCategory category,

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    String subject,

    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    String content,

    CommunicationPriority priority,

    Boolean requiresFollowUp,

    @Future(message = "Follow-up date must be in the future")
    LocalDateTime followUpDate,

    FollowUpStatus followUpStatus,

    @Size(max = 50, message = "Minimum role required must not exceed 50 characters")
    String minimumRoleRequired,

    Long relatedCommunicationId,

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    String tags
) {
}
