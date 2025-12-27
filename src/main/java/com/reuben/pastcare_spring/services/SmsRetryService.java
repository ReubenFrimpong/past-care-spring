package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.SmsMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service for handling SMS retry logic and failure recovery
 */
@Service
@Slf4j
public class SmsRetryService {

    private final SmsMessageRepository smsMessageRepository;
    private final ChurchSmsCreditService churchSmsCreditService;
    private final SmsGatewayRouter smsGatewayRouter;

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MINUTES = 15; // Wait 15 minutes between retries

    public SmsRetryService(
        SmsMessageRepository smsMessageRepository,
        ChurchSmsCreditService churchSmsCreditService,
        SmsGatewayRouter smsGatewayRouter
    ) {
        this.smsMessageRepository = smsMessageRepository;
        this.churchSmsCreditService = churchSmsCreditService;
        this.smsGatewayRouter = smsGatewayRouter;
    }

    /**
     * Find failed messages eligible for retry
     * Criteria:
     * - Status = FAILED
     * - Retry count < MAX_RETRY_ATTEMPTS
     * - Last retry attempt was > RETRY_DELAY_MINUTES ago (or never retried)
     */
    public List<SmsMessage> findMessagesForRetry() {
        LocalDateTime retryThreshold = LocalDateTime.now().minusMinutes(RETRY_DELAY_MINUTES);
        return smsMessageRepository.findFailedMessagesForRetry(MAX_RETRY_ATTEMPTS, retryThreshold);
    }

    /**
     * Retry a single failed SMS message
     */
    @Transactional
    public SmsRetryResult retrySms(SmsMessage message) {
        log.info("Retrying SMS message ID: {}, Attempt: {}/{}",
            message.getId(), message.getRetryCount() + 1, MAX_RETRY_ATTEMPTS);

        try {
            // Check if church has sufficient credits
            BigDecimal cost = message.getCost();
            Long churchId = message.getChurch().getId();

            if (!churchSmsCreditService.hasSufficientCredits(churchId, cost)) {
                log.warn("Insufficient credits to retry SMS {}. Required: {}, Available: {}",
                    message.getId(), cost, churchSmsCreditService.getBalance(churchId));
                return new SmsRetryResult(false, "Insufficient credits", message);
            }

            // Deduct credits for retry
            String referenceId = "SMS-RETRY-" + message.getId() + "-" + (message.getRetryCount() + 1);
            churchSmsCreditService.deductCredits(
                churchId,
                message.getSender() != null ? message.getSender().getId() : null,
                cost,
                "SMS retry attempt " + (message.getRetryCount() + 1),
                referenceId
            );

            // Update retry metadata
            message.setRetryCount(message.getRetryCount() + 1);
            message.setLastRetryAt(LocalDateTime.now());
            message.setStatus(SmsStatus.SENDING);
            smsMessageRepository.save(message);

            // Attempt to send via gateway
            SmsGatewayService.SmsGatewayResponse response = smsGatewayRouter.sendSms(
                message.getRecipientPhone(),
                message.getMessage(),
                Map.of(
                    "sms_id", message.getId().toString(),
                    "retry_attempt", String.valueOf(message.getRetryCount())
                )
            );

            // Process result
            if (response.isSuccess()) {
                message.setStatus(SmsStatus.SENT);
                message.setGatewayMessageId(response.getMessageId());
                message.setSentAt(LocalDateTime.now());
                message.setGatewayResponse("Sent on retry attempt " + message.getRetryCount());
                smsMessageRepository.save(message);

                log.info("SMS {} successfully sent on retry attempt {}",
                    message.getId(), message.getRetryCount());

                return new SmsRetryResult(true, "Sent successfully", message);

            } else {
                // Retry failed - refund credits
                churchSmsCreditService.refundCredits(
                    churchId,
                    message.getSender() != null ? message.getSender().getId() : null,
                    cost,
                    "Refund for failed retry attempt " + message.getRetryCount(),
                    referenceId
                );

                // Check if we should give up
                if (message.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
                    message.setStatus(SmsStatus.FAILED);
                    message.setGatewayResponse("Failed after " + MAX_RETRY_ATTEMPTS +
                        " attempts: " + response.getErrorMessage());
                    smsMessageRepository.save(message);

                    log.error("SMS {} failed permanently after {} attempts: {}",
                        message.getId(), MAX_RETRY_ATTEMPTS, response.getErrorMessage());

                    return new SmsRetryResult(false, "Max retries exceeded", message);
                }

                // Will retry again later
                message.setStatus(SmsStatus.FAILED);
                message.setGatewayResponse("Retry " + message.getRetryCount() +
                    " failed: " + response.getErrorMessage());
                smsMessageRepository.save(message);

                log.warn("SMS {} retry attempt {} failed: {}. Will retry again.",
                    message.getId(), message.getRetryCount(), response.getErrorMessage());

                return new SmsRetryResult(false, "Retry failed, will try again", message);
            }

        } catch (Exception e) {
            log.error("Exception during SMS retry for message {}: {}",
                message.getId(), e.getMessage(), e);

            // Attempt refund on exception
            try {
                BigDecimal cost = message.getCost();
                Long churchId = message.getChurch().getId();
                churchSmsCreditService.refundCredits(
                    churchId,
                    message.getSender() != null ? message.getSender().getId() : null,
                    cost,
                    "Refund for exception during retry",
                    "SMS-RETRY-EXCEPTION-" + message.getId()
                );
            } catch (Exception refundError) {
                log.error("Failed to refund credits after retry exception: {}",
                    refundError.getMessage());
            }

            // Mark as failed if max retries reached
            if (message.getRetryCount() >= MAX_RETRY_ATTEMPTS) {
                message.setStatus(SmsStatus.FAILED);
                message.setGatewayResponse("Exception after " + MAX_RETRY_ATTEMPTS +
                    " attempts: " + e.getMessage());
                smsMessageRepository.save(message);
            }

            return new SmsRetryResult(false, "Exception: " + e.getMessage(), message);
        }
    }

    /**
     * Process all failed messages that are eligible for retry
     * Returns summary of retry results
     */
    @Transactional
    public RetryBatchResult processRetryBatch() {
        List<SmsMessage> messagesToRetry = findMessagesForRetry();

        if (messagesToRetry.isEmpty()) {
            log.info("No SMS messages requiring retry");
            return new RetryBatchResult(0, 0, 0, 0);
        }

        log.info("Processing retry batch: {} messages", messagesToRetry.size());

        int successful = 0;
        int failed = 0;
        int permanentFailures = 0;
        int insufficientCredits = 0;

        for (SmsMessage message : messagesToRetry) {
            SmsRetryResult result = retrySms(message);

            if (result.success()) {
                successful++;
            } else {
                failed++;
                if (result.message().contains("Max retries exceeded")) {
                    permanentFailures++;
                } else if (result.message().contains("Insufficient credits")) {
                    insufficientCredits++;
                }
            }
        }

        log.info("Retry batch complete: {} total, {} successful, {} failed, {} permanent failures, {} insufficient credits",
            messagesToRetry.size(), successful, failed, permanentFailures, insufficientCredits);

        return new RetryBatchResult(messagesToRetry.size(), successful, failed, permanentFailures);
    }

    /**
     * Result of a single retry attempt
     */
    public record SmsRetryResult(
        boolean success,
        String message,
        SmsMessage smsMessage
    ) {}

    /**
     * Summary of a batch retry operation
     */
    public record RetryBatchResult(
        int totalProcessed,
        int successful,
        int failed,
        int permanentFailures
    ) {}
}
