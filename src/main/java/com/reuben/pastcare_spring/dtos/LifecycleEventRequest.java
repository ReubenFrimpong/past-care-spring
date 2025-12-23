package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.LifecycleEventType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Request DTO for creating or updating a lifecycle event.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
public record LifecycleEventRequest(
    @NotNull(message = "Member ID is required")
    Long memberId,

    @NotNull(message = "Event type is required")
    LifecycleEventType eventType,

    @NotNull(message = "Event date is required")
    @PastOrPresent(message = "Event date cannot be in the future")
    LocalDate eventDate,

    @Size(max = 200, message = "Location must not exceed 200 characters")
    String location,

    @Size(max = 100, message = "Officiating minister name must not exceed 100 characters")
    String officiatingMinister,

    @Size(max = 100, message = "Certificate number must not exceed 100 characters")
    String certificateNumber,

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    String notes,

    @Size(max = 500, message = "Document URL must not exceed 500 characters")
    String documentUrl,

    @Size(max = 500, message = "Witnesses list must not exceed 500 characters")
    String witnesses,

    Boolean isVerified
) {
}
