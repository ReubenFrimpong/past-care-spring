package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.models.ScheduledJobExecution;
import com.reuben.pastcare_spring.services.JobExecutionService;
import com.reuben.pastcare_spring.services.JobMonitoringService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.reuben.pastcare_spring.enums.Permission.PLATFORM_MANAGE_CHURCHES;

/**
 * Controller for SUPERADMIN job monitoring dashboard.
 * Provides visibility into scheduled jobs, execution history, and manual control.
 */
@RestController
@RequestMapping("/api/platform/jobs")
@RequiredArgsConstructor
@Slf4j
public class JobMonitoringController {

    private final JobMonitoringService jobMonitoringService;
    private final JobExecutionService jobExecutionService;

    /**
     * Get all recent job executions (last 24 hours by default)
     * SUPERADMIN only
     *
     * @param hoursBack Number of hours to look back (default 24)
     * @return List of job executions
     */
    @GetMapping("/executions")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<List<JobExecutionResponse>> getRecentExecutions(
            @RequestParam(defaultValue = "24") int hoursBack) {

        log.info("SUPERADMIN requesting job executions for last {} hours", hoursBack);

        List<ScheduledJobExecution> executions = jobMonitoringService.getRecentExecutions(hoursBack);
        List<JobExecutionResponse> responses = executions.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get executions for a specific job
     * SUPERADMIN only
     *
     * @param jobName Job name
     * @return List of executions for this job
     */
    @GetMapping("/executions/by-job/{jobName}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<List<JobExecutionResponse>> getExecutionsForJob(@PathVariable String jobName) {
        log.info("SUPERADMIN requesting executions for job: {}", jobName);

        List<ScheduledJobExecution> executions = jobMonitoringService.getExecutionsForJob(jobName);
        List<JobExecutionResponse> responses = executions.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get currently running jobs
     * SUPERADMIN only
     *
     * @return List of running jobs
     */
    @GetMapping("/running")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<List<JobExecutionResponse>> getRunningJobs() {
        log.info("SUPERADMIN requesting currently running jobs");

        List<ScheduledJobExecution> executions = jobMonitoringService.getRunningJobs();
        List<JobExecutionResponse> responses = executions.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get failed jobs that haven't been retried
     * SUPERADMIN only
     *
     * @return List of failed jobs needing attention
     */
    @GetMapping("/failed")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<List<JobExecutionResponse>> getFailedJobs() {
        log.info("SUPERADMIN requesting failed jobs");

        List<ScheduledJobExecution> executions = jobMonitoringService.getFailedJobsNeedingRetry();
        List<JobExecutionResponse> responses = executions.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Get details for a specific job execution
     * SUPERADMIN only
     *
     * @param executionId Execution ID
     * @return Execution details
     */
    @GetMapping("/executions/{executionId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<JobExecutionResponse> getExecutionDetails(@PathVariable Long executionId) {
        log.info("SUPERADMIN requesting execution details for ID: {}", executionId);

        ScheduledJobExecution execution = jobMonitoringService.getExecutionById(executionId)
            .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));

        return ResponseEntity.ok(mapToResponse(execution));
    }

    /**
     * Manually execute a job
     * SUPERADMIN only
     *
     * @param request Job execution request
     * @param authentication Current user
     * @return Success message
     */
    @PostMapping("/execute")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<String> executeJob(
            @RequestBody ExecuteJobRequest request,
            Authentication authentication) {

        String triggeredBy = authentication.getName();
        log.info("SUPERADMIN {} manually executing job: {}", triggeredBy, request.getJobName());

        jobExecutionService.executeJob(request.getJobName(), triggeredBy);

        return ResponseEntity.ok("Job '" + request.getJobName() + "' started successfully");
    }

    /**
     * Retry a failed job execution
     * SUPERADMIN only
     *
     * @param executionId Execution ID to retry
     * @param authentication Current user
     * @return Success message
     */
    @PostMapping("/executions/{executionId}/retry")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<String> retryFailedJob(
            @PathVariable Long executionId,
            Authentication authentication) {

        String triggeredBy = authentication.getName();
        log.info("SUPERADMIN {} retrying failed execution: {}", triggeredBy, executionId);

        jobExecutionService.retryFailedJob(executionId, triggeredBy);

        return ResponseEntity.ok("Job retry started successfully");
    }

    /**
     * Cancel a running job execution
     * SUPERADMIN only
     *
     * @param executionId Execution ID to cancel
     * @param authentication Current user
     * @return Success message
     */
    @PostMapping("/executions/{executionId}/cancel")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<String> cancelJob(
            @PathVariable Long executionId,
            Authentication authentication) {

        String canceledBy = authentication.getName();
        log.warn("SUPERADMIN {} canceling job execution: {}", canceledBy, executionId);

        jobMonitoringService.cancelJobExecution(executionId, canceledBy);

        return ResponseEntity.ok("Job execution canceled successfully");
    }

    /**
     * Get list of available jobs that can be executed manually
     * SUPERADMIN only
     *
     * @return List of available jobs
     */
    @GetMapping("/available")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<List<AvailableJobResponse>> getAvailableJobs() {
        log.info("SUPERADMIN requesting list of available jobs");

        List<AvailableJobResponse> jobs = List.of(
            new AvailableJobResponse("sendDailyEventReminders", "Send Event Reminders", "Send event reminders for upcoming events", "0 0 9 * * *"),
            new AvailableJobResponse("weeklyCleanup", "Weekly Cleanup", "Cleanup old data and temporary files", "0 0 2 * * SUN"),
            new AvailableJobResponse("processSubscriptionRenewals", "Process Renewals", "Process subscription renewals and charges", "0 0 2 * * *"),
            new AvailableJobResponse("suspendPastDueSubscriptions", "Suspend Past-Due", "Suspend subscriptions that are past due", "0 0 3 * * *"),
            new AvailableJobResponse("sendDeletionWarnings", "Send Deletion Warnings", "Send 7-day deletion warning emails", "0 0 1 * * *"),
            new AvailableJobResponse("deleteExpiredChurchData", "Delete Expired Data", "Permanently delete church data after retention period", "0 0 4 * * *")
        );

        return ResponseEntity.ok(jobs);
    }

    // ==================== DTOs ====================

    /**
     * Map ScheduledJobExecution to response DTO
     */
    private JobExecutionResponse mapToResponse(ScheduledJobExecution execution) {
        return JobExecutionResponse.builder()
            .id(execution.getId())
            .jobName(execution.getJobName())
            .description(execution.getDescription())
            .status(execution.getStatus().name())
            .startTime(execution.getStartTime())
            .endTime(execution.getEndTime())
            .durationMs(execution.getDurationMs())
            .retryCount(execution.getRetryCount())
            .errorMessage(execution.getErrorMessage())
            .stackTrace(execution.getStackTrace())
            .itemsProcessed(execution.getItemsProcessed())
            .itemsFailed(execution.getItemsFailed())
            .manuallyTriggered(execution.getManuallyTriggered())
            .triggeredBy(execution.getTriggeredBy())
            .canceled(execution.getCanceled())
            .build();
    }

    @Data
    @Builder
    public static class JobExecutionResponse {
        private Long id;
        private String jobName;
        private String description;
        private String status;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long durationMs;
        private Integer retryCount;
        private String errorMessage;
        private String stackTrace;
        private Integer itemsProcessed;
        private Integer itemsFailed;
        private Boolean manuallyTriggered;
        private String triggeredBy;
        private Boolean canceled;
    }

    @Data
    public static class ExecuteJobRequest {
        private String jobName;
    }

    @Data
    @AllArgsConstructor
    public static class AvailableJobResponse {
        private String jobName;
        private String displayName;
        private String description;
        private String cronExpression;
    }
}
