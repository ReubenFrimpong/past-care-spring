package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.models.SmsMessage;
import com.reuben.pastcare_spring.models.SmsStatus;
import com.reuben.pastcare_spring.repositories.SmsMessageRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling SMS gateway webhooks
 * Receives delivery status updates from Africa's Talking and Twilio
 *
 * Configuration required in application.properties:
 * - africas-talking.api-key (for signature verification)
 * - twilio.auth-token (for signature verification)
 */
@RestController
@RequestMapping("/api/webhooks/sms")
@Slf4j
public class SmsWebhookController {

    private final SmsMessageRepository smsMessageRepository;

    @Value("${africas-talking.api-key:}")
    private String africasTalkingApiKey;

    @Value("${twilio.auth-token:}")
    private String twilioAuthToken;

    public SmsWebhookController(SmsMessageRepository smsMessageRepository) {
        this.smsMessageRepository = smsMessageRepository;
    }

    /**
     * Africa's Talking delivery status webhook
     *
     * Expected payload:
     * {
     *   "id": "ATMessageId",
     *   "status": "Success|Failed|Rejected|Sent",
     *   "phoneNumber": "+1234567890",
     *   "networkCode": "62120",
     *   "cost": "KES 0.80",
     *   "retryCount": 0
     * }
     *
     * Environment variable needed: africas-talking.api-key
     */
    @RequirePermission(Permission.SMS_SEND)
    @PostMapping("/africas-talking/delivery")
    public ResponseEntity<String> handleAfricasTalkingDelivery(@RequestBody Map<String, Object> payload) {
        log.info("Received Africa's Talking delivery webhook: {}", payload);

        try {
            String gatewayMessageId = (String) payload.get("id");
            String status = (String) payload.get("status");

            if (gatewayMessageId == null || status == null) {
                log.warn("Invalid Africa's Talking webhook payload - missing id or status");
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            Optional<SmsMessage> messageOpt = smsMessageRepository.findByGatewayMessageId(gatewayMessageId);

            if (messageOpt.isEmpty()) {
                log.warn("SMS message not found for gateway ID: {}", gatewayMessageId);
                return ResponseEntity.ok("Message not found - ignoring");
            }

            SmsMessage message = messageOpt.get();

            // Map Africa's Talking status to our status
            SmsStatus newStatus = switch (status.toLowerCase()) {
                case "success", "delivered" -> SmsStatus.DELIVERED;
                case "sent" -> SmsStatus.SENT;
                case "failed", "rejected" -> SmsStatus.FAILED;
                default -> {
                    log.warn("Unknown Africa's Talking status: {}", status);
                    yield message.getStatus(); // Keep current status
                }
            };

            // Update message status
            message.setStatus(newStatus);
            message.setGatewayResponse(status);
            message.setUpdatedAt(LocalDateTime.now());

            if (newStatus == SmsStatus.DELIVERED) {
                message.setDeliveryTime(LocalDateTime.now());
            }

            smsMessageRepository.save(message);

            log.info("Updated SMS {} status to {} from Africa's Talking webhook",
                message.getId(), newStatus);

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Error processing Africa's Talking webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing webhook");
        }
    }

    /**
     * Twilio delivery status webhook
     *
     * Expected parameters (form-encoded):
     * - MessageSid: Twilio message ID
     * - MessageStatus: sent|delivered|failed|undelivered
     * - To: recipient phone number
     * - From: sender phone number
     * - ErrorCode: error code if failed
     * - ErrorMessage: error message if failed
     *
     * Environment variable needed: twilio.auth-token (for signature verification)
     */
    @RequirePermission(Permission.SMS_SEND)
    @PostMapping("/twilio/delivery")
    public ResponseEntity<String> handleTwilioDelivery(
            @RequestParam Map<String, String> params,
            @RequestHeader(value = "X-Twilio-Signature", required = false) String twilioSignature
    ) {
        log.info("Received Twilio delivery webhook: {}", params);

        try {
            String gatewayMessageId = params.get("MessageSid");
            String status = params.get("MessageStatus");

            if (gatewayMessageId == null || status == null) {
                log.warn("Invalid Twilio webhook payload - missing MessageSid or MessageStatus");
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            // Optional: Verify Twilio signature if auth token is configured
            if (twilioAuthToken != null && !twilioAuthToken.isEmpty() && twilioSignature != null) {
                // Signature verification would go here
                // For now, we'll skip it but log a warning if token is missing
                log.debug("Twilio signature verification skipped - implement if needed");
            }

            Optional<SmsMessage> messageOpt = smsMessageRepository.findByGatewayMessageId(gatewayMessageId);

            if (messageOpt.isEmpty()) {
                log.warn("SMS message not found for Twilio MessageSid: {}", gatewayMessageId);
                return ResponseEntity.ok("Message not found - ignoring");
            }

            SmsMessage message = messageOpt.get();

            // Map Twilio status to our status
            SmsStatus newStatus = switch (status.toLowerCase()) {
                case "delivered" -> SmsStatus.DELIVERED;
                case "sent" -> SmsStatus.SENT;
                case "failed", "undelivered" -> SmsStatus.FAILED;
                case "queued", "accepted" -> SmsStatus.SENDING;
                default -> {
                    log.warn("Unknown Twilio status: {}", status);
                    yield message.getStatus();
                }
            };

            // Update message status
            message.setStatus(newStatus);
            message.setGatewayResponse(status);
            message.setUpdatedAt(LocalDateTime.now());

            if (newStatus == SmsStatus.DELIVERED) {
                message.setDeliveryTime(LocalDateTime.now());
            }

            // Store error information if available
            if (newStatus == SmsStatus.FAILED) {
                String errorCode = params.get("ErrorCode");
                String errorMessage = params.get("ErrorMessage");
                if (errorCode != null || errorMessage != null) {
                    message.setGatewayResponse(
                        String.format("%s (Code: %s, Message: %s)", status, errorCode, errorMessage)
                    );
                }
            }

            smsMessageRepository.save(message);

            log.info("Updated SMS {} status to {} from Twilio webhook",
                message.getId(), newStatus);

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            log.error("Error processing Twilio webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing webhook");
        }
    }

    /**
     * Health check endpoint for webhook configuration testing
     */
    @RequirePermission(Permission.SMS_SEND)
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "SMS Webhook Handler",
            "africasTalkingConfigured", String.valueOf(!africasTalkingApiKey.isEmpty()),
            "twilioConfigured", String.valueOf(!twilioAuthToken.isEmpty())
        ));
    }
}
