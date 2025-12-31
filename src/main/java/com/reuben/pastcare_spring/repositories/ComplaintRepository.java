package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Complaint;
import com.reuben.pastcare_spring.models.Complaint.ComplaintCategory;
import com.reuben.pastcare_spring.models.Complaint.ComplaintPriority;
import com.reuben.pastcare_spring.models.Complaint.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Complaint entity.
 * Provides data access methods with multi-tenant support.
 */
@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    /**
     * Find all complaints for a specific church.
     */
    List<Complaint> findByChurchId(Long churchId);

    /**
     * Find complaints by church ID and status.
     */
    List<Complaint> findByChurchIdAndStatus(Long churchId, ComplaintStatus status);

    /**
     * Find complaints by church ID and category.
     */
    List<Complaint> findByChurchIdAndCategory(Long churchId, ComplaintCategory category);

    /**
     * Find complaints by church ID and priority.
     */
    List<Complaint> findByChurchIdAndPriority(Long churchId, ComplaintPriority priority);

    /**
     * Find complaints submitted by a specific user.
     */
    List<Complaint> findByChurchIdAndSubmittedById(Long churchId, Long userId);

    /**
     * Find complaints assigned to a specific user.
     */
    List<Complaint> findByChurchIdAndAssignedToId(Long churchId, Long userId);

    /**
     * Find a complaint by ID and church ID (multi-tenant safety).
     */
    Optional<Complaint> findByIdAndChurchId(Long id, Long churchId);

    /**
     * Find unassigned complaints for a church.
     */
    @Query("SELECT c FROM Complaint c WHERE c.church.id = :churchId AND c.assignedTo IS NULL")
    List<Complaint> findUnassignedByChurchId(@Param("churchId") Long churchId);

    /**
     * Find complaints by status list (for multiple status filtering).
     */
    @Query("SELECT c FROM Complaint c WHERE c.church.id = :churchId AND c.status IN :statuses")
    List<Complaint> findByChurchIdAndStatuses(@Param("churchId") Long churchId, @Param("statuses") List<ComplaintStatus> statuses);

    /**
     * Find complaints submitted within a date range.
     */
    @Query("SELECT c FROM Complaint c WHERE c.church.id = :churchId AND c.submittedAt BETWEEN :startDate AND :endDate")
    List<Complaint> findByChurchIdAndSubmittedAtBetween(@Param("churchId") Long churchId,
                                                         @Param("startDate") LocalDateTime startDate,
                                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Count complaints by church ID and status.
     */
    Long countByChurchIdAndStatus(Long churchId, ComplaintStatus status);

    /**
     * Count complaints by church ID and priority.
     */
    Long countByChurchIdAndPriority(Long churchId, ComplaintPriority priority);

    /**
     * Count total complaints for a church.
     */
    Long countByChurchId(Long churchId);

    /**
     * Get statistics for a church's complaints.
     */
    @Query("SELECT new map(" +
           "COUNT(c) as total, " +
           "SUM(CASE WHEN c.status = 'SUBMITTED' THEN 1 ELSE 0 END) as submitted, " +
           "SUM(CASE WHEN c.status = 'UNDER_REVIEW' THEN 1 ELSE 0 END) as underReview, " +
           "SUM(CASE WHEN c.status = 'IN_PROGRESS' THEN 1 ELSE 0 END) as inProgress, " +
           "SUM(CASE WHEN c.status = 'RESOLVED' THEN 1 ELSE 0 END) as resolved, " +
           "SUM(CASE WHEN c.priority = 'URGENT' THEN 1 ELSE 0 END) as urgent) " +
           "FROM Complaint c WHERE c.church.id = :churchId")
    Object getStatsByChurchId(@Param("churchId") Long churchId);

    /**
     * Find recent complaints (last N days).
     */
    @Query("SELECT c FROM Complaint c WHERE c.church.id = :churchId AND c.submittedAt >= :since ORDER BY c.submittedAt DESC")
    List<Complaint> findRecentByChurchId(@Param("churchId") Long churchId, @Param("since") LocalDateTime since);

    /**
     * Search complaints by subject or description.
     */
    @Query("SELECT c FROM Complaint c WHERE c.church.id = :churchId AND " +
           "(LOWER(c.subject) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Complaint> searchByChurchId(@Param("churchId") Long churchId, @Param("searchTerm") String searchTerm);

    /**
     * Delete all complaints for a church (for cleanup/testing).
     */
    void deleteByChurchId(Long churchId);
}
