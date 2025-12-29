package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.enums.GoalStatus;
import com.reuben.pastcare_spring.enums.GoalType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing a goal that a church is tracking.
 * Dashboard Phase 2.3: Goal Tracking
 *
 * Goals can track various metrics like attendance, giving, membership, or events.
 * Progress is automatically calculated based on the goal type.
 */
@Entity
@Table(name = "goals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Filter(name = "churchFilter", condition = "church_id = :churchId")
public class Goal extends TenantBaseEntity {

    /**
     * Type of goal (ATTENDANCE, GIVING, MEMBERS, EVENTS)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "goal_type", nullable = false, length = 50)
    private GoalType goalType;

    /**
     * Target value to achieve
     */
    @Column(name = "target_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal targetValue;

    /**
     * Current progress towards goal
     */
    @Column(name = "current_value", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal currentValue = BigDecimal.ZERO;

    /**
     * When the goal period starts
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * When the goal period ends
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Current status of the goal
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private GoalStatus status = GoalStatus.ACTIVE;

    /**
     * Goal title/name
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Optional goal description
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * User who created this goal
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Calculate progress percentage
     */
    public double getProgressPercentage() {
        if (targetValue.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }
        return currentValue.divide(targetValue, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /**
     * Check if goal is achieved
     */
    public boolean isAchieved() {
        return currentValue.compareTo(targetValue) >= 0;
    }

    /**
     * Check if goal period is active
     */
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate) && status == GoalStatus.ACTIVE;
    }

    /**
     * Check if goal period has ended
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(endDate);
    }

    /**
     * Get days remaining in goal period
     */
    public long getDaysRemaining() {
        LocalDate now = LocalDate.now();
        if (now.isAfter(endDate)) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(now, endDate);
    }

    /**
     * Get total days in goal period
     */
    public long getTotalDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
    }
}
