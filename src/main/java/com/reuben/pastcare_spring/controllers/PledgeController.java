package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.PledgePaymentRequest;
import com.reuben.pastcare_spring.dtos.PledgeRequest;
import com.reuben.pastcare_spring.dtos.PledgeResponse;
import com.reuben.pastcare_spring.dtos.PledgeStatsResponse;
import com.reuben.pastcare_spring.services.PledgeService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * REST API for managing member pledges
 */
@RestController
@RequestMapping("/api/pledges")
@RequiredArgsConstructor
@Tag(name = "Pledges", description = "Pledge management endpoints")
public class PledgeController {

  private final PledgeService pledgeService;
  private final RequestContextUtil requestContextUtil;

  /**
   * Get all pledges for the current church
   */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get all pledges", description = "Returns all pledges for the current church")
  public ResponseEntity<List<PledgeResponse>> getAllPledges(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<PledgeResponse> pledges = pledgeService.getAllPledges(churchId);
    return ResponseEntity.ok(pledges);
  }

  /**
   * Get pledge by ID
   */
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get pledge by ID", description = "Returns a single pledge by ID")
  public ResponseEntity<PledgeResponse> getPledgeById(
      @PathVariable Long id,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    PledgeResponse pledge = pledgeService.getPledgeById(churchId, id);
    return ResponseEntity.ok(pledge);
  }

  /**
   * Create a new pledge
   */
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Create pledge", description = "Creates a new member pledge")
  public ResponseEntity<PledgeResponse> createPledge(
      @Valid @RequestBody PledgeRequest request,
      HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    PledgeResponse pledge = pledgeService.createPledge(churchId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(pledge);
  }

  /**
   * Update an existing pledge
   */
  @PutMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Update pledge", description = "Updates an existing pledge")
  public ResponseEntity<PledgeResponse> updatePledge(
      @PathVariable Long id,
      @Valid @RequestBody PledgeRequest request,
      HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    PledgeResponse pledge = pledgeService.updatePledge(churchId, id, request);
    return ResponseEntity.ok(pledge);
  }

  /**
   * Delete a pledge
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Delete pledge", description = "Deletes a pledge")
  public ResponseEntity<Void> deletePledge(
      @PathVariable Long id,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    pledgeService.deletePledge(churchId, id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get pledges by member
   */
  @GetMapping("/member/{memberId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get pledges by member", description = "Returns all pledges for a specific member")
  public ResponseEntity<List<PledgeResponse>> getPledgesByMember(
      @PathVariable Long memberId,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<PledgeResponse> pledges = pledgeService.getPledgesByMember(churchId, memberId);
    return ResponseEntity.ok(pledges);
  }

  /**
   * Get pledges by campaign
   */
  @GetMapping("/campaign/{campaignId}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get pledges by campaign", description = "Returns all pledges for a specific campaign")
  public ResponseEntity<List<PledgeResponse>> getPledgesByCampaign(
      @PathVariable Long campaignId,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<PledgeResponse> pledges = pledgeService.getPledgesByCampaign(churchId, campaignId);
    return ResponseEntity.ok(pledges);
  }

  /**
   * Get active pledges
   */
  @GetMapping("/active")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get active pledges", description = "Returns all active pledges")
  public ResponseEntity<List<PledgeResponse>> getActivePledges(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<PledgeResponse> pledges = pledgeService.getActivePledges(churchId);
    return ResponseEntity.ok(pledges);
  }

  /**
   * Get overdue pledges
   */
  @GetMapping("/overdue")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get overdue pledges", description = "Returns pledges with overdue payments")
  public ResponseEntity<List<PledgeResponse>> getOverduePledges(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<PledgeResponse> pledges = pledgeService.getOverduePledges(churchId);
    return ResponseEntity.ok(pledges);
  }

  /**
   * Record a pledge payment
   */
  @PostMapping("/payment")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Record pledge payment", description = "Records a payment toward a pledge")
  public ResponseEntity<PledgeResponse> recordPayment(
      @Valid @RequestBody PledgePaymentRequest request,
      HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    PledgeResponse pledge = pledgeService.recordPayment(churchId, request);
    return ResponseEntity.ok(pledge);
  }

  /**
   * Cancel a pledge
   */
  @PostMapping("/{id}/cancel")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Cancel pledge", description = "Cancels a pledge")
  public ResponseEntity<PledgeResponse> cancelPledge(
      @PathVariable Long id,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    PledgeResponse pledge = pledgeService.cancelPledge(churchId, id);
    return ResponseEntity.ok(pledge);
  }

  /**
   * Get pledge statistics
   */
  @GetMapping("/stats")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get pledge statistics", description = "Returns pledge statistics for the church")
  public ResponseEntity<PledgeStatsResponse> getPledgeStats(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    PledgeStatsResponse stats = pledgeService.getPledgeStats(churchId);
    return ResponseEntity.ok(stats);
  }
}
