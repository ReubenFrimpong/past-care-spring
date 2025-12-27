package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for creating or updating an event registration.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRegistrationRequest {

    @NotNull(message = "Event ID is required")
    private Long eventId;

    // Member registration
    private Long memberId;

    // Guest registration (for non-members)
    private Boolean isGuest;

    @Size(max = 200, message = "Guest name must not exceed 200 characters")
    private String guestName;

    @Email(message = "Guest email must be valid")
    @Size(max = 200, message = "Guest email must not exceed 200 characters")
    private String guestEmail;

    @Size(max = 20, message = "Guest phone must not exceed 20 characters")
    private String guestPhone;

    // Guest count
    @Min(value = 0, message = "Number of guests must be non-negative")
    private Integer numberOfGuests;

    @Size(max = 1000, message = "Guest names must not exceed 1000 characters")
    private String guestNames;

    // Notes and requirements
    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    @Size(max = 5000, message = "Special requirements must not exceed 5000 characters")
    private String specialRequirements;

    @AssertTrue(message = "Either member ID or guest information must be provided")
    private boolean isMemberOrGuestProvided() {
        if (isGuest != null && isGuest) {
            return guestName != null && !guestName.trim().isEmpty();
        }
        return memberId != null;
    }
}
