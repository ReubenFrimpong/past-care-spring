package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.ComplaintActivity;
import com.reuben.pastcare_spring.models.ComplaintActivity.ActivityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for ComplaintActivity entity.
 * Provides audit trail and activity tracking for complaints.
 */
@Repository
public interface ComplaintActivityRepository extends JpaRepository<ComplaintActivity, Long> {

    /**
     * Find all activities for a specific complaint.
     */
    List<ComplaintActivity> findByComplaintIdOrderByPerformedAtDesc(Long complaintId);

    /**
     * Find all activities for a complaint that are visible to complainant.
     */
    List<ComplaintActivity> findByComplaintIdAndVisibleToComplainantTrueOrderByPerformedAtDesc(Long complaintId);

    /**
     * Find activities by complaint and church ID (multi-tenant safety).
     */
    List<ComplaintActivity> findByComplaintIdAndChurchIdOrderByPerformedAtDesc(Long complaintId, Long churchId);

    /**
     * Find activities by church ID.
     */
    List<ComplaintActivity> findByChurchIdOrderByPerformedAtDesc(Long churchId);

    /**
     * Find activities by type for a church.
     */
    List<ComplaintActivity> findByChurchIdAndActivityTypeOrderByPerformedAtDesc(Long churchId, ActivityType activityType);

    /**
     * Find activities performed by a specific user.
     */
    List<ComplaintActivity> findByChurchIdAndPerformedByIdOrderByPerformedAtDesc(Long churchId, Long userId);

    /**
     * Find activities within a date range for a church.
     */
    @Query("SELECT a FROM ComplaintActivity a WHERE a.church.id = :churchId AND a.performedAt BETWEEN :startDate AND :endDate ORDER BY a.performedAt DESC")
    List<ComplaintActivity> findByChurchIdAndPerformedAtBetween(@Param("churchId") Long churchId,
                                                                 @Param("startDate") LocalDateTime startDate,
                                                                 @Param("endDate") LocalDateTime endDate);

    /**
     * Count activities for a complaint.
     */
    Long countByComplaintId(Long complaintId);

    /**
     * Count activities for a church.
     */
    Long countByChurchId(Long churchId);

    /**
     * Get recent activities for a church (last N days).
     */
    @Query("SELECT a FROM ComplaintActivity a WHERE a.church.id = :churchId AND a.performedAt >= :since ORDER BY a.performedAt DESC")
    List<ComplaintActivity> findRecentByChurchId(@Param("churchId") Long churchId, @Param("since") LocalDateTime since);

    /**
     * Delete all activities for a complaint.
     */
    void deleteByComplaintId(Long complaintId);

    /**
     * Delete all activities for a church.
     */
    void deleteByChurchId(Long churchId);
}
