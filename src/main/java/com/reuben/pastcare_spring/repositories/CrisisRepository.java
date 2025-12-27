package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Crisis;
import com.reuben.pastcare_spring.models.CrisisSeverity;
import com.reuben.pastcare_spring.models.CrisisStatus;
import com.reuben.pastcare_spring.models.CrisisType;
import com.reuben.pastcare_spring.models.Church;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CrisisRepository extends JpaRepository<Crisis, Long> {

    // Find by ID and church
    Optional<Crisis> findByIdAndChurch(Long id, Church church);

    // Find by church
    Page<Crisis> findByChurch(Church church, Pageable pageable);

    // Find by church and status
    Page<Crisis> findByChurchAndStatus(Church church, CrisisStatus status, Pageable pageable);

    // Find by church and type
    Page<Crisis> findByChurchAndCrisisType(Church church, CrisisType crisisType, Pageable pageable);

    // Find by church and severity
    Page<Crisis> findByChurchAndSeverity(Church church, CrisisSeverity severity, Pageable pageable);

    // Find active crises
    @Query("SELECT c FROM Crisis c WHERE c.church = :church AND (c.status = 'ACTIVE' OR c.status = 'IN_RESPONSE') ORDER BY c.severity DESC, c.reportedDate DESC")
    List<Crisis> findActiveCrises(@Param("church") Church church);

    // Find critical crises
    @Query("SELECT c FROM Crisis c WHERE c.church = :church AND c.severity = 'CRITICAL' AND (c.status = 'ACTIVE' OR c.status = 'IN_RESPONSE') ORDER BY c.reportedDate DESC")
    List<Crisis> findCriticalCrises(@Param("church") Church church);

    // Find crises requiring follow-up
    @Query("SELECT c FROM Crisis c WHERE c.church = :church AND c.followUpRequired = true AND c.followUpDate <= :date AND c.status != 'CLOSED'")
    List<Crisis> findCrisesRequiringFollowUp(@Param("church") Church church, @Param("date") LocalDateTime date);

    // Find by status
    List<Crisis> findByStatusOrderByReportedDateDesc(CrisisStatus status);

    // Find by type
    List<Crisis> findByCrisisTypeOrderByReportedDateDesc(CrisisType crisisType);

    // Find by severity
    List<Crisis> findBySeverityOrderByReportedDateDesc(CrisisSeverity severity);

    // Count by church
    Long countByChurch(Church church);

    // Count by church and status
    Long countByChurchAndStatus(Church church, CrisisStatus status);

    // Count by church and severity
    Long countByChurchAndSeverity(Church church, CrisisSeverity severity);

    // Count by church and type
    Long countByChurchAndCrisisType(Church church, CrisisType crisisType);

    // Search crises
    @Query("SELECT c FROM Crisis c WHERE c.church = :church AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.location) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Crisis> searchCrises(@Param("church") Church church, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Find by date range
    @Query("SELECT c FROM Crisis c WHERE c.church = :church AND c.incidentDate BETWEEN :startDate AND :endDate ORDER BY c.incidentDate DESC")
    List<Crisis> findByIncidentDateRange(@Param("church") Church church, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
