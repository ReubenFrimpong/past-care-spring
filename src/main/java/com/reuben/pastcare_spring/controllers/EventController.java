package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for event management.
 * Handles CRUD operations, filtering, search, and event-related operations.
 */
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final EventOrganizerService organizerService;
    private final EventTagService tagService;
    private final ImageService imageService;
    private final EventImageService eventImageService;
    private final CalendarExportService calendarExportService;
    private final EventReminderService reminderService;
    private final EventAnalyticsService analyticsService;
    private final RecurringEventService recurringEventService;
    private final EventAttendanceService eventAttendanceService;
    private final com.reuben.pastcare_spring.repositories.UserRepository userRepository;

    /**
     * Create a new event
     */
    @PostMapping
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<EventResponse> createEvent(
        @Valid @RequestBody EventRequest request,
        Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        EventResponse response = eventService.createEvent(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an event
     */
    @PutMapping("/{id}")
    @RequirePermission(Permission.EVENT_EDIT)
    public ResponseEntity<EventResponse> updateEvent(
        @PathVariable Long id,
        @Valid @RequestBody EventRequest request,
        Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        EventResponse response = eventService.updateEvent(id, request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get event by ID
     */
    @GetMapping("/{id}")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long id) {
        EventResponse response = eventService.getEvent(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all events (paginated)
     */
    @GetMapping
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Page<EventResponse>> getAllEvents(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "startDate") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EventResponse> response = eventService.getAllEvents(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get upcoming events
     */
    @GetMapping("/upcoming")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Page<EventResponse>> getUpcomingEvents(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Page<EventResponse> response = eventService.getUpcomingEvents(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get ongoing events
     */
    @GetMapping("/ongoing")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<List<EventResponse>> getOngoingEvents() {
        List<EventResponse> response = eventService.getOngoingEvents();
        return ResponseEntity.ok(response);
    }

    /**
     * Get past events
     */
    @GetMapping("/past")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Page<EventResponse>> getPastEvents(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<EventResponse> response = eventService.getPastEvents(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Search events
     */
    @GetMapping("/search")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Page<EventResponse>> searchEvents(
        @RequestParam String q,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").descending());
        Page<EventResponse> response = eventService.searchEvents(q, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Filter events
     */
    @GetMapping("/filter")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Page<EventResponse>> filterEvents(
        @RequestParam(required = false) EventType eventType,
        @RequestParam(required = false) EventVisibility visibility,
        @RequestParam(required = false) Long locationId,
        @RequestParam(required = false) Boolean requiresRegistration,
        @RequestParam(required = false) Boolean isCancelled,
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "startDate") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EventResponse> response = eventService.filterEvents(
            eventType, visibility, locationId, requiresRegistration, isCancelled, search, pageable
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel event
     */
    @PostMapping("/{id}/cancel")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<EventResponse> cancelEvent(
        @PathVariable Long id,
        @RequestParam String reason,
        Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        EventResponse response = eventService.cancelEvent(id, reason, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete event
     */
    @DeleteMapping("/{id}")
    @RequirePermission(Permission.EVENT_EDIT)
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get event statistics
     */
    @GetMapping("/stats")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<EventStatsResponse> getEventStats() {
        EventStatsResponse response = eventService.getEventStats();
        return ResponseEntity.ok(response);
    }

    // ==================== Organizer Operations ====================

    /**
     * Add organizer to event
     */
    @PostMapping("/{eventId}/organizers")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<EventOrganizerResponse> addOrganizer(
        @PathVariable Long eventId,
        @Valid @RequestBody EventOrganizerRequest request,
        Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        EventOrganizerResponse response = organizerService.addOrganizer(eventId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get event organizers
     */
    @GetMapping("/{eventId}/organizers")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<List<EventOrganizerResponse>> getEventOrganizers(@PathVariable Long eventId) {
        List<EventOrganizerResponse> response = organizerService.getEventOrganizers(eventId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update organizer
     */
    @PutMapping("/organizers/{organizerId}")
    @RequirePermission(Permission.EVENT_EDIT)
    public ResponseEntity<EventOrganizerResponse> updateOrganizer(
        @PathVariable Long organizerId,
        @Valid @RequestBody EventOrganizerRequest request
    ) {
        EventOrganizerResponse response = organizerService.updateOrganizer(organizerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove organizer
     */
    @DeleteMapping("/organizers/{organizerId}")
    @RequirePermission(Permission.EVENT_EDIT)
    public ResponseEntity<Void> removeOrganizer(@PathVariable Long organizerId) {
        organizerService.removeOrganizer(organizerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set as primary organizer
     */
    @PostMapping("/organizers/{organizerId}/set-primary")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<EventOrganizerResponse> setAsPrimaryOrganizer(@PathVariable Long organizerId) {
        EventOrganizerResponse response = organizerService.setAsPrimary(organizerId);
        return ResponseEntity.ok(response);
    }

    // ==================== Tag Operations ====================

    /**
     * Add tag to event
     */
    @PostMapping("/{eventId}/tags")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<Void> addTag(
        @PathVariable Long eventId,
        @Valid @RequestBody EventTagRequest request,
        Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        tagService.addTag(eventId, request.getTag(), request.getTagColor(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Get event tags
     */
    @GetMapping("/{eventId}/tags")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<List<String>> getEventTags(@PathVariable Long eventId) {
        List<String> response = tagService.getEventTags(eventId);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove tag from event
     */
    @DeleteMapping("/{eventId}/tags/{tag}")
    @RequirePermission(Permission.EVENT_EDIT)
    public ResponseEntity<Void> removeTag(
        @PathVariable Long eventId,
        @PathVariable String tag
    ) {
        tagService.removeTag(eventId, tag);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get all tags
     */
    @GetMapping("/tags/all")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<List<String>> getAllTags() {
        List<String> response = tagService.getAllTags();
        return ResponseEntity.ok(response);
    }

    /**
     * Search tags (autocomplete)
     */
    @GetMapping("/tags/search")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<List<String>> searchTags(@RequestParam String q) {
        List<String> response = tagService.searchTags(q);
        return ResponseEntity.ok(response);
    }

    /**
     * Find events by tag
     */
    @GetMapping("/tags/{tag}/events")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<List<EventResponse>> findEventsByTag(@PathVariable String tag) {
        List<EventResponse> response = tagService.findEventsByTag(tag);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload event image
     */
    @PostMapping("/{id}/upload-image")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<Map<String, String>> uploadEventImage(
        @PathVariable Long id,
        @RequestParam("image") MultipartFile image,
        Authentication authentication
    ) {
        try {
            // Get event
            EventResponse event = eventService.getEvent(id);

            // Upload image
            String imageUrl = imageService.uploadEventImage(image, event.getImageUrl());

            // Update event with image URL
            EventRequest updateRequest = EventRequest.builder()
                .name(event.getName())
                .description(event.getDescription())
                .eventType(event.getEventType())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .timezone(event.getTimezone())
                .locationType(event.getLocationType())
                .physicalLocation(event.getPhysicalLocation())
                .virtualLink(event.getVirtualLink())
                .virtualPlatform(event.getVirtualPlatform())
                .locationId(event.getLocationId())
                .requiresRegistration(event.getRequiresRegistration())
                .registrationDeadline(event.getRegistrationDeadline())
                .maxCapacity(event.getMaxCapacity())
                .allowWaitlist(event.getAllowWaitlist())
                .autoApproveRegistrations(event.getAutoApproveRegistrations())
                .visibility(event.getVisibility())
                .isRecurring(event.getIsRecurring())
                .recurrencePattern(event.getRecurrencePattern())
                .recurrenceEndDate(event.getRecurrenceEndDate())
                .parentEventId(event.getParentEventId())
                .primaryOrganizerId(event.getPrimaryOrganizerId())
                .notes(event.getNotes())
                .imageUrl(imageUrl)
                .reminderDaysBefore(event.getReminderDaysBefore())
                .build();

            Long userId = getUserIdFromAuth(authentication);
            eventService.updateEvent(id, updateRequest, userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Event image uploaded successfully");
            response.put("imageUrl", imageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // ==================== Calendar Export ====================

    /**
     * Export single event to iCal format
     */
    @GetMapping("/{id}/ical")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<String> exportEventToICal(@PathVariable Long id) {
        EventResponse eventResponse = eventService.getEvent(id);
        Event event = eventService.getEventEntity(id);

        String icalContent = calendarExportService.generateICalForEvent(event);

        return ResponseEntity.ok()
            .header("Content-Type", "text/calendar; charset=utf-8")
            .header("Content-Disposition", "attachment; filename=\"event-" + id + ".ics\"")
            .body(icalContent);
    }

    /**
     * Export all church events to iCal format
     */
    @GetMapping("/ical")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<String> exportAllEventsToICal(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found"));

        List<Event> events = eventService.getAllEventsForExport();
        String calendarName = user.getChurch().getName() + " Events";

        String icalContent = calendarExportService.generateICalForEvents(events, calendarName);

        return ResponseEntity.ok()
            .header("Content-Type", "text/calendar; charset=utf-8")
            .header("Content-Disposition", "attachment; filename=\"church-events.ics\"")
            .body(icalContent);
    }

    /**
     * Get Google Calendar export URL for an event
     */
    @GetMapping("/{id}/google-calendar-url")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Map<String, String>> getGoogleCalendarUrl(@PathVariable Long id) {
        Event event = eventService.getEventEntity(id);
        String googleCalendarUrl = calendarExportService.generateGoogleCalendarUrl(event);

        Map<String, String> response = new HashMap<>();
        response.put("url", googleCalendarUrl);
        response.put("eventName", event.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * Get embed code for public calendar
     */
    @GetMapping("/embed-code")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Map<String, String>> getEmbedCode(
        @RequestParam(defaultValue = "800") int width,
        @RequestParam(defaultValue = "600") int height,
        Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found"));

        String embedCode = calendarExportService.generateEmbedCode(user.getChurch().getId(), width, height);

        Map<String, String> response = new HashMap<>();
        response.put("embedCode", embedCode);
        response.put("width", String.valueOf(width));
        response.put("height", String.valueOf(height));

        return ResponseEntity.ok(response);
    }

    // ==================== Communication & Reminders ====================

    /**
     * Send reminders for a specific event
     */
    @PostMapping("/{id}/send-reminders")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<Map<String, String>> sendEventReminders(@PathVariable Long id) {
        Event event = eventService.getEventEntity(id);
        reminderService.sendEventReminders(event);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Reminders sent successfully");
        response.put("eventName", event.getName());

        return ResponseEntity.ok(response);
    }

    /**
     * Send invitations to specific members
     */
    @PostMapping("/{id}/send-invitations")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<Map<String, Object>> sendInvitations(
        @PathVariable Long id,
        @RequestBody Map<String, Object> request
    ) {
        @SuppressWarnings("unchecked")
        List<Long> memberIds = (List<Long>) request.get("memberIds");
        String personalMessage = (String) request.get("personalMessage");

        int sent = reminderService.sendEventInvitations(id, memberIds, personalMessage);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Invitations sent successfully");
        response.put("invitationsSent", sent);

        return ResponseEntity.ok(response);
    }

    /**
     * Send invitations to all members
     */
    @PostMapping("/{id}/send-invitations-all")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<Map<String, Object>> sendInvitationsToAll(
        @PathVariable Long id,
        @RequestBody(required = false) Map<String, String> request
    ) {
        String personalMessage = request != null ? request.get("personalMessage") : null;

        int sent = reminderService.sendEventInvitationsToAll(id, personalMessage);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Invitations sent to all members");
        response.put("invitationsSent", sent);

        return ResponseEntity.ok(response);
    }

    // ==================== Analytics ====================

    /**
     * Get detailed analytics for all events
     */
    @GetMapping("/analytics/overview")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Map<String, Object>> getAnalyticsOverview() {
        Map<String, Object> stats = analyticsService.getEventStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get detailed analytics for a specific event
     */
    @GetMapping("/{id}/analytics")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Map<String, Object>> getEventAnalytics(@PathVariable Long id) {
        Map<String, Object> stats = analyticsService.getEventStats(id);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get attendance analytics
     */
    @GetMapping("/analytics/attendance")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Map<String, Object>> getAttendanceAnalytics(
        @RequestParam String startDate,
        @RequestParam String endDate
    ) {
        java.time.LocalDateTime start = java.time.LocalDateTime.parse(startDate);
        java.time.LocalDateTime end = java.time.LocalDateTime.parse(endDate);

        Map<String, Object> analytics = analyticsService.getAttendanceAnalytics(start, end);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get event trends
     */
    @GetMapping("/analytics/trends")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Map<String, Object>> getEventTrends(
        @RequestParam(defaultValue = "6") int months
    ) {
        Map<String, Object> trends = analyticsService.getEventTrends(months);
        return ResponseEntity.ok(trends);
    }

    // ==================== Recurring Events ====================

    /**
     * Generate recurring event instances
     */
    @PostMapping("/{id}/generate-instances")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<Map<String, Object>> generateRecurringInstances(
        @PathVariable Long id,
        @RequestParam(required = false) Integer maxInstances
    ) {
        List<EventResponse> instances = recurringEventService.generateRecurringInstances(id, maxInstances);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Successfully generated " + instances.size() + " recurring instances");
        response.put("count", instances.size());
        response.put("instances", instances);

        return ResponseEntity.ok(response);
    }

    /**
     * Update future instances of a recurring event
     */
    @PutMapping("/{id}/update-future-instances")
    @RequirePermission(Permission.EVENT_EDIT)
    public ResponseEntity<Map<String, Object>> updateFutureInstances(
        @PathVariable Long id,
        @Valid @RequestBody EventRequest request
    ) {
        int updateCount = recurringEventService.updateFutureInstances(id, request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Successfully updated " + updateCount + " future instances");
        response.put("count", updateCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Delete future instances of a recurring event
     */
    @DeleteMapping("/{id}/delete-future-instances")
    @RequirePermission(Permission.EVENT_EDIT)
    public ResponseEntity<Map<String, Object>> deleteFutureInstances(@PathVariable Long id) {
        int deleteCount = recurringEventService.deleteFutureInstances(id);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Successfully deleted " + deleteCount + " future instances");
        response.put("count", deleteCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Get all instances of a recurring event
     */
    @GetMapping("/{id}/instances")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<Map<String, Object>> getRecurringInstances(@PathVariable Long id) {
        List<Event> instances = recurringEventService.getRecurringInstances(id);

        Map<String, Object> response = new HashMap<>();
        response.put("count", instances.size());
        response.put("instances", instances.stream()
            .map(event -> EventResponse.fromEntity(event))
            .collect(Collectors.toList()));

        return ResponseEntity.ok(response);
    }

    // ==================== Photo Gallery Endpoints ====================

    /**
     * Get all images for an event
     */
    @GetMapping("/{id}/images")
    public ResponseEntity<List<EventImageResponse>> getEventImages(@PathVariable Long id) {
        List<EventImageResponse> images = eventImageService.getEventImages(id);
        return ResponseEntity.ok(images);
    }

    /**
     * Upload image to event gallery
     */
    @PostMapping("/{id}/images")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<EventImageResponse> uploadEventGalleryImage(
        @PathVariable Long id,
        @RequestParam("image") MultipartFile image,
        @RequestParam(value = "caption", required = false) String caption,
        @RequestParam(value = "isCoverImage", required = false) Boolean isCoverImage,
        Authentication authentication
    ) {
        try {
            Long userId = getUserIdFromAuth(authentication);
            EventImageResponse response = eventImageService.uploadEventImage(
                id, image, caption, isCoverImage, userId
            );
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update event image details
     */
    @PutMapping("/{eventId}/images/{imageId}")
    @RequirePermission(Permission.EVENT_EDIT)
    public ResponseEntity<EventImageResponse> updateEventImage(
        @PathVariable Long eventId,
        @PathVariable Long imageId,
        @RequestBody EventImageRequest request
    ) {
        EventImageResponse response = eventImageService.updateEventImage(eventId, imageId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete event image
     */
    @DeleteMapping("/{eventId}/images/{imageId}")
    @RequirePermission(Permission.EVENT_EDIT)
    public ResponseEntity<Void> deleteEventImage(
        @PathVariable Long eventId,
        @PathVariable Long imageId
    ) {
        eventImageService.deleteEventImage(eventId, imageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reorder event images
     */
    @PutMapping("/{id}/images/reorder")
    @RequirePermission(Permission.EVENT_EDIT)
    public ResponseEntity<Void> reorderEventImages(
        @PathVariable Long id,
        @RequestBody List<Long> imageIds
    ) {
        eventImageService.reorderImages(id, imageIds);
        return ResponseEntity.ok().build();
    }

    // ==================== Event-Attendance Integration ====================

    /**
     * Create attendance session from an event
     */
    @PostMapping("/{id}/create-attendance-session")
    @RequirePermission(Permission.EVENT_CREATE)
    public ResponseEntity<AttendanceSessionResponse> createAttendanceSessionFromEvent(@PathVariable Long id) {
        AttendanceSessionResponse response = eventAttendanceService.createAttendanceSessionFromEvent(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get attendance sessions for an event
     */
    @GetMapping("/{id}/attendance-sessions")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<List<AttendanceSessionResponse>> getAttendanceSessionsForEvent(@PathVariable Long id) {
        List<AttendanceSessionResponse> response = eventAttendanceService.getAttendanceSessionsForEvent(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all events with attendance tracking enabled
     */
    @GetMapping("/with-attendance-tracking")
    @RequirePermission(Permission.EVENT_VIEW_ALL)
    public ResponseEntity<List<Event>> getEventsWithAttendanceTracking(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found"));

        List<Event> events = eventAttendanceService.getEventsWithAttendanceTracking(user.getChurch().getId());
        return ResponseEntity.ok(events);
    }

    // ==================== Helper Methods ====================

    private Long getUserIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getId();
    }
}
