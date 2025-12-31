package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.InvitationCode;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.services.InvitationCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for invitation code management.
 */
@RestController
@RequestMapping("/api/invitation-codes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class InvitationCodeController {

    private final InvitationCodeService invitationCodeService;

    /**
     * Create a new invitation code.
     * Requires ADMIN or PASTOR role.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<InvitationCode> createInvitationCode(
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Integer maxUses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime expiresAt,
            @RequestParam(required = false) String defaultRole,
            @AuthenticationPrincipal User user) {

        Role role = defaultRole != null ? Role.valueOf(defaultRole) : Role.MEMBER;

        InvitationCode code = invitationCodeService.createInvitationCode(
                user.getChurch().getId(),
                user.getId(),
                description,
                maxUses,
                expiresAt,
                role
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(code);
    }

    /**
     * Get all invitation codes for the church.
     * Requires ADMIN or PASTOR role.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<List<InvitationCode>> getInvitationCodes(@AuthenticationPrincipal User user) {
        List<InvitationCode> codes = invitationCodeService.getChurchInvitationCodes(user.getChurch().getId());
        return ResponseEntity.ok(codes);
    }

    /**
     * Get active invitation codes only.
     * Requires ADMIN or PASTOR role.
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<List<InvitationCode>> getActiveInvitationCodes(@AuthenticationPrincipal User user) {
        List<InvitationCode> codes = invitationCodeService.getActiveInvitationCodes(user.getChurch().getId());
        return ResponseEntity.ok(codes);
    }

    /**
     * Validate an invitation code (public endpoint for registration).
     */
    @GetMapping("/validate/{code}")
    public ResponseEntity<Map<String, Object>> validateInvitationCode(@PathVariable String code) {
        Optional<InvitationCode> invitationCode = invitationCodeService.validateInvitationCode(code);

        Map<String, Object> response = new HashMap<>();
        if (invitationCode.isPresent()) {
            InvitationCode ic = invitationCode.get();
            response.put("valid", true);
            response.put("churchId", ic.getChurch().getId());
            response.put("churchName", ic.getChurch().getName());
            response.put("defaultRole", ic.getDefaultRole());
            return ResponseEntity.ok(response);
        } else {
            response.put("valid", false);
            response.put("message", "Invalid or expired invitation code");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get invitation code by ID.
     * Requires ADMIN or PASTOR role.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<InvitationCode> getInvitationCode(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        InvitationCode code = invitationCodeService.getInvitationCodeById(id, user.getChurch().getId());
        return ResponseEntity.ok(code);
    }

    /**
     * Deactivate an invitation code.
     * Requires ADMIN or PASTOR role.
     */
    @PutMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<Map<String, String>> deactivateInvitationCode(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        invitationCodeService.deactivateInvitationCode(id, user.getChurch().getId());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Invitation code deactivated successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Delete an invitation code.
     * Requires ADMIN role only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteInvitationCode(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        invitationCodeService.deleteInvitationCode(id, user.getChurch().getId());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Invitation code deleted successfully");
        return ResponseEntity.ok(response);
    }
}
