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

@Repository
public interface CounselingSessionRepository extends JpaRepository<CounselingSession, Long> {

    // Find by church
    Page<CounselingSession> findByChurch(Church church, Pageable pageable);

    // Find by church and status
    Page<CounselingSession> findByChurchAndStatus(Church church, CounselingStatus status, Pageable pageable);

    // Find by church and type
    Page<CounselingSession> findByChurchAndType(Church church, CounselingType type, Pageable pageable);

    // Find by church and counselor
    Page<CounselingSession> findByChurchAndCounselor(Church church, User counselor, Pageable pageable);

    // Find by member
    List<CounselingSession> findByMemberOrderBySessionDateDesc(Member member);

    // Find by member (without ordering for filtering)
    List<CounselingSession> findByMember(Member member);

    // Find by counselor
    List<CounselingSession> findByCounselorOrderBySessionDateAsc(User counselor);

    // Find by care need
    List<CounselingSession> findByCareNeedOrderBySessionDateDesc(CareNeed careNeed);

    // Find by type
    List<CounselingSession> findByTypeOrderBySessionDateDesc(CounselingType type);

    // Find by status
    List<CounselingSession> findByStatusOrderBySessionDateDesc(CounselingStatus status);

    // Find upcoming sessions
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionDate >= :currentDate AND (cs.status = 'SCHEDULED' OR cs.status = 'RESCHEDULED') ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findUpcomingSessions(@Param("currentDate") LocalDateTime currentDate);

    // Find upcoming sessions for a church
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.church = :church AND cs.sessionDate >= :currentDate AND (cs.status = 'SCHEDULED' OR cs.status = 'RESCHEDULED') ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findUpcomingSessions(@Param("church") Church church, @Param("currentDate") LocalDateTime currentDate);

    // Find upcoming sessions for a counselor
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.counselor = :counselor AND cs.sessionDate >= :currentDate AND (cs.status = 'SCHEDULED' OR cs.status = 'RESCHEDULED') ORDER BY cs.sessionDate ASC")
    List<CounselingSession> findUpcomingSessionsByCounselor(@Param("counselor") User counselor, @Param("currentDate") LocalDateTime currentDate);

    // Find completed sessions
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.status = 'COMPLETED' ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findCompletedSessions();

    // Find completed sessions for a church
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.church = :church AND cs.status = 'COMPLETED' ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findCompletedSessions(@Param("church") Church church);

    // Find sessions requiring follow-up
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.followUpRequired = true AND cs.followUpDate <= :date AND cs.status = 'COMPLETED' ORDER BY cs.followUpDate ASC")
    List<CounselingSession> findSessionsRequiringFollowUp(@Param("date") LocalDateTime date);

    // Find sessions requiring follow-up for a church
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.church = :church AND cs.followUpRequired = true AND cs.followUpDate <= :date AND cs.status = 'COMPLETED' ORDER BY cs.followUpDate ASC")
    List<CounselingSession> findSessionsRequiringFollowUp(@Param("church") Church church, @Param("date") LocalDateTime date);

    // Find sessions with referrals needed
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.isReferralNeeded = true ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findSessionsWithReferralsNeeded();

    // Find sessions with referrals needed for a church
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.church = :church AND cs.isReferralNeeded = true ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findSessionsWithReferralsNeeded(@Param("church") Church church);

    // Find by date range
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.sessionDate BETWEEN :startDate AND :endDate ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Find by date range for a church
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.church = :church AND cs.sessionDate BETWEEN :startDate AND :endDate ORDER BY cs.sessionDate DESC")
    List<CounselingSession> findByDateRange(@Param("church") Church church, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Count by church
    Long countByChurch(Church church);

    // Count by church and status
    Long countByChurchAndStatus(Church church, CounselingStatus status);

    // Count by church and type
    Long countByChurchAndType(Church church, CounselingType type);

    // Count by counselor and status
    Long countByCounselorAndStatus(User counselor, CounselingStatus status);

    // Count by status
    Long countByStatus(CounselingStatus status);

    // Count by type
    Long countByType(CounselingType type);

    // Count sessions with referrals
    @Query("SELECT COUNT(cs) FROM CounselingSession cs WHERE cs.church = :church AND cs.isReferralNeeded = true")
    Long countSessionsWithReferrals(@Param("church") Church church);

    // Count sessions requiring follow-up
    @Query("SELECT COUNT(cs) FROM CounselingSession cs WHERE cs.church = :church AND cs.followUpRequired = true AND cs.followUpDate <= :date AND cs.status = 'COMPLETED'")
    Long countSessionsRequiringFollowUp(@Param("church") Church church, @Param("date") LocalDateTime date);

    // Search sessions by title or notes
    @Query("SELECT cs FROM CounselingSession cs WHERE LOWER(cs.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(cs.sessionNotes) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<CounselingSession> searchSessions(@Param("searchTerm") String searchTerm);

    // Search sessions with church filter and pagination
    @Query("SELECT cs FROM CounselingSession cs WHERE cs.church = :church AND (LOWER(cs.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(cs.sessionNotes) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CounselingSession> searchSessions(@Param("church") Church church, @Param("searchTerm") String searchTerm, Pageable pageable);
}
