package com.reuben.pastcare_spring.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.dtos.PortalRegistrationRequest;
import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for portal registration with invitation codes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PortalRegistrationWithInvitationCodeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChurchRepository churchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationCodeRepository invitationCodeRepository;

    @Autowired
    private PortalUserRepository portalUserRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Church testChurch;
    private User adminUser;
    private InvitationCode validInvitationCode;
    private InvitationCode expiredInvitationCode;
    private InvitationCode maxUsesReachedCode;

    @BeforeEach
    public void setUp() {
        // Clean up
        portalUserRepository.deleteAll();
        memberRepository.deleteAll();
        invitationCodeRepository.deleteAll();
        userRepository.deleteAll();
        churchRepository.deleteAll();

        // Create test church
        testChurch = new Church();
        testChurch.setName("Test Church");
        testChurch.setEmail("testchurch@example.com");
        testChurch.setPhoneNumber("+1234567890");
        testChurch.setActive(true);
        testChurch = churchRepository.save(testChurch);

        // Create admin user
        adminUser = new User();
        adminUser.setName("Admin User");
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("hashedPassword");
        adminUser.setRole(Role.ADMIN);
        adminUser.setChurch(testChurch);
        adminUser.setAccountLocked(false);
        adminUser.setFailedLoginAttempts(0);
        adminUser = userRepository.save(adminUser);

        // Create valid invitation code
        validInvitationCode = new InvitationCode();
        validInvitationCode.setChurch(testChurch);
        validInvitationCode.setCreatedBy(adminUser);
        validInvitationCode.setCode("VALID123");
        validInvitationCode.setDescription("Valid test code");
        validInvitationCode.setMaxUses(10);
        validInvitationCode.setUsedCount(0);
        validInvitationCode.setIsActive(true);
        validInvitationCode.setDefaultRole(Role.MEMBER);
        validInvitationCode = invitationCodeRepository.save(validInvitationCode);

        // Create expired invitation code
        expiredInvitationCode = new InvitationCode();
        expiredInvitationCode.setChurch(testChurch);
        expiredInvitationCode.setCreatedBy(adminUser);
        expiredInvitationCode.setCode("EXPIRED1");
        expiredInvitationCode.setDescription("Expired test code");
        expiredInvitationCode.setMaxUses(10);
        expiredInvitationCode.setUsedCount(0);
        expiredInvitationCode.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired yesterday
        expiredInvitationCode.setIsActive(true);
        expiredInvitationCode.setDefaultRole(Role.MEMBER);
        expiredInvitationCode = invitationCodeRepository.save(expiredInvitationCode);

        // Create max uses reached code
        maxUsesReachedCode = new InvitationCode();
        maxUsesReachedCode.setChurch(testChurch);
        maxUsesReachedCode.setCreatedBy(adminUser);
        maxUsesReachedCode.setCode("MAXUSED1");
        maxUsesReachedCode.setDescription("Max uses reached code");
        maxUsesReachedCode.setMaxUses(5);
        maxUsesReachedCode.setUsedCount(5); // Already at max
        maxUsesReachedCode.setIsActive(true);
        maxUsesReachedCode.setDefaultRole(Role.MEMBER);
        maxUsesReachedCode = invitationCodeRepository.save(maxUsesReachedCode);
    }

    @Test
    public void testPortalRegistrationWithoutInvitationCode_ShouldFail() throws Exception {
        // Create registration request WITHOUT invitation code
        PortalRegistrationRequest request = new PortalRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("Test@123456");
        request.setPhoneNumber("+1234567890");
        // No invitation code set

        mockMvc.perform(post("/api/portal/register")
                .param("churchId", testChurch.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.invitationCode").exists());
    }

    @Test
    public void testPortalRegistrationWithValidInvitationCode_ShouldSucceed() throws Exception {
        // Create registration request with valid invitation code
        PortalRegistrationRequest request = new PortalRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("Test@123456");
        request.setPhoneNumber("+1234567890");
        request.setInvitationCode("VALID123");

        mockMvc.perform(post("/api/portal/register")
                .param("churchId", testChurch.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.memberFirstName").value("John"))
            .andExpect(jsonPath("$.memberLastName").value("Doe"))
            .andExpect(jsonPath("$.status").exists());

        // Verify invitation code usage was incremented
        InvitationCode updatedCode = invitationCodeRepository.findByCode("VALID123").orElseThrow();
        assertEquals(1, updatedCode.getUsedCount());
        assertNotNull(updatedCode.getLastUsedAt());

        // Verify member was created
        assertEquals(1, memberRepository.count());
        Member member = memberRepository.findAll().get(0);
        assertEquals("John", member.getFirstName());
        assertEquals("Doe", member.getLastName());

        // Verify portal user was created
        assertEquals(1, portalUserRepository.count());
        PortalUser portalUser = portalUserRepository.findAll().get(0);
        assertEquals("john.doe@example.com", portalUser.getEmail());
        assertEquals(member.getId(), portalUser.getMember().getId());
    }

    @Test
    public void testPortalRegistrationWithInvalidInvitationCode_ShouldFail() throws Exception {
        PortalRegistrationRequest request = new PortalRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("Test@123456");
        request.setPhoneNumber("+1234567890");
        request.setInvitationCode("INVALID999");

        mockMvc.perform(post("/api/portal/register")
                .param("churchId", testChurch.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid or expired invitation code")));

        // Verify no member or portal user was created
        assertEquals(0, memberRepository.count());
        assertEquals(0, portalUserRepository.count());
    }

    @Test
    public void testPortalRegistrationWithExpiredInvitationCode_ShouldFail() throws Exception {
        PortalRegistrationRequest request = new PortalRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("Test@123456");
        request.setPhoneNumber("+1234567890");
        request.setInvitationCode("EXPIRED1");

        mockMvc.perform(post("/api/portal/register")
                .param("churchId", testChurch.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid or expired invitation code")));

        // Verify no member or portal user was created
        assertEquals(0, memberRepository.count());
        assertEquals(0, portalUserRepository.count());

        // Verify invitation code usage was NOT incremented
        InvitationCode code = invitationCodeRepository.findByCode("EXPIRED1").orElseThrow();
        assertEquals(0, code.getUsedCount());
    }

    @Test
    public void testPortalRegistrationWithMaxUsesReachedCode_ShouldFail() throws Exception {
        PortalRegistrationRequest request = new PortalRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("Test@123456");
        request.setPhoneNumber("+1234567890");
        request.setInvitationCode("MAXUSED1");

        mockMvc.perform(post("/api/portal/register")
                .param("churchId", testChurch.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid or expired invitation code")));

        // Verify no member or portal user was created
        assertEquals(0, memberRepository.count());
        assertEquals(0, portalUserRepository.count());

        // Verify invitation code usage was NOT incremented
        InvitationCode code = invitationCodeRepository.findByCode("MAXUSED1").orElseThrow();
        assertEquals(5, code.getUsedCount()); // Still at max
    }

    @Test
    public void testPortalRegistrationWithCodeFromDifferentChurch_ShouldFail() throws Exception {
        // Create another church
        Church otherChurch = new Church();
        otherChurch.setName("Other Church");
        otherChurch.setEmail("otherchurch@example.com");
        otherChurch.setPhoneNumber("+0987654321");
        otherChurch.setActive(true);
        otherChurch = churchRepository.save(otherChurch);

        PortalRegistrationRequest request = new PortalRegistrationRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("Test@123456");
        request.setPhoneNumber("+1234567890");
        request.setInvitationCode("VALID123"); // Code belongs to testChurch

        mockMvc.perform(post("/api/portal/register")
                .param("churchId", otherChurch.getId().toString()) // Different church
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invitation code does not belong to this church")));

        // Verify no member or portal user was created
        assertEquals(0, memberRepository.count());
        assertEquals(0, portalUserRepository.count());
    }

    @Test
    public void testMultipleRegistrationsWithSameCode_ShouldIncrementUsageCount() throws Exception {
        // First registration
        PortalRegistrationRequest request1 = new PortalRegistrationRequest();
        request1.setFirstName("John");
        request1.setLastName("Doe");
        request1.setEmail("john.doe@example.com");
        request1.setPassword("Test@123456");
        request1.setPhoneNumber("+1234567890");
        request1.setInvitationCode("VALID123");

        mockMvc.perform(post("/api/portal/register")
                .param("churchId", testChurch.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1)))
            .andExpect(status().isCreated());

        // Verify usage count is 1
        InvitationCode code = invitationCodeRepository.findByCode("VALID123").orElseThrow();
        assertEquals(1, code.getUsedCount());

        // Second registration with same code but different email
        PortalRegistrationRequest request2 = new PortalRegistrationRequest();
        request2.setFirstName("Jane");
        request2.setLastName("Smith");
        request2.setEmail("jane.smith@example.com");
        request2.setPassword("Test@123456");
        request2.setPhoneNumber("+0987654321");
        request2.setInvitationCode("VALID123");

        mockMvc.perform(post("/api/portal/register")
                .param("churchId", testChurch.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2)))
            .andExpect(status().isCreated());

        // Verify usage count is now 2
        code = invitationCodeRepository.findByCode("VALID123").orElseThrow();
        assertEquals(2, code.getUsedCount());

        // Verify both members and portal users were created
        assertEquals(2, memberRepository.count());
        assertEquals(2, portalUserRepository.count());
    }

    @Test
    public void testPortalRegistrationWithPhoto_ShouldSucceed() throws Exception {
        // Create a mock image file
        MockMultipartFile photoFile = new MockMultipartFile(
            "photo",
            "profile.jpg",
            "image/jpeg",
            "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/portal/register-with-photo")
                .file(photoFile)
                .param("churchId", testChurch.getId().toString())
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("password", "Test@123456")
                .param("phoneNumber", "+1234567890")
                .param("invitationCode", "VALID123"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.memberFirstName").value("John"))
            .andExpect(jsonPath("$.memberLastName").value("Doe"))
            .andExpect(jsonPath("$.profileImageUrl").exists());

        // Verify member was created with profile image
        assertEquals(1, memberRepository.count());
        Member member = memberRepository.findAll().get(0);
        assertNotNull(member.getProfileImageUrl());

        // Verify invitation code usage was incremented
        InvitationCode updatedCode = invitationCodeRepository.findByCode("VALID123").orElseThrow();
        assertEquals(1, updatedCode.getUsedCount());
    }

    @Test
    public void testPortalRegistrationWithInvalidPhotoType_ShouldFail() throws Exception {
        // Create a mock non-image file
        MockMultipartFile pdfFile = new MockMultipartFile(
            "photo",
            "document.pdf",
            "application/pdf",
            "fake-pdf-content".getBytes()
        );

        mockMvc.perform(multipart("/api/portal/register-with-photo")
                .file(pdfFile)
                .param("churchId", testChurch.getId().toString())
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("password", "Test@123456")
                .param("phoneNumber", "+1234567890")
                .param("invitationCode", "VALID123"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("image")));

        // Verify no member or portal user was created
        assertEquals(0, memberRepository.count());
        assertEquals(0, portalUserRepository.count());

        // Verify invitation code usage was NOT incremented
        InvitationCode code = invitationCodeRepository.findByCode("VALID123").orElseThrow();
        assertEquals(0, code.getUsedCount());
    }

    @Test
    public void testPortalRegistrationWithOversizedPhoto_ShouldFail() throws Exception {
        // Create a mock large file (>10MB)
        byte[] largeContent = new byte[11 * 1024 * 1024]; // 11MB
        MockMultipartFile largeFile = new MockMultipartFile(
            "photo",
            "large.jpg",
            "image/jpeg",
            largeContent
        );

        mockMvc.perform(multipart("/api/portal/register-with-photo")
                .file(largeFile)
                .param("churchId", testChurch.getId().toString())
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("password", "Test@123456")
                .param("phoneNumber", "+1234567890")
                .param("invitationCode", "VALID123"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("10MB")));

        // Verify no member or portal user was created
        assertEquals(0, memberRepository.count());
        assertEquals(0, portalUserRepository.count());
    }

    @Test
    public void testPortalRegistrationWithPhotoButNoInvitationCode_ShouldFail() throws Exception {
        MockMultipartFile photoFile = new MockMultipartFile(
            "photo",
            "profile.jpg",
            "image/jpeg",
            "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/portal/register-with-photo")
                .file(photoFile)
                .param("churchId", testChurch.getId().toString())
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("password", "Test@123456")
                .param("phoneNumber", "+1234567890")
                .param("invitationCode", "")) // Empty invitation code
            .andExpect(status().isBadRequest());

        // Verify no member or portal user was created
        assertEquals(0, memberRepository.count());
        assertEquals(0, portalUserRepository.count());
    }

    @Test
    public void testPortalRegistrationWithPhotoAndInvalidCode_ShouldFail() throws Exception {
        MockMultipartFile photoFile = new MockMultipartFile(
            "photo",
            "profile.jpg",
            "image/jpeg",
            "fake-image-content".getBytes()
        );

        mockMvc.perform(multipart("/api/portal/register-with-photo")
                .file(photoFile)
                .param("churchId", testChurch.getId().toString())
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("password", "Test@123456")
                .param("phoneNumber", "+1234567890")
                .param("invitationCode", "INVALID999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message", containsString("Invalid or expired invitation code")));

        // Verify no member or portal user was created
        assertEquals(0, memberRepository.count());
        assertEquals(0, portalUserRepository.count());
    }

    @Test
    public void testPortalRegistrationWithoutPhoto_ShouldStillSucceed() throws Exception {
        // Test that photo is optional - registration without photo should work
        mockMvc.perform(multipart("/api/portal/register-with-photo")
                .param("churchId", testChurch.getId().toString())
                .param("firstName", "John")
                .param("lastName", "Doe")
                .param("email", "john.doe@example.com")
                .param("password", "Test@123456")
                .param("phoneNumber", "+1234567890")
                .param("invitationCode", "VALID123"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("john.doe@example.com"));

        // Verify member was created without profile image
        assertEquals(1, memberRepository.count());
        Member member = memberRepository.findAll().get(0);
        // Profile image should be null if no photo uploaded
        // (or could be a default image depending on implementation)
    }
}
