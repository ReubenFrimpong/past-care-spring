package com.reuben.pastcare_spring.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.reuben.pastcare_spring.models.Attendance;
import com.reuben.pastcare_spring.enums.AttendanceStatus;
import com.reuben.pastcare_spring.enums.CheckInMethod;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
  // Basic queries
  List<Attendance> findByAttendanceSessionId(Long attendanceSessionId);
  List<Attendance> findByMemberId(Long memberId);
  Optional<Attendance> findByMemberIdAndAttendanceSessionId(Long memberId, Long attendanceSessionId);
  boolean existsByAttendanceSessionIdAndMemberId(Long attendanceSessionId, Long memberId);

  // Phase 2: Analytics Queries

  /**
   * Count total attendance records by church and date range
   */
  @Query("SELECT COUNT(a) FROM Attendance a " +
         "JOIN a.attendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate")
  Long countByChurchAndDateRange(@Param("churchId") Long churchId,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate);

  /**
   * Count attendance records by status
   */
  @Query("SELECT COUNT(a) FROM Attendance a " +
         "JOIN a.attendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate " +
         "AND a.status = :status")
  Long countByChurchDateRangeAndStatus(@Param("churchId") Long churchId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate,
                                        @Param("status") AttendanceStatus status);

  /**
   * Count unique members who attended in date range
   */
  @Query("SELECT COUNT(DISTINCT a.member.id) FROM Attendance a " +
         "JOIN a.attendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate " +
         "AND a.status = 'PRESENT'")
  Long countUniqueMembersAttended(@Param("churchId") Long churchId,
                                   @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

  /**
   * Count attendance by check-in method
   */
  @Query("SELECT a.checkInMethod, COUNT(a) FROM Attendance a " +
         "JOIN a.attendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate " +
         "AND a.checkInMethod IS NOT NULL " +
         "GROUP BY a.checkInMethod")
  List<Object[]> countByCheckInMethod(@Param("churchId") Long churchId,
                                       @Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate);

  /**
   * Get late arrival statistics
   * Returns a single-row result with [COUNT, AVG, MAX]
   */
  @Query("SELECT COUNT(a), AVG(a.minutesLate), MAX(a.minutesLate) FROM Attendance a " +
         "JOIN a.attendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate " +
         "AND a.isLate = true")
  List<Object[]> getLateArrivalStats(@Param("churchId") Long churchId,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

  /**
   * Find frequently late members
   */
  @Query("SELECT m.firstName, m.lastName, COUNT(a) as lateCount " +
         "FROM Attendance a " +
         "JOIN a.member m " +
         "JOIN a.attendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate " +
         "AND a.isLate = true " +
         "GROUP BY m.id, m.firstName, m.lastName " +
         "ORDER BY lateCount DESC")
  List<Object[]> findFrequentlyLateMembers(@Param("churchId") Long churchId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

  /**
   * Get member engagement data
   */
  @Query("SELECT m.id, m.firstName, m.lastName, " +
         "COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END), " +
         "COUNT(a), " +
         "COUNT(CASE WHEN a.isLate = true THEN 1 END), " +
         "MAX(s.sessionDate) " +
         "FROM Member m " +
         "LEFT JOIN Attendance a ON a.member.id = m.id " +
         "LEFT JOIN AttendanceSession s ON a.attendanceSession.id = s.id " +
         "WHERE m.church.id = :churchId " +
         "AND (s.sessionDate BETWEEN :startDate AND :endDate OR s.sessionDate IS NULL) " +
         "GROUP BY m.id, m.firstName, m.lastName")
  List<Object[]> getMemberEngagementData(@Param("churchId") Long churchId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

  /**
   * Get attendance trends by day
   */
  @Query("SELECT s.sessionDate, COUNT(a), " +
         "COUNT(CASE WHEN a.status = 'PRESENT' THEN 1 END) " +
         "FROM Attendance a " +
         "JOIN a.attendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate " +
         "GROUP BY s.sessionDate " +
         "ORDER BY s.sessionDate")
  List<Object[]> getAttendanceTrendsByDay(@Param("churchId") Long churchId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

  /**
   * Get preferred check-in method per member
   */
  @Query("SELECT m.id, a.checkInMethod, COUNT(a) " +
         "FROM Attendance a " +
         "JOIN a.member m " +
         "JOIN a.attendanceSession s " +
         "WHERE s.church.id = :churchId " +
         "AND a.checkInMethod IS NOT NULL " +
         "GROUP BY m.id, a.checkInMethod " +
         "ORDER BY COUNT(a) DESC")
  List<Object[]> getMemberPreferredCheckInMethods(@Param("churchId") Long churchId);

  // Phase 4: Export and Integration Queries

  /**
   * Find all attendance records for a session
   */
  List<Attendance> findByAttendanceSession(com.reuben.pastcare_spring.models.AttendanceSession session);

  /**
   * Find attendance by session and member
   */
  List<Attendance> findByAttendanceSessionAndMemberId(
    com.reuben.pastcare_spring.models.AttendanceSession session,
    Long memberId
  );

  /**
   * Count attendance by member, status, and date range
   */
  @Query("SELECT COUNT(a) FROM Attendance a " +
         "JOIN a.attendanceSession s " +
         "WHERE a.member.id = :memberId " +
         "AND a.status = :status " +
         "AND s.sessionDate BETWEEN :startDate AND :endDate")
  Long countByMemberIdAndStatusAndSessionDateBetween(
    @Param("memberId") Long memberId,
    @Param("status") AttendanceStatus status,
    @Param("startDate") LocalDate startDate,
    @Param("endDate") LocalDate endDate
  );

  /**
   * Find most recent attendance by member and status
   */
  Optional<Attendance> findTopByMemberIdAndStatusOrderByCheckInTimeDesc(
    Long memberId,
    AttendanceStatus status
  );
}
