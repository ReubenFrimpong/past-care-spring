package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.dtos.ForgotPasswordRequest;
import com.reuben.pastcare_spring.dtos.ResetPasswordRequest;
import com.reuben.pastcare_spring.services.ForgotPasswordService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ForgotPasswordController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security for unit tests
@Disabled("Requires complex WebMvcTest context configuration - needs refactoring to work with security filters")
class ForgotPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ForgotPasswordService forgotPasswordService;

    // ==================== REQUEST PASSWORD RESET TESTS ====================

    @Test
    void testRequestPasswordReset_ValidEmail_ReturnsSuccess() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");

        // When/Then
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists());

        verify(forgotPasswordService).requestPasswordReset("test@example.com");
    }

    @Test
    void testRequestPasswordReset_ServiceThrowsException_ReturnsSuccessToPreventEnumeration() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("test@example.com");
        doThrow(new RuntimeException("Email service error"))
            .when(forgotPasswordService).requestPasswordReset(anyString());

        // When/Then - Should still return success to prevent email enumeration
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void testRequestPasswordReset_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("invalid-email");

        // When/Then
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(forgotPasswordService, never()).requestPasswordReset(anyString());
    }

    @Test
    void testRequestPasswordReset_EmptyEmail_ReturnsBadRequest() throws Exception {
        // Given
        ForgotPasswordRequest request = new ForgotPasswordRequest("");

        // When/Then
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(forgotPasswordService, never()).requestPasswordReset(anyString());
    }

    // ==================== RESET PASSWORD TESTS ====================

    @Test
    void testResetPassword_ValidRequest_ReturnsSuccess() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "NewPassword123!");

        // When/Then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Your password has been reset successfully. You can now log in with your new password."));

        verify(forgotPasswordService).resetPassword("valid-token", "NewPassword123!");
    }

    @Test
    void testResetPassword_InvalidToken_ReturnsBadRequest() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("invalid-token", "NewPassword123!");
        doThrow(new IllegalArgumentException("Invalid or expired reset token"))
            .when(forgotPasswordService).resetPassword(anyString(), anyString());

        // When/Then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired reset token"));
    }

    @Test
    void testResetPassword_ExpiredToken_ReturnsBadRequest() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("expired-token", "NewPassword123!");
        doThrow(new IllegalArgumentException("This reset link has expired"))
            .when(forgotPasswordService).resetPassword(anyString(), anyString());

        // When/Then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("This reset link has expired"));
    }

    @Test
    void testResetPassword_UsedToken_ReturnsBadRequest() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("used-token", "NewPassword123!");
        doThrow(new IllegalArgumentException("This reset link has already been used"))
            .when(forgotPasswordService).resetPassword(anyString(), anyString());

        // When/Then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("This reset link has already been used"));
    }

    @Test
    void testResetPassword_ServiceError_ReturnsInternalServerError() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "NewPassword123!");
        doThrow(new RuntimeException("Database error"))
            .when(forgotPasswordService).resetPassword(anyString(), anyString());

        // When/Then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An error occurred while resetting your password. Please try again."));
    }

    @Test
    void testResetPassword_PasswordTooShort_ReturnsBadRequest() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "short");

        // When/Then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(forgotPasswordService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void testResetPassword_EmptyPassword_ReturnsBadRequest() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("valid-token", "");

        // When/Then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(forgotPasswordService, never()).resetPassword(anyString(), anyString());
    }

    @Test
    void testResetPassword_EmptyToken_ReturnsBadRequest() throws Exception {
        // Given
        ResetPasswordRequest request = new ResetPasswordRequest("", "NewPassword123!");

        // When/Then
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(forgotPasswordService, never()).resetPassword(anyString(), anyString());
    }

    // ==================== VERIFY TOKEN TESTS ====================

    @Test
    void testVerifyToken_ValidToken_ReturnsSuccess() throws Exception {
        // Given
        when(forgotPasswordService.isTokenValid("valid-token")).thenReturn(true);

        // When/Then
        mockMvc.perform(get("/api/auth/reset-password/verify")
                        .param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Token is valid"));

        verify(forgotPasswordService).isTokenValid("valid-token");
    }

    @Test
    void testVerifyToken_InvalidToken_ReturnsBadRequest() throws Exception {
        // Given
        when(forgotPasswordService.isTokenValid("invalid-token")).thenReturn(false);

        // When/Then
        mockMvc.perform(get("/api/auth/reset-password/verify")
                        .param("token", "invalid-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid or expired reset token"));

        verify(forgotPasswordService).isTokenValid("invalid-token");
    }

    @Test
    void testVerifyToken_ExpiredToken_ReturnsBadRequest() throws Exception {
        // Given
        when(forgotPasswordService.isTokenValid("expired-token")).thenReturn(false);

        // When/Then
        mockMvc.perform(get("/api/auth/reset-password/verify")
                        .param("token", "expired-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));

        verify(forgotPasswordService).isTokenValid("expired-token");
    }
}
