package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.CareNeed;
import com.reuben.pastcare_spring.models.CareNeedPriority;
import com.reuben.pastcare_spring.models.CareNeedStatus;
import com.reuben.pastcare_spring.models.CareNeedType;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CareNeedRepository extends JpaRepository<CareNeed, Long> {

    // Find by church
    Page<CareNeed> findByChurch(Church church, Pageable pageable);

    // Find by church and status
    Page<CareNeed> findByChurchAndStatus(Church church, CareNeedStatus status, Pageable pageable);

    // Find by church and type
    Page<CareNeed> findByChurchAndType(Church church, CareNeedType type, Pageable pageable);

    // Find by church and assigned user
    Page<CareNeed> findByChurchAndAssignedTo(Church church, User user, Pageable pageable);

    // Find by church and priority with status filter
    List<CareNeed> findByChurchAndPriorityAndStatusIn(Church church, CareNeedPriority priority, List<CareNeedStatus> statuses);

    // Find unassigned care needs
    List<CareNeed> findByChurchAndAssignedToIsNullAndStatus(Church church, CareNeedStatus status);

    // Find by status
    List<CareNeed> findByStatusOrderByCreatedAtDesc(CareNeedStatus status);

    // Find by member
    List<CareNeed> findByMemberOrderByCreatedAtDesc(Member member);

    // Find by member (without ordering for filtering)
    List<CareNeed> findByMember(Member member);

    // Find by assigned user
    List<CareNeed> findByAssignedToOrderByDueDateAsc(User user);

    // Find by type
    List<CareNeed> findByTypeOrderByCreatedAtDesc(CareNeedType type);

    // Find by priority
    List<CareNeed> findByPriorityOrderByCreatedAtDesc(CareNeedPriority priority);

    // Find overdue care needs
    @Query("SELECT c FROM CareNeed c WHERE c.dueDate < :currentDate AND (c.status = 'OPEN' OR c.status = 'IN_PROGRESS')")
    List<CareNeed> findOverdueCareNeeds(@Param("currentDate") LocalDate currentDate);

    // Find urgent care needs
    @Query("SELECT c FROM CareNeed c WHERE c.priority = 'URGENT' AND (c.status = 'OPEN' OR c.status = 'IN_PROGRESS') ORDER BY c.createdAt DESC")
    List<CareNeed> findUrgentCareNeeds();

    // Find unassigned care needs
    @Query("SELECT c FROM CareNeed c WHERE c.assignedTo IS NULL AND c.status = 'OPEN' ORDER BY c.priority DESC, c.createdAt DESC")
    List<CareNeed> findUnassignedCareNeeds();

    // Find by date range
    @Query("SELECT c FROM CareNeed c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    List<CareNeed> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Find care needs requiring follow-up
    @Query("SELECT c FROM CareNeed c WHERE c.followUpRequired = true AND c.followUpDate <= :date AND c.status != 'RESOLVED' AND c.status != 'CLOSED'")
    List<CareNeed> findCareNeedsRequiringFollowUp(@Param("date") LocalDate date);

    // Count by status
    Long countByStatus(CareNeedStatus status);

    // Count by priority
    Long countByPriority(CareNeedPriority priority);

    // Count by type
    Long countByType(CareNeedType type);

    // Count overdue
    @Query("SELECT COUNT(c) FROM CareNeed c WHERE c.dueDate < :currentDate AND (c.status = 'OPEN' OR c.status = 'IN_PROGRESS')")
    Long countOverdue(@Param("currentDate") LocalDate currentDate);

    // Count assigned to user
    Long countByAssignedToAndStatus(User user, CareNeedStatus status);

    // Search by title or description
    @Query("SELECT c FROM CareNeed c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<CareNeed> searchCareNeeds(@Param("searchTerm") String searchTerm);

    // Search care needs with church filter and pagination
    @Query("SELECT c FROM CareNeed c WHERE c.church = :church AND (LOWER(c.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<CareNeed> searchCareNeeds(@Param("church") Church church, @Param("searchTerm") String searchTerm, Pageable pageable);

    // Find overdue care needs with church filter
    @Query("SELECT c FROM CareNeed c WHERE c.church = :church AND c.dueDate < :currentDate AND (c.status = 'OPEN' OR c.status = 'IN_PROGRESS' OR c.status = 'ASSIGNED')")
    List<CareNeed> findOverdueCareNeeds(@Param("church") Church church, @Param("currentDate") LocalDate currentDate);

    // Count by church
    Long countByChurch(Church church);

    // Count by church and status
    Long countByChurchAndStatus(Church church, CareNeedStatus status);

    // Count by church and priority
    Long countByChurchAndPriority(Church church, CareNeedPriority priority);

    // Find members with consecutive absences (for auto-detection)
    // Only includes members who have been in the system for at least 3 weeks
    // to avoid flagging newly added members
    @Query(value = "SELECT DISTINCT m.id FROM member m " +
        "LEFT JOIN attendance a ON m.id = a.member_id " +
        "LEFT JOIN attendance_session asession ON a.attendance_session_id = asession.id " +
        "WHERE m.church_id = :churchId " +
        "AND m.created_at < :threeWeeksAgo " + // Member must have been added at least 3 weeks ago
        "AND (asession.session_date IS NULL OR asession.session_date < :threeWeeksAgo) " +
        "GROUP BY m.id " +
        "HAVING COUNT(a.id) = 0 OR MAX(asession.session_date) < :threeWeeksAgo",
        nativeQuery = true)
    List<Long> findMembersWithConsecutiveAbsences(
        @Param("churchId") Long churchId,
        @Param("threeWeeksAgo") LocalDateTime threeWeeksAgo
    );
}
