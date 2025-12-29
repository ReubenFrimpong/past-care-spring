package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.enums.ExecutionStatus;
import com.reuben.pastcare_spring.enums.ReportFormat;
import com.reuben.pastcare_spring.enums.ReportType;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Main service for managing reports, executions, and orchestration.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportExecutionRepository reportExecutionRepository;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;
    private final ReportGeneratorService reportGeneratorService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // ========== REPORT CRUD ==========

    /**
     * Create a new report.
     */
    @Transactional
    public ReportResponse createReport(ReportRequest request, Long userId, Long churchId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found"));

        Report report = new Report();
        report.setName(request.getName());
        report.setDescription(request.getDescription());
        report.setReportType(request.getReportType());
        report.setIsCustom(false);  // Pre-built reports
        report.setFilters(request.getFilters());
        report.setFields(request.getFields());
        report.setSorting(request.getSorting());
        report.setGrouping(request.getGrouping());
        report.setCreatedBy(user);
        report.setChurch(church);
        report.setIsTemplate(request.getIsTemplate() != null ? request.getIsTemplate() : false);
        report.setIsShared(request.getIsShared() != null ? request.getIsShared() : false);

        if (request.getSharedWithUserIds() != null) {
            report.setSharedWithUserIds(request.getSharedWithUserIds());
        }

        Report saved = reportRepository.save(report);
        return toReportResponse(saved);
    }

    /**
     * Update an existing report.
     */
    @Transactional
    public ReportResponse updateReport(Long id, ReportRequest request, Long churchId) {
        Report report = reportRepository.findByIdAndChurchId(id, churchId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setName(request.getName());
        report.setDescription(request.getDescription());
        report.setReportType(request.getReportType());
        report.setFilters(request.getFilters());
        report.setFields(request.getFields());
        report.setSorting(request.getSorting());
        report.setGrouping(request.getGrouping());
        report.setIsTemplate(request.getIsTemplate() != null ? request.getIsTemplate() : report.getIsTemplate());
        report.setIsShared(request.getIsShared() != null ? request.getIsShared() : report.getIsShared());

        if (request.getSharedWithUserIds() != null) {
            report.setSharedWithUserIds(request.getSharedWithUserIds());
        }

        Report updated = reportRepository.save(report);
        return toReportResponse(updated);
    }

    /**
     * Delete a report.
     */
    @Transactional
    public void deleteReport(Long id, Long churchId) {
        Report report = reportRepository.findByIdAndChurchId(id, churchId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        reportRepository.delete(report);
    }

    /**
     * Get a report by ID.
     */
    public ReportResponse getReportById(Long id, Long churchId) {
        Report report = reportRepository.findByIdAndChurchId(id, churchId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        return toReportResponse(report);
    }

    /**
     * Get all reports for a church.
     */
    public List<ReportResponse> getAllReports(Long churchId) {
        List<Report> reports = reportRepository.findByChurchId(churchId);
        return reports.stream()
                .map(this::toReportResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all pre-built report types.
     */
    public List<ReportTypeInfo> getPrebuiltReports() {
        return List.of(
            new ReportTypeInfo(ReportType.MEMBER_DIRECTORY, ReportType.MEMBER_DIRECTORY.getDisplayName(),
                ReportType.MEMBER_DIRECTORY.getDescription(), ReportType.MEMBER_DIRECTORY.getCategory(), "pi-users"),
            new ReportTypeInfo(ReportType.BIRTHDAY_ANNIVERSARY_LIST, ReportType.BIRTHDAY_ANNIVERSARY_LIST.getDisplayName(),
                ReportType.BIRTHDAY_ANNIVERSARY_LIST.getDescription(), ReportType.BIRTHDAY_ANNIVERSARY_LIST.getCategory(), "pi-calendar"),
            new ReportTypeInfo(ReportType.INACTIVE_MEMBERS, ReportType.INACTIVE_MEMBERS.getDisplayName(),
                ReportType.INACTIVE_MEMBERS.getDescription(), ReportType.INACTIVE_MEMBERS.getCategory(), "pi-user-minus"),
            new ReportTypeInfo(ReportType.HOUSEHOLD_ROSTER, ReportType.HOUSEHOLD_ROSTER.getDisplayName(),
                ReportType.HOUSEHOLD_ROSTER.getDescription(), ReportType.HOUSEHOLD_ROSTER.getCategory(), "pi-home"),
            new ReportTypeInfo(ReportType.ATTENDANCE_SUMMARY, ReportType.ATTENDANCE_SUMMARY.getDisplayName(),
                ReportType.ATTENDANCE_SUMMARY.getDescription(), ReportType.ATTENDANCE_SUMMARY.getCategory(), "pi-check-circle"),
            new ReportTypeInfo(ReportType.FIRST_TIME_VISITORS, ReportType.FIRST_TIME_VISITORS.getDisplayName(),
                ReportType.FIRST_TIME_VISITORS.getDescription(), ReportType.FIRST_TIME_VISITORS.getCategory(), "pi-user-plus"),
            new ReportTypeInfo(ReportType.GIVING_SUMMARY, ReportType.GIVING_SUMMARY.getDisplayName(),
                ReportType.GIVING_SUMMARY.getDescription(), ReportType.GIVING_SUMMARY.getCategory(), "pi-dollar"),
            new ReportTypeInfo(ReportType.TOP_DONORS, ReportType.TOP_DONORS.getDisplayName(),
                ReportType.TOP_DONORS.getDescription(), ReportType.TOP_DONORS.getCategory(), "pi-star"),
            new ReportTypeInfo(ReportType.CAMPAIGN_PROGRESS, ReportType.CAMPAIGN_PROGRESS.getDisplayName(),
                ReportType.CAMPAIGN_PROGRESS.getDescription(), ReportType.CAMPAIGN_PROGRESS.getCategory(), "pi-chart-line"),
            new ReportTypeInfo(ReportType.FELLOWSHIP_ROSTER, ReportType.FELLOWSHIP_ROSTER.getDisplayName(),
                ReportType.FELLOWSHIP_ROSTER.getDescription(), ReportType.FELLOWSHIP_ROSTER.getCategory(), "pi-sitemap"),
            new ReportTypeInfo(ReportType.PASTORAL_CARE_SUMMARY, ReportType.PASTORAL_CARE_SUMMARY.getDisplayName(),
                ReportType.PASTORAL_CARE_SUMMARY.getDescription(), ReportType.PASTORAL_CARE_SUMMARY.getCategory(), "pi-heart"),
            new ReportTypeInfo(ReportType.EVENT_ATTENDANCE, ReportType.EVENT_ATTENDANCE.getDisplayName(),
                ReportType.EVENT_ATTENDANCE.getDescription(), ReportType.EVENT_ATTENDANCE.getCategory(), "pi-calendar-plus"),
            new ReportTypeInfo(ReportType.GROWTH_TREND, ReportType.GROWTH_TREND.getDisplayName(),
                ReportType.GROWTH_TREND.getDescription(), ReportType.GROWTH_TREND.getCategory(), "pi-chart-bar")
        );
    }

    /**
     * Get custom reports created by a user.
     */
    public List<ReportResponse> getCustomReports(Long userId, Long churchId) {
        List<Report> reports = reportRepository.findByChurchIdAndCreatedById(churchId, userId);
        return reports.stream()
                .filter(Report::getIsCustom)
                .map(this::toReportResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get shared reports.
     */
    public List<ReportResponse> getSharedReports(Long churchId) {
        List<Report> reports = reportRepository.findByChurchIdAndIsSharedTrue(churchId);
        return reports.stream()
                .map(this::toReportResponse)
                .collect(Collectors.toList());
    }

    // ========== REPORT EXECUTION ==========

    /**
     * Generate a report and create execution record.
     */
    @Transactional
    public ReportExecutionResponse generateReport(GenerateReportRequest request, Long userId, Long churchId) {
        long startTime = System.currentTimeMillis();

        // Find or create report
        Report report = null;
        if (request.getReportId() != null) {
            report = reportRepository.findByIdAndChurchId(request.getReportId(), churchId)
                    .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found"));

        // Create execution record
        ReportExecution execution = new ReportExecution();
        execution.setReport(report);
        execution.setExecutedBy(user);
        execution.setExecutionDate(LocalDateTime.now());
        execution.setFormat(request.getFormat());
        execution.setStatus(ExecutionStatus.RUNNING);
        execution.setChurch(church);

        try {
            String parameters = objectMapper.writeValueAsString(request);
            execution.setParameters(parameters);
        } catch (Exception e) {
            execution.setParameters("{}");
        }

        execution = reportExecutionRepository.save(execution);

        try {
            // Generate report
            byte[] reportData = reportGeneratorService.generateReport(request, churchId);

            // Store file (simplified - in production, upload to S3)
            String filename = generateFilename(request);
            execution.setOutputFileName(filename);
            execution.setOutputFileUrl("/api/reports/executions/" + execution.getId() + "/download");
            execution.setFileSizeBytes((long) reportData.length);
            execution.setRowCount(0);  // Could be extracted from data
            execution.setStatus(ExecutionStatus.COMPLETED);

            long executionTime = System.currentTimeMillis() - startTime;
            execution.setExecutionTimeMs(executionTime);

            execution = reportExecutionRepository.save(execution);

            return toReportExecutionResponse(execution);

        } catch (IOException e) {
            execution.setStatus(ExecutionStatus.FAILED);
            execution.setErrorMessage(e.getMessage());
            execution.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            reportExecutionRepository.save(execution);
            throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    /**
     * Download a generated report.
     */
    public byte[] downloadReport(Long executionId, Long churchId) throws IOException {
        ReportExecution execution = reportExecutionRepository.findByIdAndChurchId(executionId, churchId)
                .orElseThrow(() -> new IllegalArgumentException("Report execution not found"));

        if (execution.getStatus() != ExecutionStatus.COMPLETED) {
            throw new IllegalStateException("Report generation is not completed");
        }

        // In a real implementation, fetch from file storage (S3, local file system, etc.)
        // For now, regenerate the report
        GenerateReportRequest request = new GenerateReportRequest();
        request.setReportType(execution.getReport().getReportType());
        request.setFormat(execution.getFormat());

        return reportGeneratorService.generateReport(request, churchId);
    }

    /**
     * Get report execution history.
     */
    public List<ReportExecutionResponse> getReportHistory(Long reportId, Long churchId) {
        List<ReportExecution> executions = reportExecutionRepository.findByReportIdOrderByExecutionDateDesc(reportId);
        return executions.stream()
                .filter(e -> e.getChurch().getId().equals(churchId))
                .map(this::toReportExecutionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get user's report execution history.
     */
    public List<ReportExecutionResponse> getUserReportHistory(Long userId, Long churchId) {
        List<ReportExecution> executions = reportExecutionRepository.findByExecutedByIdOrderByExecutionDateDesc(userId);
        return executions.stream()
                .filter(e -> e.getChurch().getId().equals(churchId))
                .limit(50)
                .map(this::toReportExecutionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get recent executions for all users in a church.
     */
    public List<ReportExecutionResponse> getRecentExecutions(Long churchId, int limit) {
        List<ReportExecution> executions = reportExecutionRepository
                .findByChurchIdOrderByExecutionDateDesc(churchId, PageRequest.of(0, limit));
        return executions.stream()
                .map(this::toReportExecutionResponse)
                .collect(Collectors.toList());
    }

    // ========== REPORT SHARING ==========

    /**
     * Share a report with other users.
     */
    @Transactional
    public void shareReport(Long reportId, List<Long> userIds, Long churchId) {
        Report report = reportRepository.findByIdAndChurchId(reportId, churchId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setIsShared(true);
        report.setSharedWithUserIds(userIds);
        reportRepository.save(report);
    }

    /**
     * Unshare a report.
     */
    @Transactional
    public void unshareReport(Long reportId, Long churchId) {
        Report report = reportRepository.findByIdAndChurchId(reportId, churchId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setIsShared(false);
        report.getSharedWithUserIds().clear();
        reportRepository.save(report);
    }

    // ========== REPORT TEMPLATES ==========

    /**
     * Save a report as a template.
     */
    @Transactional
    public ReportResponse saveAsTemplate(Long reportId, Long churchId) {
        Report report = reportRepository.findByIdAndChurchId(reportId, churchId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));

        report.setIsTemplate(true);
        Report saved = reportRepository.save(report);
        return toReportResponse(saved);
    }

    /**
     * Get all report templates.
     */
    public List<ReportResponse> getReportTemplates(Long churchId) {
        List<Report> templates = reportRepository.findByChurchIdAndIsTemplate(churchId, true);
        return templates.stream()
                .map(this::toReportResponse)
                .collect(Collectors.toList());
    }

    // ========== HELPER METHODS ==========

    private ReportResponse toReportResponse(Report report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setName(report.getName());
        response.setDescription(report.getDescription());
        response.setReportType(report.getReportType());
        response.setIsCustom(report.getIsCustom());
        response.setIsTemplate(report.getIsTemplate());
        response.setIsShared(report.getIsShared());
        response.setCreatedByName(report.getCreatedBy() != null ? report.getCreatedBy().getName() : "System");
        response.setCreatedAt(report.getCreatedAt() != null ?
            LocalDateTime.ofInstant(report.getCreatedAt(), java.time.ZoneId.systemDefault()) : null);

        // Get execution count and last execution date
        List<ReportExecution> executions = reportExecutionRepository.findByReportIdOrderByExecutionDateDesc(report.getId());
        response.setExecutionCount(executions.size());
        if (!executions.isEmpty()) {
            response.setLastExecutedAt(executions.get(0).getExecutionDate());
        }

        return response;
    }

    private ReportExecutionResponse toReportExecutionResponse(ReportExecution execution) {
        ReportExecutionResponse response = new ReportExecutionResponse();
        response.setId(execution.getId());
        response.setReportName(execution.getReport() != null ? execution.getReport().getName() : "Ad-hoc Report");
        response.setExecutionDate(execution.getExecutionDate());
        response.setExecutedByName(execution.getExecutedBy() != null ? execution.getExecutedBy().getName() : "System");
        response.setFormat(execution.getFormat());
        response.setOutputFileUrl(execution.getOutputFileUrl());
        response.setOutputFileName(execution.getOutputFileName());
        response.setFileSizeBytes(execution.getFileSizeBytes());
        response.setStatus(execution.getStatus());
        response.setErrorMessage(execution.getErrorMessage());
        response.setRowCount(execution.getRowCount());
        response.setExecutionTimeMs(execution.getExecutionTimeMs());
        return response;
    }

    private String generateFilename(GenerateReportRequest request) {
        String reportName = request.getReportType().getDisplayName().replaceAll("[^a-zA-Z0-9]", "_");
        String timestamp = LocalDateTime.now().toString().replaceAll("[^0-9]", "");
        String extension = request.getFormat().getExtension();
        return reportName + "_" + timestamp + extension;
    }
}
