package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CommunicationDirection;
import com.reuben.pastcare_spring.models.CommunicationPriority;
import com.reuben.pastcare_spring.models.CommunicationType;
import com.reuben.pastcare_spring.models.FollowUpStatus;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Response DTO for communication log data.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public record CommunicationLogResponse(
    Long id,
    Long memberId,
    String memberName,
    CommunicationType communicationType,
    CommunicationDirection direction,
    LocalDateTime communicationDate,
    Integer durationMinutes,
    String subject,
    String notes,
    Long userId,
    String userName,
    Boolean followUpRequired,
    LocalDateTime followUpDate,
    FollowUpStatus followUpStatus,
    CommunicationPriority priority,
    String outcome,
    Boolean isConfidential,
    String tags,
    Instant createdAt,
    Instant updatedAt
) {
}
