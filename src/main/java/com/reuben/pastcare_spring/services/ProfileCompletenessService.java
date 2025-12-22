package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Member;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for calculating and managing member profile completeness.
 * Completeness is calculated as a percentage (0-100) based on filled fields.
 */
@Service
public class ProfileCompletenessService {

    /**
     * Calculates the profile completeness percentage for a member.
     *
     * Field weights:
     * - Core fields (25% total): firstName, lastName, phoneNumber, sex (already required)
     * - Important fields (50% total): email (10%), dob (10%), maritalStatus (5%),
     *   spouseName if married (5%), location (10%), profileImageUrl (10%)
     * - Additional fields (25% total): occupation (5%), memberSince (5%),
     *   fellowships (5%), emergencyContactName (5%), emergencyContactNumber (5%)
     *
     * @param member the member to calculate completeness for
     * @return completeness percentage (0-100)
     */
    public int calculateCompleteness(Member member) {
        if (member == null) {
            return 0;
        }

        int completeness = 0;

        // Core fields (25% total) - these are required for creation
        // firstName, lastName, phoneNumber, sex are always present
        completeness += 25;

        // Important fields (50% total)
        if (isFieldFilled(member.getDob())) {
            completeness += 10; // Date of birth
        }

        if (isFieldFilled(member.getLocation())) {
            completeness += 10; // Location
        }

        if (isFieldFilled(member.getProfileImageUrl())) {
            completeness += 10; // Profile image
        }

        if (isFieldFilled(member.getMaritalStatus())) {
            completeness += 5; // Marital status
        }

        // Spouse name only counts if married
        if ("married".equalsIgnoreCase(member.getMaritalStatus())) {
            if (isFieldFilled(member.getSpouseName())) {
                completeness += 5; // Spouse name
            }
        } else {
            // If not married, spouse name is not required, so add the points
            completeness += 5;
        }

        // Additional fields (25% total)
        if (isFieldFilled(member.getOccupation())) {
            completeness += 5; // Occupation
        }

        if (isFieldFilled(member.getMemberSince())) {
            completeness += 5; // Member since
        }

        if (member.getFellowships() != null && !member.getFellowships().isEmpty()) {
            completeness += 5; // Fellowships
        }

        if (isFieldFilled(member.getEmergencyContactName())) {
            completeness += 5; // Emergency contact name
        }

        if (isFieldFilled(member.getEmergencyContactNumber())) {
            completeness += 5; // Emergency contact number
        }

        return Math.min(completeness, 100); // Cap at 100%
    }

    /**
     * Gets a list of missing fields that would increase profile completeness.
     *
     * @param member the member to check
     * @return list of human-readable missing field names
     */
    public List<String> getMissingFields(Member member) {
        List<String> missingFields = new ArrayList<>();

        if (member == null) {
            return missingFields;
        }

        // Important fields
        if (!isFieldFilled(member.getDob())) {
            missingFields.add("Date of Birth");
        }

        if (!isFieldFilled(member.getLocation())) {
            missingFields.add("Location");
        }

        if (!isFieldFilled(member.getProfileImageUrl())) {
            missingFields.add("Profile Image");
        }

        if (!isFieldFilled(member.getMaritalStatus())) {
            missingFields.add("Marital Status");
        }

        // Spouse name only if married
        if ("married".equalsIgnoreCase(member.getMaritalStatus()) &&
            !isFieldFilled(member.getSpouseName())) {
            missingFields.add("Spouse Name");
        }

        // Additional fields
        if (!isFieldFilled(member.getOccupation())) {
            missingFields.add("Occupation");
        }

        if (!isFieldFilled(member.getMemberSince())) {
            missingFields.add("Member Since");
        }

        if (member.getFellowships() == null || member.getFellowships().isEmpty()) {
            missingFields.add("Fellowship");
        }

        if (!isFieldFilled(member.getEmergencyContactName())) {
            missingFields.add("Emergency Contact Name");
        }

        if (!isFieldFilled(member.getEmergencyContactNumber())) {
            missingFields.add("Emergency Contact Number");
        }

        return missingFields;
    }

    /**
     * Gets suggestions for completing missing fields.
     *
     * @param member the member to get suggestions for
     * @return list of actionable suggestions
     */
    public List<String> getSuggestions(Member member) {
        List<String> suggestions = new ArrayList<>();
        List<String> missingFields = getMissingFields(member);

        for (String field : missingFields) {
            suggestions.add("Add " + field);
        }

        return suggestions;
    }

    /**
     * Checks if a field is filled (not null and not empty for strings).
     *
     * @param field the field to check
     * @return true if field has a value, false otherwise
     */
    private boolean isFieldFilled(Object field) {
        if (field == null) {
            return false;
        }

        if (field instanceof String) {
            String str = (String) field;
            return !str.trim().isEmpty();
        }

        return true;
    }
}
