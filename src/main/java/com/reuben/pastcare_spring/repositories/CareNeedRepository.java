package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for CareNeed entity with support for complex queries and filtering
 */
@Repository
public interface CareNeedRepository extends JpaRepository<CareNeed, Long>, JpaSpecificationExecutor<CareNeed> {

    Page<CareNeed> findByChurch(Church church, Pageable pageable);

    Page<CareNeed> findByChurchAndStatus(Church church, CareNeedStatus status, Pageable pageable);

    Page<CareNeed> findByChurchAndType(Church church, CareNeedType type, Pageable pageable);

    Page<CareNeed> findByChurchAndPriority(Church church, CareNeedPriority priority, Pageable pageable);

    List<CareNeed> findByMember(Member member);

    Page<CareNeed> findByChurchAndAssignedTo(Church church, User assignedTo, Pageable pageable);

    @Query("SELECT c FROM CareNeed c WHERE c.church = :church AND " +
           "(LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.member.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.member.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<CareNeed> searchCareNeeds(@Param("church") Church church, @Param("search") String search, Pageable pageable);

    long countByChurch(Church church);

    long countByChurchAndStatus(Church church, CareNeedStatus status);

    long countByChurchAndPriority(Church church, CareNeedPriority priority);

    // Overdue care needs
    @Query("SELECT c FROM CareNeed c WHERE c.church = :church AND c.dueDate < :date AND c.status NOT IN ('RESOLVED', 'CLOSED')")
    List<CareNeed> findOverdueCareNeeds(@Param("church") Church church, @Param("date") LocalDateTime date);

    // Auto-detection: members with 3+ consecutive absences (no attendance in last 21 days)
    @Query(value = "SELECT DISTINCT m.id FROM member m " +
           "WHERE m.church_id = :churchId " +
           "AND NOT EXISTS (SELECT 1 FROM attendance a WHERE a.member_id = m.id " +
           "AND a.created_at >= DATE_SUB(NOW(), INTERVAL 21 DAY) AND a.status = 'PRESENT')",
           nativeQuery = true)
    List<Long> findMembersWithConsecutiveAbsences(@Param("churchId") Long churchId);
}
