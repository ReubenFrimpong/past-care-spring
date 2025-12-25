package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Dashboard service providing aggregated data for church management dashboard.
 * All data is real, queried from the database.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

  private final UserRepository userRepository;
  private final MemberRepository memberRepository;
  private final AttendanceAnalyticsService attendanceAnalyticsService;

  /**
   * Get complete dashboard data for the current user.
   *
   * @param userId Current user ID from JWT
   * @return Complete dashboard data with stats, care needs, events, and activities
   */
  public DashboardResponse getDashboardData(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    return new DashboardResponse(
        user.getName(),
        getStats(userId),
        getPastoralCareNeeds(userId),
        getUpcomingEvents(userId),
        getRecentActivities(userId)
    );
  }

  /**
   * Get dashboard statistics (real data from database).
   *
   * @param userId Current user ID from JWT
   * @return Dashboard statistics
   */
  private DashboardStatsResponse getStats(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new DashboardStatsResponse(0, 0, 0, "0%");
    }

    // Get real member count
    long activeMembers = memberRepository.countByChurch(user.getChurch());

    // TODO: Implement real queries for these stats when features are added
    int needPrayer = 0; // Will be implemented with Prayer Requests module
    int eventsThisWeek = 0; // Will be implemented with Events module
    String attendanceRate = "0%"; // Will be implemented with attendance statistics

    return new DashboardStatsResponse(
        (int) activeMembers,
        needPrayer,
        eventsThisWeek,
        attendanceRate
    );
  }

  /**
   * Get pastoral care needs (real data from database).
   * TODO: Implement when Prayer Requests/Pastoral Care module is added.
   *
   * @param userId Current user ID from JWT
   * @return List of pastoral care needs
   */
  private List<PastoralCareNeedResponse> getPastoralCareNeeds(Long userId) {
    // Return empty list - will be implemented with Pastoral Care module
    return new ArrayList<>();
  }

  /**
   * Get upcoming events (real data from database).
   * TODO: Implement when Events module is added.
   *
   * @param userId Current user ID from JWT
   * @return List of upcoming events
   */
  private List<UpcomingEventResponse> getUpcomingEvents(Long userId) {
    // Return empty list - will be implemented with Events module
    return new ArrayList<>();
  }

  /**
   * Get recent activities (real data from database).
   * TODO: Implement when Activity Log module is added.
   *
   * @param userId Current user ID from JWT
   * @return List of recent activities
   */
  private List<RecentActivityResponse> getRecentActivities(Long userId) {
    // Return empty list - will be implemented with Activity Log module
    return new ArrayList<>();
  }

  /**
   * Get dashboard statistics only.
   */
  public DashboardStatsResponse getStats() {
    // This method is called without userId context, return empty stats
    return new DashboardStatsResponse(0, 0, 0, "0%");
  }

  /**
   * Get pastoral care needs only.
   */
  public List<PastoralCareNeedResponse> getPastoralCareNeeds() {
    return new ArrayList<>();
  }

  /**
   * Get upcoming events only.
   */
  public List<UpcomingEventResponse> getUpcomingEvents() {
    return new ArrayList<>();
  }

  /**
   * Get recent activities only.
   */
  public List<RecentActivityResponse> getRecentActivities() {
    return new ArrayList<>();
  }

  /**
   * Get location-based member statistics for map visualization.
   *
   * @param userId Current user ID from JWT
   * @return List of locations with member counts and GPS coordinates
   */
  public List<LocationStatsResponse> getLocationStatistics(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return memberRepository.getLocationStatistics(user.getChurch());
  }

  /**
   * Get members with birthdays this week (real data).
   * Dashboard Phase 1: Enhanced Widgets
   *
   * @param userId Current user ID from JWT
   * @return List of members with birthdays this week
   */
  public List<BirthdayResponse> getBirthdaysThisWeek(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return memberRepository.findMembersWithBirthdaysThisWeek(user.getChurch());
  }

  /**
   * Get members with anniversaries this month (real data).
   * Dashboard Phase 1: Enhanced Widgets
   *
   * @param userId Current user ID from JWT
   * @return List of members with anniversaries this month
   */
  public List<AnniversaryResponse> getAnniversariesThisMonth(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return memberRepository.findMembersWithAnniversariesThisMonth(user.getChurch());
  }

  /**
   * Get irregular attenders (members absent for 3+ consecutive weeks).
   * Dashboard Phase 1: Enhanced Widgets
   *
   * @param userId Current user ID from JWT
   * @return List of members who haven't attended in 3+ weeks
   */
  public List<IrregularAttenderResponse> getIrregularAttenders(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return memberRepository.findIrregularAttenders(user.getChurch(), 3); // 3 weeks threshold
  }

  /**
   * Get member growth trend for the last 6 months.
   * Dashboard Phase 1: Enhanced Widgets
   *
   * @param userId Current user ID from JWT
   * @return Monthly member growth data
   */
  public List<MemberGrowthResponse> getMemberGrowthTrend(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    return memberRepository.getMemberGrowthTrend(user.getChurch(), 6); // Last 6 months
  }

  /**
   * Get attendance summary for this month.
   * Dashboard Phase 1: Additional Widgets
   *
   * @param userId Current user ID from JWT
   * @return Attendance summary with key metrics
   */
  public AttendanceSummaryResponse getAttendanceSummaryThisMonth(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return null; // Frontend will handle null
    }

    LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
    LocalDate today = LocalDate.now();

    return attendanceAnalyticsService.getAttendanceSummary(user.getChurch().getId(), startOfMonth, today);
  }

  /**
   * Get service type analytics for the last 30 days.
   * Dashboard Phase 1: Additional Widgets
   *
   * @param userId Current user ID from JWT
   * @return List of service type analytics
   */
  public List<ServiceTypeAnalyticsResponse> getServiceTypeAnalytics(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    LocalDate start = LocalDate.now().minusDays(30);
    LocalDate end = LocalDate.now();

    return attendanceAnalyticsService.getServiceTypeAnalytics(user.getChurch().getId(), start, end);
  }

  /**
   * Get top active members based on attendance.
   * Dashboard Phase 1: Additional Widgets
   *
   * @param userId Current user ID from JWT
   * @return List of top 10 active members
   */
  public List<MemberEngagementResponse> getTopActiveMembers(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    if (user.getChurch() == null) {
      return new ArrayList<>();
    }

    LocalDate start = LocalDate.now().minusDays(90); // Last 90 days
    LocalDate end = LocalDate.now();

    List<MemberEngagementResponse> allEngagement = attendanceAnalyticsService.getMemberEngagement(
        user.getChurch().getId(), start, end);

    // Return top 10 by sessions attended
    return allEngagement.stream()
        .sorted((a, b) -> Long.compare(b.sessionsAttended(), a.sessionsAttended()))
        .limit(10)
        .toList();
  }
}
