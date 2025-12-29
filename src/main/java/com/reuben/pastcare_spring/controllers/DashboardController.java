package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.services.DashboardService;
import com.reuben.pastcare_spring.services.DashboardLayoutService;
import com.reuben.pastcare_spring.services.DashboardTemplateService;
import com.reuben.pastcare_spring.services.GoalService;
import com.reuben.pastcare_spring.services.InsightService;
import com.reuben.pastcare_spring.security.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  private final DashboardLayoutService dashboardLayoutService;
  private final DashboardTemplateService dashboardTemplateService;
  private final GoalService goalService;
  private final InsightService insightService;
  private final JwtUtil jwtUtil;

  /**
   * Get complete dashboard data.
   *
   * @return Complete dashboard with stats, care needs, events, and activities
   */
  @GetMapping
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
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
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get fellowship health overview", description = "Returns fellowship comparison data ranked by health status and size")
  public ResponseEntity<List<FellowshipComparisonResponse>> getFellowshipHealthOverview(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<FellowshipComparisonResponse> fellowshipHealth = dashboardService.getFellowshipHealthOverview(userId);
    return ResponseEntity.ok(fellowshipHealth);
  }

  // Dashboard Phase 3: Additional Module Widgets

  /**
   * Get donation statistics.
   * Dashboard Phase 3: Donations Widget
   *
   * @return Donation statistics including totals and time-based breakdowns
   */
  @GetMapping("/donations")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get donation statistics", description = "Returns donation statistics for all time, this week, and this month")
  public ResponseEntity<DonationStatsResponse> getDonationStats(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    DonationStatsResponse stats = dashboardService.getDonationStats(userId);
    return ResponseEntity.ok(stats);
  }

  /**
   * Get crisis management statistics.
   * Dashboard Phase 3: Crisis Management Widget
   *
   * @return Crisis statistics including active, resolved, and urgent crises
   */
  @GetMapping("/crises")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get crisis statistics", description = "Returns crisis management statistics including active and resolved crises")
  public ResponseEntity<CrisisStatsResponse> getCrisisStats(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    CrisisStatsResponse stats = dashboardService.getCrisisStats(userId);
    return ResponseEntity.ok(stats);
  }

  /**
   * Get upcoming counseling sessions.
   * Dashboard Phase 3: Counseling Sessions Widget
   *
   * @return List of upcoming counseling sessions this week
   */
  @GetMapping("/counseling")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get upcoming counseling sessions", description = "Returns scheduled and confirmed counseling sessions for this week")
  public ResponseEntity<List<CounselingSessionResponse>> getUpcomingCounselingSessions(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<CounselingSessionResponse> sessions = dashboardService.getUpcomingCounselingSessions(userId);
    return ResponseEntity.ok(sessions);
  }

  // ==================== Dashboard Phase 2.1: Custom Layouts ====================

  /**
   * Get all available widgets for current user (filtered by role).
   * Dashboard Phase 2.1: Custom Layouts MVP
   */
  @GetMapping("/widgets/available")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get available widgets", description = "Returns widgets available to current user based on role")
  public ResponseEntity<List<WidgetResponse>> getAvailableWidgets(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<WidgetResponse> widgets = dashboardLayoutService.getAvailableWidgets(userId);
    return ResponseEntity.ok(widgets);
  }

  /**
   * Get user's dashboard layout.
   * Dashboard Phase 2.1: Custom Layouts MVP
   */
  @GetMapping("/layout")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get dashboard layout", description = "Returns user's customized dashboard layout")
  public ResponseEntity<DashboardLayoutResponse> getLayout(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    DashboardLayoutResponse layout = dashboardLayoutService.getUserLayout(userId);
    return ResponseEntity.ok(layout);
  }

  /**
   * Save/update dashboard layout.
   * Dashboard Phase 2.1: Custom Layouts MVP
   */
  @PostMapping("/layout")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Save dashboard layout", description = "Save user's customized dashboard layout")
  public ResponseEntity<DashboardLayoutResponse> saveLayout(
      HttpServletRequest request,
      @RequestBody DashboardLayoutRequest layoutRequest
  ) {
    Long userId = extractUserIdFromRequest(request);
    DashboardLayoutResponse layout = dashboardLayoutService.saveLayout(userId, layoutRequest);
    return ResponseEntity.ok(layout);
  }

  /**
   * Reset to default layout.
   * Dashboard Phase 2.1: Custom Layouts MVP
   */
  @PostMapping("/layout/reset")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Reset layout", description = "Reset dashboard to default layout")
  public ResponseEntity<DashboardLayoutResponse> resetLayout(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    DashboardLayoutResponse layout = dashboardLayoutService.resetToDefault(userId);
    return ResponseEntity.ok(layout);
  }

  // ==================== Dashboard Phase 2.2: Role-Based Templates ====================

  /**
   * Get all templates available for current user's role.
   * Dashboard Phase 2.2: Role-Based Templates
   */
  @GetMapping("/templates")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get available templates", description = "Returns templates available for current user's role")
  public ResponseEntity<List<DashboardTemplateResponse>> getTemplates(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    List<DashboardTemplateResponse> templates = dashboardTemplateService.getTemplatesForRole(userId);
    return ResponseEntity.ok(templates);
  }

  /**
   * Get a specific template by ID.
   * Dashboard Phase 2.2: Role-Based Templates
   */
  @GetMapping("/templates/{id}")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get template", description = "Returns specific template details")
  public ResponseEntity<DashboardTemplateResponse> getTemplate(@PathVariable Long id) {
    DashboardTemplateResponse template = dashboardTemplateService.getTemplate(id);
    return ResponseEntity.ok(template);
  }

  /**
   * Get templates for a specific role (admin only).
   * Dashboard Phase 2.2: Role-Based Templates
   */
  @GetMapping("/templates/role/{role}")
  @Operation(summary = "Get templates for role", description = "Returns templates for specific role (admin only)")
  public ResponseEntity<List<DashboardTemplateResponse>> getTemplatesForRole(@PathVariable Role role) {
    List<DashboardTemplateResponse> templates = dashboardTemplateService.getTemplatesForSpecificRole(role);
    return ResponseEntity.ok(templates);
  }

  /**
   * Create a new custom template (admin only).
   * Dashboard Phase 2.2: Role-Based Templates
   */
  @PostMapping("/templates")
  @Operation(summary = "Create template", description = "Create new dashboard template (admin only)")
  public ResponseEntity<DashboardTemplateResponse> createTemplate(
      HttpServletRequest request,
      @RequestBody DashboardTemplateRequest templateRequest
  ) {
    Long userId = extractUserIdFromRequest(request);
    DashboardTemplateResponse template = dashboardTemplateService.createTemplate(templateRequest, userId);
    return ResponseEntity.ok(template);
  }

  /**
   * Update an existing template (admin only).
   * Dashboard Phase 2.2: Role-Based Templates
   */
  @PutMapping("/templates/{id}")
  @Operation(summary = "Update template", description = "Update existing dashboard template (admin only)")
  public ResponseEntity<DashboardTemplateResponse> updateTemplate(
      @PathVariable Long id,
      @RequestBody DashboardTemplateRequest templateRequest
  ) {
    DashboardTemplateResponse template = dashboardTemplateService.updateTemplate(id, templateRequest);
    return ResponseEntity.ok(template);
  }

  /**
   * Delete a template (admin only).
   * Dashboard Phase 2.2: Role-Based Templates
   */
  @DeleteMapping("/templates/{id}")
  @Operation(summary = "Delete template", description = "Delete dashboard template (admin only)")
  public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
    dashboardTemplateService.deleteTemplate(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Apply a template to user's dashboard.
   * Dashboard Phase 2.2: Role-Based Templates
   */
  @PostMapping("/templates/{id}/apply")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Apply template", description = "Apply template to user's dashboard")
  public ResponseEntity<Void> applyTemplate(
      HttpServletRequest request,
      @PathVariable Long id
  ) {
    Long userId = extractUserIdFromRequest(request);
    dashboardTemplateService.applyTemplate(userId, id);
    return ResponseEntity.ok().build();
  }

  // ==================== Dashboard Phase 2.3: Goal Tracking ====================

  /**
   * Get all active goals.
   * Dashboard Phase 2.3: Goal Tracking
   */
  @GetMapping("/goals")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get active goals", description = "Returns all active goals for the church")
  public ResponseEntity<List<GoalResponse>> getActiveGoals() {
    List<GoalResponse> goals = goalService.getActiveGoals();
    return ResponseEntity.ok(goals);
  }

  /**
   * Get a specific goal by ID.
   * Dashboard Phase 2.3: Goal Tracking
   */
  @GetMapping("/goals/{id}")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get goal", description = "Returns specific goal details")
  public ResponseEntity<GoalResponse> getGoal(@PathVariable Long id) {
    GoalResponse goal = goalService.getGoal(id);
    return ResponseEntity.ok(goal);
  }

  /**
   * Create a new goal.
   * Dashboard Phase 2.3: Goal Tracking
   */
  @PostMapping("/goals")
  @Operation(summary = "Create goal", description = "Create new goal for tracking")
  public ResponseEntity<GoalResponse> createGoal(
      HttpServletRequest request,
      @RequestBody GoalRequest goalRequest
  ) {
    Long userId = extractUserIdFromRequest(request);
    GoalResponse goal = goalService.createGoal(goalRequest, userId);
    return ResponseEntity.ok(goal);
  }

  /**
   * Update an existing goal.
   * Dashboard Phase 2.3: Goal Tracking
   */
  @PutMapping("/goals/{id}")
  @Operation(summary = "Update goal", description = "Update existing goal")
  public ResponseEntity<GoalResponse> updateGoal(
      @PathVariable Long id,
      @RequestBody GoalRequest goalRequest
  ) {
    GoalResponse goal = goalService.updateGoal(id, goalRequest);
    return ResponseEntity.ok(goal);
  }

  /**
   * Delete a goal.
   * Dashboard Phase 2.3: Goal Tracking
   */
  @DeleteMapping("/goals/{id}")
  @Operation(summary = "Delete goal", description = "Delete goal permanently")
  public ResponseEntity<Void> deleteGoal(@PathVariable Long id) {
    goalService.deleteGoal(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Manually recalculate progress for a specific goal.
   * Dashboard Phase 2.3: Goal Tracking
   */
  @PostMapping("/goals/{id}/recalculate")
  @Operation(summary = "Recalculate goal progress", description = "Manually trigger progress update for a goal")
  public ResponseEntity<GoalResponse> recalculateGoalProgress(@PathVariable Long id) {
    GoalResponse goal = goalService.recalculateProgress(id);
    return ResponseEntity.ok(goal);
  }

  // ==================== Dashboard Phase 2.4: Advanced Analytics ====================

  /**
   * Get all active insights.
   * Dashboard Phase 2.4: Advanced Analytics
   */
  @GetMapping("/insights")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get insights", description = "Returns active AI-generated insights and recommendations")
  public ResponseEntity<List<InsightResponse>> getInsights() {
    List<InsightResponse> insights = insightService.getActiveInsights();
    return ResponseEntity.ok(insights);
  }

  /**
   * Get high priority insights.
   * Dashboard Phase 2.4: Advanced Analytics
   */
  @GetMapping("/insights/priority")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get high priority insights", description = "Returns high and critical priority insights")
  public ResponseEntity<List<InsightResponse>> getHighPriorityInsights() {
    List<InsightResponse> insights = insightService.getHighPriorityInsights();
    return ResponseEntity.ok(insights);
  }

  /**
   * Get actionable insights.
   * Dashboard Phase 2.4: Advanced Analytics
   */
  @GetMapping("/insights/actionable")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Get actionable insights", description = "Returns insights requiring action")
  public ResponseEntity<List<InsightResponse>> getActionableInsights() {
    List<InsightResponse> insights = insightService.getActionableInsights();
    return ResponseEntity.ok(insights);
  }

  /**
   * Generate new insights by analyzing church data.
   * Dashboard Phase 2.4: Advanced Analytics
   */
  @PostMapping("/insights/generate")
  @Operation(summary = "Generate insights", description = "Trigger insight generation from church data (admin only)")
  public ResponseEntity<List<InsightResponse>> generateInsights(HttpServletRequest request) {
    Long userId = extractUserIdFromRequest(request);
    // Get churchId from user
    // For now, we'll pass a placeholder - in production, extract from authenticated user context
    List<InsightResponse> insights = insightService.generateInsights(userId);
    return ResponseEntity.ok(insights);
  }

  /**
   * Dismiss an insight.
   * Dashboard Phase 2.4: Advanced Analytics
   */
  @PostMapping("/insights/{id}/dismiss")
  @RequirePermission(Permission.MEMBER_VIEW_ALL)
  @Operation(summary = "Dismiss insight", description = "Mark an insight as dismissed")
  public ResponseEntity<InsightResponse> dismissInsight(
      HttpServletRequest request,
      @PathVariable Long id
  ) {
    Long userId = extractUserIdFromRequest(request);
    InsightResponse insight = insightService.dismissInsight(id, userId);
    return ResponseEntity.ok(insight);
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
