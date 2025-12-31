package com.reuben.pastcare_spring.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reuben.pastcare_spring.dtos.AttendanceSessionResponse;
import com.reuben.pastcare_spring.dtos.EventRequest;
import com.reuben.pastcare_spring.dtos.EventResponse;
import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.RecurrencePattern;
import com.reuben.pastcare_spring.repositories.EventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing recurring events
 * Handles generation of event instances from recurrence patterns
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringEventService {

    private final EventRepository eventRepository;
    private final EventService eventService;
    private final EventAttendanceService eventAttendanceService;

    /**
     * Generate recurring event instances from a parent event
     *
     * @param parentEventId The ID of the parent recurring event
     * @param maxInstances Maximum number of instances to generate (default: 52 for weekly events, 365 days worth)
     * @return List of created event instances
     */
    @Transactional
    public List<EventResponse> generateRecurringInstances(Long parentEventId, Integer maxInstances) {
        Event parentEvent = eventRepository.findById(parentEventId)
            .orElseThrow(() -> new IllegalArgumentException("Parent event not found"));

        if (!parentEvent.getIsRecurring() || parentEvent.getRecurrencePattern() == null) {
            throw new IllegalArgumentException("Event is not a recurring event");
        }

        List<EventResponse> instances = new ArrayList<>();
        LocalDateTime currentStart = parentEvent.getStartDate();
        LocalDateTime currentEnd = parentEvent.getEndDate();
        LocalDate recurrenceEndDate = parentEvent.getRecurrenceEndDate();
        int instanceCount = 0;
        int maxCount = maxInstances != null ? maxInstances : 52;

        // Generate instances until we hit the recurrence end date or max instances
        while (instanceCount < maxCount) {
            currentStart = calculateNextOccurrence(currentStart, parentEvent.getRecurrencePattern());
            currentEnd = calculateNextOccurrence(currentEnd, parentEvent.getRecurrencePattern());

            // Check if we've passed the recurrence end date
            if (recurrenceEndDate != null && currentStart.toLocalDate().isAfter(recurrenceEndDate)) {
                break;
            }

            // Check if we're too far in the future (max 2 years)
            if (currentStart.isAfter(LocalDateTime.now().plusYears(2))) {
                break;
            }

            // Create instance
            EventRequest instanceRequest = buildInstanceRequest(parentEvent, currentStart, currentEnd);

            try {
                // Use the created_by user ID from the parent event
                Long userId = parentEvent.getCreatedBy() != null ? parentEvent.getCreatedBy().getId() : null;
                EventResponse instance = eventService.createEvent(instanceRequest, userId);
                instances.add(instance);
                instanceCount++;
                log.info("Created recurring instance {} for parent event {}", instance.getId(), parentEventId);
            } catch (Exception e) {
                log.error("Failed to create recurring instance: {}", e.getMessage());
                break;
            }
        }

        log.info("Generated {} recurring instances for event {}", instances.size(), parentEventId);

        // Auto-generate attendance sessions if this is a service-type event
        try {
            if (eventAttendanceService.shouldEnableAttendanceTracking(parentEvent)) {
                List<AttendanceSessionResponse> attendanceSessions =
                    eventAttendanceService.generateAttendanceSessionsForRecurringEvent(parentEventId);
                log.info("Auto-generated {} attendance sessions for recurring event {}",
                    attendanceSessions.size(), parentEventId);
            }
        } catch (Exception e) {
            log.error("Failed to auto-generate attendance sessions: {}", e.getMessage());
            // Don't fail the whole operation if attendance generation fails
        }

        return instances;
    }

    /**
     * Calculate the next occurrence date based on recurrence pattern
     */
    private LocalDateTime calculateNextOccurrence(LocalDateTime current, RecurrencePattern pattern) {
        return switch (pattern) {
            case DAILY -> current.plusDays(1);
            case WEEKLY -> current.plusWeeks(1);
            case BI_WEEKLY -> current.plusWeeks(2);
            case MONTHLY -> current.plusMonths(1);
            case QUARTERLY -> current.plusMonths(3);
            case YEARLY -> current.plusYears(1);
            case CUSTOM -> current.plusWeeks(1); // Default for custom
        };
    }

    /**
     * Build an EventRequest for a recurring instance
     */
    private EventRequest buildInstanceRequest(Event parentEvent, LocalDateTime startDate, LocalDateTime endDate) {
        return EventRequest.builder()
            .name(parentEvent.getName())
            .description(parentEvent.getDescription())
            .eventType(parentEvent.getEventType())
            .startDate(startDate)
            .endDate(endDate)
            .timezone(parentEvent.getTimezone())
            .locationType(parentEvent.getLocationType())
            .locationId(parentEvent.getLocation() != null ? parentEvent.getLocation().getId() : null)
            .physicalLocation(parentEvent.getPhysicalLocation())
            .virtualLink(parentEvent.getVirtualLink())
            .virtualPlatform(parentEvent.getVirtualPlatform())
            .requiresRegistration(parentEvent.getRequiresRegistration())
            .registrationDeadline(parentEvent.getRegistrationDeadline())
            .maxCapacity(parentEvent.getMaxCapacity())
            .allowWaitlist(parentEvent.getAllowWaitlist())
            .autoApproveRegistrations(parentEvent.getAutoApproveRegistrations())
            .visibility(parentEvent.getVisibility())
            .isRecurring(false) // Instances are not themselves recurring
            .recurrencePattern(null)
            .recurrenceEndDate(null)
            .parentEventId(parentEvent.getId())
            .primaryOrganizerId(parentEvent.getPrimaryOrganizer() != null ? parentEvent.getPrimaryOrganizer().getId() : null)
            .notes(parentEvent.getNotes())
            .reminderDaysBefore(parentEvent.getReminderDaysBefore())
            .build();
    }

    /**
     * Update all future instances of a recurring event
     *
     * @param parentEventId The parent event ID
     * @param updates The updates to apply
     * @return Number of instances updated
     */
    @Transactional
    public int updateFutureInstances(Long parentEventId, EventRequest updates) {
        List<Event> futureInstances = eventRepository.findByParentEventIdAndStartDateAfter(
            parentEventId,
            LocalDateTime.now()
        );

        int updateCount = 0;
        for (Event instance : futureInstances) {
            try {
                // Calculate the time offset between original start and current instance start
                Event parentEvent = eventRepository.findById(parentEventId)
                    .orElseThrow(() -> new IllegalArgumentException("Parent event not found"));

                long daysBetween = ChronoUnit.DAYS.between(
                    parentEvent.getStartDate().toLocalDate(),
                    instance.getStartDate().toLocalDate()
                );

                // Apply updates while maintaining the date offset
                LocalDateTime newStart = updates.getStartDate().plusDays(daysBetween);
                long duration = ChronoUnit.MINUTES.between(updates.getStartDate(), updates.getEndDate());
                LocalDateTime newEnd = newStart.plusMinutes(duration);

                EventRequest instanceUpdate = EventRequest.builder()
                    .name(updates.getName())
                    .description(updates.getDescription())
                    .eventType(updates.getEventType())
                    .startDate(newStart)
                    .endDate(newEnd)
                    .timezone(updates.getTimezone())
                    .locationType(updates.getLocationType())
                    .locationId(updates.getLocationId())
                    .physicalLocation(updates.getPhysicalLocation())
                    .virtualLink(updates.getVirtualLink())
                    .virtualPlatform(updates.getVirtualPlatform())
                    .requiresRegistration(updates.getRequiresRegistration())
                    .registrationDeadline(updates.getRegistrationDeadline())
                    .maxCapacity(updates.getMaxCapacity())
                    .allowWaitlist(updates.getAllowWaitlist())
                    .autoApproveRegistrations(updates.getAutoApproveRegistrations())
                    .visibility(updates.getVisibility())
                    .isRecurring(false)
                    .recurrencePattern(null)
                    .recurrenceEndDate(null)
                    .parentEventId(parentEventId)
                    .primaryOrganizerId(updates.getPrimaryOrganizerId())
                    .notes(updates.getNotes())
                    .reminderDaysBefore(updates.getReminderDaysBefore())
                    .build();

                // Use the updated_by user ID from the parent event
                Long userId = parentEvent.getUpdatedBy() != null ? parentEvent.getUpdatedBy().getId() : null;
                eventService.updateEvent(instance.getId(), instanceUpdate, userId);
                updateCount++;
            } catch (Exception e) {
                log.error("Failed to update instance {}: {}", instance.getId(), e.getMessage());
            }
        }

        log.info("Updated {} future instances for parent event {}", updateCount, parentEventId);
        return updateCount;
    }

    /**
     * Delete all future instances of a recurring event
     *
     * @param parentEventId The parent event ID
     * @return Number of instances deleted
     */
    @Transactional
    public int deleteFutureInstances(Long parentEventId) {
        List<Event> futureInstances = eventRepository.findByParentEventIdAndStartDateAfter(
            parentEventId,
            LocalDateTime.now()
        );

        int deleteCount = 0;
        for (Event instance : futureInstances) {
            try {
                eventService.deleteEvent(instance.getId());
                deleteCount++;
            } catch (Exception e) {
                log.error("Failed to delete instance {}: {}", instance.getId(), e.getMessage());
            }
        }

        log.info("Deleted {} future instances for parent event {}", deleteCount, parentEventId);
        return deleteCount;
    }

    /**
     * Get all instances of a recurring event
     *
     * @param parentEventId The parent event ID
     * @return List of event instances
     */
    public List<Event> getRecurringInstances(Long parentEventId) {
        return eventRepository.findByParentEventId(parentEventId);
    }

    /**
     * Get count of instances for a recurring event
     */
    public long getInstanceCount(Long parentEventId) {
        return eventRepository.countByParentEventId(parentEventId);
    }
}
