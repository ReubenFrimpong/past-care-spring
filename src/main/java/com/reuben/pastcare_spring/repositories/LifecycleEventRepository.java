package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.LifecycleEvent;
import com.reuben.pastcare_spring.models.LifecycleEventType;
import com.reuben.pastcare_spring.models.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for LifecycleEvent entity.
 * Manages lifecycle events like baptisms, confirmations, memberships, etc.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@Repository
public interface LifecycleEventRepository extends JpaRepository<LifecycleEvent, Long> {

  /**
   * Find all lifecycle events for a specific member.
   */
  List<LifecycleEvent> findByMemberOrderByEventDateDesc(Member member);

  /**
   * Find all lifecycle events for a member with pagination.
   */
  Page<LifecycleEvent> findByMember(Member member, Pageable pageable);

  /**
   * Find all lifecycle events for a church.
   */
  Page<LifecycleEvent> findByChurch(Church church, Pageable pageable);

  /**
   * Find lifecycle events by type for a church.
   */
  Page<LifecycleEvent> findByChurchAndEventType(Church church, LifecycleEventType eventType, Pageable pageable);

  /**
   * Find lifecycle events within a date range for a church.
   */
  @Query("SELECT le FROM LifecycleEvent le WHERE le.church = :church AND le.eventDate BETWEEN :startDate AND :endDate ORDER BY le.eventDate DESC")
  List<LifecycleEvent> findByChurchAndEventDateBetween(
    @Param("church") Church church,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
  );

  /**
   * Find verified lifecycle events.
   */
  Page<LifecycleEvent> findByChurchAndIsVerified(Church church, Boolean isVerified, Pageable pageable);

  /**
   * Count lifecycle events by type for a church.
   */
  Long countByChurchAndEventType(Church church, LifecycleEventType eventType);

  /**
   * Find lifecycle events by member and event type.
   */
  List<LifecycleEvent> findByMemberAndEventType(Member member, LifecycleEventType eventType);

  /**
   * Check if a member has a specific lifecycle event.
   */
  Boolean existsByMemberAndEventType(Member member, LifecycleEventType eventType);

}
