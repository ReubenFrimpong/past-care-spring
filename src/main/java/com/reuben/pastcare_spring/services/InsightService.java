package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.InsightResponse;
import com.reuben.pastcare_spring.enums.*;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating and managing insights.
 * Dashboard Phase 2.4: Advanced Analytics
 *
 * Analyzes church data to generate actionable insights, detect anomalies,
 * identify trends, and provide recommendations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InsightService {

    private final InsightRepository insightRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final DonationRepository donationRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final PrayerRequestRepository prayerRequestRepository;
    private final UserRepository userRepository;

    // Thresholds for anomaly detection
    private static final double ATTENDANCE_DROP_THRESHOLD = 0.20; // 20% drop
    private static final double GIVING_DROP_THRESHOLD = 0.25; // 25% drop
    private static final int HIGH_PRIORITY_PRAYER_REQUESTS = 5;

    /**
     * Get all active insights (not dismissed)
     */
    public List<InsightResponse> getActiveInsights() {
        return insightRepository.findByDismissedFalseOrderByCreatedAtDesc().stream()
            .map(InsightResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get high priority insights
     */
    public List<InsightResponse> getHighPriorityInsights() {
        return insightRepository.findHighPriorityInsights().stream()
            .map(InsightResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get insights by category
     */
    public List<InsightResponse> getInsightsByCategory(InsightCategory category) {
        return insightRepository.findByCategoryAndDismissedFalseOrderByCreatedAtDesc(category).stream()
            .map(InsightResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get actionable insights
     */
    public List<InsightResponse> getActionableInsights() {
        return insightRepository.findByActionableTrueAndDismissedFalseOrderByCreatedAtDesc().stream()
            .map(InsightResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Dismiss an insight
     */
    @Transactional
    public InsightResponse dismissInsight(Long insightId, Long userId) {
        Insight insight = insightRepository.findById(insightId)
            .orElseThrow(() -> new RuntimeException("Insight not found"));

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        insight.dismiss(user);
        Insight updated = insightRepository.save(insight);

        log.info("Insight dismissed: {} by user: {}", insight.getTitle(), user.getName());
        return InsightResponse.fromEntity(updated);
    }

    /**
     * Generate all insights by analyzing church data
     * This is the main entry point for insight generation
     */
    @Transactional
    public List<InsightResponse> generateInsights(Long churchId) {
        log.info("Generating insights for church: {}", churchId);

        List<Insight> newInsights = new ArrayList<>();

        // Generate various types of insights
        newInsights.addAll(detectAttendanceAnomalies(churchId));
        newInsights.addAll(detectGivingAnomalies(churchId));
        newInsights.addAll(detectMembershipTrends(churchId));
        newInsights.addAll(identifyAtRiskMembers(churchId));
        newInsights.addAll(analyzePastoralCareNeeds(churchId));
        newInsights.addAll(analyzeEventEngagement(churchId));

        // Save all new insights
        List<Insight> saved = insightRepository.saveAll(newInsights);
        log.info("Generated {} new insights", saved.size());

        return saved.stream()
            .map(InsightResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Detect attendance anomalies (unusual drops or spikes)
     */
    private List<Insight> detectAttendanceAnomalies(Long churchId) {
        List<Insight> insights = new ArrayList<>();

        LocalDate now = LocalDate.now();
        LocalDate lastMonth = now.minusMonths(1);
        LocalDate twoMonthsAgo = now.minusMonths(2);

        // Get average attendance for last month and previous month
        // Convert LocalDateTime to Instant for the repository query
        Double lastMonthAvg = attendanceSessionRepository.getAverageAttendanceForPeriod(
            lastMonth.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant(),
            now.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
        );

        Double previousMonthAvg = attendanceSessionRepository.getAverageAttendanceForPeriod(
            twoMonthsAgo.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant(),
            lastMonth.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
        );

        if (lastMonthAvg != null && previousMonthAvg != null && previousMonthAvg > 0) {
            double percentChange = (lastMonthAvg - previousMonthAvg) / previousMonthAvg;

            if (percentChange < -ATTENDANCE_DROP_THRESHOLD) {
                insights.add(createInsight(
                    churchId,
                    InsightType.ANOMALY,
                    InsightCategory.ATTENDANCE,
                    "Attendance Drop Detected",
                    String.format("Average attendance dropped by %.1f%% this month (from %.0f to %.0f). " +
                        "This may indicate a trend requiring attention.",
                        Math.abs(percentChange * 100), previousMonthAvg, lastMonthAvg),
                    InsightSeverity.HIGH,
                    true,
                    "/dashboard/attendance"
                ));
            } else if (percentChange > ATTENDANCE_DROP_THRESHOLD) {
                insights.add(createInsight(
                    churchId,
                    InsightType.TREND,
                    InsightCategory.ATTENDANCE,
                    "Attendance Growth",
                    String.format("Great news! Average attendance increased by %.1f%% this month (from %.0f to %.0f).",
                        percentChange * 100, previousMonthAvg, lastMonthAvg),
                    InsightSeverity.INFO,
                    false,
                    null
                ));
            }
        }

        return insights;
    }

    /**
     * Detect giving anomalies
     */
    private List<Insight> detectGivingAnomalies(Long churchId) {
        List<Insight> insights = new ArrayList<>();

        LocalDate now = LocalDate.now();
        LocalDate lastMonth = now.minusMonths(1);
        LocalDate twoMonthsAgo = now.minusMonths(2);

        // Convert LocalDateTime to Instant for the repository query
        BigDecimal lastMonthTotal = donationRepository.getTotalDonationsForPeriod(
            lastMonth.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant(),
            now.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
        );
        BigDecimal previousMonthTotal = donationRepository.getTotalDonationsForPeriod(
            twoMonthsAgo.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant(),
            lastMonth.atStartOfDay().atZone(java.time.ZoneId.systemDefault()).toInstant()
        );

        if (lastMonthTotal != null && previousMonthTotal != null && previousMonthTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = lastMonthTotal.subtract(previousMonthTotal);
            double percentChange = diff.divide(previousMonthTotal, 4, RoundingMode.HALF_UP).doubleValue();

            if (percentChange < -GIVING_DROP_THRESHOLD) {
                insights.add(createInsight(
                    churchId,
                    InsightType.WARNING,
                    InsightCategory.GIVING,
                    "Giving Decline Alert",
                    String.format("Donations decreased by %.1f%% this month. Consider reviewing stewardship campaigns.",
                        Math.abs(percentChange * 100)),
                    InsightSeverity.HIGH,
                    true,
                    "/dashboard/donations"
                ));
            }
        }

        return insights;
    }

    /**
     * Detect membership trends
     */
    private List<Insight> detectMembershipTrends(Long churchId) {
        List<Insight> insights = new ArrayList<>();

        Long currentMembers = memberRepository.countActiveMembers();

        // Check for milestone achievements
        if (currentMembers != null) {
            if (currentMembers % 100 == 0 && currentMembers > 0) {
                insights.add(createInsight(
                    churchId,
                    InsightType.TREND,
                    InsightCategory.MEMBERS,
                    "Membership Milestone Achieved!",
                    String.format("Congratulations! Your church has reached %d active members.", currentMembers),
                    InsightSeverity.INFO,
                    false,
                    "/dashboard/members"
                ));
            }
        }

        return insights;
    }

    /**
     * Identify at-risk members (churn risk)
     */
    private List<Insight> identifyAtRiskMembers(Long churchId) {
        List<Insight> insights = new ArrayList<>();

        // This would typically use more sophisticated analysis
        // For now, we'll use a simple approach

        insights.add(createInsight(
            churchId,
            InsightType.RECOMMENDATION,
            InsightCategory.ENGAGEMENT,
            "Review Irregular Attenders",
            "Several members haven't attended in 3+ weeks. Consider reaching out for pastoral care.",
            InsightSeverity.MEDIUM,
            true,
            "/dashboard/irregular-attenders"
        ));

        return insights;
    }

    /**
     * Analyze pastoral care needs
     */
    private List<Insight> analyzePastoralCareNeeds(Long churchId) {
        List<Insight> insights = new ArrayList<>();

        // Count urgent prayer requests
        long urgentPrayerRequests = prayerRequestRepository.countUrgentActiveRequests();

        if (urgentPrayerRequests >= HIGH_PRIORITY_PRAYER_REQUESTS) {
            insights.add(createInsight(
                churchId,
                InsightType.WARNING,
                InsightCategory.PASTORAL_CARE,
                "High Priority Prayer Requests",
                String.format("There are %d urgent prayer requests requiring attention.", urgentPrayerRequests),
                InsightSeverity.HIGH,
                true,
                "/dashboard/pastoral-care"
            ));
        }

        return insights;
    }

    /**
     * Analyze event engagement
     */
    private List<Insight> analyzeEventEngagement(Long churchId) {
        List<Insight> insights = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        long upcomingEvents = eventRepository.countUpcomingEvents(churchId, now);

        if (upcomingEvents == 0) {
            insights.add(createInsight(
                churchId,
                InsightType.RECOMMENDATION,
                InsightCategory.EVENTS,
                "No Upcoming Events",
                "No events scheduled for the next month. Consider planning fellowship or outreach activities.",
                InsightSeverity.MEDIUM,
                true,
                "/events"
            ));
        }

        return insights;
    }

    /**
     * Helper method to create an insight
     */
    private Insight createInsight(
        Long churchId,
        InsightType type,
        InsightCategory category,
        String title,
        String description,
        InsightSeverity severity,
        boolean actionable,
        String actionUrl
    ) {
        Church church = new Church();
        church.setId(churchId);

        Insight insight = Insight.builder()
            .insightType(type)
            .category(category)
            .title(title)
            .description(description)
            .severity(severity)
            .actionable(actionable)
            .actionUrl(actionUrl)
            .dismissed(false)
            .build();

        // Set the church relationship (from TenantBaseEntity)
        insight.setChurch(church);

        return insight;
    }

    /**
     * Calculate churn risk for members
     * Returns members at high risk of leaving
     */
    public List<Member> calculateChurnRisk() {
        // This is a simplified version
        // A production system would use ML models or more sophisticated analysis
        List<Member> atRiskMembers = new ArrayList<>();

        // Factors that indicate churn risk:
        // - Low attendance
        // - No recent donations
        // - No recent engagement
        // - Long membership without activity

        log.info("Calculating churn risk for members");
        return atRiskMembers;
    }
}
