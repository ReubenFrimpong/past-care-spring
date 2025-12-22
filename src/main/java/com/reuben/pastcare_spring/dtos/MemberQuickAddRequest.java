package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.annotations.Unique;
import com.reuben.pastcare_spring.validators.InternationalPhoneNumber;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Quick add request for creating a member with minimal required information.
 * Used for fast member registration (e.g., at church entrance, events).
 * Sets default values for optional fields and marks member for later profile completion.
 */
public record MemberQuickAddRequest(

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must not exceed 100 characters")
    String firstName,

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must not exceed 100 characters")
    String lastName,

    @NotBlank(message = "Phone number is required")
    @InternationalPhoneNumber
    @Unique(table = "member", column = "phone_number", message = "Phone number already exists")
    String phoneNumber,

    @NotBlank(message = "Sex is required")
    @Pattern(regexp = "^(male|female|MALE|FEMALE)$", message = "Sex must be either 'male' or 'female'")
    String sex,

    // Optional fields
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be a valid 2-letter ISO code")
    String countryCode, // Defaults to church's country if not provided

    String coordinates, // Location coordinates (optional)

    Object nominatimAddress, // Full Nominatim address object for location creation (optional)

    Set<String> tags // e.g., "visitor", "first-timer", "event:concert-2024"

) {
    /**
     * Constructor with defaults for optional fields
     */
    public MemberQuickAddRequest {
        // Normalize sex to lowercase
        if (sex != null) {
            sex = sex.toLowerCase();
        }

        // Initialize empty set if tags is null
        if (tags == null) {
            tags = Set.of();
        }
    }
}
