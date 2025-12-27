package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for Event entity.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Long id;
    private Long churchId;
    private String churchName;

    // Basic information
    private String name;
    private String description;
    private EventType eventType;
    private String eventTypeDisplay;

    // Date and time
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String timezone;

    // Location details
    private EventLocationType locationType;
    private String locationTypeDisplay;
    private String physicalLocation;
    private String virtualLink;
    private String virtualPlatform;
    private Long locationId;
    private String locationName;

    // Registration management
    private Boolean requiresRegistration;
    private LocalDateTime registrationDeadline;
    private Integer maxCapacity;
    private Integer currentRegistrations;
    private Integer availableCapacity;
    private Boolean allowWaitlist;
    private Boolean autoApproveRegistrations;
    private Boolean registrationOpen;

    // Visibility
    private EventVisibility visibility;
    private String visibilityDisplay;

    // Recurrence
    private Boolean isRecurring;
    private RecurrencePattern recurrencePattern;
    private String recurrencePatternDisplay;
    private LocalDate recurrenceEndDate;
    private Long parentEventId;
    private String parentEventName;

    // Organizer
    private Long primaryOrganizerId;
    private String primaryOrganizerName;

    // Status
    private String eventStatus; // UPCOMING, ONGOING, PAST, CANCELLED
    private Boolean isCancelled;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private String cancelledByName;

    // Additional info
    private String notes;
    private String imageUrl;
    private Boolean reminderSent;
    private Integer reminderDaysBefore;

    // Statistics
    private Long totalRegistrations;
    private Long approvedRegistrations;
    private Long pendingRegistrations;
    private Long waitlistCount;
    private Long attendanceCount;

    // Collections
    private List<EventOrganizerResponse> organizers;
    private List<String> tags;

    // Audit
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdByName;
    private String updatedByName;

    /**
     * Convert Event entity to EventResponse DTO
     */
    public static EventResponse fromEntity(Event event) {
        if (event == null) {
            return null;
        }

        EventResponseBuilder builder = EventResponse.builder()
            .id(event.getId())
            .churchId(event.getChurch() != null ? event.getChurch().getId() : null)
            .churchName(event.getChurch() != null ? event.getChurch().getName() : null)
            .name(event.getName())
            .description(event.getDescription())
            .eventType(event.getEventType())
            .eventTypeDisplay(event.getEventType() != null ? event.getEventType().getDisplayName() : null)
            .startDate(event.getStartDate())
            .endDate(event.getEndDate())
            .timezone(event.getTimezone())
            .locationType(event.getLocationType())
            .locationTypeDisplay(event.getLocationType() != null ? event.getLocationType().getDisplayName() : null)
            .physicalLocation(event.getPhysicalLocation())
            .virtualLink(event.getVirtualLink())
            .virtualPlatform(event.getVirtualPlatform())
            .locationId(event.getLocation() != null ? event.getLocation().getId() : null)
            .locationName(event.getLocation() != null ? event.getLocation().getFullAddress() : null)
            .requiresRegistration(event.getRequiresRegistration())
            .registrationDeadline(event.getRegistrationDeadline())
            .maxCapacity(event.getMaxCapacity())
            .currentRegistrations(event.getCurrentRegistrations())
            .availableCapacity(event.getAvailableCapacity())
            .allowWaitlist(event.getAllowWaitlist())
            .autoApproveRegistrations(event.getAutoApproveRegistrations())
            .registrationOpen(event.isRegistrationOpen())
            .visibility(event.getVisibility())
            .visibilityDisplay(event.getVisibility() != null ? event.getVisibility().getDisplayName() : null)
            .isRecurring(event.getIsRecurring())
            .recurrencePattern(event.getRecurrencePattern())
            .recurrencePatternDisplay(event.getRecurrencePattern() != null ? event.getRecurrencePattern().getDisplayName() : null)
            .recurrenceEndDate(event.getRecurrenceEndDate())
            .parentEventId(event.getParentEvent() != null ? event.getParentEvent().getId() : null)
            .parentEventName(event.getParentEvent() != null ? event.getParentEvent().getName() : null)
            .primaryOrganizerId(event.getPrimaryOrganizer() != null ? event.getPrimaryOrganizer().getId() : null)
            .primaryOrganizerName(event.getPrimaryOrganizer() != null ?
                event.getPrimaryOrganizer().getFirstName() + " " + event.getPrimaryOrganizer().getLastName() : null)
            .eventStatus(determineEventStatus(event))
            .isCancelled(event.getIsCancelled())
            .cancellationReason(event.getCancellationReason())
            .cancelledAt(event.getCancelledAt())
            .cancelledByName(event.getCancelledBy() != null ? event.getCancelledBy().getName() : null)
            .notes(event.getNotes())
            .imageUrl(event.getImageUrl())
            .reminderSent(event.getReminderSent())
            .reminderDaysBefore(event.getReminderDaysBefore())
            .createdAt(event.getCreatedAt())
            .updatedAt(event.getUpdatedAt())
            .createdByName(event.getCreatedBy() != null ? event.getCreatedBy().getName() : null)
            .updatedByName(event.getUpdatedBy() != null ? event.getUpdatedBy().getName() : null);

        // Add organizers if loaded
        if (event.getOrganizers() != null && !event.getOrganizers().isEmpty()) {
            builder.organizers(event.getOrganizers().stream()
                .map(EventOrganizerResponse::fromEntity)
                .collect(Collectors.toList()));
        }

        // Add tags if loaded
        if (event.getTags() != null && !event.getTags().isEmpty()) {
            builder.tags(event.getTags().stream()
                .map(EventTag::getTag)
                .collect(Collectors.toList()));
        }

        return builder.build();
    }

    /**
     * Determine event status based on dates and cancellation
     */
    private static String determineEventStatus(Event event) {
        if (event.getIsCancelled()) {
            return "CANCELLED";
        }
        if (event.isOngoing()) {
            return "ONGOING";
        }
        if (event.isUpcoming()) {
            return "UPCOMING";
        }
        if (event.hasEnded()) {
            return "PAST";
        }
        return "UNKNOWN";
    }

    /**
     * Convert Event entity to EventResponse DTO with statistics
     */
    public static EventResponse fromEntityWithStats(Event event,
                                                     Long totalRegistrations,
                                                     Long approvedRegistrations,
                                                     Long pendingRegistrations,
                                                     Long waitlistCount,
                                                     Long attendanceCount) {
        EventResponse response = fromEntity(event);
        if (response != null) {
            response.setTotalRegistrations(totalRegistrations);
            response.setApprovedRegistrations(approvedRegistrations);
            response.setPendingRegistrations(pendingRegistrations);
            response.setWaitlistCount(waitlistCount);
            response.setAttendanceCount(attendanceCount);
        }
        return response;
    }
}
