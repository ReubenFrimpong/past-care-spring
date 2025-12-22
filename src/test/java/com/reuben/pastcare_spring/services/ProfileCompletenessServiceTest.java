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
    @DisplayName("Should return 30% for minimal member (core fields + spouse not required)")
    void testCalculateCompletenessMinimal() {
        Member member = createMinimalMember();
        int completeness = service.calculateCompleteness(member);
        assertEquals(30, completeness); // 25 core + 5 spouse not required
    }

    @Test
    @DisplayName("Should return 90% for complete member profile")
    void testCalculateCompletenessComplete() {
        Member member = createCompleteMember();
        int completeness = service.calculateCompleteness(member);
        assertEquals(90, completeness); // All fields filled
    }

    @Test
    @DisplayName("Should calculate 40% for member with DOB only")
    void testCalculateCompletenessWithDob() {
        Member member = createMinimalMember();
        member.setDob(LocalDate.of(1990, 1, 1));

        int completeness = service.calculateCompleteness(member);
        assertEquals(40, completeness); // 30 (base) + 10 (dob)
    }

    @Test
    @DisplayName("Should calculate 40% for member with location only")
    void testCalculateCompletenessWithLocation() {
        Member member = createMinimalMember();
        Location location = new Location();
        location.setCoordinates("0.0,0.0");
        location.setCity("Nairobi");
        member.setLocation(location);

        int completeness = service.calculateCompleteness(member);
        assertEquals(40, completeness); // 30 (base) + 10 (location)
    }

    @Test
    @DisplayName("Should calculate 40% for member with profile image only")
    void testCalculateCompletenessWithProfileImage() {
        Member member = createMinimalMember();
        member.setProfileImageUrl("https://example.com/image.jpg");

        int completeness = service.calculateCompleteness(member);
        assertEquals(40, completeness); // 30 (base) + 10 (profile image)
    }

    @Test
    @DisplayName("Should calculate 35% for single member with marital status")
    void testCalculateCompletenessSingleMember() {
        Member member = createMinimalMember();
        member.setMaritalStatus("single");

        int completeness = service.calculateCompleteness(member);
        assertEquals(35, completeness); // 30 (base) + 5 (marital status)
    }

    @Test
    @DisplayName("Should calculate 30% for married member without spouse name")
    void testCalculateCompletenessMarriedWithoutSpouse() {
        Member member = createMinimalMember();
        member.setMaritalStatus("married");

        int completeness = service.calculateCompleteness(member);
        assertEquals(30, completeness); // 25 (core) + 5 (marital status), no spouse name, no auto-add
    }

    @Test
    @DisplayName("Should calculate 35% for married member with spouse name")
    void testCalculateCompletenessMarriedWithSpouse() {
        Member member = createMinimalMember();
        member.setMaritalStatus("married");
        member.setSpouseName("Jane Doe");

        int completeness = service.calculateCompleteness(member);
        assertEquals(35, completeness); // 25 (core) + 5 (marital status) + 5 (spouse name)
    }

    @Test
    @DisplayName("Should calculate 35% for member with occupation only")
    void testCalculateCompletenessWithOccupation() {
        Member member = createMinimalMember();
        member.setOccupation("Engineer");

        int completeness = service.calculateCompleteness(member);
        assertEquals(35, completeness); // 30 (base) + 5 (occupation)
    }

    @Test
    @DisplayName("Should calculate 35% for member with memberSince only")
    void testCalculateCompletenessWithMemberSince() {
        Member member = createMinimalMember();
        member.setMemberSince(YearMonth.now());

        int completeness = service.calculateCompleteness(member);
        assertEquals(35, completeness); // 30 (base) + 5 (memberSince)
    }

    @Test
    @DisplayName("Should calculate 35% for member with fellowships")
    void testCalculateCompletenessWithFellowships() {
        Member member = createMinimalMember();
        List<Fellowship> fellowships = new ArrayList<>();
        Fellowship fellowship = new Fellowship();
        fellowship.setName("Youth");
        fellowships.add(fellowship);
        member.setFellowships(fellowships);

        int completeness = service.calculateCompleteness(member);
        assertEquals(35, completeness); // 30 (base) + 5 (fellowships)
    }

    @Test
    @DisplayName("Should not count empty fellowships list")
    void testCalculateCompletenessWithEmptyFellowships() {
        Member member = createMinimalMember();
        member.setFellowships(new ArrayList<>());

        int completeness = service.calculateCompleteness(member);
        assertEquals(30, completeness); // 30 (base) only
    }

    @Test
    @DisplayName("Should calculate 35% for member with emergency contact name")
    void testCalculateCompletenessWithEmergencyContactName() {
        Member member = createMinimalMember();
        member.setEmergencyContactName("Jane Doe");

        int completeness = service.calculateCompleteness(member);
        assertEquals(35, completeness); // 30 (base) + 5 (emergency contact name)
    }

    @Test
    @DisplayName("Should calculate 35% for member with emergency contact number")
    void testCalculateCompletenessWithEmergencyContactNumber() {
        Member member = createMinimalMember();
        member.setEmergencyContactNumber("+254700000000");

        int completeness = service.calculateCompleteness(member);
        assertEquals(35, completeness); // 30 (base) + 5 (emergency contact number)
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
    @DisplayName("Should get empty missing fields for complete member")
    void testGetMissingFieldsComplete() {
        Member member = createCompleteMember();

        List<String> missingFields = service.getMissingFields(member);

        assertTrue(missingFields.isEmpty());
    }

    @Test
    @DisplayName("Should include spouse name in missing fields for married member")
    void testGetMissingFieldsMarriedWithoutSpouse() {
        Member member = createMinimalMember();
        member.setMaritalStatus("married");

        List<String> missingFields = service.getMissingFields(member);

        assertTrue(missingFields.contains("Spouse Name"));
    }

    @Test
    @DisplayName("Should not include spouse name in missing fields for single member")
    void testGetMissingFieldsSingleMember() {
        Member member = createMinimalMember();
        member.setMaritalStatus("single");

        List<String> missingFields = service.getMissingFields(member);

        assertFalse(missingFields.contains("Spouse Name"));
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
    @DisplayName("Should get empty suggestions for complete member")
    void testGetSuggestionsComplete() {
        Member member = createCompleteMember();

        List<String> suggestions = service.getSuggestions(member);

        assertTrue(suggestions.isEmpty());
    }

    @Test
    @DisplayName("Should suggest spouse name for married member without it")
    void testGetSuggestionsMarriedWithoutSpouse() {
        Member member = createMinimalMember();
        member.setMaritalStatus("married");

        List<String> suggestions = service.getSuggestions(member);

        assertTrue(suggestions.contains("Add Spouse Name"));
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

        assertEquals(30, completeness); // base
        assertFalse(missingFields.isEmpty());
        assertFalse(suggestions.isEmpty());
    }

    @Test
    @DisplayName("Should cap completeness at 100%")
    void testCompletenessNeverExceeds100() {
        Member member = createCompleteMember();
        // Add extra data that shouldn't increase beyond 100%
        member.setTitle("Pastor");

        int completeness = service.calculateCompleteness(member);

        assertEquals(90, completeness); // Current maximum with all fields
    }

    @Test
    @DisplayName("Should handle null strings as empty")
    void testNullStringHandling() {
        Member member = createMinimalMember();
        member.setOccupation(null);
        member.setSpouseName(null);
        member.setEmergencyContactName(null);
        member.setEmergencyContactNumber(null);

        int completeness = service.calculateCompleteness(member);

        assertEquals(30, completeness); // base (25 core + 5 spouse not required)
    }

    @Test
    @DisplayName("Should handle empty strings as empty")
    void testEmptyStringHandling() {
        Member member = createMinimalMember();
        member.setOccupation("");
        member.setSpouseName("   ");

        int completeness = service.calculateCompleteness(member);

        assertEquals(30, completeness); // base (25 core + 5 spouse not required)
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
        member.setSpouseName("Jane Doe");
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
