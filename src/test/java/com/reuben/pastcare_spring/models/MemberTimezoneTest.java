package com.reuben.pastcare_spring.models;

import com.reuben.pastcare_spring.dtos.MemberRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Member timezone functionality.
 * Tests timezone validation and storage.
 */
@DisplayName("Member Timezone Tests")
class MemberTimezoneTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Valid timezone - Africa/Accra")
    void testValidTimezoneAfricaAccra() {
        MemberRequest request = createMemberRequest("Africa/Accra");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Africa/Accra should be valid timezone");
    }

    @Test
    @DisplayName("Valid timezone - America/New_York")
    void testValidTimezoneAmericaNewYork() {
        MemberRequest request = createMemberRequest("America/New_York");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "America/New_York should be valid timezone");
    }

    @Test
    @DisplayName("Valid timezone - Europe/London")
    void testValidTimezoneEuropeLondon() {
        MemberRequest request = createMemberRequest("Europe/London");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Europe/London should be valid timezone");
    }

    @Test
    @DisplayName("Valid timezone - Asia/Tokyo")
    void testValidTimezoneAsiaTokyo() {
        MemberRequest request = createMemberRequest("Asia/Tokyo");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Asia/Tokyo should be valid timezone");
    }

    @Test
    @DisplayName("Valid timezone with sublocation - America/Indiana/Indianapolis")
    void testValidTimezoneWithSublocation() {
        MemberRequest request = createMemberRequest("America/Indiana/Indianapolis");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "America/Indiana/Indianapolis should be valid timezone");
    }

    @Test
    @DisplayName("Valid timezone - Pacific/Auckland")
    void testValidTimezonePacificAuckland() {
        MemberRequest request = createMemberRequest("Pacific/Auckland");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Pacific/Auckland should be valid timezone");
    }

    @Test
    @DisplayName("Invalid timezone - missing area")
    void testInvalidTimezoneMissingArea() {
        MemberRequest request = createMemberRequest("Accra");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Timezone without area (just 'Accra') should be invalid");
    }

    @Test
    @DisplayName("Invalid timezone - just slash")
    void testInvalidTimezoneJustSlash() {
        MemberRequest request = createMemberRequest("/");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Just slash should be invalid");
    }

    @Test
    @DisplayName("Invalid timezone - numbers")
    void testInvalidTimezoneWithNumbers() {
        MemberRequest request = createMemberRequest("UTC+3");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "UTC+3 format should be invalid (not IANA format)");
    }

    @Test
    @DisplayName("Invalid timezone - spaces")
    void testInvalidTimezoneWithSpaces() {
        MemberRequest request = createMemberRequest("Africa/ Accra");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Timezone with spaces should be invalid");
    }

    @Test
    @DisplayName("Null timezone is valid (optional field)")
    void testNullTimezoneIsValid() {
        MemberRequest request = createMemberRequest(null);
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        // Check that timezone is not the source of any violations
        boolean hasTimezoneViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("timezone"));
        assertFalse(hasTimezoneViolation, "Null timezone should be valid (optional field)");
    }

    @Test
    @DisplayName("Empty timezone is invalid")
    void testEmptyTimezoneIsInvalid() {
        MemberRequest request = createMemberRequest("");
        Set<ConstraintViolation<MemberRequest>> violations = validator.validate(request);
        boolean hasTimezoneViolation = violations.stream()
            .anyMatch(v -> v.getPropertyPath().toString().equals("timezone"));
        assertTrue(hasTimezoneViolation, "Empty string timezone should be invalid");
    }

    /**
     * Helper method to create a MemberRequest with a specific timezone
     */
    private MemberRequest createMemberRequest(String timezone) {
        return new MemberRequest(
            null, // id
            "John", // firstName
            "Middle", // otherName
            "Doe", // lastName
            "Mr", // title
            "Male", // sex
            1L, // churchId
            null, // fellowshipIds
            null, // dob
            "GH", // countryCode
            timezone, // timezone
            "+233241234567", // phoneNumber
            null, // whatsappNumber
            null, // otherPhoneNumber
            null, // coordinates
            null, // nominatimAddress
            null, // profileImageUrl
            "single", // maritalStatus
            null, // spouseName
            null, // occupation
            null, // memberSince
            null, // emergencyContactName
            null, // emergencyContactNumber
            null, // notes
            null // tags
        );
    }
}
