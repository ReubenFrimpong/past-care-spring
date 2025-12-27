package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.SmsGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Africa's Talking SMS Gateway Implementation
 * API Documentation: https://developers.africastalking.com/docs/sms/overview
 */
@Service
@Slf4j
public class AfricasTalkingGatewayService implements SmsGatewayService {

    @Value("${sms.africastalking.api-key:}")
    private String apiKey;

    @Value("${sms.africastalking.username:}")
    private String username;

    @Value("${sms.africastalking.sender-id:}")
    private String senderId;

    @Value("${sms.africastalking.api-url:https://api.africastalking.com/version1/messaging}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final PhoneNumberService phoneNumberService;

    // Africa's Talking supports these African countries well
    private static final Set<String> SUPPORTED_COUNTRIES = Set.of(
        "+233", // Ghana
        "+234", // Nigeria
        "+254", // Kenya
        "+27",  // South Africa
        "+256", // Uganda
        "+255", // Tanzania
        "+250", // Rwanda
        "+263", // Zimbabwe
        "+260", // Zambia
        "+265", // Malawi
        "+237", // Cameroon
        "+225", // Ivory Coast
        "+221", // Senegal
        "+251", // Ethiopia
        "+252", // Somalia
        "+257", // Burundi
        "+261", // Madagascar
        "+266", // Lesotho
        "+267", // Botswana
        "+268", // Eswatini
        "+269"  // Comoros
    );

    public AfricasTalkingGatewayService(RestTemplate restTemplate, PhoneNumberService phoneNumberService) {
        this.restTemplate = restTemplate;
        this.phoneNumberService = phoneNumberService;
    }

    @Override
    public SmsGatewayResponse sendSms(String to, String message, Map<String, String> metadata) {
        SmsGatewayResponse response = new SmsGatewayResponse();

        try {
            // Normalize phone number
            String normalizedPhone = phoneNumberService.normalizePhoneNumber(to);

            // Prepare request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("apiKey", apiKey);
            headers.set("Accept", "application/json");

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("username", username);
            body.add("to", normalizedPhone);
            body.add("message", message);

            if (senderId != null && !senderId.isEmpty()) {
                body.add("from", senderId);
            }

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // Send request
            ResponseEntity<Map> apiResponse = restTemplate.postForEntity(
                apiUrl,
                request,
                Map.class
            );

            // Parse response
            Map<String, Object> responseBody = apiResponse.getBody();
            log.info("Africa's Talking API Response: {}", responseBody);

            if (responseBody != null && responseBody.containsKey("SMSMessageData")) {
                Map<String, Object> smsData = (Map<String, Object>) responseBody.get("SMSMessageData");
                List<Map<String, Object>> recipients = (List<Map<String, Object>>) smsData.get("Recipients");

                if (recipients != null && !recipients.isEmpty()) {
                    Map<String, Object> recipient = recipients.get(0);
                    String status = (String) recipient.get("status");
                    String messageId = (String) recipient.get("messageId");
                    String statusCode = (String) recipient.get("statusCode");

                    response.setSuccess("Success".equalsIgnoreCase(status) || "Sent".equalsIgnoreCase(status));
                    response.setMessageId(messageId);
                    response.setStatus(status);
                    response.setRawResponse(responseBody);

                    if (!"201".equals(statusCode) && !"200".equals(statusCode)) {
                        response.setErrorMessage("SMS failed with status: " + status);
                    }
                } else {
                    response.setSuccess(false);
                    response.setErrorMessage("No recipients in response");
                    response.setRawResponse(responseBody);
                }
            } else {
                response.setSuccess(false);
                response.setErrorMessage("Invalid API response format");
                response.setRawResponse(responseBody);
            }

        } catch (Exception e) {
            log.error("Error sending SMS via Africa's Talking: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    @Override
    public String getDeliveryStatus(String gatewayMessageId) {
        // Africa's Talking requires separate API call to fetch delivery reports
        // This can be implemented using their delivery reports API
        // For now, returning null to indicate status not available
        return null;
    }

    @Override
    public boolean supportsCountry(String countryCode) {
        return SUPPORTED_COUNTRIES.contains(countryCode);
    }

    @Override
    public SmsGateway getGatewayType() {
        return SmsGateway.AFRICAS_TALKING;
    }
}
