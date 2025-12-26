package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response DTO for a care need
 */
public record CareNeedResponse(
    Long id,
    Long memberId,
    String memberName,
    String title,
    String description,
    CareNeedType type,
    CareNeedPriority priority,
    CareNeedStatus status,
    UserSummary assignedTo,
    UserSummary createdBy,
    LocalDateTime dueDate,
    LocalDateTime resolvedDate,
    String resolutionNotes,
    Boolean followUpRequired,
    LocalDateTime followUpDate,
    FollowUpStatus followUpStatus,
    Set<String> tags,
    Instant createdAt,
    Instant updatedAt
) {
    /**
     * Nested record for user summary information
     */
    public record UserSummary(Long id, String name, String email) {}

    /**
     * Convert a CareNeed entity to a CareNeedResponse DTO
     */
    public static CareNeedResponse fromEntity(CareNeed careNeed) {
        UserSummary assignedToSummary = null;
        if (careNeed.getAssignedTo() != null) {
            assignedToSummary = new UserSummary(
                careNeed.getAssignedTo().getId(),
                careNeed.getAssignedTo().getName(),
                careNeed.getAssignedTo().getEmail()
            );
        }

        UserSummary createdBySummary = new UserSummary(
            careNeed.getCreatedBy().getId(),
            careNeed.getCreatedBy().getName(),
            careNeed.getCreatedBy().getEmail()
        );

        String memberName = careNeed.getMember().getFirstName() + " " +
                           careNeed.getMember().getLastName();

        return new CareNeedResponse(
            careNeed.getId(),
            careNeed.getMember().getId(),
            memberName,
            careNeed.getTitle(),
            careNeed.getDescription(),
            careNeed.getType(),
            careNeed.getPriority(),
            careNeed.getStatus(),
            assignedToSummary,
            createdBySummary,
            careNeed.getDueDate(),
            careNeed.getResolvedDate(),
            careNeed.getResolutionNotes(),
            careNeed.getFollowUpRequired(),
            careNeed.getFollowUpDate(),
            careNeed.getFollowUpStatus(),
            careNeed.getTags(),
            careNeed.getCreatedAt(),
            careNeed.getUpdatedAt()
        );
    }
}
