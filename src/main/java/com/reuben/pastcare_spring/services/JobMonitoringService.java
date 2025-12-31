package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.ScheduledJobExecution;
import com.reuben.pastcare_spring.models.ScheduledJobExecution.JobStatus;
import com.reuben.pastcare_spring.repositories.ScheduledJobExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for monitoring and managing scheduled job executions.
 * Provides job tracking, status monitoring, and manual execution capabilities for SUPERADMIN.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobMonitoringService {

    private final ScheduledJobExecutionRepository executionRepository;

    /**
     * Start tracking a new job execution
     */
    @Transactional
    public ScheduledJobExecution startJobExecution(String jobName, String description) {
        return startJobExecution(jobName, description, false, null);
    }

    /**
     * Start tracking a new job execution (with manual trigger info)
     */
    @Transactional
    public ScheduledJobExecution startJobExecution(String jobName, String description, boolean manuallyTriggered, String triggeredBy) {
        ScheduledJobExecution execution = ScheduledJobExecution.builder()
            .jobName(jobName)
            .description(description)
            .status(JobStatus.RUNNING)
            .startTime(LocalDateTime.now())
            .retryCount(0)
            .manuallyTriggered(manuallyTriggered)
            .triggeredBy(triggeredBy)
            .build();

        log.info("Starting job execution: {} (ID will be assigned)", jobName);
        return executionRepository.save(execution);
    }

    /**
     * Mark a job execution as completed successfully
     */
    @Transactional
    public void markJobCompleted(Long executionId, Integer itemsProcessed, Integer itemsFailed) {
        executionRepository.findById(executionId).ifPresent(execution -> {
            execution.markAsCompleted(itemsProcessed, itemsFailed);
            executionRepository.save(execution);
            log.info("Job {} completed successfully. Processed: {}, Failed: {}",
                execution.getJobName(), itemsProcessed, itemsFailed);
        });
    }

    /**
     * Mark a job execution as failed
     */
    @Transactional
    public void markJobFailed(Long executionId, Exception exception) {
        executionRepository.findById(executionId).ifPresent(execution -> {
            String errorMessage = exception.getMessage();
            String stackTrace = getStackTraceAsString(exception);

            execution.markAsFailed(errorMessage, stackTrace);
            executionRepository.save(execution);
            log.error("Job {} failed: {}", execution.getJobName(), errorMessage);
        });
    }

    /**
     * Cancel a running job execution
     */
    @Transactional
    public void cancelJobExecution(Long executionId, String canceledBy) {
        executionRepository.findById(executionId).ifPresent(execution -> {
            if (execution.getStatus() == JobStatus.RUNNING) {
                execution.markAsCanceled();
                executionRepository.save(execution);
                log.warn("Job {} canceled by {}", execution.getJobName(), canceledBy);
            } else {
                log.warn("Cannot cancel job {} - status is {}", execution.getJobName(), execution.getStatus());
            }
        });
    }

    /**
     * Get all recent job executions (last 24 hours by default)
     */
    public List<ScheduledJobExecution> getRecentExecutions(int hoursBack) {
        LocalDateTime since = LocalDateTime.now().minusHours(hoursBack);
        return executionRepository.findRecentExecutions(since);
    }

    /**
     * Get all executions for a specific job
     */
    public List<ScheduledJobExecution> getExecutionsForJob(String jobName) {
        return executionRepository.findByJobNameOrderByStartTimeDesc(jobName);
    }

    /**
     * Get currently running jobs
     */
    public List<ScheduledJobExecution> getRunningJobs() {
        return executionRepository.findByStatus(JobStatus.RUNNING);
    }

    /**
     * Get failed jobs that haven't been retried
     */
    public List<ScheduledJobExecution> getFailedJobsNeedingRetry() {
        return executionRepository.findFailedJobsNeedingRetry();
    }

    /**
     * Get last execution for a specific job
     */
    public Optional<ScheduledJobExecution> getLastExecutionForJob(String jobName) {
        return executionRepository.findLastExecutionForJob(jobName);
    }

    /**
     * Get execution by ID
     */
    public Optional<ScheduledJobExecution> getExecutionById(Long id) {
        return executionRepository.findById(id);
    }

    /**
     * Increment retry count for a job execution
     */
    @Transactional
    public void incrementRetryCount(Long executionId) {
        executionRepository.findById(executionId).ifPresent(execution -> {
            execution.incrementRetry();
            executionRepository.save(execution);
            log.info("Incremented retry count for job {}: {} retries",
                execution.getJobName(), execution.getRetryCount());
        });
    }

    /**
     * Delete old execution records (cleanup)
     * Keep last 90 days of execution history
     */
    @Transactional
    public void cleanupOldExecutions() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        executionRepository.deleteByStartTimeBefore(cutoffDate);
        log.info("Cleaned up job execution records older than {}", cutoffDate);
    }

    /**
     * Convert exception stack trace to string
     */
    private String getStackTraceAsString(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Get job statistics summary for the last N days
     */
    public List<Object[]> getJobStatisticsSummary(int daysBack) {
        LocalDateTime since = LocalDateTime.now().minusDays(daysBack);
        return executionRepository.getJobStatisticsSummary(since);
    }
}
