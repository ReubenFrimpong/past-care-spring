package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.dtos.PortalLoginRequest;
import com.reuben.pastcare_spring.dtos.PortalRegistrationRequest;
import com.reuben.pastcare_spring.dtos.PortalUserResponse;
import com.reuben.pastcare_spring.models.PortalUserStatus;
import com.reuben.pastcare_spring.services.PortalUserService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalUserController {

    private final PortalUserService portalUserService;
    private final RequestContextUtil requestContextUtil;

    /**
     * Register new portal user (public endpoint - requires invitation code)
     * Supports both JSON and multipart/form-data for photo upload
     */
    @PostMapping("/register")
    public ResponseEntity<PortalUserResponse> register(
            @Valid @RequestBody PortalRegistrationRequest request,
            @RequestParam Long churchId) {
        PortalUserResponse response = portalUserService.registerPortalUser(churchId, request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Register new portal user with photo upload (public endpoint - requires invitation code)
     * Accepts multipart/form-data with photo file
     */
    @PostMapping(value = "/register-with-photo", consumes = "multipart/form-data")
    public ResponseEntity<PortalUserResponse> registerWithPhoto(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String phoneNumber,
            @RequestParam String invitationCode,
            @RequestParam(required = false) String dateOfBirth,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String additionalInfo,
            @RequestParam(required = false) MultipartFile photo,
            @RequestParam Long churchId) {

        // Build registration request
        PortalRegistrationRequest request = new PortalRegistrationRequest();
        request.setFirstName(firstName);
        request.setLastName(lastName);
        request.setEmail(email);
        request.setPassword(password);
        request.setPhoneNumber(phoneNumber);
        request.setInvitationCode(invitationCode);
        request.setDateOfBirth(dateOfBirth);
        request.setAddress(address);
        request.setAdditionalInfo(additionalInfo);

        PortalUserResponse response = portalUserService.registerPortalUser(churchId, request, photo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Login portal user (public endpoint)
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody PortalLoginRequest request,
            @RequestParam Long churchId) {
        Map<String, Object> loginResponse = portalUserService.loginPortalUser(churchId, request);
        return ResponseEntity.ok(loginResponse);
    }

    /**
     * Verify email with token (public endpoint)
     */
    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestParam String token) {
        portalUserService.verifyEmail(token);
        return ResponseEntity.ok(Map.of(
            "message", "Email verified successfully. Please wait for admin approval."
        ));
    }

    /**
     * Resend verification email (public endpoint)
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @RequestParam String email,
            @RequestParam Long churchId) {
        portalUserService.resendVerificationEmail(email, churchId);
        return ResponseEntity.ok(Map.of(
            "message", "Verification email resent successfully"
        ));
    }

    /**
     * Request password reset (public endpoint)
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestParam String email,
            @RequestParam Long churchId) {
        portalUserService.requestPasswordReset(email, churchId);
        return ResponseEntity.ok(Map.of(
            "message", "Password reset email sent successfully"
        ));
    }

    /**
     * Reset password with token (public endpoint)
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        portalUserService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of(
            "message", "Password reset successfully"
        ));
    }

    // Admin endpoints (require authentication)

    /**
     * Get all portal users by status (admin only)
     */
    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping("/users")
    public ResponseEntity<List<PortalUserResponse>> getPortalUsersByStatus(
            @RequestParam(required = false) PortalUserStatus status,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<PortalUserResponse> users = status != null
            ? portalUserService.getPortalUsersByStatus(churchId, status)
            : portalUserService.getPortalUsersByStatus(churchId, PortalUserStatus.PENDING_APPROVAL);
        return ResponseEntity.ok(users);
    }

    /**
     * Get portal user by ID (admin only)
     */
    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping("/users/{id}")
    public ResponseEntity<PortalUserResponse> getPortalUserById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        PortalUserResponse response = portalUserService.getPortalUserById(churchId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Approve portal user (admin only)
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PostMapping("/users/{id}/approve")
    public ResponseEntity<PortalUserResponse> approvePortalUser(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        // TODO: Extract admin user ID from JWT
        Long approvedByUserId = 1L; // Placeholder
        PortalUserResponse response = portalUserService.approvePortalUser(churchId, id, approvedByUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject portal user (admin only)
     */
    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PostMapping("/users/{id}/reject")
    public ResponseEntity<PortalUserResponse> rejectPortalUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        String reason = payload.getOrDefault("reason", "No reason provided");
        PortalUserResponse response = portalUserService.rejectPortalUser(churchId, id, reason);
        return ResponseEntity.ok(response);
    }

    /**
     * Upload profile picture for portal user (authenticated portal user)
     * This endpoint will be used by members to upload their own profile picture from the portal
     */
    @PostMapping("/profile/picture")
    public ResponseEntity<Map<String, String>> uploadProfilePicture(
            @RequestParam("file") MultipartFile file,
            @RequestParam("email") String email,
            @RequestParam("churchId") Long churchId) {
        try {
            String imageUrl = portalUserService.uploadProfilePicture(email, churchId, file);
            Map<String, String> response = Map.of(
                "message", "Profile picture uploaded successfully",
                "imageUrl", imageUrl
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get current portal user profile (authenticated portal user)
     */
    @GetMapping("/profile")
    public ResponseEntity<PortalUserResponse> getProfile(
            @RequestParam("email") String email,
            @RequestParam("churchId") Long churchId) {
        PortalUserResponse response = portalUserService.getPortalUserByEmail(email, churchId);
        return ResponseEntity.ok(response);
    }
}
