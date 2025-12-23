package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.dtos.ConfidentialNoteResponse;
import com.reuben.pastcare_spring.models.ConfidentialNote;

/**
 * Mapper for ConfidentialNote entity.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public class ConfidentialNoteMapper {

    public static ConfidentialNoteResponse toResponse(ConfidentialNote note) {
        if (note == null) {
            return null;
        }

        return new ConfidentialNoteResponse(
            note.getId(),
            note.getMember() != null ? note.getMember().getId() : null,
            note.getMember() != null ? note.getMember().getFirstName() + " " + note.getMember().getLastName() : null,
            note.getCategory(),
            note.getSubject(),
            note.getContent(),
            note.getCreatedBy() != null ? note.getCreatedBy().getId() : null,
            note.getCreatedBy() != null ? note.getCreatedBy().getName() : null,
            note.getLastModifiedAt(),
            note.getLastModifiedBy() != null ? note.getLastModifiedBy().getId() : null,
            note.getLastModifiedBy() != null ? note.getLastModifiedBy().getName() : null,
            note.getPriority(),
            note.getRequiresFollowUp(),
            note.getFollowUpDate(),
            note.getFollowUpStatus(),
            note.getMinimumRoleRequired(),
            note.getIsArchived(),
            note.getRelatedCommunication() != null ? note.getRelatedCommunication().getId() : null,
            note.getTags(),
            note.getCreatedAt(),
            note.getUpdatedAt()
        );
    }
}
