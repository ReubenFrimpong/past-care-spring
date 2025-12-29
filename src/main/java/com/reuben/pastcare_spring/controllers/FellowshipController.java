package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.FellowshipMemberAction;
import com.reuben.pastcare_spring.models.FellowshipType;
import com.reuben.pastcare_spring.services.FellowshipService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get all fellowships", description = "Returns all fellowships for the current user's church")
  public ResponseEntity<List<FellowshipResponse>> getAllFellowships() {
    List<FellowshipResponse> fellowships = fellowshipService.getAllFellowships();
    return ResponseEntity.ok(fellowships);
  }

  /**
   * Get all active fellowships
   */
  @GetMapping("/active")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get active fellowships", description = "Returns all active fellowships")
  public ResponseEntity<List<FellowshipResponse>> getActiveFellowships() {
    List<FellowshipResponse> fellowships = fellowshipService.getActiveFellowships();
    return ResponseEntity.ok(fellowships);
  }

  /**
   * Get fellowships accepting new members
   */
  @GetMapping("/accepting-members")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get fellowships accepting members", description = "Returns fellowships currently accepting new members")
  public ResponseEntity<List<FellowshipResponse>> getFellowshipsAcceptingMembers() {
    List<FellowshipResponse> fellowships = fellowshipService.getFellowshipsAcceptingMembers();
    return ResponseEntity.ok(fellowships);
  }

  /**
   * Get fellowships by type
   */
  @GetMapping("/type/{type}")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get fellowships by type", description = "Returns fellowships of a specific type")
  public ResponseEntity<List<FellowshipResponse>> getFellowshipsByType(@PathVariable FellowshipType type) {
    List<FellowshipResponse> fellowships = fellowshipService.getFellowshipsByType(type);
    return ResponseEntity.ok(fellowships);
  }

  /**
   * Get fellowship by ID.
   */
  @GetMapping("/{id}")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get fellowship by ID", description = "Returns a single fellowship by ID")
  public ResponseEntity<FellowshipResponse> getFellowshipById(@PathVariable Long id) {
    FellowshipResponse fellowship = fellowshipService.getFellowshipById(id);
    return ResponseEntity.ok(fellowship);
  }

  /**
   * Create a new fellowship.
   */
  @PostMapping
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
  @Operation(summary = "Delete fellowship", description = "Deletes a fellowship")
  public ResponseEntity<Void> deleteFellowship(@PathVariable Long id) {
    fellowshipService.deleteFellowship(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Assign a leader to a fellowship
   */
  @PostMapping("/{fellowshipId}/leader/{userId}")
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
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
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
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
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
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

  /**
   * Get member IDs for a fellowship
   */
  @GetMapping("/{id}/members/ids")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get fellowship member IDs", description = "Returns list of member IDs in this fellowship")
  public ResponseEntity<java.util.List<Long>> getFellowshipMemberIds(@PathVariable Long id) {
    java.util.List<Long> memberIds = fellowshipService.getFellowshipMemberIds(id);
    return ResponseEntity.ok(memberIds);
  }

  // ========== Fellowship Phase 2: Analytics Endpoints ==========

  /**
   * Get analytics for a specific fellowship
   */
  @GetMapping("/{id}/analytics")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get fellowship analytics", description = "Returns analytics for a specific fellowship including health metrics, growth trends, and occupancy")
  public ResponseEntity<FellowshipAnalyticsResponse> getFellowshipAnalytics(@PathVariable Long id) {
    FellowshipAnalyticsResponse analytics = fellowshipService.getFellowshipAnalytics(id);
    return ResponseEntity.ok(analytics);
  }

  /**
   * Get analytics for all fellowships
   */
  @GetMapping("/analytics/all")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get all fellowship analytics", description = "Returns analytics for all fellowships in the church")
  public ResponseEntity<List<FellowshipAnalyticsResponse>> getAllFellowshipAnalytics() {
    List<FellowshipAnalyticsResponse> analytics = fellowshipService.getAllFellowshipAnalytics();
    return ResponseEntity.ok(analytics);
  }

  /**
   * Get fellowship comparison data
   */
  @GetMapping("/analytics/comparison")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get fellowship comparison", description = "Returns comparison data for all fellowships ranked by health and size")
  public ResponseEntity<List<FellowshipComparisonResponse>> getFellowshipComparison() {
    List<FellowshipComparisonResponse> comparison = fellowshipService.getFellowshipComparison();
    return ResponseEntity.ok(comparison);
  }

  /**
   * Get fellowship retention metrics
   */
  @GetMapping("/{id}/retention")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get fellowship retention metrics", description = "Returns retention metrics for a fellowship over a specified time period")
  public ResponseEntity<FellowshipRetentionResponse> getFellowshipRetention(
      @PathVariable Long id,
      @RequestParam String startDate,
      @RequestParam String endDate) {
    java.time.LocalDate start = java.time.LocalDate.parse(startDate);
    java.time.LocalDate end = java.time.LocalDate.parse(endDate);
    FellowshipRetentionResponse retention = fellowshipService.getFellowshipRetention(id, start, end);
    return ResponseEntity.ok(retention);
  }

  /**
   * Record a fellowship membership action
   */
  @PostMapping("/{id}/record-action")
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
  @Operation(summary = "Record membership action", description = "Records a fellowship membership action for retention tracking")
  public ResponseEntity<Void> recordMembershipAction(
      @PathVariable Long id,
      @RequestBody Map<String, Object> request,
      HttpServletRequest httpRequest) {
    Long memberId = Long.valueOf(request.get("memberId").toString());
    String actionStr = request.get("action").toString();
    String notes = request.get("notes") != null ? request.get("notes").toString() : null;
    Long userId = requestContextUtil.extractUserId(httpRequest);

    FellowshipMemberAction action = FellowshipMemberAction.valueOf(actionStr);
    fellowshipService.recordMembershipAction(id, memberId, action, notes, userId);
    return ResponseEntity.ok().build();
  }

  // ========== Fellowship Multiplication Tracking ==========

  /**
   * Get all fellowship multiplications
   */
  @GetMapping("/multiplications")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get all multiplications", description = "Returns all fellowship multiplication events")
  public ResponseEntity<List<FellowshipMultiplicationResponse>> getAllMultiplications() {
    List<FellowshipMultiplicationResponse> multiplications = fellowshipService.getAllMultiplications();
    return ResponseEntity.ok(multiplications);
  }

  /**
   * Get multiplications for a specific fellowship
   */
  @GetMapping("/{id}/multiplications")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get fellowship multiplications", description = "Returns multiplication events for a specific fellowship (as parent)")
  public ResponseEntity<List<FellowshipMultiplicationResponse>> getFellowshipMultiplications(
      @PathVariable Long id) {
    List<FellowshipMultiplicationResponse> multiplications = fellowshipService.getFellowshipMultiplications(id);
    return ResponseEntity.ok(multiplications);
  }

  /**
   * Record a fellowship multiplication
   */
  @PostMapping("/{id}/multiply")
  @RequirePermission(Permission.FELLOWSHIP_MANAGE)
  @Operation(summary = "Record multiplication", description = "Records a fellowship multiplication event")
  public ResponseEntity<FellowshipMultiplicationResponse> recordMultiplication(
      @PathVariable Long id,
      @RequestBody RecordMultiplicationRequest request,
      HttpServletRequest httpRequest) {
    Long userId = requestContextUtil.extractUserId(httpRequest);
    FellowshipMultiplicationResponse multiplication = fellowshipService.recordMultiplication(id, request, userId);
    return ResponseEntity.ok(multiplication);
  }

  // ========== Fellowship Balance Recommendations ==========

  /**
   * Get balance recommendations for all fellowships
   */
  @GetMapping("/balance-recommendations")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get balance recommendations", description = "Returns balance recommendations for all fellowships")
  public ResponseEntity<List<FellowshipBalanceRecommendationResponse>> getBalanceRecommendations() {
    List<FellowshipBalanceRecommendationResponse> recommendations = fellowshipService.getBalanceRecommendations();
    return ResponseEntity.ok(recommendations);
  }

  /**
   * Get balance recommendation for a specific fellowship
   */
  @GetMapping("/{id}/balance-recommendation")
  @RequirePermission(Permission.FELLOWSHIP_VIEW_ALL)
  @Operation(summary = "Get fellowship balance recommendation", description = "Returns balance recommendation for a specific fellowship")
  public ResponseEntity<FellowshipBalanceRecommendationResponse> getFellowshipBalanceRecommendation(
      @PathVariable Long id) {
    FellowshipBalanceRecommendationResponse recommendation = fellowshipService.getFellowshipBalanceRecommendation(id);
    return ResponseEntity.ok(recommendation);
  }
}
