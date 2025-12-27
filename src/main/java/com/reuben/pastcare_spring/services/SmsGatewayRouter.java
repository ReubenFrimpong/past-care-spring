package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.SmsGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Routes SMS to the appropriate gateway based on destination country
 */
@Service
@Slf4j
public class SmsGatewayRouter {

    private final AfricasTalkingGatewayService africasTalkingGateway;
    private final TwilioGatewayService twilioGateway;
    private final PhoneNumberService phoneNumberService;

    public SmsGatewayRouter(
        AfricasTalkingGatewayService africasTalkingGateway,
        TwilioGatewayService twilioGateway,
        PhoneNumberService phoneNumberService
    ) {
        this.africasTalkingGateway = africasTalkingGateway;
        this.twilioGateway = twilioGateway;
        this.phoneNumberService = phoneNumberService;
    }

    /**
     * Select best gateway for destination
     */
    public SmsGatewayService selectGateway(String phoneNumber) {
        String countryCode = phoneNumberService.extractCountryCode(phoneNumber);

        // Prefer Africa's Talking for African countries (better rates)
        if (africasTalkingGateway.supportsCountry(countryCode)) {
            log.debug("Selected Africa's Talking for country code: {}", countryCode);
            return africasTalkingGateway;
        }

        // Use Twilio for international (better global coverage)
        log.debug("Selected Twilio for country code: {}", countryCode);
        return twilioGateway;
    }

    /**
     * Send SMS through best gateway
     */
    public SmsGatewayService.SmsGatewayResponse sendSms(String to, String message, Map<String, String> metadata) {
        SmsGatewayService gateway = selectGateway(to);
        return gateway.sendSms(to, message, metadata);
    }

    /**
     * Get gateway by type
     */
    public SmsGatewayService getGateway(SmsGateway gatewayType) {
        return switch (gatewayType) {
            case AFRICAS_TALKING -> africasTalkingGateway;
            case TWILIO -> twilioGateway;
            default -> africasTalkingGateway;
        };
    }
}
