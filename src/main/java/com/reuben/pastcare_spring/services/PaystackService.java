package com.reuben.pastcare_spring.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.config.PaystackConfig;
import com.reuben.pastcare_spring.dtos.PaymentInitializationRequest;
import com.reuben.pastcare_spring.dtos.PaymentInitializationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Service for Paystack payment gateway integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaystackService {

    private final PaystackConfig paystackConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Initialize a payment transaction with Paystack
     */
    public PaymentInitializationResponse initializePayment(PaymentInitializationRequest request) {
        try {
            String url = paystackConfig.getBaseUrl() + "/transaction/initialize";

            // Convert amount to kobo (smallest currency unit - multiply by 100)
            BigDecimal amountInKobo = request.getAmount().multiply(BigDecimal.valueOf(100));

            // Use provided reference or generate unique reference
            // Subscriptions provide "SUB-" prefix, donations/SMS credits use "PCS-"
            String reference = request.getReference() != null && !request.getReference().isEmpty()
                ? request.getReference()
                : "PCS-" + UUID.randomUUID().toString();

            // Build request body
            Map<String, Object> body = new HashMap<>();
            body.put("email", request.getEmail());
            body.put("amount", amountInKobo.intValue());
            body.put("reference", reference);
            body.put("currency", request.getCurrency());
            body.put("callback_url", request.getCallbackUrl() != null ? request.getCallbackUrl() : paystackConfig.getCallbackUrl());

            // Enable both card and mobile money channels for Ghana
            // Paystack supports: card, bank, ussd, qr, mobile_money, bank_transfer
            body.put("channels", new String[]{"card", "mobile_money"});

            // Add metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("memberId", request.getMemberId());
            // Only add donationType if it's not null (subscription payments don't have donation type)
            if (request.getDonationType() != null) {
                metadata.put("donationType", request.getDonationType().toString());
            }
            if (request.getCampaign() != null) {
                metadata.put("campaign", request.getCampaign());
            }
            if (request.getSetupRecurring()) {
                metadata.put("setupRecurring", true);
            }
            body.put("metadata", metadata);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(paystackConfig.getSecretKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // Make API call
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            // Parse response
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());

            PaymentInitializationResponse initResponse = new PaymentInitializationResponse();
            initResponse.setStatus(jsonResponse.get("status").asBoolean());
            initResponse.setMessage(jsonResponse.get("message").asText());

            if (jsonResponse.has("data")) {
                JsonNode data = jsonResponse.get("data");
                initResponse.setAuthorizationUrl(data.get("authorization_url").asText());
                initResponse.setAccessCode(data.get("access_code").asText());
                initResponse.setReference(data.get("reference").asText());
            }

            log.info("Payment initialized successfully: {}", reference);
            return initResponse;

        } catch (Exception e) {
            log.error("Error initializing payment", e);
            throw new RuntimeException("Failed to initialize payment: " + e.getMessage());
        }
    }

    /**
     * Verify a payment transaction
     */
    public JsonNode verifyPayment(String reference) {
        try {
            String url = paystackConfig.getBaseUrl() + "/transaction/verify/" + reference;

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(paystackConfig.getSecretKey());

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
            );

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            log.info("Payment verified for reference: {}", reference);

            return jsonResponse;

        } catch (Exception e) {
            log.error("Error verifying payment: {}", reference, e);
            throw new RuntimeException("Failed to verify payment: " + e.getMessage());
        }
    }

    /**
     * Charge authorization (for recurring payments)
     */
    public JsonNode chargeAuthorization(String authorizationCode, BigDecimal amount, String email, String reference) {
        try {
            String url = paystackConfig.getBaseUrl() + "/transaction/charge_authorization";

            // Convert amount to kobo
            BigDecimal amountInKobo = amount.multiply(BigDecimal.valueOf(100));

            Map<String, Object> body = new HashMap<>();
            body.put("authorization_code", authorizationCode);
            body.put("email", email);
            body.put("amount", amountInKobo.intValue());
            body.put("reference", reference);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(paystackConfig.getSecretKey());

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            log.info("Authorization charged successfully for: {}", authorizationCode);

            return jsonResponse;

        } catch (Exception e) {
            log.error("Error charging authorization: {}", authorizationCode, e);
            throw new RuntimeException("Failed to charge authorization: " + e.getMessage());
        }
    }

    /**
     * Verify webhook signature
     */
    public boolean verifyWebhookSignature(String signature, String payload) {
        try {
            // Paystack uses HMAC SHA512
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA512");
            javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                paystackConfig.getWebhookSecret().getBytes(),
                "HmacSHA512"
            );
            mac.init(secretKeySpec);

            byte[] hash = mac.doFinal(payload.getBytes());
            StringBuilder hashString = new StringBuilder();
            for (byte b : hash) {
                hashString.append(String.format("%02x", b));
            }

            return hashString.toString().equals(signature);

        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }
}
