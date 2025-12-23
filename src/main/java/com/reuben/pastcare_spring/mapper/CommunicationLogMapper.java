package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.dtos.CommunicationLogResponse;
import com.reuben.pastcare_spring.models.CommunicationLog;

/**
 * Mapper for CommunicationLog entity.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public class CommunicationLogMapper {

    public static CommunicationLogResponse toResponse(CommunicationLog log) {
        if (log == null) {
            return null;
        }

        return new CommunicationLogResponse(
            log.getId(),
            log.getMember() != null ? log.getMember().getId() : null,
            log.getMember() != null ? log.getMember().getFirstName() + " " + log.getMember().getLastName() : null,
            log.getCommunicationType(),
            log.getDirection(),
            log.getCommunicationDate(),
            log.getDurationMinutes(),
            log.getSubject(),
            log.getNotes(),
            log.getUser() != null ? log.getUser().getId() : null,
            log.getUser() != null ? log.getUser().getName() : null,
            log.getFollowUpRequired(),
            log.getFollowUpDate(),
            log.getFollowUpStatus(),
            log.getPriority(),
            log.getOutcome(),
            log.getIsConfidential(),
            log.getTags(),
            log.getCreatedAt(),
            log.getUpdatedAt()
        );
    }
}
