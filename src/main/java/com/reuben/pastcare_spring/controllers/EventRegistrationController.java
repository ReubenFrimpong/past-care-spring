package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.services.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for event registration management.
 * Handles registration CRUD, approval workflow, waitlist, and attendance tracking.
 */
@RestController
@RequestMapping("/api/event-registrations")
@RequiredArgsConstructor
public class EventRegistrationController {

    private final EventRegistrationService registrationService;
    private final QRCodeService qrCodeService;
    private final com.reuben.pastcare_spring.repositories.UserRepository userRepository;

    /**
     * Register for an event
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF', 'MEMBER')")
    public ResponseEntity<EventRegistrationResponse> registerForEvent(
        @Valid @RequestBody EventRegistrationRequest request,
        Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        EventRegistrationResponse response = registrationService.registerForEvent(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get registration by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF', 'MEMBER')")
    public ResponseEntity<EventRegistrationResponse> getRegistration(@PathVariable Long id) {
        EventRegistrationResponse response = registrationService.getRegistration(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all registrations for an event
     */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF')")
    public ResponseEntity<Page<EventRegistrationResponse>> getEventRegistrations(
        @PathVariable Long eventId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<EventRegistrationResponse> response = registrationService.getEventRegistrations(eventId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get registrations for a member
     */
    @GetMapping("/member/{memberId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF', 'MEMBER')")
    public ResponseEntity<Page<EventRegistrationResponse>> getMemberRegistrations(
        @PathVariable Long memberId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<EventRegistrationResponse> response = registrationService.getMemberRegistrations(memberId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get pending approvals
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF')")
    public ResponseEntity<Page<EventRegistrationResponse>> getPendingApprovals(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<EventRegistrationResponse> response = registrationService.getPendingApprovals(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get waitlist for an event
     */
    @GetMapping("/event/{eventId}/waitlist")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF')")
    public ResponseEntity<List<EventRegistrationResponse>> getEventWaitlist(@PathVariable Long eventId) {
        List<EventRegistrationResponse> response = registrationService.getEventWaitlist(eventId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get attendees for an event
     */
    @GetMapping("/event/{eventId}/attendees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF')")
    public ResponseEntity<List<EventRegistrationResponse>> getEventAttendees(@PathVariable Long eventId) {
        List<EventRegistrationResponse> response = registrationService.getEventAttendees(eventId);
        return ResponseEntity.ok(response);
    }

    /**
     * Filter registrations
     */
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF')")
    public ResponseEntity<Page<EventRegistrationResponse>> filterRegistrations(
        @RequestParam(required = false) Long eventId,
        @RequestParam(required = false) Long memberId,
        @RequestParam(required = false) RegistrationStatus status,
        @RequestParam(required = false) Boolean isOnWaitlist,
        @RequestParam(required = false) Boolean attended,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ?
            Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<EventRegistrationResponse> response = registrationService.filterRegistrations(
            eventId, memberId, status, isOnWaitlist, attended, pageable
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Approve a registration
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF')")
    public ResponseEntity<EventRegistrationResponse> approveRegistration(
        @PathVariable Long id,
        Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        EventRegistrationResponse response = registrationService.approveRegistration(id, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Reject a registration
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF')")
    public ResponseEntity<EventRegistrationResponse> rejectRegistration(
        @PathVariable Long id,
        @RequestParam String reason,
        Authentication authentication
    ) {
        Long userId = getUserIdFromAuth(authentication);
        EventRegistrationResponse response = registrationService.rejectRegistration(id, reason, userId);
        return ResponseEntity.ok(response);
    }


    /**
     * Mark registration as attended
     */
    @PostMapping("/{id}/attended")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF')")
    public ResponseEntity<EventRegistrationResponse> markAsAttended(@PathVariable Long id) {
        EventRegistrationResponse response = registrationService.markAsAttended(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark registration as no-show
     */
    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF')")
    public ResponseEntity<EventRegistrationResponse> markAsNoShow(@PathVariable Long id) {
        EventRegistrationResponse response = registrationService.markAsNoShow(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Generate QR code ticket for registration
     */
    @GetMapping("/{id}/qr-ticket")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF', 'MEMBER')")
    public ResponseEntity<QRCodeResponse> generateQRTicket(@PathVariable Long id) {
        EventRegistrationResponse registration = registrationService.getRegistration(id);

        // Generate or retrieve ticket code
        String ticketCode = registration.getTicketCode();
        if (ticketCode == null || ticketCode.isEmpty()) {
            ticketCode = registrationService.generateTicketCode(id);
        }

        // Generate QR code image
        String qrCodeImage = qrCodeService.generateQRCodeImage(ticketCode);

        // Extract expiry from ticket code (registrations don't expire, but QR uses event end date)
        QRCodeResponse response = new QRCodeResponse(
            registration.getId(),
            registration.getEventName(),
            ticketCode,
            qrCodeImage,
            registration.getEventEndDate(),
            "QR code ticket generated successfully"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Send confirmation email for registration
     */
    @PostMapping("/{id}/send-confirmation")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF')")
    public ResponseEntity<String> sendConfirmationEmail(@PathVariable Long id) {
        registrationService.sendConfirmationEmail(id);
        return ResponseEntity.ok("Confirmation email sent successfully");
    }

    /**
     * Cancel registration by attendee
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'PASTOR', 'STAFF', 'MEMBER')")
    public ResponseEntity<EventRegistrationResponse> cancelRegistration(
        @PathVariable Long id,
        @RequestBody(required = false) CancellationRequest request
    ) {
        String reason = request != null ? request.getReason() : "Cancelled by attendee";
        EventRegistrationResponse response = registrationService.cancelRegistration(id, reason);
        return ResponseEntity.ok(response);
    }

    // ==================== Helper Methods ====================

    private Long getUserIdFromAuth(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByEmail(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));
        return user.getId();
    }
}
