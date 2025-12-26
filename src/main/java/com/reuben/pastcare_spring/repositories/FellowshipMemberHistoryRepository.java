package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.FellowshipMemberAction;
import com.reuben.pastcare_spring.models.FellowshipMemberHistory;
import com.reuben.pastcare_spring.models.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FellowshipMemberHistoryRepository extends JpaRepository<FellowshipMemberHistory, Long> {

    /**
     * Get all history for a fellowship
     */
    List<FellowshipMemberHistory> findByFellowshipOrderByEffectiveDateDesc(Fellowship fellowship);

    /**
     * Get all history for a member
     */
    List<FellowshipMemberHistory> findByMemberOrderByEffectiveDateDesc(Member member);

    /**
     * Get history for a fellowship in a date range
     */
    List<FellowshipMemberHistory> findByFellowshipAndEffectiveDateBetween(
        Fellowship fellowship,
        LocalDate startDate,
        LocalDate endDate
    );

    /**
     * Count actions by type for a fellowship in a date range
     */
    @Query("SELECT COUNT(h) FROM FellowshipMemberHistory h " +
           "WHERE h.fellowship = :fellowship " +
           "AND h.action = :action " +
           "AND h.effectiveDate BETWEEN :startDate AND :endDate")
    Long countByFellowshipAndActionAndDateRange(
        @Param("fellowship") Fellowship fellowship,
        @Param("action") FellowshipMemberAction action,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    /**
     * Get retention rate: (members who stayed) / (members at start of period)
     * Members who stayed = current members - new joins during period
     */
    @Query(value = "SELECT " +
           "CASE WHEN start_count.cnt > 0 THEN " +
           "  ROUND(((start_count.cnt - left_count.cnt) * 100.0 / start_count.cnt), 2) " +
           "ELSE 0 END as retention_rate " +
           "FROM " +
           "(SELECT COUNT(DISTINCT member_id) as cnt FROM fellowship_member_history " +
           " WHERE fellowship_id = :fellowshipId " +
           " AND effective_date < :startDate " +
           " AND action IN ('JOINED', 'TRANSFERRED_IN', 'REACTIVATED')) as start_count, " +
           "(SELECT COUNT(DISTINCT member_id) as cnt FROM fellowship_member_history " +
           " WHERE fellowship_id = :fellowshipId " +
           " AND effective_date BETWEEN :startDate AND :endDate " +
           " AND action IN ('LEFT', 'TRANSFERRED_OUT', 'INACTIVE')) as left_count",
           nativeQuery = true)
    Double calculateRetentionRate(
        @Param("fellowshipId") Long fellowshipId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );
}
