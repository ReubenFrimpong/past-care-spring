package com.reuben.pastcare_spring.testutil;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.MemberStatus;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Test data builder for Member entities.
 *
 * Provides a fluent API for creating test members with various configurations.
 *
 * Usage:
 * <pre>
 * {@code
 * Member member = new MemberTestBuilder()
 *     .withFirstName("John")
 *     .withLastName("Doe")
 *     .withChurchId(churchId)
 *     .withStatus(MemberStatus.MEMBER)
 *     .build();
 * }
 * </pre>
 */
public class MemberTestBuilder {

    private String firstName = "Test";
    private String otherName;
    private String lastName = "Member";
    private String title;
    private String sex = "MALE";
    private LocalDate dob = LocalDate.of(1990, 1, 1);
    private String countryCode = "KE";
    private String timezone = "Africa/Nairobi";
    private String phoneNumber = "+254700000000";
    private String email;
    private String whatsappNumber;
    private String otherPhoneNumber;
    private String profileImageUrl;
    private String maritalStatus = "SINGLE";
    private String occupation;
    private YearMonth memberSince = YearMonth.now();
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String notes;
    private Boolean isVerified = true;
    private MemberStatus status = MemberStatus.MEMBER;

    public MemberTestBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public MemberTestBuilder withOtherName(String otherName) {
        this.otherName = otherName;
        return this;
    }

    public MemberTestBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public MemberTestBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public MemberTestBuilder withSex(String sex) {
        this.sex = sex;
        return this;
    }

    public MemberTestBuilder withMale() {
        this.sex = "MALE";
        return this;
    }

    public MemberTestBuilder withFemale() {
        this.sex = "FEMALE";
        return this;
    }

    public MemberTestBuilder withDob(LocalDate dob) {
        this.dob = dob;
        return this;
    }

    public MemberTestBuilder withCountryCode(String countryCode) {
        this.countryCode = countryCode;
        return this;
    }

    public MemberTestBuilder withTimezone(String timezone) {
        this.timezone = timezone;
        return this;
    }

    public MemberTestBuilder withPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public MemberTestBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public MemberTestBuilder withWhatsappNumber(String whatsappNumber) {
        this.whatsappNumber = whatsappNumber;
        return this;
    }

    public MemberTestBuilder withOtherPhoneNumber(String otherPhoneNumber) {
        this.otherPhoneNumber = otherPhoneNumber;
        return this;
    }

    public MemberTestBuilder withProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
        return this;
    }

    public MemberTestBuilder withMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
        return this;
    }

    public MemberTestBuilder withMarried() {
        this.maritalStatus = "MARRIED";
        return this;
    }

    public MemberTestBuilder withSingle() {
        this.maritalStatus = "SINGLE";
        return this;
    }

    public MemberTestBuilder withOccupation(String occupation) {
        this.occupation = occupation;
        return this;
    }

    public MemberTestBuilder withMemberSince(YearMonth memberSince) {
        this.memberSince = memberSince;
        return this;
    }

    public MemberTestBuilder withEmergencyContact(String name, String number) {
        this.emergencyContactName = name;
        this.emergencyContactNumber = number;
        return this;
    }

    public MemberTestBuilder withNotes(String notes) {
        this.notes = notes;
        return this;
    }

    public MemberTestBuilder withVerified(Boolean isVerified) {
        this.isVerified = isVerified;
        return this;
    }

    public MemberTestBuilder withStatus(MemberStatus status) {
        this.status = status;
        return this;
    }

    public MemberTestBuilder withVisitorStatus() {
        this.status = MemberStatus.VISITOR;
        return this;
    }

    public MemberTestBuilder withFirstTimerStatus() {
        this.status = MemberStatus.FIRST_TIMER;
        return this;
    }

    public MemberTestBuilder withRegularStatus() {
        this.status = MemberStatus.REGULAR;
        return this;
    }

    public MemberTestBuilder withMemberStatus() {
        this.status = MemberStatus.MEMBER;
        return this;
    }

    public MemberTestBuilder withLeaderStatus() {
        this.status = MemberStatus.LEADER;
        return this;
    }

    public MemberTestBuilder withInactiveStatus() {
        this.status = MemberStatus.INACTIVE;
        return this;
    }

    /**
     * Build the Member instance.
     *
     * Note: The Church must be set manually after building using setChurch(church),
     * as Member extends TenantBaseEntity which requires a Church object.
     *
     * @return The built Member instance
     */
    public Member build() {
        Member member = new Member();

        member.setFirstName(firstName);
        member.setOtherName(otherName);
        member.setLastName(lastName);
        member.setTitle(title);
        member.setSex(sex);
        member.setDob(dob);
        member.setCountryCode(countryCode);
        member.setTimezone(timezone);
        member.setPhoneNumber(phoneNumber);
        member.setEmail(email);
        member.setWhatsappNumber(whatsappNumber);
        member.setOtherPhoneNumber(otherPhoneNumber);
        member.setProfileImageUrl(profileImageUrl);
        member.setMaritalStatus(maritalStatus);
        member.setOccupation(occupation);
        member.setMemberSince(memberSince);
        member.setEmergencyContactName(emergencyContactName);
        member.setEmergencyContactNumber(emergencyContactNumber);
        member.setNotes(notes);
        member.setIsVerified(isVerified);
        member.setStatus(status);

        // Note: churchId is handled by TenantBaseEntity
        // Caller should set it using setChurchId() after building
        // Or use withChurch() to set the Church entity

        return member;
    }

    /**
     * Build and set church in one call.
     *
     * @param church The Church object to set
     * @return The built Member instance with church set
     */
    public Member buildWithChurch(Church church) {
        Member member = build();
        member.setChurch(church);
        return member;
    }

    /**
     * Create a minimal valid member for quick testing.
     *
     * @param firstName First name
     * @param lastName Last name
     * @param phoneNumber Phone number (must be unique)
     * @param church The Church object
     * @return Minimal valid member
     */
    public static Member createMinimal(String firstName, String lastName, String phoneNumber, Church church) {
        return new MemberTestBuilder()
                .withFirstName(firstName)
                .withLastName(lastName)
                .withPhoneNumber(phoneNumber)
                .buildWithChurch(church);
    }

    /**
     * Create a fully populated member for comprehensive testing.
     *
     * @param church The Church object
     * @return Fully populated member
     */
    public static Member createComplete(Church church) {
        return new MemberTestBuilder()
                .withFirstName("Complete")
                .withOtherName("Test")
                .withLastName("Member")
                .withTitle("Mr.")
                .withMale()
                .withDob(LocalDate.of(1985, 6, 15))
                .withCountryCode("KE")
                .withTimezone("Africa/Nairobi")
                .withPhoneNumber("+254700999999")
                .withEmail("complete.member@example.com")
                .withWhatsappNumber("+254700999999")
                .withMarried()
                .withOccupation("Software Engineer")
                .withMemberSince(YearMonth.of(2020, 1))
                .withEmergencyContact("Emergency Contact", "+254700888888")
                .withNotes("Fully populated member for testing")
                .withVerified(true)
                .withMemberStatus()
                .buildWithChurch(church);
    }
}
