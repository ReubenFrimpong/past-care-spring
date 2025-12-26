package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.services.PaystackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for Paystack webhook events
 */
@RestController
@RequestMapping("/api/webhooks/paystack")
@RequiredArgsConstructor
@Slf4j
public class PaystackWebhookController {

    private final PaystackService paystackService;
    private final ObjectMapper objectMapper;

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestHeader("x-paystack-signature") String signature,
            @RequestBody String payload) {

        log.info("Received Paystack webhook");

        try {
            // Verify webhook signature
            if (!paystackService.verifyWebhookSignature(signature, payload)) {
                log.warn("Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            // Parse payload
            JsonNode event = objectMapper.readTree(payload);
            String eventType = event.get("event").asText();

            log.info("Processing webhook event: {}", eventType);

            // Handle different event types
            switch (eventType) {
                case "charge.success":
                    handleChargeSuccess(event);
                    break;
                case "charge.failed":
                    handleChargeFailed(event);
                    break;
                case "subscription.create":
                    handleSubscriptionCreate(event);
                    break;
                case "subscription.disable":
                    handleSubscriptionDisable(event);
                    break;
                default:
                    log.info("Unhandled event type: {}", eventType);
            }

            return ResponseEntity.ok("Webhook processed");

        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }

    private void handleChargeSuccess(JsonNode event) {
        log.info("Handling charge success event");
        // Implementation would update donation record, send receipts, etc.
        // This would be called by RecurringDonationService.processRecurringDonation()
    }

    private void handleChargeFailed(JsonNode event) {
        log.info("Handling charge failed event");
        // Implementation would retry payment or notify admin
    }

    private void handleSubscriptionCreate(JsonNode event) {
        log.info("Handling subscription create event");
        // Implementation would create RecurringDonation record
    }

    private void handleSubscriptionDisable(JsonNode event) {
        log.info("Handling subscription disable event");
        // Implementation would update RecurringDonation status
    }
}
