package com.reuben.pastcare_spring.integration;

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

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for portal user profile picture upload.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PortalProfilePictureUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChurchRepository churchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PortalUserRepository portalUserRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Church testChurch;
    private Member testMember;
    private PortalUser testPortalUser;

    @BeforeEach
    public void setUp() {
        // Clean up
        portalUserRepository.deleteAll();
        memberRepository.deleteAll();
        userRepository.deleteAll();
        churchRepository.deleteAll();

        // Create test church
        testChurch = new Church();
        testChurch.setName("Test Church");
        testChurch.setEmail("testchurch@example.com");
        testChurch.setPhoneNumber("+1234567890");
        testChurch.setActive(true);
        testChurch = churchRepository.save(testChurch);

        // Create test member
        testMember = new Member();
        testMember.setFirstName("John");
        testMember.setLastName("Doe");
        testMember.setPhoneNumber("+1234567890");
        testMember.setSex("Male");
        testMember.setChurch(testChurch);
        testMember.setIsVerified(true);
        testMember.setStatus(MemberStatus.MEMBER);
        testMember = memberRepository.save(testMember);

        // Create test portal user
        testPortalUser = new PortalUser();
        testPortalUser.setEmail("john.doe@example.com");
        testPortalUser.setPasswordHash("hashedPassword");
        testPortalUser.setMember(testMember);
        testPortalUser.setChurch(testChurch);
        testPortalUser.setStatus(PortalUserStatus.APPROVED);
        testPortalUser.setIsActive(true);
        testPortalUser = portalUserRepository.save(testPortalUser);
    }

    @Test
    public void testUploadProfilePicture_ShouldSucceed() throws Exception {
        // Create a mock image file
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "profile.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portal/profile/picture")
                .file(mockFile)
                .param("email", testPortalUser.getEmail())
                .param("churchId", testChurch.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Profile picture uploaded successfully"))
            .andExpect(jsonPath("$.imageUrl").exists());

        // Verify member profile image URL was updated
        Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertNotNull(updatedMember.getProfileImageUrl());
        assertTrue(updatedMember.getProfileImageUrl().contains("uploads/profile-images"));
    }

    @Test
    public void testUploadProfilePicture_WithInvalidEmail_ShouldFail() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "profile.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portal/profile/picture")
                .file(mockFile)
                .param("email", "nonexistent@example.com")
                .param("churchId", testChurch.getId().toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Portal user not found"));
    }

    @Test
    public void testUploadProfilePicture_WithInvalidChurchId_ShouldFail() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "profile.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portal/profile/picture")
                .file(mockFile)
                .param("email", testPortalUser.getEmail())
                .param("churchId", "999999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Portal user not found"));
    }

    @Test
    public void testUploadProfilePicture_ReplaceExistingImage_ShouldSucceed() throws Exception {
        // Set existing profile image
        testMember.setProfileImageUrl("uploads/profile-images/old-image.jpg");
        memberRepository.save(testMember);

        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "new-profile.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "new fake image content".getBytes()
        );

        mockMvc.perform(multipart("/api/portal/profile/picture")
                .file(mockFile)
                .param("email", testPortalUser.getEmail())
                .param("churchId", testChurch.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Profile picture uploaded successfully"))
            .andExpect(jsonPath("$.imageUrl").exists());

        // Verify member profile image URL was updated
        Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertNotNull(updatedMember.getProfileImageUrl());
        assertNotEquals("uploads/profile-images/old-image.jpg", updatedMember.getProfileImageUrl());
    }

    @Test
    public void testGetPortalUserProfile_ShouldIncludeProfileImageUrl() throws Exception {
        // Set profile image
        testMember.setProfileImageUrl("uploads/profile-images/profile.jpg");
        memberRepository.save(testMember);

        mockMvc.perform(get("/api/portal/profile")
                .param("email", testPortalUser.getEmail())
                .param("churchId", testChurch.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(testPortalUser.getEmail()))
            .andExpect(jsonPath("$.memberFirstName").value("John"))
            .andExpect(jsonPath("$.memberLastName").value("Doe"))
            .andExpect(jsonPath("$.profileImageUrl").value("uploads/profile-images/profile.jpg"));
    }

    @Test
    public void testGetPortalUserProfile_WithoutProfileImage_ShouldReturnNull() throws Exception {
        mockMvc.perform(get("/api/portal/profile")
                .param("email", testPortalUser.getEmail())
                .param("churchId", testChurch.getId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value(testPortalUser.getEmail()))
            .andExpect(jsonPath("$.profileImageUrl").doesNotExist());
    }
}
