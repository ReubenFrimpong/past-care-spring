package com.reuben.pastcare_spring.config;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import com.reuben.pastcare_spring.services.EventReminderService;
import com.reuben.pastcare_spring.services.BillingService;
import com.reuben.pastcare_spring.services.DataDeletionService;
import com.reuben.pastcare_spring.services.JobMonitoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduled tasks for automated background jobs
 * Handles event reminders, notifications, billing renewals, and other recurring tasks
 * All jobs are tracked via JobMonitoringService for SUPERADMIN monitoring
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final EventReminderService eventReminderService;
    private final ChurchRepository churchRepository;
    private final BillingService billingService;
    private final DataDeletionService dataDeletionService;
    private final ChurchSubscriptionRepository subscriptionRepository;
    private final JobMonitoringService jobMonitoringService;

    /**
     * Send event reminders daily at 9:00 AM
     * Processes all churches and sends reminders for upcoming events
     * based on each event's reminderDaysBefore setting (1-7 days)
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "UTC")
    public void sendDailyEventReminders() {
        var execution = jobMonitoringService.startJobExecution(
            "sendDailyEventReminders",
            "Send event reminders for upcoming events"
        );

        try {
            List<Church> churches = churchRepository.findAll();

            int totalChurchesProcessed = 0;
            int totalFailed = 0;

            for (Church church : churches) {
                try {
                    eventReminderService.sendScheduledReminders(church.getId());
                    totalChurchesProcessed++;
                } catch (Exception e) {
                    log.error("Error processing reminders for church {}: {}", church.getId(), e.getMessage(), e);
                    totalFailed++;
                }
            }

            jobMonitoringService.markJobCompleted(execution.getId(), totalChurchesProcessed, totalFailed);

        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            log.error("Error in daily event reminder job: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleanup old data weekly on Sunday at 2:00 AM
     * Can be extended to clean up old logs, expired tokens, etc.
     */
    @Scheduled(cron = "0 0 2 * * SUN", zone = "UTC")
    public void weeklyCleanup() {
        var execution = jobMonitoringService.startJobExecution(
            "weeklyCleanup",
            "Weekly cleanup of old data and temporary files"
        );

        try {
            // Cleanup old job executions (keep last 90 days)
            jobMonitoringService.cleanupOldExecutions();

            jobMonitoringService.markJobCompleted(execution.getId(), 0, 0);

        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            log.error("Error in weekly cleanup job: {}", e.getMessage(), e);
        }
    }

    /**
     * Health check every hour
     * Logs system status and can be extended for monitoring
     */
    @Scheduled(cron = "0 0 * * * *", zone = "UTC")
    public void hourlyHealthCheck() {
        // Lightweight health check
        long activeChurches = churchRepository.count();
        log.debug("Hourly health check - Active churches: {}", activeChurches);
    }

    /**
     * Process subscription renewals daily at 2:00 AM
     * Charges subscriptions that are due for renewal
     * Uses promotional credits first if available, otherwise charges via Paystack
     */
    @Scheduled(cron = "0 0 2 * * *", zone = "UTC")
    public void processSubscriptionRenewals() {
        var execution = jobMonitoringService.startJobExecution(
            "processSubscriptionRenewals",
            "Process subscription renewals and charges"
        );

        try {
            billingService.processSubscriptionRenewals();
            jobMonitoringService.markJobCompleted(execution.getId(), 0, 0);
        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            log.error("Error in subscription renewal job: {}", e.getMessage(), e);
        }
    }

    /**
     * Suspend past-due subscriptions daily at 3:00 AM
     * Suspends subscriptions that have exceeded their grace period (7 days)
     */
    @Scheduled(cron = "0 0 3 * * *", zone = "UTC")
    public void suspendPastDueSubscriptions() {
        var execution = jobMonitoringService.startJobExecution(
            "suspendPastDueSubscriptions",
            "Suspend subscriptions that are past due"
        );

        try {
            billingService.suspendPastDueSubscriptions();
            jobMonitoringService.markJobCompleted(execution.getId(), 0, 0);
        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            log.error("Error in suspension job: {}", e.getMessage(), e);
        }
    }

    /**
     * Send deletion warning emails daily at 1:00 AM
     * Sends 7-day warning emails to churches whose data retention period is ending soon
     */
    @Scheduled(cron = "0 0 1 * * *", zone = "UTC")
    public void sendDeletionWarnings() {
        var execution = jobMonitoringService.startJobExecution(
            "sendDeletionWarnings",
            "Send 7-day deletion warning emails to suspended churches"
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
            log.error("Error in deletion warning job: {}", e.getMessage(), e);
        }
    }

    /**
     * Delete church data daily at 4:00 AM
     * Permanently deletes data for suspended churches after 90-day retention period + 7-day warning
     */
    @Scheduled(cron = "0 0 4 * * *", zone = "UTC")
    public void deleteExpiredChurchData() {
        var execution = jobMonitoringService.startJobExecution(
            "deleteExpiredChurchData",
            "Permanently delete church data after 90-day retention period"
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
                    String churchName = churchRepository.findById(churchId)
                        .map(Church::getName)
                        .orElse("Unknown Church");

                    log.warn("DELETING CHURCH DATA: {} (ID: {})", churchName, churchId);
                    dataDeletionService.deleteChurchData(churchId, subscription);
                    deletionCount++;
                    log.warn("✅ Church data deleted: {} (ID: {})", churchName, churchId);

                } catch (Exception e) {
                    log.error("❌ Failed to delete data for church {}: {}",
                        subscription.getChurchId(), e.getMessage(), e);
                    deletionFailed++;
                }
            }

            jobMonitoringService.markJobCompleted(execution.getId(), deletionCount, deletionFailed);

        } catch (Exception e) {
            jobMonitoringService.markJobFailed(execution.getId(), e);
            log.error("Error in data deletion job: {}", e.getMessage(), e);
        }
    }
}
