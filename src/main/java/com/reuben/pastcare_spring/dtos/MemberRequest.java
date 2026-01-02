package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.reuben.pastcare_spring.annotations.Unique;
import com.reuben.pastcare_spring.validators.InternationalPhoneNumber;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record MemberRequest(
  
  Long id,
  
  @NotBlank(message = "Firstname is required")
  String firstName,
  
  String otherName,
  
  @NotBlank(message = "Lastname is required")
  String lastName,
  
  String title,
  
  @NotBlank(message = "Sex is required")
  String sex,

  Long churchId,
  
  List<Long> fellowshipIds,

  LocalDate dob,

  @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be a valid 2-letter ISO code (e.g., GH, US, NG)")
  String countryCode,

  // IANA timezone (e.g., "Africa/Accra", "America/New_York", "Europe/London")
  // Pattern allows timezone names like: Area/Location or Area/Location/Sublocation
  @Pattern(regexp = "^[A-Za-z_]+/[A-Za-z_]+(/[A-Za-z_]+)?$", message = "Timezone must be a valid IANA timezone (e.g., Africa/Accra, America/New_York)")
  String timezone,

  @NotBlank(message = "Phone number is required")
  @InternationalPhoneNumber
  @Unique(table = "member", column = "phone_number", message = "Phone number already taken")
  String phoneNumber,

  @Pattern(regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Email must be valid")
  String email,

  @InternationalPhoneNumber
  String whatsappNumber,

  @InternationalPhoneNumber
  String otherPhoneNumber,

  // GPS coordinates as "latitude,longitude" string
  String coordinates,

  // Optional: Full Nominatim address data for location creation
  Map<String, Object> nominatimAddress,

  String profileImageUrl,

  @NotBlank(message = "Marital status is required")
  String maritalStatus,

  Long spouseId,  // Optional: ID of linked spouse member

  String occupation,

  YearMonth memberSince,

  String emergencyContactName,

  @InternationalPhoneNumber
  String emergencyContactNumber,

  String notes,

  // Phase 2 fields
  Set<String> tags,

  // Household management fields (unified submission)
  Long householdId,  // Join existing household
  Boolean createNewHousehold,  // Flag to create new household
  String newHouseholdName,  // Name if creating new (auto-generated from lastName if null)
  Boolean makeHouseholdHead,  // Whether this member should be household head

  // Family relation fields (for atomic submission)
  Long linkSpouseId,  // Link spouse during member creation/update
  List<Long> linkParentIds,  // Link parents during member creation/update
  List<Long> linkChildIds  // Link children during member creation/update

) {

}
