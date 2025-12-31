package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.Location;
import com.reuben.pastcare_spring.models.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProfileCompletenessService Tests")
class ProfileCompletenessServiceTest {

    private ProfileCompletenessService service;

    @BeforeEach
    void setUp() {
        service = new ProfileCompletenessService();
    }

    @Test
    @DisplayName("Should return 0 for null member")
    void testCalculateCompletenessNullMember() {
        int completeness = service.calculateCompleteness(null);
        assertEquals(0, completeness);
    }

    @Test
    @DisplayName("Should return 32% for minimal member (core fields + spouse not required)")
    void testCalculateCompletenessMinimal() {
        Member member = createMinimalMember();
        int completeness = service.calculateCompleteness(member);
        assertEquals(32, completeness); // 25 core + 7 spouse not required (not married)
    }

    @Test
    @DisplayName("Should return 100% for complete single member profile")
    void testCalculateCompletenessComplete() {
        Member member = createCompleteMember();
        member.setMaritalStatus("single"); // Single member doesn't need spouse link
        int completeness = service.calculateCompleteness(member);
        // 25 (core) + 12 (dob) + 12 (location) + 13 (profile image) + 6 (marital status)
        // + 7 (spouse not required for single) + 5 (occupation) + 5 (memberSince) + 5 (fellowships)
        // + 5 (emergency contact name) + 5 (emergency contact number) = 100
        assertEquals(100, completeness);
    }

    @Test
    @DisplayName("Should calculate 44% for member with DOB only")
    void testCalculateCompletenessWithDob() {
        Member member = createMinimalMember();
        member.setDob(LocalDate.of(1990, 1, 1));

        int completeness = service.calculateCompleteness(member);
        assertEquals(44, completeness); // 32 (base) + 12 (dob)
    }

    @Test
    @DisplayName("Should calculate 44% for member with location only")
    void testCalculateCompletenessWithLocation() {
        Member member = createMinimalMember();
        Location location = new Location();
        location.setCoordinates("0.0,0.0");
        location.setCity("Nairobi");
        member.setLocation(location);

        int completeness = service.calculateCompleteness(member);
        assertEquals(44, completeness); // 32 (base) + 12 (location)
    }

    @Test
    @DisplayName("Should calculate 45% for member with profile image only")
    void testCalculateCompletenessWithProfileImage() {
        Member member = createMinimalMember();
        member.setProfileImageUrl("https://example.com/image.jpg");

        int completeness = service.calculateCompleteness(member);
        assertEquals(45, completeness); // 32 (base) + 13 (profile image)
    }

    @Test
    @DisplayName("Should calculate 38% for single member with marital status")
    void testCalculateCompletenessSingleMember() {
        Member member = createMinimalMember();
        member.setMaritalStatus("single");

        int completeness = service.calculateCompleteness(member);
        assertEquals(38, completeness); // 32 (base) + 6 (marital status)
    }

    @Test
    @DisplayName("Should calculate 31% for married member without spouse link")
    void testCalculateCompletenessMarriedWithoutSpouse() {
        Member member = createMinimalMember();
        member.setMaritalStatus("married");

        int completeness = service.calculateCompleteness(member);
        // 25 (core) + 6 (marital status) + 0 (no spouse not required bonus for married) = 31
        assertEquals(31, completeness);
    }

    @Test
    @DisplayName("Should calculate 31% for married member without spouse link (spouse field is optional)")
    void testCalculateCompletenessMarriedWithSpouse() {
        Member member = createMinimalMember();
        member.setMaritalStatus("married");
        // Spouse linking is optional - spouse field removed

        int completeness = service.calculateCompleteness(member);
        // 25 (core) + 6 (marital status) + 0 (no spouse linked) = 31
        assertEquals(31, completeness);
    }

    @Test
    @DisplayName("Should calculate 37% for member with occupation only")
    void testCalculateCompletenessWithOccupation() {
        Member member = createMinimalMember();
        member.setOccupation("Engineer");

        int completeness = service.calculateCompleteness(member);
        assertEquals(37, completeness); // 32 (base) + 5 (occupation)
    }

    @Test
    @DisplayName("Should calculate 37% for member with memberSince only")
    void testCalculateCompletenessWithMemberSince() {
        Member member = createMinimalMember();
        member.setMemberSince(YearMonth.now());

        int completeness = service.calculateCompleteness(member);
        assertEquals(37, completeness); // 32 (base) + 5 (memberSince)
    }

    @Test
    @DisplayName("Should calculate 37% for member with fellowships")
    void testCalculateCompletenessWithFellowships() {
        Member member = createMinimalMember();
        List<Fellowship> fellowships = new ArrayList<>();
        Fellowship fellowship = new Fellowship();
        fellowship.setName("Youth");
        fellowships.add(fellowship);
        member.setFellowships(fellowships);

        int completeness = service.calculateCompleteness(member);
        assertEquals(37, completeness); // 32 (base) + 5 (fellowships)
    }

    @Test
    @DisplayName("Should not count empty fellowships list")
    void testCalculateCompletenessWithEmptyFellowships() {
        Member member = createMinimalMember();
        member.setFellowships(new ArrayList<>());

        int completeness = service.calculateCompleteness(member);
        assertEquals(32, completeness); // 32 (base) only
    }

    @Test
    @DisplayName("Should calculate 37% for member with emergency contact name")
    void testCalculateCompletenessWithEmergencyContactName() {
        Member member = createMinimalMember();
        member.setEmergencyContactName("Jane Doe");

        int completeness = service.calculateCompleteness(member);
        assertEquals(37, completeness); // 32 (base) + 5 (emergency contact name)
    }

    @Test
    @DisplayName("Should calculate 37% for member with emergency contact number")
    void testCalculateCompletenessWithEmergencyContactNumber() {
        Member member = createMinimalMember();
        member.setEmergencyContactNumber("+254700000000");

        int completeness = service.calculateCompleteness(member);
        assertEquals(37, completeness); // 32 (base) + 5 (emergency contact number)
    }

    @Test
    @DisplayName("Should get missing fields for minimal member")
    void testGetMissingFieldsMinimal() {
        Member member = createMinimalMember();

        List<String> missingFields = service.getMissingFields(member);

        assertTrue(missingFields.contains("Date of Birth"));
        assertTrue(missingFields.contains("Location"));
        assertTrue(missingFields.contains("Profile Image"));
        assertTrue(missingFields.contains("Marital Status"));
        assertTrue(missingFields.contains("Occupation"));
        assertTrue(missingFields.contains("Member Since"));
        assertTrue(missingFields.contains("Fellowship"));
        assertTrue(missingFields.contains("Emergency Contact Name"));
        assertTrue(missingFields.contains("Emergency Contact Number"));
    }

    @Test
    @DisplayName("Should have no missing fields for complete single member")
    void testGetMissingFieldsComplete() {
        Member member = createCompleteMember();
        member.setMaritalStatus("single"); // Single member doesn't need spouse link

        List<String> missingFields = service.getMissingFields(member);

        // Complete single member has all fields filled
        assertEquals(0, missingFields.size());
    }

    @Test
    @DisplayName("Should include spouse link in missing fields for married member")
    void testGetMissingFieldsMarriedWithoutSpouse() {
        Member member = createMinimalMember();
        member.setMaritalStatus("married");

        List<String> missingFields = service.getMissingFields(member);

        assertTrue(missingFields.contains("Spouse Link"));
    }

    @Test
    @DisplayName("Should not include spouse link in missing fields for single member")
    void testGetMissingFieldsSingleMember() {
        Member member = createMinimalMember();
        member.setMaritalStatus("single");

        List<String> missingFields = service.getMissingFields(member);

        assertFalse(missingFields.contains("Spouse Link"));
    }

    @Test
    @DisplayName("Should get suggestions for minimal member")
    void testGetSuggestionsMinimal() {
        Member member = createMinimalMember();

        List<String> suggestions = service.getSuggestions(member);

        assertTrue(suggestions.contains("Add Date of Birth"));
        assertTrue(suggestions.contains("Add Location"));
        assertTrue(suggestions.contains("Add Profile Image"));
        assertTrue(suggestions.contains("Add Marital Status"));
        assertTrue(suggestions.contains("Add Occupation"));
        assertTrue(suggestions.contains("Add Member Since"));
        assertTrue(suggestions.contains("Add Fellowship"));
        assertTrue(suggestions.contains("Add Emergency Contact Name"));
        assertTrue(suggestions.contains("Add Emergency Contact Number"));
    }

    @Test
    @DisplayName("Should have no suggestions for complete single member")
    void testGetSuggestionsComplete() {
        Member member = createCompleteMember();
        member.setMaritalStatus("single"); // Single member doesn't need spouse link

        List<String> suggestions = service.getSuggestions(member);

        // Complete single member has no suggestions
        assertEquals(0, suggestions.size());
    }

    @Test
    @DisplayName("Should suggest spouse link for married member without it")
    void testGetSuggestionsMarriedWithoutSpouse() {
        Member member = createMinimalMember();
        member.setMaritalStatus("married");

        List<String> suggestions = service.getSuggestions(member);

        assertTrue(suggestions.contains("Add Spouse Link"));
    }

    @Test
    @DisplayName("Should not suggest spouse name for single member")
    void testGetSuggestionsSingleMember() {
        Member member = createMinimalMember();
        member.setMaritalStatus("single");

        List<String> suggestions = service.getSuggestions(member);

        assertFalse(suggestions.stream().anyMatch(s -> s.contains("spouse")));
    }

    @Test
    @DisplayName("Should return completeness, missing fields, and suggestions")
    void testGetCompletenessAndSuggestions() {
        Member member = createMinimalMember();

        int completeness = service.calculateCompleteness(member);
        List<String> missingFields = service.getMissingFields(member);
        List<String> suggestions = service.getSuggestions(member);

        assertEquals(32, completeness); // base (25 core + 7 not married spouse bonus)
        assertFalse(missingFields.isEmpty());
        assertFalse(suggestions.isEmpty());
    }

    @Test
    @DisplayName("Should cap completeness at 100%")
    void testCompletenessNeverExceeds100() {
        Member member = createCompleteMember();
        member.setMaritalStatus("single"); // Single member can reach 100%
        // Add extra data that shouldn't increase beyond 100%
        member.setTitle("Pastor");

        int completeness = service.calculateCompleteness(member);

        assertEquals(100, completeness); // Maximum is 100% for complete single member
    }

    @Test
    @DisplayName("Should handle null strings as empty")
    void testNullStringHandling() {
        Member member = createMinimalMember();
        member.setOccupation(null);
        // Spouse linking is optional - no spouse set
        member.setEmergencyContactName(null);
        member.setEmergencyContactNumber(null);

        int completeness = service.calculateCompleteness(member);

        assertEquals(32, completeness); // base (25 core + 7 spouse not required for non-married)
    }

    @Test
    @DisplayName("Should handle empty strings as empty")
    void testEmptyStringHandling() {
        Member member = createMinimalMember();
        member.setOccupation("");
        // Spouse linking is optional - no spouse set

        int completeness = service.calculateCompleteness(member);

        assertEquals(32, completeness); // base (25 core + 7 spouse not required for non-married)
    }

    // Helper methods
    private Member createMinimalMember() {
        Member member = new Member();
        member.setFirstName("John");
        member.setLastName("Doe");
        member.setPhoneNumber("+254700000000");
        member.setSex("male");
        return member;
    }

    private Member createCompleteMember() {
        Member member = createMinimalMember();
        member.setDob(LocalDate.of(1990, 1, 1));

        Location location = new Location();
        location.setCoordinates("0.0,0.0");
        location.setCity("Nairobi");
        member.setLocation(location);

        member.setProfileImageUrl("https://example.com/image.jpg");
        member.setMaritalStatus("married");
        // Spouse linking is optional - spouse field removed
        member.setOccupation("Engineer");
        member.setMemberSince(YearMonth.of(2020, 1));

        List<Fellowship> fellowships = new ArrayList<>();
        Fellowship fellowship = new Fellowship();
        fellowship.setName("Youth");
        fellowships.add(fellowship);
        member.setFellowships(fellowships);

        member.setEmergencyContactName("Jane Doe");
        member.setEmergencyContactNumber("+254700000001");

        return member;
    }
}
