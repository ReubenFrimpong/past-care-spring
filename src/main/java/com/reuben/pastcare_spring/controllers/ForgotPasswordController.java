package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.ForgotPasswordRequest;
import com.reuben.pastcare_spring.dtos.PasswordResetResponse;
import com.reuben.pastcare_spring.dtos.ResetPasswordRequest;
import com.reuben.pastcare_spring.services.ForgotPasswordService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    /**
     * Request a password reset link
     * POST /api/auth/forgot-password
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<PasswordResetResponse> requestPasswordReset(
        @Valid @RequestBody ForgotPasswordRequest request
    ) {
        try {
            forgotPasswordService.requestPasswordReset(request.email());
            // Always return success to prevent email enumeration
            return ResponseEntity.ok(new PasswordResetResponse(
                "If an account exists with that email, you will receive a password reset link shortly.",
                true
            ));
        } catch (Exception e) {
            log.error("Error processing password reset request", e);
            return ResponseEntity.ok(new PasswordResetResponse(
                "If an account exists with that email, you will receive a password reset link shortly.",
                true
            ));
        }
    }

    /**
     * Reset password using token
     * POST /api/auth/reset-password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<PasswordResetResponse> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        try {
            forgotPasswordService.resetPassword(request.token(), request.newPassword());
            return ResponseEntity.ok(new PasswordResetResponse(
                "Your password has been reset successfully. You can now log in with your new password.",
                true
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new PasswordResetResponse(
                e.getMessage(),
                false
            ));
        } catch (Exception e) {
            log.error("Error resetting password", e);
            return ResponseEntity.internalServerError().body(new PasswordResetResponse(
                "An error occurred while resetting your password. Please try again.",
                false
            ));
        }
    }

    /**
     * Verify if a reset token is valid
     * GET /api/auth/reset-password/verify?token=xxx
     */
    @GetMapping("/reset-password/verify")
    public ResponseEntity<PasswordResetResponse> verifyToken(@RequestParam String token) {
        boolean isValid = forgotPasswordService.isTokenValid(token);
        if (isValid) {
            return ResponseEntity.ok(new PasswordResetResponse(
                "Token is valid",
                true
            ));
        } else {
            return ResponseEntity.badRequest().body(new PasswordResetResponse(
                "Invalid or expired reset token",
                false
            ));
        }
    }
}
