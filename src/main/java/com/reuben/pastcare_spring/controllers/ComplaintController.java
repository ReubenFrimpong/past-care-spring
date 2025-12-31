package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dto.*;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.services.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for complaint management.
 */
@RestController
@RequestMapping("/api/complaints")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ComplaintController {

    private final ComplaintService complaintService;

    /**
     * Create a new complaint.
     * Any authenticated user can submit a complaint.
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ComplaintDTO> createComplaint(
            @Valid @RequestBody CreateComplaintRequest request,
            @AuthenticationPrincipal User user) {
        ComplaintDTO complaint = complaintService.createComplaint(request, user.getId(), user.getChurch().getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(complaint);
    }

    /**
     * Get all complaints for the church.
     * Requires ADMIN or PASTOR role.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<List<ComplaintDTO>> getAllComplaints(@AuthenticationPrincipal User user) {
        List<ComplaintDTO> complaints = complaintService.getAllComplaints(user.getChurch().getId());
        return ResponseEntity.ok(complaints);
    }

    /**
     * Get complaints by status.
     * Requires ADMIN or PASTOR role.
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<List<ComplaintDTO>> getComplaintsByStatus(
            @PathVariable String status,
            @AuthenticationPrincipal User user) {
        List<ComplaintDTO> complaints = complaintService.getComplaintsByStatus(user.getChurch().getId(), status);
        return ResponseEntity.ok(complaints);
    }

    /**
     * Get complaints by category.
     * Requires ADMIN or PASTOR role.
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<List<ComplaintDTO>> getComplaintsByCategory(
            @PathVariable String category,
            @AuthenticationPrincipal User user) {
        List<ComplaintDTO> complaints = complaintService.getComplaintsByCategory(user.getChurch().getId(), category);
        return ResponseEntity.ok(complaints);
    }

    /**
     * Get my complaints (submitted by current user).
     * Any authenticated user can view their own complaints.
     */
    @GetMapping("/my-complaints")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ComplaintDTO>> getMyComplaints(@AuthenticationPrincipal User user) {
        List<ComplaintDTO> complaints = complaintService.getMyComplaints(user.getId(), user.getChurch().getId());
        return ResponseEntity.ok(complaints);
    }

    /**
     * Get complaints assigned to current user.
     * Requires ADMIN or PASTOR role.
     */
    @GetMapping("/assigned-to-me")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<List<ComplaintDTO>> getAssignedComplaints(@AuthenticationPrincipal User user) {
        List<ComplaintDTO> complaints = complaintService.getAssignedComplaints(user.getId(), user.getChurch().getId());
        return ResponseEntity.ok(complaints);
    }

    /**
     * Get a single complaint by ID.
     * User can view if they submitted it, or if they have ADMIN/PASTOR role.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ComplaintDTO> getComplaintById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        ComplaintDTO complaint = complaintService.getComplaintById(id, user.getChurch().getId());

        // Check if user has permission to view
        boolean isSubmitter = complaint.getSubmittedById().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN") || user.getRole().name().equals("PASTOR");

        if (!isSubmitter && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(complaint);
    }

    /**
     * Update a complaint.
     * Requires ADMIN or PASTOR role.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<ComplaintDTO> updateComplaint(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComplaintRequest request,
            @AuthenticationPrincipal User user) {
        ComplaintDTO complaint = complaintService.updateComplaint(id, request, user.getId(), user.getChurch().getId());
        return ResponseEntity.ok(complaint);
    }

    /**
     * Delete a complaint.
     * Requires ADMIN role only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteComplaint(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        complaintService.deleteComplaint(id, user.getChurch().getId());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Complaint deleted successfully");
        return ResponseEntity.ok(response);
    }

    /**
     * Get complaint activities.
     * User can view activities for complaints they submitted, admins can view all activities including internal.
     */
    @GetMapping("/{id}/activities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ComplaintActivityDTO>> getComplaintActivities(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        ComplaintDTO complaint = complaintService.getComplaintById(id, user.getChurch().getId());

        // Check if user has permission
        boolean isSubmitter = complaint.getSubmittedById().equals(user.getId());
        boolean isAdmin = user.getRole().name().equals("ADMIN") || user.getRole().name().equals("PASTOR");

        if (!isSubmitter && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Admins see all activities, submitters only see visible ones
        List<ComplaintActivityDTO> activities = complaintService.getComplaintActivities(id, user.getChurch().getId(), isAdmin);
        return ResponseEntity.ok(activities);
    }

    /**
     * Get complaint statistics.
     * Requires ADMIN or PASTOR role.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<ComplaintStatsDTO> getComplaintStats(@AuthenticationPrincipal User user) {
        ComplaintStatsDTO stats = complaintService.getComplaintStats(user.getChurch().getId());
        return ResponseEntity.ok(stats);
    }

    /**
     * Search complaints.
     * Requires ADMIN or PASTOR role.
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'PASTOR')")
    public ResponseEntity<List<ComplaintDTO>> searchComplaints(
            @RequestParam String q,
            @AuthenticationPrincipal User user) {
        List<ComplaintDTO> complaints = complaintService.searchComplaints(user.getChurch().getId(), q);
        return ResponseEntity.ok(complaints);
    }
}
