package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.enums.GoalStatus;
import com.reuben.pastcare_spring.enums.GoalType;
import com.reuben.pastcare_spring.models.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for managing goals.
 * Dashboard Phase 2.3: Goal Tracking
 *
 * Automatically filtered by church_id through @Filter annotation on Goal entity.
 */
@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    /**
     * Find all active goals for current church (tenant-filtered)
     */
    List<Goal> findByStatus(GoalStatus status);

    /**
     * Find goals by type
     */
    List<Goal> findByGoalType(GoalType goalType);

    /**
     * Find active goals by type
     */
    List<Goal> findByGoalTypeAndStatus(GoalType goalType, GoalStatus status);

    /**
     * Find goals ending soon (within specified days)
     */
    @Query("SELECT g FROM Goal g WHERE g.status = 'ACTIVE' AND g.endDate BETWEEN :now AND :endDate ORDER BY g.endDate ASC")
    List<Goal> findGoalsEndingSoon(@Param("now") LocalDate now, @Param("endDate") LocalDate endDate);

    /**
     * Find expired goals that are still marked as active
     */
    @Query("SELECT g FROM Goal g WHERE g.status = 'ACTIVE' AND g.endDate < :now")
    List<Goal> findExpiredActiveGoals(@Param("now") LocalDate now);

    /**
     * Find goals within a date range
     */
    @Query("SELECT g FROM Goal g WHERE g.startDate <= :endDate AND g.endDate >= :startDate ORDER BY g.startDate DESC")
    List<Goal> findGoalsInDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    /**
     * Find all active goals ordered by end date
     */
    List<Goal> findByStatusOrderByEndDateAsc(GoalStatus status);

    /**
     * Find goals created by a specific user
     */
    List<Goal> findByCreatedById(Long userId);

    /**
     * Count active goals
     */
    long countByStatus(GoalStatus status);

    /**
     * Count active goals by type
     */
    long countByGoalTypeAndStatus(GoalType goalType, GoalStatus status);
}
