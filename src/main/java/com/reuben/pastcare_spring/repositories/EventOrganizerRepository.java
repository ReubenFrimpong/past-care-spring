package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.EventOrganizer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for EventOrganizer entity.
 * Provides custom queries for managing event organizers and their roles.
 */
@Repository
public interface EventOrganizerRepository extends JpaRepository<EventOrganizer, Long> {

    // Basic queries
    Optional<EventOrganizer> findByIdAndChurchIdAndDeletedAtIsNull(Long id, Long churchId);

    Page<EventOrganizer> findByChurchIdAndDeletedAtIsNull(Long churchId, Pageable pageable);

    // Organizers for an event
    List<EventOrganizer> findByEventAndDeletedAtIsNull(Event event);

    @Query("SELECT o FROM EventOrganizer o WHERE o.event.id = :eventId " +
           "AND o.deletedAt IS NULL " +
           "ORDER BY o.isPrimary DESC, o.createdAt ASC")
    List<EventOrganizer> findByEventId(@Param("eventId") Long eventId);

    @Query("SELECT o FROM EventOrganizer o WHERE o.event.id = :eventId " +
           "AND o.deletedAt IS NULL " +
           "ORDER BY o.isPrimary DESC, o.createdAt ASC")
    Page<EventOrganizer> findByEventId(
        @Param("eventId") Long eventId,
        Pageable pageable
    );

    // Primary organizer
    @Query("SELECT o FROM EventOrganizer o WHERE o.event.id = :eventId " +
           "AND o.isPrimary = true " +
           "AND o.deletedAt IS NULL")
    Optional<EventOrganizer> findPrimaryOrganizerByEventId(@Param("eventId") Long eventId);

    // Contact persons
    @Query("SELECT o FROM EventOrganizer o WHERE o.event.id = :eventId " +
           "AND o.isContactPerson = true " +
           "AND o.deletedAt IS NULL " +
           "ORDER BY o.isPrimary DESC")
    List<EventOrganizer> findContactPersonsByEventId(@Param("eventId") Long eventId);

    // Events organized by a member
    @Query("SELECT o FROM EventOrganizer o WHERE o.member.id = :memberId " +
           "AND o.deletedAt IS NULL " +
           "ORDER BY o.event.startDate DESC")
    List<EventOrganizer> findByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT o FROM EventOrganizer o WHERE o.member.id = :memberId " +
           "AND o.deletedAt IS NULL " +
           "ORDER BY o.event.startDate DESC")
    Page<EventOrganizer> findByMemberId(
        @Param("memberId") Long memberId,
        Pageable pageable
    );

    // Events where member is primary organizer
    @Query("SELECT o FROM EventOrganizer o WHERE o.member.id = :memberId " +
           "AND o.isPrimary = true " +
           "AND o.deletedAt IS NULL " +
           "ORDER BY o.event.startDate DESC")
    List<EventOrganizer> findPrimaryOrganizationsByMemberId(@Param("memberId") Long memberId);

    // Check if member is organizer for event
    @Query("SELECT o FROM EventOrganizer o WHERE o.event.id = :eventId " +
           "AND o.member.id = :memberId " +
           "AND o.deletedAt IS NULL")
    Optional<EventOrganizer> findByEventIdAndMemberId(
        @Param("eventId") Long eventId,
        @Param("memberId") Long memberId
    );

    boolean existsByEventAndMemberAndDeletedAtIsNull(
        Event event,
        com.reuben.pastcare_spring.models.Member member
    );

    // Filter by role
    @Query("SELECT o FROM EventOrganizer o WHERE o.event.id = :eventId " +
           "AND LOWER(o.role) LIKE LOWER(CONCAT('%', :role, '%')) " +
           "AND o.deletedAt IS NULL")
    List<EventOrganizer> findByEventIdAndRole(
        @Param("eventId") Long eventId,
        @Param("role") String role
    );

    // Statistics
    @Query("SELECT COUNT(o) FROM EventOrganizer o WHERE o.event.id = :eventId " +
           "AND o.deletedAt IS NULL")
    long countByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(o) FROM EventOrganizer o WHERE o.member.id = :memberId " +
           "AND o.deletedAt IS NULL")
    long countByMemberId(@Param("memberId") Long memberId);

    @Query("SELECT o.role, COUNT(o) FROM EventOrganizer o " +
           "WHERE o.church.id = :churchId " +
           "AND o.deletedAt IS NULL " +
           "GROUP BY o.role")
    List<Object[]> countOrganizersByRole(@Param("churchId") Long churchId);

    // Organizers for upcoming events
    @Query("SELECT o FROM EventOrganizer o WHERE o.member.id = :memberId " +
           "AND o.deletedAt IS NULL " +
           "AND o.event.isCancelled = false " +
           "AND o.event.startDate > CURRENT_TIMESTAMP " +
           "ORDER BY o.event.startDate ASC")
    List<EventOrganizer> findUpcomingEventsByMemberId(@Param("memberId") Long memberId);

    // Delete organizer by event and member
    @Query("UPDATE EventOrganizer o SET o.deletedAt = CURRENT_TIMESTAMP " +
           "WHERE o.event.id = :eventId AND o.member.id = :memberId")
    void softDeleteByEventIdAndMemberId(
        @Param("eventId") Long eventId,
        @Param("memberId") Long memberId
    );
}
