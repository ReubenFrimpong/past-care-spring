package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.dtos.LifecycleEventResponse;
import com.reuben.pastcare_spring.models.LifecycleEvent;

/**
 * Mapper for LifecycleEvent entity.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public class LifecycleEventMapper {

    public static LifecycleEventResponse toResponse(LifecycleEvent event) {
        if (event == null) {
            return null;
        }

        return new LifecycleEventResponse(
            event.getId(),
            event.getMember() != null ? event.getMember().getId() : null,
            event.getMember() != null ? event.getMember().getFirstName() + " " + event.getMember().getLastName() : null,
            event.getEventType(),
            event.getEventDate(),
            event.getLocation(),
            event.getOfficiatingMinister(),
            event.getCertificateNumber(),
            event.getNotes(),
            event.getDocumentUrl(),
            event.getWitnesses(),
            event.getIsVerified(),
            event.getVerifiedBy() != null ? event.getVerifiedBy().getId() : null,
            event.getVerifiedBy() != null ? event.getVerifiedBy().getName() : null,
            event.getCreatedAt(),
            event.getUpdatedAt()
        );
    }
}
