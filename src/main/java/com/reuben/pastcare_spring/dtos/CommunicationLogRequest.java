package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CommunicationDirection;
import com.reuben.pastcare_spring.models.CommunicationPriority;
import com.reuben.pastcare_spring.models.CommunicationType;
import com.reuben.pastcare_spring.models.FollowUpStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

/**
 * Request DTO for creating or updating a communication log.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public record CommunicationLogRequest(
    @NotNull(message = "Member ID is required")
    Long memberId,

    @NotNull(message = "Communication type is required")
    CommunicationType communicationType,

    @NotNull(message = "Communication direction is required")
    CommunicationDirection direction,

    @NotNull(message = "Communication date is required")
    LocalDateTime communicationDate,

    @Min(value = 0, message = "Duration must be positive")
    Integer durationMinutes,

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    String subject,

    @Size(max = 10000, message = "Notes must not exceed 10000 characters")
    String notes,

    Boolean followUpRequired,

    @Future(message = "Follow-up date must be in the future")
    LocalDateTime followUpDate,

    FollowUpStatus followUpStatus,

    CommunicationPriority priority,

    @Size(max = 500, message = "Outcome must not exceed 500 characters")
    String outcome,

    Boolean isConfidential,

    @Size(max = 500, message = "Tags must not exceed 500 characters")
    String tags
) {
}
