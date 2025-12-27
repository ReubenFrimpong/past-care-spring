package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.CampaignRequest;
import com.reuben.pastcare_spring.dtos.CampaignResponse;
import com.reuben.pastcare_spring.dtos.CampaignStatsResponse;
import com.reuben.pastcare_spring.models.CampaignStatus;
import com.reuben.pastcare_spring.services.CampaignService;
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
 * REST API for managing fundraising campaigns
 */
@RestController
@RequestMapping("/api/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaigns", description = "Campaign management endpoints")
public class CampaignController {

  private final CampaignService campaignService;
  private final RequestContextUtil requestContextUtil;

  /**
   * Get all campaigns for the current church
   */
  @GetMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get all campaigns", description = "Returns all campaigns for the current church")
  public ResponseEntity<List<CampaignResponse>> getAllCampaigns(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<CampaignResponse> campaigns = campaignService.getAllCampaigns(churchId);
    return ResponseEntity.ok(campaigns);
  }

  /**
   * Get campaign by ID
   */
  @GetMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get campaign by ID", description = "Returns a single campaign by ID")
  public ResponseEntity<CampaignResponse> getCampaignById(
      @PathVariable Long id,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    CampaignResponse campaign = campaignService.getCampaignById(churchId, id);
    return ResponseEntity.ok(campaign);
  }

  /**
   * Create a new campaign
   */
  @PostMapping
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Create campaign", description = "Creates a new fundraising campaign")
  public ResponseEntity<CampaignResponse> createCampaign(
      @Valid @RequestBody CampaignRequest request,
      HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    Long userId = requestContextUtil.extractUserId(httpRequest);
    CampaignResponse campaign = campaignService.createCampaign(churchId, userId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(campaign);
  }

  /**
   * Update an existing campaign
   */
  @PutMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Update campaign", description = "Updates an existing campaign")
  public ResponseEntity<CampaignResponse> updateCampaign(
      @PathVariable Long id,
      @Valid @RequestBody CampaignRequest request,
      HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    CampaignResponse campaign = campaignService.updateCampaign(churchId, id, request);
    return ResponseEntity.ok(campaign);
  }

  /**
   * Delete a campaign
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Delete campaign", description = "Deletes a campaign")
  public ResponseEntity<Void> deleteCampaign(
      @PathVariable Long id,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    campaignService.deleteCampaign(churchId, id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get campaigns by status
   */
  @GetMapping("/status/{status}")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get campaigns by status", description = "Returns campaigns filtered by status")
  public ResponseEntity<List<CampaignResponse>> getCampaignsByStatus(
      @PathVariable CampaignStatus status,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<CampaignResponse> campaigns = campaignService.getCampaignsByStatus(churchId, status);
    return ResponseEntity.ok(campaigns);
  }

  /**
   * Get active campaigns
   */
  @GetMapping("/active")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get active campaigns", description = "Returns all active campaigns")
  public ResponseEntity<List<CampaignResponse>> getActiveCampaigns(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<CampaignResponse> campaigns = campaignService.getActiveCampaigns(churchId);
    return ResponseEntity.ok(campaigns);
  }

  /**
   * Get featured campaigns
   */
  @GetMapping("/featured")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get featured campaigns", description = "Returns featured campaigns for dashboard")
  public ResponseEntity<List<CampaignResponse>> getFeaturedCampaigns(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<CampaignResponse> campaigns = campaignService.getFeaturedCampaigns(churchId);
    return ResponseEntity.ok(campaigns);
  }

  /**
   * Get public campaigns (for member portal)
   */
  @GetMapping("/public")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get public campaigns", description = "Returns public campaigns visible in member portal")
  public ResponseEntity<List<CampaignResponse>> getPublicCampaigns(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<CampaignResponse> campaigns = campaignService.getPublicCampaigns(churchId);
    return ResponseEntity.ok(campaigns);
  }

  /**
   * Get ongoing campaigns
   */
  @GetMapping("/ongoing")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get ongoing campaigns", description = "Returns ongoing campaigns (active with future or no end date)")
  public ResponseEntity<List<CampaignResponse>> getOngoingCampaigns(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<CampaignResponse> campaigns = campaignService.getOngoingCampaigns(churchId);
    return ResponseEntity.ok(campaigns);
  }

  /**
   * Search campaigns by name
   */
  @GetMapping("/search")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Search campaigns", description = "Search campaigns by name")
  public ResponseEntity<List<CampaignResponse>> searchCampaigns(
      @RequestParam String name,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<CampaignResponse> campaigns = campaignService.searchCampaignsByName(churchId, name);
    return ResponseEntity.ok(campaigns);
  }

  /**
   * Pause a campaign
   */
  @PostMapping("/{id}/pause")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Pause campaign", description = "Pauses an active campaign")
  public ResponseEntity<CampaignResponse> pauseCampaign(
      @PathVariable Long id,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    CampaignResponse campaign = campaignService.pauseCampaign(churchId, id);
    return ResponseEntity.ok(campaign);
  }

  /**
   * Resume a paused campaign
   */
  @PostMapping("/{id}/resume")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Resume campaign", description = "Resumes a paused campaign")
  public ResponseEntity<CampaignResponse> resumeCampaign(
      @PathVariable Long id,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    CampaignResponse campaign = campaignService.resumeCampaign(churchId, id);
    return ResponseEntity.ok(campaign);
  }

  /**
   * Complete a campaign
   */
  @PostMapping("/{id}/complete")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Complete campaign", description = "Marks a campaign as completed")
  public ResponseEntity<CampaignResponse> completeCampaign(
      @PathVariable Long id,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    CampaignResponse campaign = campaignService.completeCampaign(churchId, id);
    return ResponseEntity.ok(campaign);
  }

  /**
   * Cancel a campaign
   */
  @PostMapping("/{id}/cancel")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Cancel campaign", description = "Cancels a campaign")
  public ResponseEntity<CampaignResponse> cancelCampaign(
      @PathVariable Long id,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    CampaignResponse campaign = campaignService.cancelCampaign(churchId, id);
    return ResponseEntity.ok(campaign);
  }

  /**
   * Get campaign statistics
   */
  @GetMapping("/stats")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Get campaign statistics", description = "Returns campaign statistics for the church")
  public ResponseEntity<CampaignStatsResponse> getCampaignStats(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    CampaignStatsResponse stats = campaignService.getCampaignStats(churchId);
    return ResponseEntity.ok(stats);
  }

  /**
   * Update campaign progress (manual trigger)
   */
  @PostMapping("/{id}/update-progress")
  @PreAuthorize("isAuthenticated()")
  @Operation(summary = "Update campaign progress", description = "Manually triggers campaign progress update")
  public ResponseEntity<Void> updateCampaignProgress(@PathVariable Long id) {
    campaignService.updateCampaignProgress(id);
    return ResponseEntity.ok().build();
  }
}
