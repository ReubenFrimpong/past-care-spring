package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.MemberStatus;
import jakarta.validation.constraints.*;

/**
 * Request DTO for transitioning a member's status.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public record MemberStatusTransitionRequest(
    @NotNull(message = "New status is required")
    MemberStatus newStatus,

    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    String reason,

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    String notes
) {
}
