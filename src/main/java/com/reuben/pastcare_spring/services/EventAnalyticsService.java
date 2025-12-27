package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.EventType;
import com.reuben.pastcare_spring.models.RegistrationStatus;
import com.reuben.pastcare_spring.repositories.*;
import com.reuben.pastcare_spring.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for event analytics and statistics.
 * Provides insights into event attendance, registrations, and trends.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventAnalyticsService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;

    /**
     * Get comprehensive event statistics for a church
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEventStats() {
        Long churchId = TenantContext.getCurrentChurchId();
        LocalDateTime now = LocalDateTime.now();

        Map<String, Object> stats = new HashMap<>();

        // Total events
        long totalEvents = eventRepository.countByChurchId(churchId);
        stats.put("totalEvents", totalEvents);

        // Upcoming events
        long upcomingEvents = eventRepository.countUpcomingEvents(churchId, now);
        stats.put("upcomingEvents", upcomingEvents);

        // Past events
        long pastEvents = eventRepository.countPastEvents(churchId, now);
        stats.put("pastEvents", pastEvents);

        // Events by type
        List<Object[]> eventsByType = eventRepository.countEventsByType(churchId);
        Map<String, Long> typeBreakdown = new HashMap<>();
        for (Object[] row : eventsByType) {
            EventType type = (EventType) row[0];
            Long count = (Long) row[1];
            typeBreakdown.put(type.name(), count);
        }
        stats.put("eventsByType", typeBreakdown);

        // Registration statistics
        long totalRegistrations = registrationRepository.countByChurchId(churchId);
        stats.put("totalRegistrations", totalRegistrations);

        return stats;
    }

    /**
     * Get statistics for a specific event
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEventStats(Long eventId) {
        Long churchId = TenantContext.getCurrentChurchId();

        Event event = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Map<String, Object> stats = new HashMap<>();

        // Basic event info
        stats.put("eventId", event.getId());
        stats.put("eventName", event.getName());
        stats.put("eventType", event.getEventType());

        // Registration counts
        long totalRegistrations = registrationRepository.countByEventId(eventId);
        stats.put("totalRegistrations", totalRegistrations);

        // Registrations by status
        List<Object[]> byStatus = registrationRepository.countRegistrationsByStatus(eventId);
        Map<String, Long> statusBreakdown = new HashMap<>();
        for (Object[] row : byStatus) {
            RegistrationStatus status = (RegistrationStatus) row[0];
            Long count = (Long) row[1];
            statusBreakdown.put(status.name(), count);
        }
        stats.put("registrationsByStatus", statusBreakdown);

        // Attendance stats
        long attendedCount = registrationRepository.countAttendeesForEvent(eventId);
        stats.put("attendedCount", attendedCount);

        long noShowCount = registrationRepository.countNoShowsForEvent(eventId);
        stats.put("noShowCount", noShowCount);

        // Capacity info
        if (event.getMaxCapacity() != null) {
            stats.put("maxCapacity", event.getMaxCapacity());
            stats.put("currentRegistrations", event.getCurrentRegistrations());
            stats.put("availableCapacity", event.getAvailableCapacity());
            stats.put("capacityPercentage",
                (double) event.getCurrentRegistrations() / event.getMaxCapacity() * 100);
        }

        // Waitlist info
        if (event.getAllowWaitlist()) {
            long waitlistCount = registrationRepository.countWaitlist(eventId);
            stats.put("waitlistCount", waitlistCount);
        }

        // Guest statistics
        Long totalGuests = registrationRepository.sumGuestsByEvent(eventId);
        stats.put("totalGuests", totalGuests != null ? totalGuests : 0);

        return stats;
    }

    /**
     * Get attendance rate for past events
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAttendanceAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        Long churchId = TenantContext.getCurrentChurchId();

        Map<String, Object> analytics = new HashMap<>();

        // Get past events in date range
        List<Event> events = eventRepository.findEventsByDateRange(churchId, startDate, endDate);

        int totalEvents = events.size();
        long totalRegistrations = 0;
        long totalAttended = 0;
        long totalNoShows = 0;

        for (Event event : events) {
            long eventRegs = registrationRepository.countByEventId(event.getId());
            long attended = registrationRepository.countAttendeesForEvent(event.getId());
            long noShows = registrationRepository.countNoShowsForEvent(event.getId());

            totalRegistrations += eventRegs;
            totalAttended += attended;
            totalNoShows += noShows;
        }

        analytics.put("totalEvents", totalEvents);
        analytics.put("totalRegistrations", totalRegistrations);
        analytics.put("totalAttended", totalAttended);
        analytics.put("totalNoShows", totalNoShows);

        if (totalRegistrations > 0) {
            double attendanceRate = (double) totalAttended / totalRegistrations * 100;
            analytics.put("attendanceRate", Math.round(attendanceRate * 100.0) / 100.0);
        } else {
            analytics.put("attendanceRate", 0.0);
        }

        return analytics;
    }

    /**
     * Get event trends over time
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getEventTrends(int months) {
        Long churchId = TenantContext.getCurrentChurchId();
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(months);

        Map<String, Object> trends = new HashMap<>();

        List<Event> events = eventRepository.findEventsByDateRange(churchId, startDate, endDate);

        trends.put("totalEvents", events.size());
        trends.put("periodMonths", months);
        trends.put("averageEventsPerMonth", events.size() / (double) months);

        return trends;
    }
}
