package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.services.DashboardService;
import com.reuben.pastcare_spring.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Dashboard controller providing aggregated data for church management dashboard.
 * All endpoints are secured and require authentication.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard data endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class DashboardController {

  private final DashboardService dashboardService;
  private final JwtUtil jwtUtil;

  /**
   * Get complete dashboard data.
   *
   * @return Complete dashboard with stats, care needs, events, and activities
   */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get complete dashboard data", description = "Returns all dashboard sections including stats, pastoral care needs, upcoming events, and recent activities")
  public ResponseEntity<DashboardResponse> getDashboard(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    DashboardResponse dashboard = dashboardService.getDashboardData(userId);
    return ResponseEntity.ok(dashboard);
  }

  /**
   * Get dashboard statistics only.
   *
   * @return Dashboard statistics (active members, prayer needs, events, attendance rate)
   */
  @GetMapping("/stats")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get dashboard statistics", description = "Returns dashboard statistics including member count, prayer requests, events, and attendance rate")
  public ResponseEntity<DashboardStatsResponse> getStats() {
    DashboardStatsResponse stats = dashboardService.getStats();
    return ResponseEntity.ok(stats);
  }

  /**
   * Get pastoral care needs.
   *
   * @return List of pastoral care needs requiring attention
   */
  @GetMapping("/pastoral-care")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get pastoral care needs", description = "Returns list of members requiring pastoral care attention")
  public ResponseEntity<List<PastoralCareNeedResponse>> getPastoralCareNeeds() {
    List<PastoralCareNeedResponse> needs = dashboardService.getPastoralCareNeeds();
    return ResponseEntity.ok(needs);
  }

  /**
   * Get upcoming events.
   *
   * @return List of upcoming church events
   */
  @GetMapping("/events")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get upcoming events", description = "Returns list of upcoming church events")
  public ResponseEntity<List<UpcomingEventResponse>> getUpcomingEvents() {
    List<UpcomingEventResponse> events = dashboardService.getUpcomingEvents();
    return ResponseEntity.ok(events);
  }

  /**
   * Get recent activities.
   *
   * @return List of recent church activities
   */
  @GetMapping("/activities")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get recent activities", description = "Returns list of recent church activities including new members, donations, and attendance updates")
  public ResponseEntity<List<RecentActivityResponse>> getRecentActivities() {
    List<RecentActivityResponse> activities = dashboardService.getRecentActivities();
    return ResponseEntity.ok(activities);
  }

  /**
   * Get location-based member statistics for map visualization.
   *
   * @return List of locations with member counts and GPS coordinates
   */
  @GetMapping("/location-stats")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get location statistics", description = "Returns member distribution by geographic location for map visualization")
  public ResponseEntity<List<LocationStatsResponse>> getLocationStatistics(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<LocationStatsResponse> locationStats = dashboardService.getLocationStatistics(userId);
    return ResponseEntity.ok(locationStats);
  }

  // Dashboard Phase 1: Enhanced Widgets

  /**
   * Get members with birthdays this week.
   *
   * @return List of members celebrating birthdays this week
   */
  @GetMapping("/birthdays")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get birthdays this week", description = "Returns members with birthdays this week")
  public ResponseEntity<List<BirthdayResponse>> getBirthdaysThisWeek(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<BirthdayResponse> birthdays = dashboardService.getBirthdaysThisWeek(userId);
    return ResponseEntity.ok(birthdays);
  }

  /**
   * Get members with membership anniversaries this month.
   *
   * @return List of members celebrating membership anniversaries
   */
  @GetMapping("/anniversaries")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get anniversaries this month", description = "Returns members with membership anniversaries this month")
  public ResponseEntity<List<AnniversaryResponse>> getAnniversariesThisMonth(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<AnniversaryResponse> anniversaries = dashboardService.getAnniversariesThisMonth(userId);
    return ResponseEntity.ok(anniversaries);
  }

  /**
   * Get irregular attenders (members absent 3+ weeks).
   *
   * @return List of members requiring follow-up due to irregular attendance
   */
  @GetMapping("/irregular-attenders")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get irregular attenders", description = "Returns members who haven't attended in 3+ weeks")
  public ResponseEntity<List<IrregularAttenderResponse>> getIrregularAttenders(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<IrregularAttenderResponse> irregularAttenders = dashboardService.getIrregularAttenders(userId);
    return ResponseEntity.ok(irregularAttenders);
  }

  /**
   * Get member growth trend (last 6 months).
   *
   * @return Monthly member growth data
   */
  @GetMapping("/member-growth")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get member growth trend", description = "Returns member growth data for the last 6 months")
  public ResponseEntity<List<MemberGrowthResponse>> getMemberGrowthTrend(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<MemberGrowthResponse> growthTrend = dashboardService.getMemberGrowthTrend(userId);
    return ResponseEntity.ok(growthTrend);
  }

  // Dashboard Phase 1: Additional Widgets

  /**
   * Get attendance summary for this month.
   *
   * @return Attendance summary with key metrics
   */
  @GetMapping("/attendance-summary")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get attendance summary", description = "Returns attendance summary for current month")
  public ResponseEntity<AttendanceSummaryResponse> getAttendanceSummary(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    AttendanceSummaryResponse summary = dashboardService.getAttendanceSummaryThisMonth(userId);
    return ResponseEntity.ok(summary);
  }

  /**
   * Get service type analytics (last 30 days).
   *
   * @return List of service type analytics
   */
  @GetMapping("/service-analytics")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get service type analytics", description = "Returns service type breakdown for last 30 days")
  public ResponseEntity<List<ServiceTypeAnalyticsResponse>> getServiceAnalytics(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<ServiceTypeAnalyticsResponse> analytics = dashboardService.getServiceTypeAnalytics(userId);
    return ResponseEntity.ok(analytics);
  }

  /**
   * Get top active members (last 90 days).
   *
   * @return List of top 10 most active members
   */
  @GetMapping("/top-active-members")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get top active members", description = "Returns top 10 most active members based on attendance")
  public ResponseEntity<List<MemberEngagementResponse>> getTopActiveMembers(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<MemberEngagementResponse> topMembers = dashboardService.getTopActiveMembers(userId);
    return ResponseEntity.ok(topMembers);
  }

  /**
   * Get fellowship health overview.
   *
   * @return Fellowship comparison data ranked by health
   */
  @GetMapping("/fellowship-health")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get fellowship health overview", description = "Returns fellowship comparison data ranked by health status and size")
  public ResponseEntity<List<FellowshipComparisonResponse>> getFellowshipHealthOverview(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<FellowshipComparisonResponse> fellowshipHealth = dashboardService.getFellowshipHealthOverview(userId);
    return ResponseEntity.ok(fellowshipHealth);
  }

  /**
   * Extract user ID from JWT token in request.
   * Checks both Authorization header and cookies.
   */
  private Long extractUserIdFromRequest(HttpServletRequest request) {
    String token = null;

    // If not found, try cookie
    if (token == null && request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("access_token".equals(cookie.getName())) {
          token = cookie.getValue();
          break;
        }
      }
    }

    if (token != null) {
      return jwtUtil.extractUserId(token);
    }

    throw new RuntimeException("No valid JWT token found");
  }
}
