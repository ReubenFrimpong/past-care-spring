package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CommunicationPriority;
import com.reuben.pastcare_spring.models.ConfidentialNoteCategory;
import com.reuben.pastcare_spring.models.FollowUpStatus;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Response DTO for confidential note data.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public record ConfidentialNoteResponse(
    Long id,
    Long memberId,
    String memberName,
    ConfidentialNoteCategory category,
    String subject,
    String content,
    Long createdByUserId,
    String createdByUserName,
    LocalDateTime lastModifiedAt,
    Long lastModifiedByUserId,
    String lastModifiedByUserName,
    CommunicationPriority priority,
    Boolean requiresFollowUp,
    LocalDateTime followUpDate,
    FollowUpStatus followUpStatus,
    String minimumRoleRequired,
    Boolean isArchived,
    Long relatedCommunicationId,
    String tags,
    Instant createdAt,
    Instant updatedAt
) {
}
