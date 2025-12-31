package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for manually executing scheduled jobs via SUPERADMIN dashboard.
 * Provides retry and on-demand execution capabilities.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobExecutionService {

    private final JobMonitoringService jobMonitoringService;
    private final EventReminderService eventReminderService;
    private final ChurchRepository churchRepository;
    private final BillingService billingService;
    private final DataDeletionService dataDeletionService;
    private final ChurchSubscriptionRepository subscriptionRepository;

    /**
     * Execute a job manually (triggered by SUPERADMIN)
     */
    public void executeJob(String jobName, String triggeredBy) {
        log.info("Manually executing job: {} (triggered by: {})", jobName, triggeredBy);

        switch (jobName) {
            case "sendDailyEventReminders" -> executeSendDailyEventReminders(triggeredBy);
            case "weeklyCleanup" -> executeWeeklyCleanup(triggeredBy);
            case "processSubscriptionRenewals" -> executeProcessSubscriptionRenewals(triggeredBy);
            case "suspendPastDueSubscriptions" -> executeSuspendPastDueSubscriptions(triggeredBy);
            case "sendDeletionWarnings" -> executeSendDeletionWarnings(triggeredBy);
            case "deleteExpiredChurchData" -> executeDeleteExpiredChurchData(triggeredBy);
            default -> throw new IllegalArgumentException("Unknown job: " + jobName);
        }
    }

    /**
     * Retry a failed job execution
     */
    public void retryFailedJob(Long executionId, String triggeredBy) {
        var execution = jobMonitoringService.getExecutionById(executionId)
            .orElseThrow(() -> new IllegalArgumentException("Execution not found: " + executionId));

        log.info("Retrying failed job: {} (execution ID: {}, triggered by: {})",
            execution.getJobName(), executionId, triggeredBy);

        // Increment retry count
        jobMonitoringService.incrementRetryCount(executionId);

        // Execute the job again
        executeJob(execution.getJobName(), triggeredBy);
    }

    // ==================== JOB IMPLEMENTATIONS ====================

    private void executeSendDailyEventReminders(String triggeredBy) {
        var execution = jobMonitoringService.startJobExecution(
            "sendDailyEventReminders",
            "Send event reminders for upcoming events",
            true,
            triggeredBy
        );

        try {
            List<Church> churches = churchRepository.findAll();
            int totalProcessed = 0;
            int totalFailed = 0;

            for (Church church : churches) {
                try {
                    eventReminderService.sendScheduledReminders(church.getId());
                    totalProcessed++;
                } catch (Exception e) {
                    log.error("Error processing reminders for church {}: {}", church.getId(), e.getMessage(), e);
                    totalFailed++;
                }
            }

            jobMonitoringService.markJobCompleted(execution.getId(), totalProcessed, totalFailed);

        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            throw e;
        }
    }

    private void executeWeeklyCleanup(String triggeredBy) {
        var execution = jobMonitoringService.startJobExecution(
            "weeklyCleanup",
            "Weekly cleanup of old data and temporary files",
            true,
            triggeredBy
        );

        try {
            // Cleanup old job executions
            jobMonitoringService.cleanupOldExecutions();

            jobMonitoringService.markJobCompleted(execution.getId(), 0, 0);

        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            throw e;
        }
    }

    private void executeProcessSubscriptionRenewals(String triggeredBy) {
        var execution = jobMonitoringService.startJobExecution(
            "processSubscriptionRenewals",
            "Process subscription renewals and charges",
            true,
            triggeredBy
        );

        try {
            billingService.processSubscriptionRenewals();
            jobMonitoringService.markJobCompleted(execution.getId(), 0, 0);

        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            throw e;
        }
    }

    private void executeSuspendPastDueSubscriptions(String triggeredBy) {
        var execution = jobMonitoringService.startJobExecution(
            "suspendPastDueSubscriptions",
            "Suspend subscriptions that are past due",
            true,
            triggeredBy
        );

        try {
            billingService.suspendPastDueSubscriptions();
            jobMonitoringService.markJobCompleted(execution.getId(), 0, 0);

        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            throw e;
        }
    }

    private void executeSendDeletionWarnings(String triggeredBy) {
        var execution = jobMonitoringService.startJobExecution(
            "sendDeletionWarnings",
            "Send 7-day deletion warning emails to suspended churches",
            true,
            triggeredBy
        );

        try {
            LocalDate today = LocalDate.now();
            LocalDate warningThreshold = today.plusDays(7);

            List<ChurchSubscription> subscriptionsNeedingWarning =
                subscriptionRepository.findNeedingDeletionWarning(warningThreshold);

            int warningsSent = 0;
            int warningsFailed = 0;

            for (ChurchSubscription subscription : subscriptionsNeedingWarning) {
                try {
                    dataDeletionService.sendDeletionWarningEmail(subscription.getChurchId(), subscription);
                    subscription.markDeletionWarningSent();
                    subscriptionRepository.save(subscription);
                    warningsSent++;
                } catch (Exception e) {
                    log.error("Failed to send deletion warning for church {}: {}",
                        subscription.getChurchId(), e.getMessage(), e);
                    warningsFailed++;
                }
            }

            jobMonitoringService.markJobCompleted(execution.getId(), warningsSent, warningsFailed);

        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            throw e;
        }
    }

    private void executeDeleteExpiredChurchData(String triggeredBy) {
        var execution = jobMonitoringService.startJobExecution(
            "deleteExpiredChurchData",
            "Permanently delete church data after 90-day retention period",
            true,
            triggeredBy
        );

        try {
            LocalDate today = LocalDate.now();
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

            List<ChurchSubscription> eligibleForDeletion =
                subscriptionRepository.findEligibleForDeletion(today, sevenDaysAgo);

            int deletionCount = 0;
            int deletionFailed = 0;

            for (ChurchSubscription subscription : eligibleForDeletion) {
                try {
                    Long churchId = subscription.getChurchId();
                    dataDeletionService.deleteChurchData(churchId, subscription);
                    deletionCount++;
                } catch (Exception e) {
                    log.error("Failed to delete data for church {}: {}",
                        subscription.getChurchId(), e.getMessage(), e);
                    deletionFailed++;
                }
            }

            jobMonitoringService.markJobCompleted(execution.getId(), deletionCount, deletionFailed);

        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            throw e;
        }
    }
}
