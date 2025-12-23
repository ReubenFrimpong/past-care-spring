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
 * Repository for ConfidentialNote entity.
 * Manages confidential notes about members with role-based access control.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@Repository
public interface ConfidentialNoteRepository extends JpaRepository<ConfidentialNote, Long> {

  /**
   * Find all confidential notes for a specific member.
   * Excludes archived notes by default.
   */
  @Query("SELECT cn FROM ConfidentialNote cn WHERE cn.member = :member AND cn.isArchived = false ORDER BY cn.createdAt DESC")
  List<ConfidentialNote> findByMember(@Param("member") Member member);

  /**
   * Find all confidential notes for a member with pagination.
   */
  Page<ConfidentialNote> findByMemberAndIsArchived(Member member, Boolean isArchived, Pageable pageable);

  /**
   * Find all confidential notes for a church (non-archived).
   */
  Page<ConfidentialNote> findByChurchAndIsArchived(Church church, Boolean isArchived, Pageable pageable);

  /**
   * Find confidential notes by category.
   */
  Page<ConfidentialNote> findByChurchAndCategoryAndIsArchived(
    Church church,
    ConfidentialNoteCategory category,
    Boolean isArchived,
    Pageable pageable
  );

  /**
   * Find confidential notes created by a specific user.
   */
  Page<ConfidentialNote> findByCreatedByAndIsArchived(User user, Boolean isArchived, Pageable pageable);

  /**
   * Find confidential notes requiring follow-up.
   */
  @Query("SELECT cn FROM ConfidentialNote cn WHERE cn.church = :church AND cn.requiresFollowUp = true AND cn.followUpStatus IN ('PENDING', 'IN_PROGRESS', 'OVERDUE') AND cn.isArchived = false ORDER BY cn.followUpDate ASC")
  List<ConfidentialNote> findByChurchAndRequiresFollowUp(@Param("church") Church church);

  /**
   * Find overdue follow-ups for confidential notes.
   */
  @Query("SELECT cn FROM ConfidentialNote cn WHERE cn.church = :church AND cn.requiresFollowUp = true AND cn.followUpDate < :now AND cn.followUpStatus != 'COMPLETED' AND cn.isArchived = false ORDER BY cn.followUpDate ASC")
  List<ConfidentialNote> findOverdueFollowUps(@Param("church") Church church, @Param("now") LocalDateTime now);

  /**
   * Find high priority confidential notes.
   */
  @Query("SELECT cn FROM ConfidentialNote cn WHERE cn.church = :church AND cn.priority IN ('HIGH', 'URGENT') AND cn.isArchived = false ORDER BY cn.priority DESC, cn.createdAt DESC")
  List<ConfidentialNote> findHighPriorityNotes(@Param("church") Church church);

  /**
   * Find confidential notes by priority.
   */
  Page<ConfidentialNote> findByChurchAndPriorityAndIsArchived(
    Church church,
    CommunicationPriority priority,
    Boolean isArchived,
    Pageable pageable
  );

  /**
   * Count confidential notes for a member.
   */
  Long countByMemberAndIsArchived(Member member, Boolean isArchived);

  /**
   * Count confidential notes by category for a church.
   */
  Long countByChurchAndCategoryAndIsArchived(Church church, ConfidentialNoteCategory category, Boolean isArchived);

  /**
   * Search confidential notes by subject or tags.
   */
  @Query("SELECT cn FROM ConfidentialNote cn WHERE cn.church = :church AND cn.isArchived = false AND " +
         "(LOWER(cn.subject) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
         "LOWER(cn.tags) LIKE LOWER(CONCAT('%', :search, '%')))")
  Page<ConfidentialNote> searchNotes(
    @Param("church") Church church,
    @Param("search") String search,
    Pageable pageable
  );

}
