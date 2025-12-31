package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.ScheduledJobExecution;
import com.reuben.pastcare_spring.models.ScheduledJobExecution.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduledJobExecutionRepository extends JpaRepository<ScheduledJobExecution, Long> {

    /**
     * Find all executions for a specific job, ordered by start time descending
     */
    List<ScheduledJobExecution> findByJobNameOrderByStartTimeDesc(String jobName);

    /**
     * Find recent executions (last 24 hours)
     */
    @Query("SELECT j FROM ScheduledJobExecution j WHERE j.startTime >= :since ORDER BY j.startTime DESC")
    List<ScheduledJobExecution> findRecentExecutions(LocalDateTime since);

    /**
     * Find currently running jobs
     */
    List<ScheduledJobExecution> findByStatus(JobStatus status);

    /**
     * Find failed jobs that haven't been retried yet
     */
    @Query("SELECT j FROM ScheduledJobExecution j WHERE j.status = 'FAILED' AND j.retryCount = 0 ORDER BY j.startTime DESC")
    List<ScheduledJobExecution> findFailedJobsNeedingRetry();

    /**
     * Find last execution for a specific job
     */
    @Query("SELECT j FROM ScheduledJobExecution j WHERE j.jobName = :jobName ORDER BY j.startTime DESC LIMIT 1")
    Optional<ScheduledJobExecution> findLastExecutionForJob(String jobName);

    /**
     * Get job statistics summary
     */
    @Query("""
        SELECT j.jobName, j.status, COUNT(j), AVG(j.durationMs), MAX(j.startTime)
        FROM ScheduledJobExecution j
        WHERE j.startTime >= :since
        GROUP BY j.jobName, j.status
        ORDER BY j.jobName, j.status
    """)
    List<Object[]> getJobStatisticsSummary(LocalDateTime since);

    /**
     * Delete old execution records (cleanup)
     */
    void deleteByStartTimeBefore(LocalDateTime cutoffDate);
}
