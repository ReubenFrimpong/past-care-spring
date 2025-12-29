package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.PrayerCategory;
import com.reuben.pastcare_spring.models.PrayerPriority;
import com.reuben.pastcare_spring.models.PrayerRequest;
import com.reuben.pastcare_spring.models.PrayerRequestStatus;
import com.reuben.pastcare_spring.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PrayerRequestRepository extends JpaRepository<PrayerRequest, Long> {

    // Find by church
    Page<PrayerRequest> findByChurch(Church church, Pageable pageable);

    // Find by church and status
    Page<PrayerRequest> findByChurchAndStatus(Church church, PrayerRequestStatus status, Pageable pageable);

    // Find by church and category
    Page<PrayerRequest> findByChurchAndCategory(Church church, PrayerCategory category, Pageable pageable);

    // Find by church and priority
    Page<PrayerRequest> findByChurchAndPriority(Church church, PrayerPriority priority, Pageable pageable);

    // Find by member
    List<PrayerRequest> findByMemberOrderByCreatedAtDesc(Member member);

    // Find by submitted by user
    Page<PrayerRequest> findByChurchAndSubmittedBy(Church church, User user, Pageable pageable);

    // Find active prayer requests (ACTIVE status)
    @Query("SELECT p FROM PrayerRequest p WHERE p.church = :church AND p.status = 'ACTIVE' AND p.isPublic = true ORDER BY p.createdAt DESC")
    List<PrayerRequest> findActivePrayerRequests(@Param("church") Church church);

    // Find urgent prayer requests
    @Query("SELECT p FROM PrayerRequest p WHERE p.church = :church AND p.isUrgent = true AND (p.status = 'PENDING' OR p.status = 'ACTIVE') ORDER BY p.createdAt DESC")
    List<PrayerRequest> findUrgentPrayerRequests(@Param("church") Church church);

    // Find answered prayer requests
    @Query("SELECT p FROM PrayerRequest p WHERE p.church = :church AND p.status = 'ANSWERED' ORDER BY p.answeredDate DESC")
    Page<PrayerRequest> findAnsweredPrayerRequests(@Param("church") Church church, Pageable pageable);

    // Find expiring soon (within next 7 days)
    @Query("SELECT p FROM PrayerRequest p WHERE p.church = :church AND p.expirationDate BETWEEN :today AND :nextWeek AND (p.status = 'PENDING' OR p.status = 'ACTIVE') ORDER BY p.expirationDate ASC")
    List<PrayerRequest> findExpiringSoon(@Param("church") Church church, @Param("today") LocalDate today, @Param("nextWeek") LocalDate nextWeek);

    // Find expired prayer requests
    @Query("SELECT p FROM PrayerRequest p WHERE p.church = :church AND p.expirationDate < :currentDate AND p.status != 'ANSWERED' AND p.status != 'ARCHIVED' ORDER BY p.expirationDate DESC")
    List<PrayerRequest> findExpiredPrayerRequests(@Param("church") Church church, @Param("currentDate") LocalDate currentDate);

    // Count by church
    Long countByChurch(Church church);

    // Count by church and status
    Long countByChurchAndStatus(Church church, PrayerRequestStatus status);

    // Count by church and category
    Long countByChurchAndCategory(Church church, PrayerCategory category);

    // Count urgent prayer requests
    @Query("SELECT COUNT(p) FROM PrayerRequest p WHERE p.church = :church AND p.isUrgent = true AND (p.status = 'PENDING' OR p.status = 'ACTIVE')")
    Long countUrgent(@Param("church") Church church);

    // Count active public prayer requests
    @Query("SELECT COUNT(p) FROM PrayerRequest p WHERE p.church = :church AND p.status = 'ACTIVE' AND p.isPublic = true")
    Long countActivePublic(@Param("church") Church church);

    // Search prayer requests by title or description
    @Query("SELECT p FROM PrayerRequest p WHERE p.church = :church AND (LOWER(p.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.tags) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<PrayerRequest> searchPrayerRequests(@Param("church") Church church, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Find public prayer requests (for member portal)
    @Query("SELECT p FROM PrayerRequest p WHERE p.church = :church AND p.isPublic = true AND (p.status = 'ACTIVE' OR p.status = 'ANSWERED') ORDER BY p.createdAt DESC")
    Page<PrayerRequest> findPublicPrayerRequests(@Param("church") Church church, Pageable pageable);

    // Find by church and status in list
    List<PrayerRequest> findByChurchAndStatusIn(Church church, List<PrayerRequestStatus> statuses);

    // Find recent prayer requests (for dashboard recent activities)
    Page<PrayerRequest> findByChurchOrderByCreatedAtDesc(Church church, Pageable pageable);

    /**
     * Count urgent active prayer requests (tenant-filtered)
     * Dashboard Phase 2.4: Advanced Analytics
     */
    @Query("SELECT COUNT(p) FROM PrayerRequest p WHERE p.isUrgent = true AND (p.status = 'PENDING' OR p.status = 'ACTIVE')")
    Long countUrgentActiveRequests();

    /**
     * Count prayer requests by church ID
     */
    Long countByChurch_Id(Long churchId);
}
