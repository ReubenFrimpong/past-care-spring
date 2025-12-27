package com.reuben.pastcare_spring.jobs;

import com.reuben.pastcare_spring.services.SmsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job to process messages scheduled for future delivery
 * Runs every minute to check for messages that need to be sent
 */
@Component
@Slf4j
public class SmsScheduledJob {

    private final SmsService smsService;

    public SmsScheduledJob(SmsService smsService) {
        this.smsService = smsService;
    }

    /**
     * Process scheduled SMS messages every minute
     * Checks for messages with scheduledTime <= now and status = SCHEDULED
     * Cron expression: 0 * * * * * (every minute at second 0)
     */
    @Scheduled(cron = "0 * * * * *")
    public void processScheduledMessages() {
        log.debug("Checking for scheduled SMS messages...");

        try {
            smsService.processScheduledMessages();
            log.debug("Scheduled SMS processing complete");

        } catch (Exception e) {
            log.error("Error processing scheduled SMS messages: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual trigger for testing
     */
    public void runManually() {
        log.info("Manually triggered scheduled SMS processing");
        processScheduledMessages();
    }
}
