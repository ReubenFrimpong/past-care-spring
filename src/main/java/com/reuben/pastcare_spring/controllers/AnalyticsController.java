package com.reuben.pastcare_spring.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.AttendanceAnalyticsResponse;
import com.reuben.pastcare_spring.dtos.AttendanceSummaryResponse;
import com.reuben.pastcare_spring.dtos.MemberEngagementResponse;
import com.reuben.pastcare_spring.dtos.ServiceTypeAnalyticsResponse;
import com.reuben.pastcare_spring.services.AttendanceAnalyticsService;

import lombok.RequiredArgsConstructor;

/**
 * Phase 2: Attendance Analytics Controller
 * REST API endpoints for attendance analytics and reporting
 */
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AttendanceAnalyticsService analyticsService;

    /**
     * Get comprehensive attendance analytics for a date range
     *
     * GET /api/analytics/attendance?churchId=1&startDate=2024-01-01&endDate=2024-12-31
     *
     * @param churchId The church ID
     * @param startDate Start date (optional, defaults to 30 days ago)
     * @param endDate End date (optional, defaults to today)
     * @return Comprehensive analytics response
     */
    @GetMapping("/attendance")
    public ResponseEntity<AttendanceAnalyticsResponse> getAttendanceAnalytics(
            @RequestParam Long churchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Default to last 30 days if not specified
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        AttendanceAnalyticsResponse analytics = analyticsService.getAttendanceAnalytics(churchId, start, end);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Get attendance summary with key metrics
     *
     * GET /api/analytics/summary?churchId=1&startDate=2024-01-01&endDate=2024-12-31
     *
     * @param churchId The church ID
     * @param startDate Start date (optional, defaults to 30 days ago)
     * @param endDate End date (optional, defaults to today)
     * @return Summary statistics
     */
    @GetMapping("/summary")
    public ResponseEntity<AttendanceSummaryResponse> getAttendanceSummary(
            @RequestParam Long churchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        AttendanceSummaryResponse summary = analyticsService.getAttendanceSummary(churchId, start, end);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get member engagement metrics
     *
     * GET /api/analytics/member-engagement?churchId=1&startDate=2024-01-01&endDate=2024-12-31
     *
     * @param churchId The church ID
     * @param startDate Start date (optional, defaults to 30 days ago)
     * @param endDate End date (optional, defaults to today)
     * @return List of member engagement metrics
     */
    @GetMapping("/member-engagement")
    public ResponseEntity<List<MemberEngagementResponse>> getMemberEngagement(
            @RequestParam Long churchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        List<MemberEngagementResponse> engagement = analyticsService.getMemberEngagement(churchId, start, end);
        return ResponseEntity.ok(engagement);
    }

    /**
     * Get service type analytics
     *
     * GET /api/analytics/service-types?churchId=1&startDate=2024-01-01&endDate=2024-12-31
     *
     * @param churchId The church ID
     * @param startDate Start date (optional, defaults to 30 days ago)
     * @param endDate End date (optional, defaults to today)
     * @return List of service type analytics
     */
    @GetMapping("/service-types")
    public ResponseEntity<List<ServiceTypeAnalyticsResponse>> getServiceTypeAnalytics(
            @RequestParam Long churchId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        List<ServiceTypeAnalyticsResponse> serviceTypes = analyticsService.getServiceTypeAnalytics(churchId, start, end);
        return ResponseEntity.ok(serviceTypes);
    }

    /**
     * Get analytics for a specific period (convenience endpoints)
     */

    @GetMapping("/summary/this-week")
    public ResponseEntity<AttendanceSummaryResponse> getThisWeekSummary(@RequestParam Long churchId) {
        LocalDate start = LocalDate.now().minusDays(7);
        LocalDate end = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getAttendanceSummary(churchId, start, end));
    }

    @GetMapping("/summary/this-month")
    public ResponseEntity<AttendanceSummaryResponse> getThisMonthSummary(@RequestParam Long churchId) {
        LocalDate start = LocalDate.now().withDayOfMonth(1);
        LocalDate end = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getAttendanceSummary(churchId, start, end));
    }

    @GetMapping("/summary/this-year")
    public ResponseEntity<AttendanceSummaryResponse> getThisYearSummary(@RequestParam Long churchId) {
        LocalDate start = LocalDate.now().withDayOfYear(1);
        LocalDate end = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getAttendanceSummary(churchId, start, end));
    }

    @GetMapping("/summary/last-30-days")
    public ResponseEntity<AttendanceSummaryResponse> getLast30DaysSummary(@RequestParam Long churchId) {
        LocalDate start = LocalDate.now().minusDays(30);
        LocalDate end = LocalDate.now();
        return ResponseEntity.ok(analyticsService.getAttendanceSummary(churchId, start, end));
    }
}
