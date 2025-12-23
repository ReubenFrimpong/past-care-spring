package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.PrayerRequest;
import com.reuben.pastcare_spring.models.PrayerRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PrayerRequestRepository extends JpaRepository<PrayerRequest, Long> {

    /**
     * Find all prayer requests for a church
     */
    List<PrayerRequest> findByChurchIdOrderByCreatedAtDesc(Long churchId);

    /**
     * Find prayer requests by member
     */
    List<PrayerRequest> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * Find prayer requests by status for a church
     */
    List<PrayerRequest> findByChurchIdAndStatusOrderByCreatedAtDesc(Long churchId, PrayerRequestStatus status);

    /**
     * Find public (non-anonymous) active prayer requests
     */
    @Query("SELECT p FROM PrayerRequest p WHERE p.churchId = :churchId AND p.isPublic = true AND p.status = :status ORDER BY p.createdAt DESC")
    List<PrayerRequest> findPublicPrayerRequests(@Param("churchId") Long churchId, @Param("status") PrayerRequestStatus status);

    /**
     * Find urgent prayer requests
     */
    List<PrayerRequest> findByChurchIdAndIsUrgentTrueOrderByCreatedAtDesc(Long churchId);

    /**
     * Find answered prayer requests (testimonies)
     */
    List<PrayerRequest> findByChurchIdAndStatusAndTestimonyIsNotNullOrderByAnsweredAtDesc(Long churchId, PrayerRequestStatus status);

    /**
     * Find expired prayer requests for archiving
     */
    @Query("SELECT p FROM PrayerRequest p WHERE p.expiresAt < :now AND p.status != :status")
    List<PrayerRequest> findExpiredPrayerRequests(@Param("now") LocalDateTime now, @Param("status") PrayerRequestStatus status);

    /**
     * Count active prayer requests for a church
     */
    Long countByChurchIdAndStatus(Long churchId, PrayerRequestStatus status);
}
