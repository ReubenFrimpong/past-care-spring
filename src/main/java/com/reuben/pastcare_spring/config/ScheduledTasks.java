package com.reuben.pastcare_spring.config;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.services.EventReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled tasks for automated background jobs
 * Handles event reminders, notifications, and other recurring tasks
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final EventReminderService eventReminderService;
    private final ChurchRepository churchRepository;

    /**
     * Send event reminders daily at 9:00 AM
     * Processes all churches and sends reminders for upcoming events
     * based on each event's reminderDaysBefore setting (1-7 days)
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "UTC")
    public void sendDailyEventReminders() {
        log.info("Starting daily event reminder job...");

        try {
            // Get all active churches
            List<Church> churches = churchRepository.findAll();

            int totalChurchesProcessed = 0;
            int totalRemindersSent = 0;

            for (Church church : churches) {
                try {
                    log.info("Processing reminders for church: {} (ID: {})",
                        church.getName(), church.getId());

                    eventReminderService.sendScheduledReminders(church.getId());
                    totalChurchesProcessed++;

                } catch (Exception e) {
                    log.error("Error processing reminders for church {}: {}",
                        church.getId(), e.getMessage(), e);
                }
            }

            log.info("Daily event reminder job completed. Processed {} churches, sent {} reminders",
                totalChurchesProcessed, totalRemindersSent);

        } catch (Exception e) {
            log.error("Error in daily event reminder job: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleanup old data weekly on Sunday at 2:00 AM
     * Can be extended to clean up old logs, expired tokens, etc.
     */
    @Scheduled(cron = "0 0 2 * * SUN", zone = "UTC")
    public void weeklyCleanup() {
        log.info("Starting weekly cleanup job...");

        try {
            // Future: Add cleanup tasks here
            // - Remove old soft-deleted records (older than 90 days)
            // - Clean up expired QR codes
            // - Archive old event data
            // - Cleanup temporary files

            log.info("Weekly cleanup job completed");

        } catch (Exception e) {
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
}
