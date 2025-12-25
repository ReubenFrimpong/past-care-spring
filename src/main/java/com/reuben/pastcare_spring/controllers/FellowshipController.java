package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.FellowshipType;
import com.reuben.pastcare_spring.services.FellowshipService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fellowships")
@RequiredArgsConstructor
@Tag(name = "Fellowship", description = "Fellowship management endpoints")
public class FellowshipController {

  private final FellowshipService fellowshipService;
  private final RequestContextUtil requestContextUtil;

  /**
   * Get all fellowships for the current user's church.
   */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get all fellowships", description = "Returns all fellowships for the current user's church")
  public ResponseEntity<List<FellowshipResponse>> getAllFellowships() {
    List<FellowshipResponse> fellowships = fellowshipService.getAllFellowships();
    return ResponseEntity.ok(fellowships);
  }

  /**
   * Get all active fellowships
   */
  @GetMapping("/active")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get active fellowships", description = "Returns all active fellowships")
  public ResponseEntity<List<FellowshipResponse>> getActiveFellowships() {
    List<FellowshipResponse> fellowships = fellowshipService.getActiveFellowships();
    return ResponseEntity.ok(fellowships);
  }

  /**
   * Get fellowships accepting new members
   */
  @GetMapping("/accepting-members")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get fellowships accepting members", description = "Returns fellowships currently accepting new members")
  public ResponseEntity<List<FellowshipResponse>> getFellowshipsAcceptingMembers() {
    List<FellowshipResponse> fellowships = fellowshipService.getFellowshipsAcceptingMembers();
    return ResponseEntity.ok(fellowships);
  }

  /**
   * Get fellowships by type
   */
  @GetMapping("/type/{type}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get fellowships by type", description = "Returns fellowships of a specific type")
  public ResponseEntity<List<FellowshipResponse>> getFellowshipsByType(@PathVariable FellowshipType type) {
    List<FellowshipResponse> fellowships = fellowshipService.getFellowshipsByType(type);
    return ResponseEntity.ok(fellowships);
  }

  /**
   * Get fellowship by ID.
   */
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get fellowship by ID", description = "Returns a single fellowship by ID")
  public ResponseEntity<FellowshipResponse> getFellowshipById(@PathVariable Long id) {
    FellowshipResponse fellowship = fellowshipService.getFellowshipById(id);
    return ResponseEntity.ok(fellowship);
  }

  /**
   * Create a new fellowship.
   */
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Create fellowship", description = "Creates a new fellowship")
  public ResponseEntity<FellowshipResponse> createFellowship(
    @Valid @RequestBody FellowshipRequest request,
    HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    FellowshipResponse createdFellowship = fellowshipService.createFellowship(churchId, request);
    return ResponseEntity.ok(createdFellowship);
  }

  /**
   * Update an existing fellowship.
   */
  @PutMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Update fellowship", description = "Updates an existing fellowship")
  public ResponseEntity<FellowshipResponse> updateFellowship(
    @PathVariable Long id,
    @Valid @RequestBody FellowshipRequest request
  ) {
    FellowshipResponse updatedFellowship = fellowshipService.updateFellowship(id, request);
    return ResponseEntity.ok(updatedFellowship);
  }

  /**
   * Delete a fellowship.
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Delete fellowship", description = "Deletes a fellowship")
  public ResponseEntity<Void> deleteFellowship(@PathVariable Long id) {
    fellowshipService.deleteFellowship(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Assign a leader to a fellowship
   */
  @PostMapping("/{fellowshipId}/leader/{userId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Assign leader", description = "Assigns a leader to a fellowship")
  public ResponseEntity<FellowshipResponse> assignLeader(
    @PathVariable Long fellowshipId,
    @PathVariable Long userId
  ) {
    FellowshipResponse updated = fellowshipService.assignLeader(fellowshipId, userId);
    return ResponseEntity.ok(updated);
  }

  /**
   * Add a co-leader to a fellowship
   */
  @PostMapping("/{fellowshipId}/coleaders/{userId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Add co-leader", description = "Adds a co-leader to a fellowship")
  public ResponseEntity<FellowshipResponse> addColeader(
    @PathVariable Long fellowshipId,
    @PathVariable Long userId
  ) {
    FellowshipResponse updated = fellowshipService.addColeader(fellowshipId, userId);
    return ResponseEntity.ok(updated);
  }

  /**
   * Remove a co-leader from a fellowship
   */
  @DeleteMapping("/{fellowshipId}/coleaders/{userId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Remove co-leader", description = "Removes a co-leader from a fellowship")
  public ResponseEntity<FellowshipResponse> removeColeader(
    @PathVariable Long fellowshipId,
    @PathVariable Long userId
  ) {
    FellowshipResponse updated = fellowshipService.removeColeader(fellowshipId, userId);
    return ResponseEntity.ok(updated);
  }

  // ========== Join Request Endpoints ==========

  /**
   * Create a join request for a fellowship
   */
  @PostMapping("/join-requests")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Create join request", description = "Creates a request to join a fellowship")
  public ResponseEntity<FellowshipJoinRequestResponse> createJoinRequest(
    @Valid @RequestBody FellowshipJoinRequestRequest request
  ) {
    FellowshipJoinRequestResponse joinRequest = fellowshipService.createJoinRequest(request);
    return ResponseEntity.ok(joinRequest);
  }

  /**
   * Get all join requests for a fellowship
   */
  @GetMapping("/{fellowshipId}/join-requests")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get join requests", description = "Returns all join requests for a fellowship")
  public ResponseEntity<List<FellowshipJoinRequestResponse>> getJoinRequestsByFellowship(
    @PathVariable Long fellowshipId
  ) {
    List<FellowshipJoinRequestResponse> requests = fellowshipService.getJoinRequestsByFellowship(fellowshipId);
    return ResponseEntity.ok(requests);
  }

  /**
   * Get all pending join requests for a fellowship
   */
  @GetMapping("/{fellowshipId}/join-requests/pending")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get pending join requests", description = "Returns all pending join requests for a fellowship")
  public ResponseEntity<List<FellowshipJoinRequestResponse>> getPendingJoinRequests(
    @PathVariable Long fellowshipId
  ) {
    List<FellowshipJoinRequestResponse> requests = fellowshipService.getPendingJoinRequests(fellowshipId);
    return ResponseEntity.ok(requests);
  }

  /**
   * Approve a join request
   */
  @PostMapping("/join-requests/{requestId}/approve")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Approve join request", description = "Approves a join request and adds member to fellowship")
  public ResponseEntity<FellowshipJoinRequestResponse> approveJoinRequest(
    @PathVariable Long requestId,
    @RequestParam Long reviewerId,
    @RequestParam(required = false) String reviewNotes,
    HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    FellowshipJoinRequestResponse updated = fellowshipService.approveJoinRequest(requestId, reviewerId, reviewNotes);
    return ResponseEntity.ok(updated);
  }

  /**
   * Reject a join request
   */
  @PostMapping("/join-requests/{requestId}/reject")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Reject join request", description = "Rejects a join request")
  public ResponseEntity<FellowshipJoinRequestResponse> rejectJoinRequest(
    @PathVariable Long requestId,
    @RequestParam Long reviewerId,
    @RequestParam(required = false) String reviewNotes,
    HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    FellowshipJoinRequestResponse updated = fellowshipService.rejectJoinRequest(requestId, reviewerId, reviewNotes);
    return ResponseEntity.ok(updated);
  }

  /**
   * Upload fellowship image
   */
  @PostMapping("/{id}/image")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Upload fellowship image", description = "Upload an image for a fellowship")
  public ResponseEntity<FellowshipResponse> uploadFellowshipImage(
    @PathVariable Long id,
    @RequestParam("image") org.springframework.web.multipart.MultipartFile image,
    HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    FellowshipResponse updated = fellowshipService.uploadFellowshipImage(id, image);
    return ResponseEntity.ok(updated);
  }

  /**
   * Add multiple members to fellowship
   */
  @PostMapping("/{id}/members/bulk")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Add multiple members", description = "Adds multiple members to a fellowship at once")
  public ResponseEntity<FellowshipResponse> addMembersBulk(
    @PathVariable Long id,
    @RequestBody java.util.List<Long> memberIds,
    HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    FellowshipResponse updated = fellowshipService.addMembersBulk(id, memberIds);
    return ResponseEntity.ok(updated);
  }

  /**
   * Remove multiple members from fellowship
   */
  @DeleteMapping("/{id}/members/bulk")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Remove multiple members", description = "Removes multiple members from a fellowship at once")
  public ResponseEntity<FellowshipResponse> removeMembersBulk(
    @PathVariable Long id,
    @RequestBody java.util.List<Long> memberIds,
    HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    FellowshipResponse updated = fellowshipService.removeMembersBulk(id, memberIds);
    return ResponseEntity.ok(updated);
  }

  // ========== Fellowship Phase 2: Analytics Endpoints ==========

  /**
   * Get analytics for a specific fellowship
   */
  @GetMapping("/{id}/analytics")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get fellowship analytics", description = "Returns analytics for a specific fellowship including health metrics, growth trends, and occupancy")
  public ResponseEntity<FellowshipAnalyticsResponse> getFellowshipAnalytics(@PathVariable Long id) {
    FellowshipAnalyticsResponse analytics = fellowshipService.getFellowshipAnalytics(id);
    return ResponseEntity.ok(analytics);
  }

  /**
   * Get analytics for all fellowships
   */
  @GetMapping("/analytics/all")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get all fellowship analytics", description = "Returns analytics for all fellowships in the church")
  public ResponseEntity<List<FellowshipAnalyticsResponse>> getAllFellowshipAnalytics() {
    List<FellowshipAnalyticsResponse> analytics = fellowshipService.getAllFellowshipAnalytics();
    return ResponseEntity.ok(analytics);
  }

  /**
   * Get fellowship comparison data
   */
  @GetMapping("/analytics/comparison")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get fellowship comparison", description = "Returns comparison data for all fellowships ranked by health and size")
  public ResponseEntity<List<FellowshipComparisonResponse>> getFellowshipComparison() {
    List<FellowshipComparisonResponse> comparison = fellowshipService.getFellowshipComparison();
    return ResponseEntity.ok(comparison);
  }
}
