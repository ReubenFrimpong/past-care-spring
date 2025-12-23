package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.LifecycleEventType;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Response DTO for lifecycle event data.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public record LifecycleEventResponse(
    Long id,
    Long memberId,
    String memberName,
    LifecycleEventType eventType,
    LocalDate eventDate,
    String location,
    String officiatingMinister,
    String certificateNumber,
    String notes,
    String documentUrl,
    String witnesses,
    Boolean isVerified,
    Long verifiedByUserId,
    String verifiedByUserName,
    Instant createdAt,
    Instant updatedAt
) {
}
