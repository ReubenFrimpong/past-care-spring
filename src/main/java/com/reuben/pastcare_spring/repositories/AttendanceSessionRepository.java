package com.reuben.pastcare_spring.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.reuben.pastcare_spring.models.AttendanceSession;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.enums.ServiceType;

@Repository
public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
  List<AttendanceSession> findByChurch_Id(Long churchId);
  List<AttendanceSession> findByFellowship_Id(Long fellowshipId);
  List<AttendanceSession> findBySessionDateBetween(LocalDate startDate, LocalDate endDate);
  List<AttendanceSession> findByChurch_IdAndSessionDateBetween(Long churchId, LocalDate startDate, LocalDate endDate);

  // Recurring session support
  List<AttendanceSession> findByIsRecurringTrue();
  boolean existsByChurch_IdAndSessionDateAndSessionNameAndIsRecurringFalse(
      Long churchId, LocalDate sessionDate, String sessionName);

  // Phase 2: Analytics Queries

  /**
   * Count sessions by service type in date range
   */
  @Query("SELECT s.serviceType, COUNT(s) FROM AttendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate " +
         "GROUP BY s.serviceType")
  List<Object[]> countSessionsByServiceType(@Param("churchId") Long churchId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

  /**
   * Get attendance statistics by service type
   */
  @Query("SELECT s.serviceType, COUNT(s), " +
         "SUM(SIZE(s.attendances)), " +
         "AVG(SIZE(s.attendances)) " +
         "FROM AttendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate " +
         "GROUP BY s.serviceType")
  List<Object[]> getAttendanceStatsByServiceType(@Param("churchId") Long churchId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

  /**
   * Count total sessions in date range
   */
  @Query("SELECT COUNT(s) FROM AttendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate")
  Long countSessionsByDateRange(@Param("churchId") Long churchId,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);

  /**
   * Count completed vs upcoming sessions
   */
  @Query("SELECT s.isCompleted, COUNT(s) FROM AttendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate " +
         "GROUP BY s.isCompleted")
  List<Object[]> countSessionsByCompletionStatus(@Param("churchId") Long churchId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

  // Phase 4: Export and Integration Queries

  /**
   * Count sessions by church and date range
   */
  @Query("SELECT COUNT(s) FROM AttendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate")
  Long countByChurchIdAndSessionDateBetween(@Param("churchId") Long churchId,
                                             @Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

  /**
   * Find sessions by church and date range, ordered by date descending
   */
  List<AttendanceSession> findByChurch_IdAndSessionDateBetweenOrderBySessionDateDesc(
    Long churchId,
    LocalDate startDate,
    LocalDate endDate
  );

  /**
   * Find recent attendance sessions (for dashboard recent activities)
   */
  org.springframework.data.domain.Page<AttendanceSession> findByChurchOrderByCreatedAtDesc(
    Church church,
    org.springframework.data.domain.Pageable pageable
  );

  /**
   * Get average attendance for a time period (for goal tracking)
   * Dashboard Phase 2.3: Goal Tracking
   */
  @Query("SELECT AVG(SIZE(s.attendances)) FROM AttendanceSession s " +
         "WHERE s.createdAt >= :startDate AND s.createdAt <= :endDate")
  Double getAverageAttendanceForPeriod(
    @Param("startDate") java.time.Instant startDate,
    @Param("endDate") java.time.Instant endDate
  );

  // Count sessions by church ID
  Long countByChurch_Id(Long churchId);

  // Event-Attendance Integration methods
  boolean existsByEvent_Id(Long eventId);
  List<AttendanceSession> findByEvent_Id(Long eventId);
  List<AttendanceSession> findByChurch_IdAndEventIsNotNull(Long churchId);
}
