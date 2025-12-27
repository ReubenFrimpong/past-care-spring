package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.SmsGateway;
import lombok.Data;

import java.util.Map;

/**
 * Interface for SMS gateway implementations
 */
public interface SmsGatewayService {

    /**
     * Send SMS through the gateway
     */
    SmsGatewayResponse sendSms(String to, String message, Map<String, String> metadata);

    /**
     * Get delivery status from gateway
     */
    String getDeliveryStatus(String gatewayMessageId);

    /**
     * Check if gateway supports destination country
     */
    boolean supportsCountry(String countryCode);

    /**
     * Get gateway type
     */
    SmsGateway getGatewayType();

    @Data
    class SmsGatewayResponse {
        private boolean success;
        private String messageId;
        private String status;
        private String errorMessage;
        private Map<String, Object> rawResponse;
    }
}
