package com.reuben.pastcare_spring.validators;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator for international phone numbers using Google libphonenumber library.
 * Validates phone numbers for all 200+ countries with proper format and length checking.
 *
 * Accepts various international phone number formats:
 * - +233 24 123 4567 (Ghana with country code)
 * - +1 (555) 123-4567 (USA)
 * - +44 20 7946 0958 (UK)
 * - +91 98765 43210 (India)
 * - +234 802 123 4567 (Nigeria)
 * - Allows spaces, dashes, and parentheses for formatting
 *
 * The validator uses the phone number's country code prefix (+233, +1, etc.)
 * to determine which country's validation rules to apply.
 */
public class InternationalPhoneNumberValidator implements ConstraintValidator<InternationalPhoneNumber, String> {

    private final PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

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

        try {
            // Parse the phone number
            // Try with "ZZ" first (for numbers with explicit country code like +233...)
            // If that fails, we'll catch the exception
            Phonenumber.PhoneNumber parsedNumber;

            try {
                parsedNumber = phoneNumberUtil.parse(phoneNumber, "ZZ");
            } catch (NumberParseException e) {
                // If ZZ fails, try parsing with default region as US (allows more formats)
                // This is more lenient for testing purposes
                parsedNumber = phoneNumberUtil.parse(phoneNumber, null);
            }

            // Validate the parsed number
            // isPossibleNumber checks if the length is valid for the country
            // isValidNumber performs more comprehensive validation
            boolean isValid = phoneNumberUtil.isValidNumber(parsedNumber);

            if (!isValid) {
                // Provide a more specific error message
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                    "Invalid phone number format. Please include country code (e.g., +233 24 123 4567)"
                ).addConstraintViolation();
            }

            return isValid;

        } catch (NumberParseException e) {
            // If parsing fails, provide helpful error message
            context.disableDefaultConstraintViolation();

            String errorMessage;
            switch (e.getErrorType()) {
                case INVALID_COUNTRY_CODE:
                    errorMessage = "Invalid country code. Please use a valid country code (e.g., +233 for Ghana)";
                    break;
                case NOT_A_NUMBER:
                    errorMessage = "Phone number contains invalid characters";
                    break;
                case TOO_SHORT_NSN:
                    errorMessage = "Phone number is too short for the selected country";
                    break;
                case TOO_LONG:
                    errorMessage = "Phone number is too long";
                    break;
                default:
                    errorMessage = "Invalid phone number format. Please include country code (e.g., +233 24 123 4567)";
            }

            context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
            return false;
        }
    }
}
