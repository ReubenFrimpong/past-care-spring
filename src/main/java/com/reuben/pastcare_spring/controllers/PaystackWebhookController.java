package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.services.ChurchSmsCreditService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Controller for handling Paystack webhooks
 * Processes payment confirmations and automatically adds SMS credits
 *
 * Configuration required in application.properties:
 * - paystack.secret-key (for webhook signature verification)
 *
 * Webhook URL to configure in Paystack dashboard:
 * https://your-domain.com/api/webhooks/paystack/events
 */
@RestController
@RequestMapping("/api/webhooks/paystack")
@Slf4j
public class PaystackWebhookController {

    private final ChurchSmsCreditService churchSmsCreditService;
    private final ChurchRepository churchRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final com.reuben.pastcare_spring.services.BillingService billingService;
    private final com.reuben.pastcare_spring.services.StorageAddonBillingService storageAddonBillingService;
    private final com.reuben.pastcare_spring.services.TierUpgradeService tierUpgradeService;

    @Value("${paystack.secret-key:}")
    private String paystackSecretKey;

    public PaystackWebhookController(
        ChurchSmsCreditService churchSmsCreditService,
        ChurchRepository churchRepository,
        UserRepository userRepository,
        ObjectMapper objectMapper,
        com.reuben.pastcare_spring.services.BillingService billingService,
        com.reuben.pastcare_spring.services.StorageAddonBillingService storageAddonBillingService,
        com.reuben.pastcare_spring.services.TierUpgradeService tierUpgradeService
    ) {
        this.churchSmsCreditService = churchSmsCreditService;
        this.churchRepository = churchRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.billingService = billingService;
        this.storageAddonBillingService = storageAddonBillingService;
        this.tierUpgradeService = tierUpgradeService;
    }

    /**
     * Paystack webhook endpoint for payment events.
     * This endpoint is called by Paystack servers and MUST NOT require authentication.
     * Security is ensured through webhook signature verification.
     */
    @PostMapping("/events")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "X-Paystack-Signature", required = false) String signature
    ) {
        log.info("Received Paystack webhook");

        try {
            if (!verifyPaystackSignature(payload, signature)) {
                log.warn("Invalid Paystack webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            JsonNode json = objectMapper.readTree(payload);
            String event = json.get("event").asText();

            log.info("Processing Paystack event: {}", event);

            return switch (event) {
                case "charge.success" -> handleChargeSuccess(json.get("data"));
                case "charge.failed" -> handleChargeFailed(json.get("data"));
                default -> {
                    log.info("Unhandled Paystack event type: {}", event);
                    yield ResponseEntity.ok("Event received but not processed");
                }
            };

        } catch (Exception e) {
            log.error("Error processing Paystack webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing webhook");
        }
    }

    private ResponseEntity<String> handleChargeSuccess(JsonNode data) {
        try {
            String reference = data.get("reference").asText();
            String status = data.get("status").asText();

            if (!"success".equals(status)) {
                log.warn("Charge status is not success: {}", status);
                return ResponseEntity.ok("Charge not successful - ignoring");
            }

            JsonNode metadata = data.get("metadata");
            if (metadata == null || metadata.isNull()) {
                log.warn("No metadata in Paystack webhook");
                return ResponseEntity.ok("No metadata - ignoring");
            }

            // Route based on reference prefix
            if (reference.startsWith("SUB-")) {
                return handleSubscriptionPayment(reference, data);
            }

            if (reference.startsWith("ADDON-")) {
                return handleAddonPayment(reference, data);
            }

            if (reference.startsWith("RENEWAL-")) {
                // Renewal payments are already processed by BillingService scheduled job
                // Just log and acknowledge
                log.info("Renewal payment webhook received: {}", reference);
                return ResponseEntity.ok("Renewal acknowledged");
            }

            if (reference.startsWith("TIER_UPGRADE-")) {
                return handleTierUpgradePayment(reference, data);
            }

            // Otherwise, handle as SMS credits purchase
            Long churchId = metadata.has("church_id") ? metadata.get("church_id").asLong() : null;
            Long userId = metadata.has("user_id") ? metadata.get("user_id").asLong() : null;
            String creditAmountStr = metadata.has("credit_amount") ? metadata.get("credit_amount").asText() : null;

            if (churchId == null || creditAmountStr == null) {
                log.warn("Missing required metadata fields for SMS purchase");
                return ResponseEntity.ok("Incomplete metadata - ignoring");
            }

            BigDecimal creditAmount = new BigDecimal(creditAmountStr);

            Church church = churchRepository.findById(churchId).orElse(null);
            if (church == null) {
                log.warn("Church not found: {}", churchId);
                return ResponseEntity.ok("Church not found - ignoring");
            }

            SmsTransaction transaction = churchSmsCreditService.purchaseCredits(
                churchId, userId, creditAmount, reference
            );

            log.info("Successfully added {} credits to church {} via Paystack. Transaction ID: {}",
                creditAmount, churchId, transaction.getId());

            return ResponseEntity.ok("Credits added successfully");

        } catch (Exception e) {
            log.error("Error handling Paystack charge.success: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing charge");
        }
    }

    /**
     * Handle subscription payment webhook.
     * This is called when a subscription payment succeeds.
     */
    private ResponseEntity<String> handleSubscriptionPayment(String reference, JsonNode data) {
        try {
            log.info("Processing subscription payment via webhook: {}", reference);

            // Use the existing billing service to activate subscription
            billingService.verifyAndActivateSubscription(reference);

            log.info("Subscription activated successfully via webhook: {}", reference);
            return ResponseEntity.ok("Subscription activated");

        } catch (Exception e) {
            log.error("Error activating subscription via webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error activating subscription: " + e.getMessage());
        }
    }

    /**
     * Handle storage addon payment webhook.
     * This is called when an addon purchase payment succeeds.
     */
    private ResponseEntity<String> handleAddonPayment(String reference, JsonNode data) {
        try {
            log.info("Processing storage addon payment via webhook: {}", reference);

            // Verify payment and activate addon
            storageAddonBillingService.verifyAndActivateAddon(reference);

            log.info("Storage addon activated successfully via webhook: {}", reference);
            return ResponseEntity.ok("Addon activated");

        } catch (Exception e) {
            log.error("Error activating addon via webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error activating addon: " + e.getMessage());
        }
    }

    /**
     * Handle tier upgrade payment webhook.
     * This is called when a tier upgrade payment succeeds.
     */
    private ResponseEntity<String> handleTierUpgradePayment(String reference, JsonNode data) {
        try {
            log.info("Processing tier upgrade payment via webhook: {}", reference);

            // Complete the tier upgrade
            tierUpgradeService.completeUpgrade(reference);

            log.info("Tier upgrade completed successfully via webhook: {}", reference);
            return ResponseEntity.ok("Tier upgrade completed");

        } catch (Exception e) {
            log.error("Error completing tier upgrade via webhook: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error completing tier upgrade: " + e.getMessage());
        }
    }

    private ResponseEntity<String> handleChargeFailed(JsonNode data) {
        try {
            String reference = data.get("reference").asText();
            String message = data.has("gateway_response") ? data.get("gateway_response").asText() : "Unknown error";

            log.warn("Paystack charge failed - Reference: {}, Message: {}", reference, message);

            return ResponseEntity.ok("Failure recorded");

        } catch (Exception e) {
            log.error("Error handling Paystack charge.failed: {}", e.getMessage(), e);
            return ResponseEntity.ok("Error logged");
        }
    }

    private boolean verifyPaystackSignature(String payload, String signature) {
        if (paystackSecretKey == null || paystackSecretKey.isEmpty()) {
            log.warn("Paystack secret key not configured - skipping signature verification");
            return true;
        }

        if (signature == null || signature.isEmpty()) {
            log.warn("No signature provided in Paystack webhook");
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                paystackSecretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA512"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            String computedSignature = HexFormat.of().formatHex(hash);

            boolean isValid = MessageDigest.isEqual(
                computedSignature.getBytes(StandardCharsets.UTF_8),
                signature.getBytes(StandardCharsets.UTF_8)
            );

            if (!isValid) {
                log.warn("Paystack signature mismatch");
            }

            return isValid;

        } catch (Exception e) {
            log.error("Error verifying Paystack signature: {}", e.getMessage(), e);
            return false;
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        boolean configured = paystackSecretKey != null && !paystackSecretKey.isEmpty();
        return ResponseEntity.ok(String.format(
            "Paystack Webhook Handler - Status: healthy, Secret Key Configured: %s", configured
        ));
    }
}
