package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entity to track scheduled job executions for monitoring and auditing.
 * Allows SUPERADMIN to view job history, status, and manage failing jobs.
 */
@Entity
@Table(name = "scheduled_job_executions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledJobExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Job name/identifier (matches method name in ScheduledTasks)
     */
    @Column(nullable = false, length = 100)
    private String jobName;

    /**
     * Human-readable job description
     */
    @Column(length = 255)
    private String description;

    /**
     * Job execution status
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JobStatus status;

    /**
     * When the job started executing
     */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /**
     * When the job completed (success or failure)
     */
    @Column
    private LocalDateTime endTime;

    /**
     * Duration of execution in milliseconds
     */
    @Column
    private Long durationMs;

    /**
     * Number of retry attempts for this execution
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * Error message if job failed
     */
    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Full stack trace if job failed
     */
    @Column(columnDefinition = "TEXT")
    private String stackTrace;

    /**
     * Number of items processed (e.g., churches, reminders)
     */
    @Column
    private Integer itemsProcessed;

    /**
     * Number of items that failed processing
     */
    @Column
    private Integer itemsFailed;

    /**
     * Additional metadata as JSON
     */
    @Column(columnDefinition = "TEXT")
    private String metadata;

    /**
     * Whether this job execution was manually triggered by SUPERADMIN
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean manuallyTriggered = false;

    /**
     * SUPERADMIN user who triggered the job (if manual)
     */
    @Column(length = 100)
    private String triggeredBy;

    /**
     * Whether this job was canceled
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean canceled = false;

    @PrePersist
    protected void onCreate() {
        if (startTime == null) {
            startTime = LocalDateTime.now();
        }
        if (status == null) {
            status = JobStatus.RUNNING;
        }
    }

    /**
     * Mark job as completed successfully
     */
    public void markAsCompleted(Integer itemsProcessed, Integer itemsFailed) {
        this.status = JobStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
        this.durationMs = Duration.between(startTime, endTime).toMillis();
        this.itemsProcessed = itemsProcessed;
        this.itemsFailed = itemsFailed;
    }

    /**
     * Mark job as failed
     */
    public void markAsFailed(String errorMessage, String stackTrace) {
        this.status = JobStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.durationMs = Duration.between(startTime, endTime).toMillis();
        this.errorMessage = errorMessage;
        this.stackTrace = stackTrace;
    }

    /**
     * Mark job as canceled
     */
    public void markAsCanceled() {
        this.status = JobStatus.CANCELED;
        this.canceled = true;
        this.endTime = LocalDateTime.now();
        this.durationMs = Duration.between(startTime, endTime).toMillis();
    }

    /**
     * Increment retry count
     */
    public void incrementRetry() {
        this.retryCount++;
    }

    public enum JobStatus {
        RUNNING,    // Currently executing
        COMPLETED,  // Finished successfully
        FAILED,     // Finished with errors
        CANCELED    // Manually canceled by SUPERADMIN
    }
}
