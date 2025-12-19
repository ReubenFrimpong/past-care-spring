package com.reuben.pastcare_spring.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for international phone numbers.
 * Accepts various international phone number formats:
 * - +1234567890 (E.164 format with country code)
 * - 001234567890 (international prefix format)
 * - 1234567890 (local format - 7 to 15 digits)
 * - Allows spaces, dashes, and parentheses for formatting
 *
 * Examples of valid numbers:
 * - +233 20 123 4567 (Ghana)
 * - +1 (555) 123-4567 (USA)
 * - +44 20 7946 0958 (UK)
 * - +91 98765 43210 (India)
 * - 0123456789 (Local)
 */
public class InternationalPhoneNumberValidator implements ConstraintValidator<InternationalPhoneNumber, String> {

    // Matches international phone numbers with optional country code
    // Format: Optional + or 00, followed by 1-3 digit country code, then 7-15 digits
    // Allows spaces, dashes, parentheses for formatting
    private static final String INTERNATIONAL_PHONE_REGEX =
        "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,5}[-\\s.]?[0-9]{1,5}[-\\s.]?[0-9]{0,5}$";

    @Override
    public void initialize(InternationalPhoneNumber constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        // Null or empty is considered valid (use @NotNull/@NotBlank for required fields)
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return true;
        }

        // Remove all spaces, dashes, parentheses, and dots for validation
        String cleanedNumber = phoneNumber.replaceAll("[\\s\\-().+]", "");

        // Must have at least 7 digits (local numbers) and max 15 (E.164 standard)
        if (cleanedNumber.length() < 7 || cleanedNumber.length() > 15) {
            return false;
        }

        // Must contain only digits
        if (!cleanedNumber.matches("\\d+")) {
            return false;
        }

        // Validate against regex for proper formatting
        return phoneNumber.matches(INTERNATIONAL_PHONE_REGEX);
    }
}
