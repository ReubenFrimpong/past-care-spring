package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.DonationRequest;
import com.reuben.pastcare_spring.dtos.DonationResponse;
import com.reuben.pastcare_spring.dtos.DonationSummaryResponse;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.models.DonationType;
import com.reuben.pastcare_spring.services.DonationService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Giving Module Phase 1: Donation Recording
 * REST API for managing donations
 */
@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
@Tag(name = "Donations", description = "Donation management endpoints")
public class DonationController {

  private final DonationService donationService;
  private final RequestContextUtil requestContextUtil;

  /**
   * Get all donations for the current church
   */
  @GetMapping
  @RequirePermission({Permission.DONATION_VIEW_ALL, Permission.DONATION_VIEW_OWN})
  @Operation(summary = "Get all donations", description = "Returns all donations for the current church")
  public ResponseEntity<List<DonationResponse>> getAllDonations(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<DonationResponse> donations = donationService.getAllDonations(churchId);
    return ResponseEntity.ok(donations);
  }

  /**
   * Get donation by ID
   */
  @GetMapping("/{id}")
  @RequirePermission({Permission.DONATION_VIEW_ALL, Permission.DONATION_VIEW_OWN})
  @Operation(summary = "Get donation by ID", description = "Returns a single donation by ID")
  public ResponseEntity<DonationResponse> getDonationById(@PathVariable Long id) {
    DonationResponse donation = donationService.getDonationById(id);
    return ResponseEntity.ok(donation);
  }

  /**
   * Create a new donation
   */
  @PostMapping
  @RequirePermission(Permission.DONATION_CREATE)
  @Operation(summary = "Create donation", description = "Creates a new donation record")
  public ResponseEntity<DonationResponse> createDonation(
      @Valid @RequestBody DonationRequest request,
      HttpServletRequest httpRequest
  ) {
    Long churchId = requestContextUtil.extractChurchId(httpRequest);
    Long userId = requestContextUtil.extractUserId(httpRequest);
    DonationResponse createdDonation = donationService.createDonation(churchId, userId, request);
    return ResponseEntity.ok(createdDonation);
  }

  /**
   * Update an existing donation
   */
  @PutMapping("/{id}")
  @RequirePermission(Permission.DONATION_EDIT)
  @Operation(summary = "Update donation", description = "Updates an existing donation record")
  public ResponseEntity<DonationResponse> updateDonation(
      @PathVariable Long id,
      @Valid @RequestBody DonationRequest request
  ) {
    DonationResponse updatedDonation = donationService.updateDonation(id, request);
    return ResponseEntity.ok(updatedDonation);
  }

  /**
   * Delete a donation
   */
  @DeleteMapping("/{id}")
  @RequirePermission(Permission.DONATION_DELETE)
  @Operation(summary = "Delete donation", description = "Deletes a donation record")
  public ResponseEntity<Void> deleteDonation(@PathVariable Long id) {
    donationService.deleteDonation(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Get donations by date range
   */
  @GetMapping("/date-range")
  @RequirePermission({Permission.DONATION_VIEW_ALL, Permission.DONATION_VIEW_OWN})
  @Operation(summary = "Get donations by date range", description = "Returns donations within a date range")
  public ResponseEntity<List<DonationResponse>> getDonationsByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<DonationResponse> donations = donationService.getDonationsByDateRange(churchId, startDate, endDate);
    return ResponseEntity.ok(donations);
  }

  /**
   * Get donations by member
   */
  @GetMapping("/member/{memberId}")
  @RequirePermission({Permission.DONATION_VIEW_ALL, Permission.DONATION_VIEW_OWN})
  @Operation(summary = "Get donations by member", description = "Returns all donations for a specific member")
  public ResponseEntity<List<DonationResponse>> getDonationsByMember(
      @PathVariable Long memberId,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<DonationResponse> donations = donationService.getDonationsByMember(churchId, memberId);
    return ResponseEntity.ok(donations);
  }

  /**
   * Get donations by type
   */
  @GetMapping("/type/{donationType}")
  @RequirePermission({Permission.DONATION_VIEW_ALL, Permission.DONATION_VIEW_OWN})
  @Operation(summary = "Get donations by type", description = "Returns all donations of a specific type")
  public ResponseEntity<List<DonationResponse>> getDonationsByType(
      @PathVariable DonationType donationType,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<DonationResponse> donations = donationService.getDonationsByType(churchId, donationType);
    return ResponseEntity.ok(donations);
  }

  /**
   * Get donations by campaign
   */
  @GetMapping("/campaign/{campaign}")
  @RequirePermission({Permission.DONATION_VIEW_ALL, Permission.CAMPAIGN_VIEW})
  @Operation(summary = "Get donations by campaign", description = "Returns all donations for a specific campaign")
  public ResponseEntity<List<DonationResponse>> getDonationsByCampaign(
      @PathVariable String campaign,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    List<DonationResponse> donations = donationService.getDonationsByCampaign(churchId, campaign);
    return ResponseEntity.ok(donations);
  }

  /**
   * Get donation summary
   */
  @GetMapping("/summary")
  @RequirePermission(Permission.DONATION_VIEW_ALL)
  @Operation(summary = "Get donation summary", description = "Returns summary statistics for all donations")
  public ResponseEntity<DonationSummaryResponse> getDonationSummary(HttpServletRequest request) {
    Long churchId = requestContextUtil.extractChurchId(request);
    DonationSummaryResponse summary = donationService.getDonationSummary(churchId);
    return ResponseEntity.ok(summary);
  }

  /**
   * Get donation summary by date range
   */
  @GetMapping("/summary/date-range")
  @RequirePermission(Permission.DONATION_VIEW_ALL)
  @Operation(summary = "Get donation summary by date range", description = "Returns summary statistics for donations within a date range")
  public ResponseEntity<DonationSummaryResponse> getDonationSummaryByDateRange(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
      HttpServletRequest request
  ) {
    Long churchId = requestContextUtil.extractChurchId(request);
    DonationSummaryResponse summary = donationService.getDonationSummaryByDateRange(churchId, startDate, endDate);
    return ResponseEntity.ok(summary);
  }

  /**
   * Issue receipt for a donation
   */
  @PostMapping("/{id}/issue-receipt")
  @RequirePermission(Permission.RECEIPT_ISSUE)
  @Operation(summary = "Issue receipt", description = "Issues a receipt for a donation")
  public ResponseEntity<DonationResponse> issueReceipt(
      @PathVariable Long id,
      @RequestParam String receiptNumber
  ) {
    DonationResponse donation = donationService.issueReceipt(id, receiptNumber);
    return ResponseEntity.ok(donation);
  }
}
