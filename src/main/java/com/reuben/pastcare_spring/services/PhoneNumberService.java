package com.reuben.pastcare_spring.services;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PhoneNumberService {

    private static final Map<String, String> COUNTRY_CODE_MAP = new HashMap<>();

    static {
        // Popular country codes (sorted by length - longest first for matching)
        COUNTRY_CODE_MAP.put("+1", "USA/Canada");
        COUNTRY_CODE_MAP.put("+44", "United Kingdom");
        COUNTRY_CODE_MAP.put("+233", "Ghana");
        COUNTRY_CODE_MAP.put("+234", "Nigeria");
        COUNTRY_CODE_MAP.put("+254", "Kenya");
        COUNTRY_CODE_MAP.put("+27", "South Africa");
        COUNTRY_CODE_MAP.put("+256", "Uganda");
        COUNTRY_CODE_MAP.put("+255", "Tanzania");
        COUNTRY_CODE_MAP.put("+250", "Rwanda");
        COUNTRY_CODE_MAP.put("+263", "Zimbabwe");
        COUNTRY_CODE_MAP.put("+260", "Zambia");
        COUNTRY_CODE_MAP.put("+265", "Malawi");
        COUNTRY_CODE_MAP.put("+237", "Cameroon");
        COUNTRY_CODE_MAP.put("+225", "Ivory Coast");
        COUNTRY_CODE_MAP.put("+221", "Senegal");
        COUNTRY_CODE_MAP.put("+91", "India");
        COUNTRY_CODE_MAP.put("+86", "China");
        COUNTRY_CODE_MAP.put("+81", "Japan");
        COUNTRY_CODE_MAP.put("+49", "Germany");
        COUNTRY_CODE_MAP.put("+33", "France");
    }

    /**
     * Normalize phone number to E.164 format
     */
    public String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        // Remove all non-digit characters except +
        String normalized = phoneNumber.replaceAll("[^\\d+]", "");

        // If doesn't start with +, assume Ghana number and add +233
        if (!normalized.startsWith("+")) {
            // Remove leading 0 if present
            if (normalized.startsWith("0")) {
                normalized = normalized.substring(1);
            }
            normalized = "+233" + normalized;
        }

        return normalized;
    }

    /**
     * Extract country code from phone number
     */
    public String extractCountryCode(String phoneNumber) {
        String normalized = normalizePhoneNumber(phoneNumber);

        if (normalized == null || !normalized.startsWith("+")) {
            return "OTHER";
        }

        // Try to match known country codes (longest first)
        for (Map.Entry<String, String> entry : COUNTRY_CODE_MAP.entrySet()) {
            if (normalized.startsWith(entry.getKey())) {
                return entry.getKey();
            }
        }

        // If no match found, extract first 1-4 digits after +
        // Most country codes are 1-4 digits
        if (normalized.length() >= 2) {
            // Try 4 digits
            if (normalized.length() >= 5) {
                return normalized.substring(0, 5);
            }
            // Try 3 digits
            if (normalized.length() >= 4) {
                return normalized.substring(0, 4);
            }
            // Try 2 digits
            if (normalized.length() >= 3) {
                return normalized.substring(0, 3);
            }
            // At least 1 digit
            return normalized.substring(0, 2);
        }

        return "OTHER";
    }

    /**
     * Check if phone number is local (Ghana)
     */
    public boolean isLocalNumber(String phoneNumber) {
        String countryCode = extractCountryCode(phoneNumber);
        return "+233".equals(countryCode);
    }

    /**
     * Calculate message count based on SMS length
     * Standard SMS: 160 characters
     * Unicode SMS (with special chars): 70 characters
     */
    public int calculateMessageCount(String message) {
        if (message == null || message.isEmpty()) {
            return 0;
        }

        // Check if message contains unicode characters
        boolean hasUnicode = message.chars().anyMatch(c -> c > 127);

        int maxLength = hasUnicode ? 70 : 160;
        int headerLength = hasUnicode ? 3 : 7; // Concatenated SMS header overhead

        if (message.length() <= maxLength) {
            return 1;
        }

        // For concatenated messages, each part has overhead
        int effectiveMaxLength = maxLength - headerLength;
        return (int) Math.ceil((double) message.length() / effectiveMaxLength);
    }

    /**
     * Validate phone number format
     */
    public boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        String normalized = normalizePhoneNumber(phoneNumber);

        // E.164 format: + followed by 7-15 digits
        return normalized.matches("^\\+\\d{7,15}$");
    }
}
