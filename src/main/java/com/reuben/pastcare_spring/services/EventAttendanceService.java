package com.reuben.pastcare_spring.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reuben.pastcare_spring.dtos.AttendanceSessionResponse;
import com.reuben.pastcare_spring.enums.ServiceType;
import com.reuben.pastcare_spring.mapper.AttendanceSessionMapper;
import com.reuben.pastcare_spring.models.AttendanceSession;
import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.EventType;
import com.reuben.pastcare_spring.repositories.AttendanceSessionRepository;
import com.reuben.pastcare_spring.repositories.EventRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service for managing the integration between Events and Attendance modules.
 * Handles automatic generation of attendance sessions from events, especially recurring events.
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-31
 */
@Service
@RequiredArgsConstructor
public class EventAttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(EventAttendanceService.class);

    private final EventRepository eventRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;

    /**
     * Create an attendance session from an event.
     * Useful for tracking attendance at event-based services.
     *
     * @param eventId The event ID
     * @return Created attendance session
     */
    @Transactional
    public AttendanceSessionResponse createAttendanceSessionFromEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        // Check if attendance session already exists for this event
        if (attendanceSessionRepository.existsByEvent_Id(eventId)) {
            throw new IllegalStateException("Attendance session already exists for this event");
        }

        AttendanceSession session = new AttendanceSession();
        session.setEvent(event);
        session.setChurch(event.getChurch());
        session.setSessionName(event.getName());
        session.setSessionDate(event.getStartDate().toLocalDate());
        session.setSessionTime(event.getStartDate().toLocalTime());
        session.setNotes("Auto-generated from event: " + event.getName());
        session.setIsCompleted(false);

        // Map event type to service type
        session.setServiceType(mapEventTypeToServiceType(event.getEventType()));

        // Set check-in windows based on event times
        session.setCheckInOpensAt(event.getStartDate().minusHours(1)); // Open 1 hour before
        session.setCheckInClosesAt(event.getEndDate());

        // Set capacity if event has one
        if (event.getMaxCapacity() != null) {
            session.setMaxCapacity(event.getMaxCapacity());
        }

        AttendanceSession savedSession = attendanceSessionRepository.save(session);
        logger.info("Created attendance session {} from event {}", savedSession.getId(), eventId);

        return AttendanceSessionMapper.toAttendanceSessionResponse(savedSession);
    }

    /**
     * Auto-generate attendance sessions for all instances of a recurring event.
     * Called after recurring event instances are generated.
     *
     * @param parentEventId The parent recurring event ID
     * @return List of created attendance sessions
     */
    @Transactional
    public List<AttendanceSessionResponse> generateAttendanceSessionsForRecurringEvent(Long parentEventId) {
        Event parentEvent = eventRepository.findById(parentEventId)
            .orElseThrow(() -> new IllegalArgumentException("Parent event not found: " + parentEventId));

        if (!parentEvent.getIsRecurring()) {
            throw new IllegalArgumentException("Event is not a recurring event");
        }

        // Get all child event instances
        List<Event> childEvents = eventRepository.findByParentEventId(parentEventId);
        List<AttendanceSessionResponse> sessions = new ArrayList<>();

        for (Event childEvent : childEvents) {
            try {
                // Skip if attendance session already exists
                if (attendanceSessionRepository.existsByEvent_Id(childEvent.getId())) {
                    logger.debug("Attendance session already exists for event {}", childEvent.getId());
                    continue;
                }

                // Create attendance session for this event instance
                AttendanceSession session = new AttendanceSession();
                session.setEvent(childEvent);
                session.setChurch(childEvent.getChurch());
                session.setSessionName(childEvent.getName());
                session.setSessionDate(childEvent.getStartDate().toLocalDate());
                session.setSessionTime(childEvent.getStartDate().toLocalTime());
                session.setNotes("Auto-generated from recurring event");
                session.setIsCompleted(false);
                session.setServiceType(mapEventTypeToServiceType(childEvent.getEventType()));
                session.setCheckInOpensAt(childEvent.getStartDate().minusHours(1));
                session.setCheckInClosesAt(childEvent.getEndDate());

                if (childEvent.getMaxCapacity() != null) {
                    session.setMaxCapacity(childEvent.getMaxCapacity());
                }

                AttendanceSession savedSession = attendanceSessionRepository.save(session);
                sessions.add(AttendanceSessionMapper.toAttendanceSessionResponse(savedSession));

                logger.debug("Created attendance session for event instance {}", childEvent.getId());
            } catch (Exception e) {
                logger.error("Failed to create attendance session for event {}: {}",
                    childEvent.getId(), e.getMessage());
            }
        }

        logger.info("Generated {} attendance sessions for recurring event {}", sessions.size(), parentEventId);
        return sessions;
    }

    /**
     * Get attendance sessions for a specific event.
     *
     * @param eventId The event ID
     * @return List of attendance sessions
     */
    public List<AttendanceSessionResponse> getAttendanceSessionsForEvent(Long eventId) {
        List<AttendanceSession> sessions = attendanceSessionRepository.findByEvent_Id(eventId);
        return sessions.stream()
            .map(AttendanceSessionMapper::toAttendanceSessionResponse)
            .toList();
    }

    /**
     * Get all events that have linked attendance sessions.
     *
     * @param churchId The church ID
     * @return List of events with attendance tracking
     */
    public List<Event> getEventsWithAttendanceTracking(Long churchId) {
        return attendanceSessionRepository.findByChurch_IdAndEventIsNotNull(churchId)
            .stream()
            .map(AttendanceSession::getEvent)
            .distinct()
            .toList();
    }

    /**
     * Delete attendance session when event is deleted.
     *
     * @param eventId The event ID
     */
    @Transactional
    public void deleteAttendanceSessionsForEvent(Long eventId) {
        List<AttendanceSession> sessions = attendanceSessionRepository.findByEvent_Id(eventId);
        attendanceSessionRepository.deleteAll(sessions);
        logger.info("Deleted {} attendance sessions for event {}", sessions.size(), eventId);
    }

    /**
     * Map EventType to ServiceType for attendance tracking.
     */
    private ServiceType mapEventTypeToServiceType(EventType eventType) {
        return switch (eventType) {
            case SERVICE -> ServiceType.SUNDAY_MAIN_SERVICE;
            case PRAYER -> ServiceType.PRAYER_MEETING;
            case YOUTH -> ServiceType.YOUTH_SERVICE;
            default -> ServiceType.SPECIAL_EVENT;
        };
    }

    /**
     * Check if an event should automatically have attendance tracking.
     * Service-type events get automatic attendance tracking.
     *
     * @param event The event
     * @return true if attendance tracking should be enabled
     */
    public boolean shouldEnableAttendanceTracking(Event event) {
        return event.getEventType() == EventType.SERVICE ||
               event.getEventType() == EventType.PRAYER ||
               event.getEventType() == EventType.YOUTH;
    }
}
