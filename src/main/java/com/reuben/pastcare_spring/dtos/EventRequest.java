package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.EventLocationType;
import com.reuben.pastcare_spring.models.EventType;
import com.reuben.pastcare_spring.models.EventVisibility;
import com.reuben.pastcare_spring.models.RecurrencePattern;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Request DTO for creating or updating an event.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventRequest {

    @NotBlank(message = "Event name is required")
    @Size(max = 200, message = "Event name must not exceed 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    @NotNull(message = "Location type is required")
    private EventLocationType locationType;

    @Size(max = 500, message = "Physical location must not exceed 500 characters")
    private String physicalLocation;

    @Size(max = 1000, message = "Virtual link must not exceed 1000 characters")
    private String virtualLink;

    @Size(max = 100, message = "Virtual platform must not exceed 100 characters")
    private String virtualPlatform;

    private Long locationId;

    @NotNull(message = "Requires registration must be specified")
    private Boolean requiresRegistration;

    private LocalDateTime registrationDeadline;

    @Min(value = 1, message = "Max capacity must be at least 1")
    private Integer maxCapacity;

    private Boolean allowWaitlist;

    private Boolean autoApproveRegistrations;

    @NotNull(message = "Visibility is required")
    private EventVisibility visibility;

    private Boolean isRecurring;

    private RecurrencePattern recurrencePattern;

    private LocalDate recurrenceEndDate;

    private Long parentEventId;

    private Long primaryOrganizerId;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Min(value = 0, message = "Reminder days before must be non-negative")
    private Integer reminderDaysBefore;

    // Organizers to add (list of member IDs with roles)
    private List<EventOrganizerRequest> organizers;

    // Tags to add
    private List<String> tags;

    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Physical location is required for physical or hybrid events")
    private boolean isPhysicalLocationValid() {
        if (locationType == null) {
            return true;
        }
        if (locationType == EventLocationType.PHYSICAL || locationType == EventLocationType.HYBRID) {
            return physicalLocation != null && !physicalLocation.trim().isEmpty();
        }
        return true;
    }

    @AssertTrue(message = "Virtual link is required for virtual or hybrid events")
    private boolean isVirtualLinkValid() {
        if (locationType == null) {
            return true;
        }
        if (locationType == EventLocationType.VIRTUAL || locationType == EventLocationType.HYBRID) {
            return virtualLink != null && !virtualLink.trim().isEmpty();
        }
        return true;
    }

    @AssertTrue(message = "Registration deadline must be before start date")
    private boolean isRegistrationDeadlineValid() {
        if (registrationDeadline == null || startDate == null) {
            return true;
        }
        return registrationDeadline.isBefore(startDate);
    }

    @AssertTrue(message = "Recurrence pattern is required for recurring events")
    private boolean isRecurrencePatternValid() {
        if (isRecurring == null || !isRecurring) {
            return true;
        }
        return recurrencePattern != null;
    }
}
