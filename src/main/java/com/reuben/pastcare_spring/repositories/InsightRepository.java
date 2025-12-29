package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.enums.InsightCategory;
import com.reuben.pastcare_spring.enums.InsightSeverity;
import com.reuben.pastcare_spring.enums.InsightType;
import com.reuben.pastcare_spring.models.Insight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing insights.
 * Dashboard Phase 2.4: Advanced Analytics
 *
 * Automatically filtered by church_id through @Filter annotation on Insight entity.
 */
@Repository
public interface InsightRepository extends JpaRepository<Insight, Long> {

    /**
     * Find all active (not dismissed) insights
     */
    List<Insight> findByDismissedFalseOrderByCreatedAtDesc();

    /**
     * Find insights by type
     */
    List<Insight> findByInsightType(InsightType insightType);

    /**
     * Find active insights by type
     */
    List<Insight> findByInsightTypeAndDismissedFalseOrderByCreatedAtDesc(InsightType insightType);

    /**
     * Find insights by category
     */
    List<Insight> findByCategory(InsightCategory category);

    /**
     * Find active insights by category
     */
    List<Insight> findByCategoryAndDismissedFalseOrderByCreatedAtDesc(InsightCategory category);

    /**
     * Find insights by severity
     */
    List<Insight> findBySeverityOrderByCreatedAtDesc(InsightSeverity severity);

    /**
     * Find active insights by severity (e.g., HIGH and CRITICAL)
     */
    @Query("SELECT i FROM Insight i WHERE i.severity = :severity AND i.dismissed = false ORDER BY i.createdAt DESC")
    List<Insight> findActiveBySeverity(@Param("severity") InsightSeverity severity);

    /**
     * Find actionable insights (not dismissed)
     */
    List<Insight> findByActionableTrueAndDismissedFalseOrderByCreatedAtDesc();

    /**
     * Find high priority insights (HIGH and CRITICAL severity)
     */
    @Query("SELECT i FROM Insight i WHERE i.severity IN ('HIGH', 'CRITICAL') AND i.dismissed = false ORDER BY i.severity DESC, i.createdAt DESC")
    List<Insight> findHighPriorityInsights();

    /**
     * Count active insights
     */
    long countByDismissedFalse();

    /**
     * Count active insights by severity
     */
    long countBySeverityAndDismissedFalse(InsightSeverity severity);

    /**
     * Count active insights by category
     */
    long countByCategoryAndDismissedFalse(InsightCategory category);

    /**
     * Count actionable insights
     */
    long countByActionableTrueAndDismissedFalse();

    /**
     * Find recent insights (last N)
     */
    @Query("SELECT i FROM Insight i WHERE i.dismissed = false ORDER BY i.createdAt DESC")
    List<Insight> findRecentInsights(org.springframework.data.domain.Pageable pageable);

    /**
     * Delete old dismissed insights (cleanup)
     */
    @Query("DELETE FROM Insight i WHERE i.dismissed = true AND i.dismissedAt < :cutoffDate")
    void deleteOldDismissedInsights(@Param("cutoffDate") java.time.Instant cutoffDate);
}
