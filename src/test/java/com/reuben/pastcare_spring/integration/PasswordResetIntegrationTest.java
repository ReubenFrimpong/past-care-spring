package com.reuben.pastcare_spring.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.dtos.ForgotPasswordRequest;
import com.reuben.pastcare_spring.dtos.ResetPasswordRequest;
import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.PasswordResetToken;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.PasswordResetTokenRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.testutil.TestJwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PasswordResetIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChurchRepository churchRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Church testChurch;
    private User superAdmin;
    private User regularUser;

    @BeforeEach
    void setUp() {
        // Create test church
        testChurch = new Church();
        testChurch.setName("Test Church for Password Reset");
        testChurch.setEmail("test@church.com");
        testChurch = churchRepository.save(testChurch);

        // Create superadmin
        superAdmin = new User();
        superAdmin.setName("Super Admin");
        superAdmin.setEmail("superadmin@test.com");
        superAdmin.setPassword(passwordEncoder.encode("password123"));
        superAdmin.setRole(Role.SUPERADMIN);
        superAdmin.setActive(true);
        superAdmin = userRepository.save(superAdmin);

        // Create regular user
        regularUser = new User();
        regularUser.setName("Regular User");
        regularUser.setEmail("user@test.com");
        regularUser.setPassword(passwordEncoder.encode("password123"));
        regularUser.setRole(Role.MEMBER);
        regularUser.setChurch(testChurch);
        regularUser.setActive(true);
        regularUser = userRepository.save(regularUser);
    }

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();
        churchRepository.deleteAll();
    }

    // ==================== SUPERADMIN FORCE PASSWORD RESET TESTS ====================

    @Test
    void testSuperAdminCanForceResetUserPassword() throws Exception {
        String token = TestJwtUtil.generateSuperadminToken(superAdmin.getId(), superAdmin.getEmail());

        mockMvc.perform(post("/api/users/{id}/reset-password", regularUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.temporaryPassword").exists());

        // Verify user must change password
        User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();
        assertThat(updatedUser.isMustChangePassword()).isTrue();
    }

    @Test
    void testNonSuperAdminCannotForceResetPassword() throws Exception {
        String token = TestJwtUtil.generateMemberToken(regularUser.getId(), regularUser.getEmail(), testChurch.getId());

        mockMvc.perform(post("/api/users/{id}/reset-password", regularUser.getId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void testForceResetPasswordForNonExistentUser() throws Exception {
        String token = TestJwtUtil.generateSuperadminToken(superAdmin.getId(), superAdmin.getEmail());

        mockMvc.perform(post("/api/users/{id}/reset-password", 99999L)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    // ==================== FORGOT PASSWORD REQUEST TESTS ====================

    @Test
    void testRequestPasswordResetWithValidEmail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest(regularUser.getEmail());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        // Verify token was created
        List<PasswordResetToken> tokens = tokenRepository.findByUser(regularUser);
        assertThat(tokens).hasSize(1);
        assertThat(tokens.get(0).isValid()).isTrue();
    }

    @Test
    void testRequestPasswordResetWithNonExistentEmail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("nonexistent@test.com");

        // Should still return success to prevent email enumeration
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Verify no token was created
        List<PasswordResetToken> tokens = tokenRepository.findAll();
        assertThat(tokens).isEmpty();
    }

    @Test
    void testRequestPasswordResetWithInvalidEmail() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("invalid-email");

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== PASSWORD RESET TESTS ====================

    @Test
    void testResetPasswordWithValidToken() throws Exception {
        // Create a valid reset token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("test-token-123");
        resetToken.setUser(regularUser);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        String newPassword = "NewPassword123!";
        ResetPasswordRequest request = new ResetPasswordRequest("test-token-123", newPassword);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        // Verify password was changed
        User updatedUser = userRepository.findById(regularUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches(newPassword, updatedUser.getPassword())).isTrue();
        assertThat(updatedUser.isMustChangePassword()).isFalse();

        // Verify token was marked as used
        PasswordResetToken usedToken = tokenRepository.findByToken("test-token-123").orElseThrow();
        assertThat(usedToken.isUsed()).isTrue();
        assertThat(usedToken.getUsedAt()).isNotNull();
    }

    @Test
    void testResetPasswordWithExpiredToken() throws Exception {
        // Create an expired reset token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("expired-token");
        resetToken.setUser(regularUser);
        resetToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        ResetPasswordRequest request = new ResetPasswordRequest("expired-token", "NewPassword123!");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testResetPasswordWithUsedToken() throws Exception {
        // Create a used reset token
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("used-token");
        resetToken.setUser(regularUser);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        ResetPasswordRequest request = new ResetPasswordRequest("used-token", "NewPassword123!");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("This reset link has already been used"));
    }

    @Test
    void testResetPasswordWithInvalidToken() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "NewPassword123!");

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testResetPasswordWithWeakPassword() throws Exception {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("test-token");
        resetToken.setUser(regularUser);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        ResetPasswordRequest request = new ResetPasswordRequest("test-token", "123"); // Too short

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ==================== TOKEN VERIFICATION TESTS ====================

    @Test
    void testVerifyValidToken() throws Exception {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken("valid-token");
        resetToken.setUser(regularUser);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);
        tokenRepository.save(resetToken);

        mockMvc.perform(get("/api/auth/reset-password/verify")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testVerifyInvalidToken() throws Exception {
        mockMvc.perform(get("/api/auth/reset-password/verify")
                        .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
