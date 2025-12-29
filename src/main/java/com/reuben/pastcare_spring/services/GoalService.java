package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.GoalRequest;
import com.reuben.pastcare_spring.dtos.GoalResponse;
import com.reuben.pastcare_spring.enums.GoalStatus;
import com.reuben.pastcare_spring.enums.GoalType;
import com.reuben.pastcare_spring.models.Goal;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing goals and calculating progress.
 * Dashboard Phase 2.3: Goal Tracking
 *
 * Provides CRUD operations for goals and automatic progress calculation
 * based on goal type (attendance, giving, members, events).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GoalService {

    private final GoalRepository goalRepository;
    private final UserRepository userRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final DonationRepository donationRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;

    /**
     * Get all active goals
     */
    public List<GoalResponse> getActiveGoals() {
        return goalRepository.findByStatus(GoalStatus.ACTIVE).stream()
            .map(GoalResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get all goals (including inactive)
     */
    public List<GoalResponse> getAllGoals() {
        return goalRepository.findAll().stream()
            .map(GoalResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific goal by ID
     */
    public GoalResponse getGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Goal not found"));
        return GoalResponse.fromEntity(goal);
    }

    /**
     * Get goals by type
     */
    public List<GoalResponse> getGoalsByType(GoalType goalType) {
        return goalRepository.findByGoalType(goalType).stream()
            .map(GoalResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Create a new goal
     */
    @Transactional
    public GoalResponse createGoal(GoalRequest request, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Goal goal = Goal.builder()
            .goalType(request.getGoalType())
            .targetValue(request.getTargetValue())
            .currentValue(BigDecimal.ZERO)
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .status(GoalStatus.ACTIVE)
            .title(request.getTitle())
            .description(request.getDescription())
            .createdBy(user)
            .build();

        goal.setChurch(user.getChurch());

        // Calculate initial progress
        calculateProgress(goal);

        Goal saved = goalRepository.save(goal);
        log.info("Created new goal: {} for church: {}", saved.getTitle(), saved.getChurchId());

        return GoalResponse.fromEntity(saved);
    }

    /**
     * Update an existing goal
     */
    @Transactional
    public GoalResponse updateGoal(Long goalId, GoalRequest request) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Goal not found"));

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        goal.setGoalType(request.getGoalType());
        goal.setTargetValue(request.getTargetValue());
        goal.setStartDate(request.getStartDate());
        goal.setEndDate(request.getEndDate());
        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());

        // Recalculate progress with updated parameters
        calculateProgress(goal);

        Goal updated = goalRepository.save(goal);
        log.info("Updated goal: {}", updated.getTitle());

        return GoalResponse.fromEntity(updated);
    }

    /**
     * Delete a goal
     */
    @Transactional
    public void deleteGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Goal not found"));

        goalRepository.delete(goal);
        log.info("Deleted goal: {}", goal.getTitle());
    }

    /**
     * Cancel a goal (soft delete - marks as cancelled)
     */
    @Transactional
    public GoalResponse cancelGoal(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Goal not found"));

        goal.setStatus(GoalStatus.CANCELLED);
        Goal updated = goalRepository.save(goal);

        log.info("Cancelled goal: {}", updated.getTitle());
        return GoalResponse.fromEntity(updated);
    }

    /**
     * Manually recalculate progress for a specific goal
     */
    @Transactional
    public GoalResponse recalculateProgress(Long goalId) {
        Goal goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new RuntimeException("Goal not found"));

        calculateProgress(goal);
        updateGoalStatus(goal);

        Goal updated = goalRepository.save(goal);
        log.info("Recalculated progress for goal: {} - Current: {}/{}",
            updated.getTitle(), updated.getCurrentValue(), updated.getTargetValue());

        return GoalResponse.fromEntity(updated);
    }

    /**
     * Recalculate progress for all active goals
     * This should be called periodically (e.g., daily via scheduled task)
     */
    @Transactional
    public void recalculateAllActiveGoals() {
        List<Goal> activeGoals = goalRepository.findByStatus(GoalStatus.ACTIVE);

        for (Goal goal : activeGoals) {
            calculateProgress(goal);
            updateGoalStatus(goal);
        }

        goalRepository.saveAll(activeGoals);
        log.info("Recalculated progress for {} active goals", activeGoals.size());
    }

    /**
     * Check and update expired goals
     * Marks expired goals as COMPLETED or FAILED based on achievement
     */
    @Transactional
    public void updateExpiredGoals() {
        List<Goal> expiredGoals = goalRepository.findExpiredActiveGoals(LocalDate.now());

        for (Goal goal : expiredGoals) {
            if (goal.isAchieved()) {
                goal.setStatus(GoalStatus.COMPLETED);
                log.info("Goal achieved: {}", goal.getTitle());
            } else {
                goal.setStatus(GoalStatus.FAILED);
                log.info("Goal failed: {} - Achieved {}/{}",
                    goal.getTitle(), goal.getCurrentValue(), goal.getTargetValue());
            }
        }

        goalRepository.saveAll(expiredGoals);
        log.info("Updated {} expired goals", expiredGoals.size());
    }

    /**
     * Calculate current progress for a goal based on its type
     */
    private void calculateProgress(Goal goal) {
        BigDecimal currentValue = BigDecimal.ZERO;

        switch (goal.getGoalType()) {
            case ATTENDANCE:
                currentValue = calculateAttendanceProgress(goal);
                break;
            case GIVING:
                currentValue = calculateGivingProgress(goal);
                break;
            case MEMBERS:
                currentValue = calculateMemberProgress(goal);
                break;
            case EVENTS:
                currentValue = calculateEventProgress(goal);
                break;
        }

        goal.setCurrentValue(currentValue);
    }

    /**
     * Calculate attendance progress (average attendance during goal period)
     */
    private BigDecimal calculateAttendanceProgress(Goal goal) {
        LocalDateTime startDateTime = goal.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = goal.getEndDate().atTime(23, 59, 59);

        // Convert LocalDateTime to Instant for the repository query
        Double avgAttendance = attendanceSessionRepository.getAverageAttendanceForPeriod(
            startDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant(),
            endDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
        );

        return avgAttendance != null ? BigDecimal.valueOf(avgAttendance) : BigDecimal.ZERO;
    }

    /**
     * Calculate giving progress (total donations during goal period)
     */
    private BigDecimal calculateGivingProgress(Goal goal) {
        LocalDateTime startDateTime = goal.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = goal.getEndDate().atTime(23, 59, 59);

        // Convert LocalDateTime to Instant for the repository query
        BigDecimal totalDonations = donationRepository.getTotalDonationsForPeriod(
            startDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant(),
            endDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
        );

        return totalDonations != null ? totalDonations : BigDecimal.ZERO;
    }

    /**
     * Calculate member progress (current active member count)
     */
    private BigDecimal calculateMemberProgress(Goal goal) {
        Long memberCount = memberRepository.countActiveMembers();
        return BigDecimal.valueOf(memberCount);
    }

    /**
     * Calculate event progress (number of events during goal period)
     */
    private BigDecimal calculateEventProgress(Goal goal) {
        LocalDateTime startDateTime = goal.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = goal.getEndDate().atTime(23, 59, 59);

        Long eventCount = eventRepository.countEventsInPeriod(startDateTime, endDateTime);
        return BigDecimal.valueOf(eventCount);
    }

    /**
     * Update goal status based on current progress and dates
     */
    private void updateGoalStatus(Goal goal) {
        if (goal.getStatus() != GoalStatus.ACTIVE) {
            return; // Don't modify non-active goals
        }

        if (goal.isExpired()) {
            goal.setStatus(goal.isAchieved() ? GoalStatus.COMPLETED : GoalStatus.FAILED);
        } else if (goal.isAchieved() && !goal.isExpired()) {
            // Goal achieved before end date
            goal.setStatus(GoalStatus.COMPLETED);
        }
    }
}
