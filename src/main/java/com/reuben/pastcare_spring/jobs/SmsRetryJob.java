package com.reuben.pastcare_spring.jobs;

import com.reuben.pastcare_spring.services.SmsRetryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job to retry failed SMS messages
 * Runs every 15 minutes to process messages that failed and are eligible for retry
 */
@Component
@Slf4j
public class SmsRetryJob {

    private final SmsRetryService smsRetryService;

    public SmsRetryJob(SmsRetryService smsRetryService) {
        this.smsRetryService = smsRetryService;
    }

    /**
     * Process SMS retry queue every 15 minutes
     * Cron expression: 0 star-slash-15 * * * * (at minute 0, 15, 30, 45 of every hour)
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void processSmsRetries() {
        log.info("Starting SMS retry job...");

        try {
            SmsRetryService.RetryBatchResult result = smsRetryService.processRetryBatch();

            log.info("SMS retry job complete - Processed: {}, Successful: {}, Failed: {}, Permanent Failures: {}",
                result.totalProcessed(),
                result.successful(),
                result.failed(),
                result.permanentFailures());

        } catch (Exception e) {
            log.error("Error in SMS retry job: {}", e.getMessage(), e);
        }
    }

    /**
     * Manual trigger for testing
     */
    public void runManually() {
        log.info("Manually triggered SMS retry job");
        processSmsRetries();
    }
}
