package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import com.reuben.pastcare_spring.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing events.
 * Handles CRUD operations, filtering, search, and business logic for events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final EventOrganizerRepository organizerRepository;
    private final EventTagRepository tagRepository;
    private final ChurchRepository churchRepository;
    private final LocationRepository locationRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    /**
     * Create a new event
     */
    @Transactional
    public EventResponse createEvent(EventRequest request, Long userId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Creating event '{}' for church {}", request.getName(), churchId);

        // Get entities
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found"));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Build event
        Event event = Event.builder()
            .church(church)
            .name(request.getName())
            .description(request.getDescription())
            .eventType(request.getEventType())
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .timezone(request.getTimezone() != null ? request.getTimezone() : "Africa/Nairobi")
            .locationType(request.getLocationType())
            .physicalLocation(request.getPhysicalLocation())
            .virtualLink(request.getVirtualLink())
            .virtualPlatform(request.getVirtualPlatform())
            .requiresRegistration(request.getRequiresRegistration())
            .registrationDeadline(request.getRegistrationDeadline())
            .maxCapacity(request.getMaxCapacity())
            .allowWaitlist(request.getAllowWaitlist())
            .autoApproveRegistrations(request.getAutoApproveRegistrations())
            .visibility(request.getVisibility())
            .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
            .recurrencePattern(request.getRecurrencePattern())
            .recurrenceEndDate(request.getRecurrenceEndDate())
            .notes(request.getNotes())
            .imageUrl(request.getImageUrl())
            .reminderDaysBefore(request.getReminderDaysBefore())
            .createdBy(user)
            .build();

        // Set location if provided
        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
            event.setLocation(location);
        }

        // Set primary organizer if provided
        if (request.getPrimaryOrganizerId() != null) {
            Member organizer = memberRepository.findById(request.getPrimaryOrganizerId())
                .orElseThrow(() -> new IllegalArgumentException("Primary organizer not found"));
            event.setPrimaryOrganizer(organizer);
        }

        // Set parent event if provided
        if (request.getParentEventId() != null) {
            Event parentEvent = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(
                request.getParentEventId(), churchId)
                .orElseThrow(() -> new IllegalArgumentException("Parent event not found"));
            event.setParentEvent(parentEvent);
        }

        // Save event
        event = eventRepository.save(event);
        log.info("Event created with ID: {}", event.getId());

        // Add organizers if provided
        if (request.getOrganizers() != null && !request.getOrganizers().isEmpty()) {
            for (EventOrganizerRequest orgRequest : request.getOrganizers()) {
                addOrganizerToEvent(event.getId(), orgRequest, userId);
            }
        }

        // Add tags if provided
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            for (String tag : request.getTags()) {
                addTagToEvent(event.getId(), tag, null, userId);
            }
        }

        return EventResponse.fromEntity(event);
    }

    /**
     * Update an existing event
     */
    @Transactional
    public EventResponse updateEvent(Long eventId, EventRequest request, Long userId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Updating event {} for church {}", eventId, churchId);

        Event event = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update fields
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setEventType(request.getEventType());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setTimezone(request.getTimezone() != null ? request.getTimezone() : "Africa/Nairobi");
        event.setLocationType(request.getLocationType());
        event.setPhysicalLocation(request.getPhysicalLocation());
        event.setVirtualLink(request.getVirtualLink());
        event.setVirtualPlatform(request.getVirtualPlatform());
        event.setRequiresRegistration(request.getRequiresRegistration());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setMaxCapacity(request.getMaxCapacity());
        event.setAllowWaitlist(request.getAllowWaitlist());
        event.setAutoApproveRegistrations(request.getAutoApproveRegistrations());
        event.setVisibility(request.getVisibility());
        event.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);
        event.setRecurrencePattern(request.getRecurrencePattern());
        event.setRecurrenceEndDate(request.getRecurrenceEndDate());
        event.setNotes(request.getNotes());
        event.setImageUrl(request.getImageUrl());
        event.setReminderDaysBefore(request.getReminderDaysBefore());
        event.setUpdatedBy(user);

        // Update location
        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
            event.setLocation(location);
        } else {
            event.setLocation(null);
        }

        // Update primary organizer
        if (request.getPrimaryOrganizerId() != null) {
            Member organizer = memberRepository.findById(request.getPrimaryOrganizerId())
                .orElseThrow(() -> new IllegalArgumentException("Primary organizer not found"));
            event.setPrimaryOrganizer(organizer);
        } else {
            event.setPrimaryOrganizer(null);
        }

        event = eventRepository.save(event);
        log.info("Event {} updated successfully", eventId);

        return EventResponse.fromEntity(event);
    }

    /**
     * Get event by ID
     */
    @Transactional(readOnly = true)
    public EventResponse getEvent(Long eventId) {
        Long churchId = TenantContext.getCurrentChurchId();
        Event event = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Get statistics
        Long totalRegistrations = registrationRepository.countByEventId(eventId);
        Long approvedRegistrations = (long) registrationRepository.findByEventIdAndStatus(eventId, RegistrationStatus.APPROVED).size();
        Long pendingRegistrations = (long) registrationRepository.findByEventIdAndStatus(eventId, RegistrationStatus.PENDING).size();
        Long waitlistCount = registrationRepository.countWaitlist(eventId);
        Long attendanceCount = registrationRepository.countAttendeesForEvent(eventId);

        return EventResponse.fromEntityWithStats(event, totalRegistrations, approvedRegistrations,
            pendingRegistrations, waitlistCount, attendanceCount);
    }

    /**
     * Get all events with pagination
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getAllEvents(Pageable pageable) {
        Long churchId = TenantContext.getCurrentChurchId();
        return eventRepository.findByChurchIdAndDeletedAtIsNull(churchId, pageable)
            .map(EventResponse::fromEntity);
    }

    /**
     * Get upcoming events
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getUpcomingEvents(Pageable pageable) {
        Long churchId = TenantContext.getCurrentChurchId();
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findUpcomingEvents(churchId, now, pageable)
            .map(EventResponse::fromEntity);
    }

    /**
     * Get ongoing events
     */
    @Transactional(readOnly = true)
    public List<EventResponse> getOngoingEvents() {
        Long churchId = TenantContext.getCurrentChurchId();
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findOngoingEvents(churchId, now).stream()
            .map(EventResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get past events
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> getPastEvents(Pageable pageable) {
        Long churchId = TenantContext.getCurrentChurchId();
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findPastEvents(churchId, now, pageable)
            .map(EventResponse::fromEntity);
    }

    /**
     * Search events
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> searchEvents(String searchTerm, Pageable pageable) {
        Long churchId = TenantContext.getCurrentChurchId();
        return eventRepository.searchEvents(churchId, searchTerm, pageable)
            .map(EventResponse::fromEntity);
    }

    /**
     * Filter events with multiple criteria
     */
    @Transactional(readOnly = true)
    public Page<EventResponse> filterEvents(
        EventType eventType,
        EventVisibility visibility,
        Long locationId,
        Boolean requiresRegistration,
        Boolean isCancelled,
        String searchTerm,
        Pageable pageable
    ) {
        Long churchId = TenantContext.getCurrentChurchId();
        return eventRepository.findEventsWithFilters(
            churchId, eventType, visibility, locationId, requiresRegistration, isCancelled, searchTerm, pageable
        ).map(EventResponse::fromEntity);
    }

    /**
     * Cancel event
     */
    @Transactional
    public EventResponse cancelEvent(Long eventId, String reason, Long userId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Cancelling event {} for church {}", eventId, churchId);

        Event event = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        event.cancel(reason, user);
        event = eventRepository.save(event);

        log.info("Event {} cancelled successfully", eventId);
        return EventResponse.fromEntity(event);
    }

    /**
     * Delete event (soft delete)
     */
    @Transactional
    public void deleteEvent(Long eventId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Deleting event {} for church {}", eventId, churchId);

        Event event = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        eventRepository.delete(event);
        log.info("Event {} deleted successfully", eventId);
    }

    /**
     * Get event statistics
     */
    @Transactional(readOnly = true)
    public EventStatsResponse getEventStats() {
        Long churchId = TenantContext.getCurrentChurchId();
        LocalDateTime now = LocalDateTime.now();

        EventStatsResponse stats = EventStatsResponse.builder()
            .totalEvents(eventRepository.countByChurchId(churchId))
            .upcomingEvents(eventRepository.countUpcomingEvents(churchId, now))
            .pastEvents(eventRepository.countPastEvents(churchId, now))
            .totalRegistrations(registrationRepository.countByChurchId(churchId))
            .build();

        // Get event type breakdown
        List<Object[]> typeResults = eventRepository.countEventsByType(churchId);
        stats.setEventsByType(typeResults.stream()
            .collect(Collectors.toMap(
                r -> ((EventType) r[0]).getDisplayName(),
                r -> (Long) r[1]
            )));

        return stats;
    }

    /**
     * Add organizer to event
     */
    @Transactional
    public void addOrganizerToEvent(Long eventId, EventOrganizerRequest request, Long userId) {
        Long churchId = TenantContext.getCurrentChurchId();

        Event event = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if already an organizer
        if (organizerRepository.existsByEventAndMemberAndDeletedAtIsNull(event, member)) {
            throw new IllegalArgumentException("Member is already an organizer for this event");
        }

        EventOrganizer organizer = EventOrganizer.builder()
            .church(event.getChurch())
            .event(event)
            .member(member)
            .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
            .role(request.getRole())
            .isContactPerson(request.getIsContactPerson() != null ? request.getIsContactPerson() : false)
            .contactEmail(request.getContactEmail())
            .contactPhone(request.getContactPhone())
            .responsibilities(request.getResponsibilities())
            .createdBy(user)
            .build();

        organizerRepository.save(organizer);
        log.info("Organizer {} added to event {}", member.getId(), eventId);
    }

    /**
     * Add tag to event
     */
    @Transactional
    public void addTagToEvent(Long eventId, String tag, String tagColor, Long userId) {
        Long churchId = TenantContext.getCurrentChurchId();

        Event event = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if tag already exists
        if (tagRepository.existsByEventIdAndTag(eventId, tag)) {
            throw new IllegalArgumentException("Tag already exists for this event");
        }

        EventTag eventTag = EventTag.builder()
            .church(event.getChurch())
            .event(event)
            .tag(tag.trim().toLowerCase())
            .tagColor(tagColor != null ? tagColor : "#3B82F6")
            .createdBy(user)
            .build();

        tagRepository.save(eventTag);
        log.info("Tag '{}' added to event {}", tag, eventId);
    }

    /**
     * Remove tag from event
     */
    @Transactional
    public void removeTagFromEvent(Long eventId, String tag) {
        Long churchId = TenantContext.getCurrentChurchId();

        // Verify event exists
        eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        tagRepository.deleteByEventIdAndTag(eventId, tag);
        log.info("Tag '{}' removed from event {}", tag, eventId);
    }

    /**
     * Get all tags for a church
     */
    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        Long churchId = TenantContext.getCurrentChurchId();
        return tagRepository.findDistinctTagsByChurchId(churchId);
    }

    /**
     * Find events by tag
     */
    @Transactional(readOnly = true)
    public List<EventResponse> findEventsByTag(String tag) {
        Long churchId = TenantContext.getCurrentChurchId();
        return tagRepository.findEventsByTag(churchId, tag).stream()
            .map(EventResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get event entity (for internal use)
     */
    @Transactional(readOnly = true)
    public Event getEventEntity(Long eventId) {
        Long churchId = TenantContext.getCurrentChurchId();
        return eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));
    }

    /**
     * Get all events for export (active events only)
     */
    @Transactional(readOnly = true)
    public List<Event> getAllEventsForExport() {
        Long churchId = TenantContext.getCurrentChurchId();
        List<Event> events = eventRepository.findByChurchIdAndDeletedAtIsNull(churchId);
        // Sort by start date
        events.sort((e1, e2) -> e1.getStartDate().compareTo(e2.getStartDate()));
        return events;
    }
}
