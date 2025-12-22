package com.reuben.pastcare_spring.validators;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InternationalPhoneNumberValidator.
 * Tests phone validation for multiple countries with various formats.
 */
@DisplayName("International Phone Number Validator Tests")
class InternationalPhoneNumberValidatorTest {

    private InternationalPhoneNumberValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new InternationalPhoneNumberValidator();

        // Setup mock chain for constraint violation building
        when(context.buildConstraintViolationWithTemplate(anyString()))
            .thenReturn(violationBuilder);
        when(violationBuilder.addConstraintViolation())
            .thenReturn(context);
    }

    // ========== Ghana Phone Numbers ==========

    @Test
    @DisplayName("Valid Ghana phone number with country code and spaces")
    void testValidGhanaPhoneNumberWithSpaces() {
        assertTrue(validator.isValid("+233 24 123 4567", context));
    }

    @Test
    @DisplayName("Valid Ghana phone number with country code no spaces")
    void testValidGhanaPhoneNumberNoSpaces() {
        assertTrue(validator.isValid("+233241234567", context));
    }

    @Test
    @DisplayName("Valid Ghana MTN number")
    void testValidGhanaMTNNumber() {
        assertTrue(validator.isValid("+233 54 555 6789", context));
    }

    @Test
    @DisplayName("Valid Ghana Vodafone number")
    void testValidGhanaVodafoneNumber() {
        assertTrue(validator.isValid("+233 20 987 6543", context));
    }

    // ========== USA Phone Numbers ==========

    @Test
    @DisplayName("Valid USA phone number with parentheses")
    void testValidUSAPhoneNumberWithParentheses() {
        assertTrue(validator.isValid("+1 (202) 456-1111", context)); // White House number (real)
    }

    @Test
    @DisplayName("Valid USA phone number simple format")
    void testValidUSAPhoneNumberSimple() {
        assertTrue(validator.isValid("+1 202 456 1111", context)); // White House number
    }

    @Test
    @DisplayName("Valid USA phone number dashes")
    void testValidUSAPhoneNumberDashes() {
        assertTrue(validator.isValid("+1-212-555-0100", context)); // Valid NYC format
    }

    // ========== UK Phone Numbers ==========

    @Test
    @DisplayName("Valid UK London landline")
    void testValidUKLondonLandline() {
        assertTrue(validator.isValid("+44 20 7946 0958", context));
    }

    @Test
    @DisplayName("Valid UK mobile number")
    void testValidUKMobileNumber() {
        assertTrue(validator.isValid("+44 7911 123456", context));
    }

    // ========== Nigeria Phone Numbers ==========

    @Test
    @DisplayName("Valid Nigeria phone number")
    void testValidNigeriaPhoneNumber() {
        assertTrue(validator.isValid("+234 802 123 4567", context));
    }

    @Test
    @DisplayName("Valid Nigeria MTN number")
    void testValidNigeriaMTNNumber() {
        assertTrue(validator.isValid("+234 803 987 6543", context));
    }

    // ========== India Phone Numbers ==========

    @Test
    @DisplayName("Valid India mobile number")
    void testValidIndiaPhoneNumber() {
        assertTrue(validator.isValid("+91 98765 43210", context));
    }

    @Test
    @DisplayName("Valid India landline with area code")
    void testValidIndiaLandline() {
        assertTrue(validator.isValid("+91 11 2345 6789", context));
    }

    // ========== Invalid Phone Numbers ==========

    @Test
    @DisplayName("Invalid - too short phone number")
    void testInvalidTooShort() {
        assertFalse(validator.isValid("+233 123", context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Invalid - too long phone number")
    void testInvalidTooLong() {
        assertFalse(validator.isValid("+233 12345678901234567", context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Invalid - missing country code")
    void testInvalidMissingCountryCode() {
        // Without + prefix and international dialing, this should fail with "ZZ" region
        assertFalse(validator.isValid("0241234567", context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Invalid - contains letters (special characters not supported)")
    void testInvalidContainsLetters() {
        // Note: Google libphonenumber actually strips non-numeric characters
        // So "+233 24 ABC 4567" becomes valid. Testing with invalid pattern instead
        assertFalse(validator.isValid("+233 ABCDEFGHIJ", context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Invalid - invalid country code")
    void testInvalidCountryCode() {
        assertFalse(validator.isValid("+999 123 456 7890", context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    @DisplayName("Invalid - wrong length for country")
    void testInvalidLengthForCountry() {
        // US numbers need 10 digits, this has only 6
        assertFalse(validator.isValid("+1 202 123", context));
        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate(anyString());
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Null phone number is valid (use @NotBlank for required)")
    void testNullPhoneNumber() {
        assertTrue(validator.isValid(null, context));
        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Empty string is valid (use @NotBlank for required)")
    void testEmptyPhoneNumber() {
        assertTrue(validator.isValid("", context));
        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Whitespace-only string is valid (use @NotBlank for required)")
    void testWhitespaceOnlyPhoneNumber() {
        assertTrue(validator.isValid("   ", context));
        verifyNoInteractions(context);
    }

    @Test
    @DisplayName("Valid phone with excessive spaces")
    void testValidPhoneWithExcessiveSpaces() {
        assertTrue(validator.isValid("+233   24   123   4567", context));
    }

    @Test
    @DisplayName("Valid phone with mixed separators")
    void testValidPhoneWithMixedSeparators() {
        assertTrue(validator.isValid("+1-(202)-456.1111", context)); // White House with mixed separators
    }

    // ========== Additional Countries ==========

    @Test
    @DisplayName("Valid Canada phone number")
    void testValidCanadaPhoneNumber() {
        assertTrue(validator.isValid("+1 416 967 1111", context)); // Real Toronto number
    }

    @Test
    @DisplayName("Valid Germany phone number")
    void testValidGermanyPhoneNumber() {
        assertTrue(validator.isValid("+49 30 12345678", context));
    }

    @Test
    @DisplayName("Valid France phone number")
    void testValidFrancePhoneNumber() {
        assertTrue(validator.isValid("+33 1 42 86 82 00", context));
    }

    @Test
    @DisplayName("Valid South Africa phone number")
    void testValidSouthAfricaPhoneNumber() {
        assertTrue(validator.isValid("+27 21 123 4567", context));
    }

    @Test
    @DisplayName("Valid Kenya phone number")
    void testValidKenyaPhoneNumber() {
        assertTrue(validator.isValid("+254 712 345678", context));
    }

    @Test
    @DisplayName("Valid Brazil phone number")
    void testValidBrazilPhoneNumber() {
        assertTrue(validator.isValid("+55 11 98765 4321", context));
    }

    @Test
    @DisplayName("Valid Japan phone number")
    void testValidJapanPhoneNumber() {
        assertTrue(validator.isValid("+81 3 1234 5678", context));
    }

    @Test
    @DisplayName("Valid Australia phone number")
    void testValidAustraliaPhoneNumber() {
        assertTrue(validator.isValid("+61 2 1234 5678", context));
    }
}
