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
