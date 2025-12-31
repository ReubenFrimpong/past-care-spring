package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.PasswordResetToken;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.PasswordResetTokenRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class ForgotPasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @Value("${app.url:http://localhost:4200}")
    private String appUrl;

    @Value("${app.password-reset.expiration-minutes:60}")
    private int resetTokenExpirationMinutes;

    /**
     * Request a password reset for a user by email
     * Generates a reset token and sends email with reset link
     *
     * @param email The user's email address
     */
    @Transactional
    public void requestPasswordReset(String email) {
        // Find user by email
        User user = userRepository.findByEmail(email)
            .orElse(null);

        // For security, don't reveal if email exists or not
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", email);
            // Still return success to prevent email enumeration
            return;
        }

        // Invalidate any existing tokens for this user
        tokenRepository.deleteByUser(user);

        // Generate secure random token
        String token = UUID.randomUUID().toString();

        // Create reset token entity
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes));
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        // Send password reset email
        if (emailService.isEmailEnabled()) {
            try {
                sendPasswordResetEmail(user, token);
                log.info("✅ Password reset email sent to: {}", email);
            } catch (Exception e) {
                log.error("❌ Failed to send password reset email to: {}", email, e);
                throw new RuntimeException("Failed to send password reset email", e);
            }
        } else {
            log.warn("📧 Email disabled - password reset token generated but not sent: {}", token);
        }
    }

    /**
     * Reset user's password using a valid reset token
     *
     * @param token The reset token
     * @param newPassword The new password
     */
    @Transactional
    public void resetPassword(String token, String newPassword) {
        // Find token
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));

        // Validate token
        if (!resetToken.isValid()) {
            if (resetToken.isUsed()) {
                throw new IllegalArgumentException("This reset link has already been used");
            } else {
                throw new IllegalArgumentException("This reset link has expired");
            }
        }

        // Get user and update password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false); // User just set their own password
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        tokenRepository.save(resetToken);

        log.info("✅ Password successfully reset for user: {}", user.getEmail());
    }

    /**
     * Verify if a reset token is valid
     *
     * @param token The reset token
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        return tokenRepository.findByToken(token)
            .map(PasswordResetToken::isValid)
            .orElse(false);
    }

    /**
     * Send password reset email with link
     */
    private void sendPasswordResetEmail(User user, String token) {
        String resetLink = appUrl + "/reset-password?token=" + token;

        // Generate HTML email
        String htmlBody = emailTemplateService.generateForgotPasswordEmail(
            user.getName(),
            resetLink,
            resetTokenExpirationMinutes
        );

        // Generate plain text fallback
        String textBody = emailTemplateService.generateForgotPasswordTextEmail(
            user.getName(),
            resetLink,
            resetTokenExpirationMinutes
        );

        // Send email
        emailService.sendHtmlEmail(
            user.getEmail(),
            "Reset Your PastCare Password",
            htmlBody,
            textBody
        );
    }

    /**
     * Clean up expired tokens (should be called periodically)
     */
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        log.info("✅ Cleaned up expired password reset tokens");
    }
}
