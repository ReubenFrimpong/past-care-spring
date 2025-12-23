package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for CommunicationLog entity.
 * Manages communication history with members (calls, emails, visits, etc.).
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@Repository
public interface CommunicationLogRepository extends JpaRepository<CommunicationLog, Long> {

  /**
   * Find all communication logs for a specific member.
   */
  List<CommunicationLog> findByMemberOrderByCommunicationDateDesc(Member member);

  /**
   * Find all communication logs for a member with pagination.
   */
  Page<CommunicationLog> findByMember(Member member, Pageable pageable);

  /**
   * Find all communication logs for a church.
   */
  Page<CommunicationLog> findByChurch(Church church, Pageable pageable);

  /**
   * Find communication logs by type for a church.
   */
  Page<CommunicationLog> findByChurchAndCommunicationType(
    Church church,
    CommunicationType communicationType,
    Pageable pageable
  );

  /**
   * Find communication logs within a date range.
   */
  @Query("SELECT cl FROM CommunicationLog cl WHERE cl.church = :church AND cl.communicationDate BETWEEN :startDate AND :endDate ORDER BY cl.communicationDate DESC")
  List<CommunicationLog> findByChurchAndCommunicationDateBetween(
    @Param("church") Church church,
    @Param("startDate") LocalDateTime startDate,
    @Param("endDate") LocalDateTime endDate
  );

  /**
   * Find communication logs requiring follow-up.
   */
  @Query("SELECT cl FROM CommunicationLog cl WHERE cl.church = :church AND cl.followUpRequired = true AND cl.followUpStatus IN ('PENDING', 'IN_PROGRESS', 'OVERDUE') ORDER BY cl.followUpDate ASC")
  List<CommunicationLog> findByChurchAndFollowUpRequired(@Param("church") Church church);

  /**
   * Find overdue follow-ups.
   */
  @Query("SELECT cl FROM CommunicationLog cl WHERE cl.church = :church AND cl.followUpRequired = true AND cl.followUpDate < :now AND cl.followUpStatus != 'COMPLETED' ORDER BY cl.followUpDate ASC")
  List<CommunicationLog> findOverdueFollowUps(@Param("church") Church church, @Param("now") LocalDateTime now);

  /**
   * Find communication logs by user (pastor/leader).
   */
  Page<CommunicationLog> findByUser(User user, Pageable pageable);

  /**
   * Find communication logs by priority.
   */
  Page<CommunicationLog> findByChurchAndPriority(
    Church church,
    CommunicationPriority priority,
    Pageable pageable
  );

  /**
   * Find confidential communication logs (role-based access).
   */
  Page<CommunicationLog> findByChurchAndIsConfidential(
    Church church,
    Boolean isConfidential,
    Pageable pageable
  );

  /**
   * Count communication logs by type for a church.
   */
  Long countByChurchAndCommunicationType(Church church, CommunicationType communicationType);

  /**
   * Count total communications for a member.
   */
  Long countByMember(Member member);

  /**
   * Find recent communications for a member (last N days).
   */
  @Query("SELECT cl FROM CommunicationLog cl WHERE cl.member = :member AND cl.communicationDate >= :since ORDER BY cl.communicationDate DESC")
  List<CommunicationLog> findRecentCommunications(
    @Param("member") Member member,
    @Param("since") LocalDateTime since
  );

}
