package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.EventType;
import com.reuben.pastcare_spring.models.EventVisibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Event entity.
 * Provides custom queries for event management, filtering, and searching.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Basic queries
    Optional<Event> findByIdAndChurchIdAndDeletedAtIsNull(Long id, Long churchId);

    Page<Event> findByChurchIdAndDeletedAtIsNull(Long churchId, Pageable pageable);

    List<Event> findByChurchIdAndDeletedAtIsNull(Long churchId);

    // Upcoming events
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.isCancelled = false " +
           "AND e.startDate > :now " +
           "ORDER BY e.startDate ASC")
    List<Event> findUpcomingEvents(
        @Param("churchId") Long churchId,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.isCancelled = false " +
           "AND e.startDate > :now " +
           "ORDER BY e.startDate ASC")
    Page<Event> findUpcomingEvents(
        @Param("churchId") Long churchId,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    // Ongoing events
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.isCancelled = false " +
           "AND e.startDate <= :now " +
           "AND e.endDate >= :now " +
           "ORDER BY e.startDate ASC")
    List<Event> findOngoingEvents(
        @Param("churchId") Long churchId,
        @Param("now") LocalDateTime now
    );

    // Past events
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.endDate < :now " +
           "ORDER BY e.startDate DESC")
    Page<Event> findPastEvents(
        @Param("churchId") Long churchId,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    // Filter by event type
    Page<Event> findByChurchIdAndEventTypeAndDeletedAtIsNull(
        Long churchId,
        EventType eventType,
        Pageable pageable
    );

    // Filter by visibility
    Page<Event> findByChurchIdAndVisibilityAndDeletedAtIsNull(
        Long churchId,
        EventVisibility visibility,
        Pageable pageable
    );

    // Filter by date range
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.startDate >= :startDate " +
           "AND e.endDate <= :endDate " +
           "ORDER BY e.startDate ASC")
    List<Event> findEventsByDateRange(
        @Param("churchId") Long churchId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.startDate >= :startDate " +
           "AND e.endDate <= :endDate " +
           "ORDER BY e.startDate ASC")
    Page<Event> findEventsByDateRange(
        @Param("churchId") Long churchId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );

    // Search by name or description
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY e.startDate DESC")
    Page<Event> searchEvents(
        @Param("churchId") Long churchId,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    // Recurring events
    List<Event> findByChurchIdAndIsRecurringAndDeletedAtIsNull(
        Long churchId,
        Boolean isRecurring
    );

    List<Event> findByParentEventAndDeletedAtIsNull(Event parentEvent);

    // Find instances by parent event ID
    List<Event> findByParentEventId(Long parentEventId);

    // Find future instances by parent event ID
    List<Event> findByParentEventIdAndStartDateAfter(Long parentEventId, LocalDateTime startDate);

    // Count instances by parent event ID
    long countByParentEventId(Long parentEventId);

    // Events requiring registration
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.requiresRegistration = true " +
           "AND e.isCancelled = false " +
           "AND e.startDate > :now " +
           "AND (e.registrationDeadline IS NULL OR e.registrationDeadline > :now) " +
           "ORDER BY e.startDate ASC")
    List<Event> findEventsWithOpenRegistration(
        @Param("churchId") Long churchId,
        @Param("now") LocalDateTime now
    );

    // Events by location
    Page<Event> findByChurchIdAndLocationIdAndDeletedAtIsNull(
        Long churchId,
        Long locationId,
        Pageable pageable
    );

    // Events by organizer
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.primaryOrganizer.id = :memberId " +
           "ORDER BY e.startDate DESC")
    Page<Event> findEventsByOrganizer(
        @Param("churchId") Long churchId,
        @Param("memberId") Long memberId,
        Pageable pageable
    );

    // Cancelled events
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.isCancelled = true " +
           "ORDER BY e.cancelledAt DESC")
    Page<Event> findCancelledEvents(
        @Param("churchId") Long churchId,
        Pageable pageable
    );

    // Events needing reminders
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.isCancelled = false " +
           "AND e.reminderSent = false " +
           "AND e.startDate > :now " +
           "AND e.startDate <= :reminderThreshold")
    List<Event> findEventsNeedingReminders(
        @Param("churchId") Long churchId,
        @Param("now") LocalDateTime now,
        @Param("reminderThreshold") LocalDateTime reminderThreshold
    );

    // Statistics queries
    @Query("SELECT COUNT(e) FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL")
    long countByChurchId(@Param("churchId") Long churchId);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.isCancelled = false " +
           "AND e.startDate > :now")
    long countUpcomingEvents(
        @Param("churchId") Long churchId,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT COUNT(e) FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND e.endDate < :now")
    long countPastEvents(
        @Param("churchId") Long churchId,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT e.eventType, COUNT(e) FROM Event e " +
           "WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "GROUP BY e.eventType")
    List<Object[]> countEventsByType(@Param("churchId") Long churchId);

    // Advanced search with multiple filters
    @Query("SELECT e FROM Event e WHERE e.church.id = :churchId " +
           "AND e.deletedAt IS NULL " +
           "AND (:eventType IS NULL OR e.eventType = :eventType) " +
           "AND (:visibility IS NULL OR e.visibility = :visibility) " +
           "AND (:locationId IS NULL OR e.location.id = :locationId) " +
           "AND (:requiresRegistration IS NULL OR e.requiresRegistration = :requiresRegistration) " +
           "AND (:isCancelled IS NULL OR e.isCancelled = :isCancelled) " +
           "AND (:searchTerm IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "ORDER BY e.startDate DESC")
    Page<Event> findEventsWithFilters(
        @Param("churchId") Long churchId,
        @Param("eventType") EventType eventType,
        @Param("visibility") EventVisibility visibility,
        @Param("locationId") Long locationId,
        @Param("requiresRegistration") Boolean requiresRegistration,
        @Param("isCancelled") Boolean isCancelled,
        @Param("searchTerm") String searchTerm,
        Pageable pageable
    );

    /**
     * Count events in a time period (for goal tracking)
     * Dashboard Phase 2.3: Goal Tracking
     */
    @Query("SELECT COUNT(e) FROM Event e WHERE e.deletedAt IS NULL " +
           "AND e.createdAt >= :startDate AND e.createdAt <= :endDate")
    Long countEventsInPeriod(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
