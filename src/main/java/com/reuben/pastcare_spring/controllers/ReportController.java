package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.services.ReportService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

/**
 * REST API controller for report management.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final RequestContextUtil requestContextUtil;

    // ========== PRE-BUILT REPORTS ==========

    /**
     * Get all available pre-built report types.
     */
    @RequirePermission(Permission.REPORT_VIEW)
    @GetMapping("/pre-built")
    public ResponseEntity<List<ReportTypeInfo>> getPrebuiltReports() {
        List<ReportTypeInfo> reportTypes = reportService.getPrebuiltReports();
        return ResponseEntity.ok(reportTypes);
    }

    /**
     * Generate a report (pre-built or custom).
     */
    @RequirePermission(Permission.REPORT_GENERATE)
    @PostMapping("/generate")
    public ResponseEntity<ReportExecutionResponse> generateReport(
            @RequestBody GenerateReportRequest request,
            HttpServletRequest httpRequest) {

        Long userId = requestContextUtil.extractUserId(httpRequest);
        Long churchId = requestContextUtil.extractChurchId(httpRequest);

        ReportExecutionResponse response = reportService.generateReport(
                request,
                userId,
                churchId
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Download a generated report.
     */
    @RequirePermission(Permission.REPORT_VIEW)
    @GetMapping("/executions/{id}/download")
    public ResponseEntity<byte[]> downloadReport(
            @PathVariable Long id,
            HttpServletRequest httpRequest) throws IOException {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        byte[] reportData = reportService.downloadReport(id, churchId);

        // Get execution to determine filename and content type
        ReportExecutionResponse execution = reportService.getReportHistory(id, churchId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Report execution not found"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(execution.getFormat().getMimeType()));
        headers.setContentDispositionFormData("attachment", execution.getOutputFileName());

        return new ResponseEntity<>(reportData, headers, HttpStatus.OK);
    }

    // ========== CUSTOM REPORTS ==========

    /**
     * Create a new custom report.
     */
    @RequirePermission(Permission.REPORT_GENERATE)
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @RequestBody ReportRequest request,
            HttpServletRequest httpRequest) {

        Long userId = requestContextUtil.extractUserId(httpRequest);
        Long churchId = requestContextUtil.extractChurchId(httpRequest);

        ReportResponse response = reportService.createReport(
                request,
                userId,
                churchId
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all reports for the current church.
     */
    @RequirePermission(Permission.REPORT_VIEW)
    @GetMapping
    public ResponseEntity<List<ReportResponse>> getAllReports(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<ReportResponse> reports = reportService.getAllReports(churchId);
        return ResponseEntity.ok(reports);
    }

    /**
     * Get a specific report by ID.
     */
    @RequirePermission(Permission.REPORT_VIEW)
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReportById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        ReportResponse response = reportService.getReportById(id, churchId);
        return ResponseEntity.ok(response);
    }

    /**
     * Update an existing report.
     */
    @RequirePermission(Permission.REPORT_GENERATE)
    @PutMapping("/{id}")
    public ResponseEntity<ReportResponse> updateReport(
            @PathVariable Long id,
            @RequestBody ReportRequest request,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        ReportResponse response = reportService.updateReport(id, request, churchId);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete a report.
     */
    @RequirePermission(Permission.REPORT_GENERATE)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        reportService.deleteReport(id, churchId);
        return ResponseEntity.noContent().build();
    }

    // ========== REPORT EXECUTION HISTORY ==========

    /**
     * Get execution history for a specific report.
     */
    @RequirePermission(Permission.REPORT_VIEW)
    @GetMapping("/{id}/executions")
    public ResponseEntity<List<ReportExecutionResponse>> getReportHistory(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<ReportExecutionResponse> history = reportService.getReportHistory(
                id,
                churchId
        );

        return ResponseEntity.ok(history);
    }

    /**
     * Get current user's report execution history.
     */
    @RequirePermission(Permission.REPORT_VIEW)
    @GetMapping("/executions/my")
    public ResponseEntity<List<ReportExecutionResponse>> getMyReportHistory(
            HttpServletRequest httpRequest) {

        Long userId = requestContextUtil.extractUserId(httpRequest);
        Long churchId = requestContextUtil.extractChurchId(httpRequest);

        List<ReportExecutionResponse> history = reportService.getUserReportHistory(
                userId,
                churchId
        );

        return ResponseEntity.ok(history);
    }

    /**
     * Get recent report executions for the church.
     */
    @RequirePermission(Permission.REPORT_VIEW)
    @GetMapping("/executions/recent")
    public ResponseEntity<List<ReportExecutionResponse>> getRecentExecutions(
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<ReportExecutionResponse> executions = reportService.getRecentExecutions(
                churchId,
                limit
        );

        return ResponseEntity.ok(executions);
    }

    // ========== REPORT TEMPLATES ==========

    /**
     * Save a report as a template.
     */
    @RequirePermission(Permission.REPORT_GENERATE)
    @PostMapping("/{id}/save-template")
    public ResponseEntity<ReportResponse> saveAsTemplate(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        ReportResponse response = reportService.saveAsTemplate(id, churchId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all report templates.
     */
    @RequirePermission(Permission.REPORT_VIEW)
    @GetMapping("/templates")
    public ResponseEntity<List<ReportResponse>> getReportTemplates(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<ReportResponse> templates = reportService.getReportTemplates(churchId);
        return ResponseEntity.ok(templates);
    }

    // ========== REPORT SHARING ==========

    /**
     * Share a report with other users.
     */
    @RequirePermission(Permission.REPORT_GENERATE)
    @PostMapping("/{id}/share")
    public ResponseEntity<Void> shareReport(
            @PathVariable Long id,
            @RequestBody List<Long> userIds,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        reportService.shareReport(id, userIds, churchId);
        return ResponseEntity.ok().build();
    }

    /**
     * Unshare a report.
     */
    @RequirePermission(Permission.REPORT_GENERATE)
    @DeleteMapping("/{id}/share")
    public ResponseEntity<Void> unshareReport(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {

        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        reportService.unshareReport(id, churchId);
        return ResponseEntity.noContent().build();
    }
}
