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
 * Twilio SMS Gateway Implementation (for international SMS)
 * API Documentation: https://www.twilio.com/docs/sms/api
 */
@Service
@Slf4j
public class TwilioGatewayService implements SmsGatewayService {

    @Value("${sms.twilio.account-sid:}")
    private String accountSid;

    @Value("${sms.twilio.auth-token:}")
    private String authToken;

    @Value("${sms.twilio.from-number:}")
    private String fromNumber;

    @Value("${sms.twilio.api-url:https://api.twilio.com/2010-04-01}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final PhoneNumberService phoneNumberService;

    // Twilio has excellent global coverage - all countries
    private static final Set<String> SUPPORTED_COUNTRIES = Set.of(
        "+1",   // USA/Canada
        "+44",  // UK
        "+49",  // Germany
        "+33",  // France
        "+39",  // Italy
        "+34",  // Spain
        "+81",  // Japan
        "+86",  // China
        "+91",  // India
        "+61",  // Australia
        "+55",  // Brazil
        "+52"   // Mexico
        // Twilio supports 180+ countries
    );

    public TwilioGatewayService(RestTemplate restTemplate, PhoneNumberService phoneNumberService) {
        this.restTemplate = restTemplate;
        this.phoneNumberService = phoneNumberService;
    }

    @Override
    public SmsGatewayResponse sendSms(String to, String message, Map<String, String> metadata) {
        SmsGatewayResponse response = new SmsGatewayResponse();

        try {
            // Normalize phone number
            String normalizedPhone = phoneNumberService.normalizePhoneNumber(to);

            // Prepare request with Basic Auth
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(accountSid, authToken);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("To", normalizedPhone);
            body.add("From", fromNumber);
            body.add("Body", message);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            String url = apiUrl + "/Accounts/" + accountSid + "/Messages.json";

            // Send request
            ResponseEntity<Map> apiResponse = restTemplate.postForEntity(
                url,
                request,
                Map.class
            );

            // Parse response
            Map<String, Object> responseBody = apiResponse.getBody();
            log.info("Twilio API Response: {}", responseBody);

            if (responseBody != null && responseBody.containsKey("sid")) {
                String sid = (String) responseBody.get("sid");
                String status = (String) responseBody.get("status");
                String errorMessage = (String) responseBody.get("error_message");

                response.setSuccess(errorMessage == null);
                response.setMessageId(sid);
                response.setStatus(status);
                response.setErrorMessage(errorMessage);
                response.setRawResponse(responseBody);
            } else {
                response.setSuccess(false);
                response.setErrorMessage("Invalid API response");
                response.setRawResponse(responseBody);
            }

        } catch (Exception e) {
            log.error("Error sending SMS via Twilio: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setErrorMessage(e.getMessage());
        }

        return response;
    }

    @Override
    public String getDeliveryStatus(String gatewayMessageId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(accountSid, authToken);

            HttpEntity<?> request = new HttpEntity<>(headers);
            String url = apiUrl + "/Accounts/" + accountSid + "/Messages/" + gatewayMessageId + ".json";

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                Map.class
            );

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("status")) {
                return (String) responseBody.get("status");
            }
        } catch (Exception e) {
            log.error("Error fetching delivery status from Twilio: {}", e.getMessage());
        }

        return null;
    }

    @Override
    public boolean supportsCountry(String countryCode) {
        // Twilio supports most countries globally
        // For simplicity, we'll return true for non-African countries
        return SUPPORTED_COUNTRIES.contains(countryCode) || !countryCode.startsWith("+2");
    }

    @Override
    public SmsGateway getGatewayType() {
        return SmsGateway.TWILIO;
    }
}
