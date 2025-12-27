package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.EventRegistration;
import com.reuben.pastcare_spring.models.RegistrationStatus;
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
 * Repository for EventRegistration entity.
 * Provides custom queries for registration management, approval workflow, and attendance tracking.
 */
@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    // Basic queries
    Optional<EventRegistration> findByIdAndChurchIdAndDeletedAtIsNull(Long id, Long churchId);

    Page<EventRegistration> findByChurchIdAndDeletedAtIsNull(Long churchId, Pageable pageable);

    // Registrations for an event
    Page<EventRegistration> findByEventAndDeletedAtIsNull(Event event, Pageable pageable);

    List<EventRegistration> findByEventAndDeletedAtIsNull(Event event);

    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.registrationDate DESC")
    List<EventRegistration> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.registrationDate DESC")
    Page<EventRegistration> findByEventId(
        @Param("eventId") Long eventId,
        Pageable pageable
    );

    // Filter by status
    List<EventRegistration> findByEventAndStatusAndDeletedAtIsNull(
        Event event,
        RegistrationStatus status
    );

    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.status = :status " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.registrationDate ASC")
    List<EventRegistration> findByEventIdAndStatus(
        @Param("eventId") Long eventId,
        @Param("status") RegistrationStatus status
    );

    // Pending approvals
    @Query("SELECT r FROM EventRegistration r WHERE r.church.id = :churchId " +
           "AND r.status = 'PENDING' " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.registrationDate ASC")
    List<EventRegistration> findPendingApprovals(@Param("churchId") Long churchId);

    @Query("SELECT r FROM EventRegistration r WHERE r.church.id = :churchId " +
           "AND r.status = 'PENDING' " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.registrationDate ASC")
    Page<EventRegistration> findPendingApprovals(
        @Param("churchId") Long churchId,
        Pageable pageable
    );

    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.status = 'PENDING' " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.registrationDate ASC")
    List<EventRegistration> findPendingApprovalsByEvent(@Param("eventId") Long eventId);

    // Waitlist
    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.isOnWaitlist = true " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.waitlistPosition ASC")
    List<EventRegistration> findWaitlist(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.isOnWaitlist = true " +
           "AND r.deletedAt IS NULL")
    long countWaitlist(@Param("eventId") Long eventId);

    // Member's registrations
    @Query("SELECT r FROM EventRegistration r WHERE r.member.id = :memberId " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.registrationDate DESC")
    List<EventRegistration> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT r FROM EventRegistration r WHERE r.member.id = :memberId " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.registrationDate DESC")
    Page<EventRegistration> findByMemberId(
        @Param("memberId") Long memberId,
        Pageable pageable
    );

    // Check if member is registered for event
    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.member.id = :memberId " +
           "AND r.deletedAt IS NULL " +
           "AND r.isCancelled = false")
    Optional<EventRegistration> findByEventIdAndMemberId(
        @Param("eventId") Long eventId,
        @Param("memberId") Long memberId
    );

    boolean existsByEventAndMemberAndIsCancelledFalseAndDeletedAtIsNull(
        Event event,
        com.reuben.pastcare_spring.models.Member member
    );

    // Attendance tracking
    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.attended = true " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.checkInTime ASC")
    List<EventRegistration> findAttendeesForEvent(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.attended = true " +
           "AND r.deletedAt IS NULL")
    long countAttendeesForEvent(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.status = 'NO_SHOW' " +
           "AND r.deletedAt IS NULL")
    long countNoShowsForEvent(@Param("eventId") Long eventId);

    // Active registrations count
    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.deletedAt IS NULL " +
           "AND r.isCancelled = false " +
           "AND r.status IN ('PENDING', 'APPROVED')")
    long countActiveRegistrations(@Param("eventId") Long eventId);

    // Guest registrations
    @Query("SELECT r FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.isGuest = true " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.registrationDate DESC")
    List<EventRegistration> findGuestRegistrations(@Param("eventId") Long eventId);

    // Registrations needing confirmation
    @Query("SELECT r FROM EventRegistration r WHERE r.church.id = :churchId " +
           "AND r.status = 'APPROVED' " +
           "AND r.confirmationSent = false " +
           "AND r.deletedAt IS NULL " +
           "ORDER BY r.approvedAt ASC")
    List<EventRegistration> findRegistrationsNeedingConfirmation(
        @Param("churchId") Long churchId
    );

    // Registrations needing reminders
    @Query("SELECT r FROM EventRegistration r WHERE r.church.id = :churchId " +
           "AND r.status = 'APPROVED' " +
           "AND r.reminderSent = false " +
           "AND r.event.startDate > :now " +
           "AND r.event.startDate <= :reminderThreshold " +
           "AND r.deletedAt IS NULL")
    List<EventRegistration> findRegistrationsNeedingReminders(
        @Param("churchId") Long churchId,
        @Param("now") LocalDateTime now,
        @Param("reminderThreshold") LocalDateTime reminderThreshold
    );

    // Statistics
    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.church.id = :churchId " +
           "AND r.deletedAt IS NULL")
    long countByChurchId(@Param("churchId") Long churchId);

    @Query("SELECT COUNT(r) FROM EventRegistration r WHERE r.event.id = :eventId " +
           "AND r.deletedAt IS NULL")
    long countByEventId(@Param("eventId") Long eventId);

    @Query("SELECT r.status, COUNT(r) FROM EventRegistration r " +
           "WHERE r.event.id = :eventId " +
           "AND r.deletedAt IS NULL " +
           "GROUP BY r.status")
    List<Object[]> countRegistrationsByStatus(@Param("eventId") Long eventId);

    @Query("SELECT SUM(r.numberOfGuests) FROM EventRegistration r " +
           "WHERE r.event.id = :eventId " +
           "AND r.deletedAt IS NULL " +
           "AND r.isCancelled = false " +
           "AND r.status IN ('PENDING', 'APPROVED', 'ATTENDED')")
    Long sumGuestsByEvent(@Param("eventId") Long eventId);

    // Advanced search
    @Query("SELECT r FROM EventRegistration r WHERE r.church.id = :churchId " +
           "AND r.deletedAt IS NULL " +
           "AND (:eventId IS NULL OR r.event.id = :eventId) " +
           "AND (:memberId IS NULL OR r.member.id = :memberId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:isOnWaitlist IS NULL OR r.isOnWaitlist = :isOnWaitlist) " +
           "AND (:attended IS NULL OR r.attended = :attended) " +
           "ORDER BY r.registrationDate DESC")
    Page<EventRegistration> findRegistrationsWithFilters(
        @Param("churchId") Long churchId,
        @Param("eventId") Long eventId,
        @Param("memberId") Long memberId,
        @Param("status") RegistrationStatus status,
        @Param("isOnWaitlist") Boolean isOnWaitlist,
        @Param("attended") Boolean attended,
        Pageable pageable
    );
}
