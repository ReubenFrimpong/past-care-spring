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
    @Query("SELECT p FROM PrayerRequest p WHERE p.church.id = :churchId ORDER BY p.createdAt DESC")
    List<PrayerRequest> findByChurchIdOrderByCreatedAtDesc(@Param("churchId") Long churchId);

    /**
     * Find prayer requests by member
     */
    List<PrayerRequest> findByMemberIdOrderByCreatedAtDesc(Long memberId);

    /**
     * Find prayer requests by status for a church
     */
    @Query("SELECT p FROM PrayerRequest p WHERE p.church.id = :churchId AND p.status = :status ORDER BY p.createdAt DESC")
    List<PrayerRequest> findByChurchIdAndStatusOrderByCreatedAtDesc(@Param("churchId") Long churchId, @Param("status") PrayerRequestStatus status);

    /**
     * Find public (non-anonymous) active prayer requests
     */
    @Query("SELECT p FROM PrayerRequest p WHERE p.church.id = :churchId AND p.isPublic = true AND p.status = :status ORDER BY p.createdAt DESC")
    List<PrayerRequest> findPublicPrayerRequests(@Param("churchId") Long churchId, @Param("status") PrayerRequestStatus status);

    /**
     * Find urgent prayer requests
     */
    @Query("SELECT p FROM PrayerRequest p WHERE p.church.id = :churchId AND p.isUrgent = true ORDER BY p.createdAt DESC")
    List<PrayerRequest> findByChurchIdAndIsUrgentTrueOrderByCreatedAtDesc(@Param("churchId") Long churchId);

    /**
     * Find answered prayer requests (testimonies)
     */
    @Query("SELECT p FROM PrayerRequest p WHERE p.church.id = :churchId AND p.status = :status AND p.testimony IS NOT NULL ORDER BY p.answeredAt DESC")
    List<PrayerRequest> findByChurchIdAndStatusAndTestimonyIsNotNullOrderByAnsweredAtDesc(@Param("churchId") Long churchId, @Param("status") PrayerRequestStatus status);

    /**
     * Find expired prayer requests for archiving
     */
    @Query("SELECT p FROM PrayerRequest p WHERE p.expiresAt < :now AND p.status != :status")
    List<PrayerRequest> findExpiredPrayerRequests(@Param("now") LocalDateTime now, @Param("status") PrayerRequestStatus status);

    /**
     * Count active prayer requests for a church
     */
    @Query("SELECT COUNT(p) FROM PrayerRequest p WHERE p.church.id = :churchId AND p.status = :status")
    Long countByChurchIdAndStatus(@Param("churchId") Long churchId, @Param("status") PrayerRequestStatus status);
}
