package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.PasswordResetToken;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.PasswordResetTokenRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository tokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailTemplateService emailTemplateService;

    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encoded-password");
        testUser.setActive(true);

        // Set configuration values using reflection
        ReflectionTestUtils.setField(forgotPasswordService, "appUrl", "http://localhost:4200");
        ReflectionTestUtils.setField(forgotPasswordService, "resetTokenExpirationMinutes", 60);
    }

    // ==================== REQUEST PASSWORD RESET TESTS ====================

    @Test
    void testRequestPasswordReset_ValidEmail_CreatesTokenAndSendsEmail() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(emailService.isEmailEnabled()).thenReturn(true);
        when(emailTemplateService.generateForgotPasswordEmail(anyString(), anyString(), anyInt()))
            .thenReturn("<html>Reset email</html>");
        when(emailTemplateService.generateForgotPasswordTextEmail(anyString(), anyString(), anyInt()))
            .thenReturn("Reset email text");

        // When
        forgotPasswordService.requestPasswordReset("test@example.com");

        // Then
        verify(userRepository).findByEmail("test@example.com");
        verify(tokenRepository).deleteByUser(testUser);

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getToken()).isNotNull();
        assertThat(savedToken.getUser()).isEqualTo(testUser);
        assertThat(savedToken.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(savedToken.isUsed()).isFalse();

        verify(emailService).sendHtmlEmail(
            eq("test@example.com"),
            eq("Reset Your PastCare Password"),
            anyString(),
            anyString()
        );
    }

    @Test
    void testRequestPasswordReset_NonExistentEmail_DoesNotCreateToken() {
        // Given
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        forgotPasswordService.requestPasswordReset("nonexistent@example.com");

        // Then
        verify(userRepository).findByEmail("nonexistent@example.com");
        verify(tokenRepository, never()).save(any());
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testRequestPasswordReset_EmailDisabled_CreatesTokenButDoesNotSendEmail() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(emailService.isEmailEnabled()).thenReturn(false);

        // When
        forgotPasswordService.requestPasswordReset("test@example.com");

        // Then
        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailService, never()).sendHtmlEmail(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testRequestPasswordReset_DeletesExistingTokens() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(emailService.isEmailEnabled()).thenReturn(false);

        // When
        forgotPasswordService.requestPasswordReset("test@example.com");

        // Then
        verify(tokenRepository).deleteByUser(testUser);
    }

    @Test
    void testRequestPasswordReset_EmailSendingFails_ThrowsException() {
        // Given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(emailService.isEmailEnabled()).thenReturn(true);
        when(emailTemplateService.generateForgotPasswordEmail(anyString(), anyString(), anyInt()))
            .thenReturn("<html>Reset email</html>");
        when(emailTemplateService.generateForgotPasswordTextEmail(anyString(), anyString(), anyInt()))
            .thenReturn("Reset email text");
        doThrow(new RuntimeException("Email sending failed"))
            .when(emailService).sendHtmlEmail(anyString(), anyString(), anyString(), anyString());

        // When/Then
        assertThatThrownBy(() -> forgotPasswordService.requestPasswordReset("test@example.com"))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Failed to send password reset email");
    }

    // ==================== RESET PASSWORD TESTS ====================

    @Test
    void testResetPassword_ValidToken_ResetsPassword() {
        // Given
        String token = "valid-token";
        String newPassword = "NewPassword123!";
        String encodedPassword = "encoded-new-password";

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedPassword);

        // When
        forgotPasswordService.resetPassword(token, newPassword);

        // Then
        verify(passwordEncoder).encode(newPassword);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(savedUser.isMustChangePassword()).isFalse();

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(tokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.isUsed()).isTrue();
        assertThat(savedToken.getUsedAt()).isNotNull();
    }

    @Test
    void testResetPassword_InvalidToken_ThrowsException() {
        // Given
        String token = "invalid-token";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> forgotPasswordService.resetPassword(token, "NewPassword123!"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid or expired reset token");

        verify(userRepository, never()).save(any());
    }

    @Test
    void testResetPassword_ExpiredToken_ThrowsException() {
        // Given
        String token = "expired-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        resetToken.setUsed(false);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // When/Then
        assertThatThrownBy(() -> forgotPasswordService.resetPassword(token, "NewPassword123!"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("This reset link has expired");

        verify(userRepository, never()).save(any());
    }

    @Test
    void testResetPassword_UsedToken_ThrowsException() {
        // Given
        String token = "used-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now().minusMinutes(10));

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // When/Then
        assertThatThrownBy(() -> forgotPasswordService.resetPassword(token, "NewPassword123!"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("This reset link has already been used");

        verify(userRepository, never()).save(any());
    }

    // ==================== TOKEN VALIDATION TESTS ====================

    @Test
    void testIsTokenValid_ValidToken_ReturnsTrue() {
        // Given
        String token = "valid-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(false);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // When
        boolean result = forgotPasswordService.isTokenValid(token);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void testIsTokenValid_InvalidToken_ReturnsFalse() {
        // Given
        String token = "invalid-token";
        when(tokenRepository.findByToken(token)).thenReturn(Optional.empty());

        // When
        boolean result = forgotPasswordService.isTokenValid(token);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testIsTokenValid_ExpiredToken_ReturnsFalse() {
        // Given
        String token = "expired-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiresAt(LocalDateTime.now().minusHours(1)); // Expired
        resetToken.setUsed(false);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // When
        boolean result = forgotPasswordService.isTokenValid(token);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void testIsTokenValid_UsedToken_ReturnsFalse() {
        // Given
        String token = "used-token";
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(testUser);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        resetToken.setUsed(true);

        when(tokenRepository.findByToken(token)).thenReturn(Optional.of(resetToken));

        // When
        boolean result = forgotPasswordService.isTokenValid(token);

        // Then
        assertThat(result).isFalse();
    }

    // ==================== CLEANUP TESTS ====================

    @Test
    void testCleanupExpiredTokens_DeletesExpiredTokens() {
        // When
        forgotPasswordService.cleanupExpiredTokens();

        // Then
        ArgumentCaptor<LocalDateTime> dateCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(tokenRepository).deleteByExpiresAtBefore(dateCaptor.capture());

        LocalDateTime capturedDate = dateCaptor.getValue();
        assertThat(capturedDate).isBefore(LocalDateTime.now().plusSeconds(1));
    }
}
