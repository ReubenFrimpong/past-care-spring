package com.reuben.pastcare_spring.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.ConvertVisitorRequest;
import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.dtos.VisitorRequest;
import com.reuben.pastcare_spring.dtos.VisitorResponse;
import com.reuben.pastcare_spring.services.VisitorService;

import jakarta.validation.Valid;

/**
 * REST controller for visitor management.
 * Phase 1: Enhanced Attendance Tracking - Visitor Management
 *
 * Endpoints:
 * - POST /api/visitors - Create new visitor
 * - GET /api/visitors - Get all visitors
 * - GET /api/visitors/{id} - Get visitor by ID
 * - PUT /api/visitors/{id} - Update visitor
 * - DELETE /api/visitors/{id} - Delete visitor
 * - POST /api/visitors/{id}/record-visit - Record a visit
 * - GET /api/visitors/first-time - Get first-time visitors
 * - GET /api/visitors/non-converted - Get visitors not yet converted to members
 * - POST /api/visitors/convert - Convert visitor to member
 * - GET /api/visitors/by-phone - Get visitor by phone number
 * - GET /api/visitors/by-email - Get visitor by email
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

  private final VisitorService visitorService;

  public VisitorController(VisitorService visitorService) {
    this.visitorService = visitorService;
  }

  /**
   * Create a new visitor.
   */
  @PostMapping
  public ResponseEntity<VisitorResponse> createVisitor(@Valid @RequestBody VisitorRequest request) {
    return ResponseEntity.ok(visitorService.createVisitor(request));
  }

  /**
   * Get all visitors.
   */
  @GetMapping
  public ResponseEntity<List<VisitorResponse>> getAllVisitors() {
    return ResponseEntity.ok(visitorService.getAllVisitors());
  }

  /**
   * Get visitor by ID.
   */
  @GetMapping("/{id}")
  public ResponseEntity<VisitorResponse> getVisitorById(@PathVariable Long id) {
    return ResponseEntity.ok(visitorService.getVisitorById(id));
  }

  /**
   * Update visitor information.
   */
  @PutMapping("/{id}")
  public ResponseEntity<VisitorResponse> updateVisitor(
      @PathVariable Long id,
      @Valid @RequestBody VisitorRequest request) {
    return ResponseEntity.ok(visitorService.updateVisitor(id, request));
  }

  /**
   * Delete a visitor.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteVisitor(@PathVariable Long id) {
    visitorService.deleteVisitor(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * Record a visit for a visitor (increments visit count).
   */
  @PostMapping("/{id}/record-visit")
  public ResponseEntity<VisitorResponse> recordVisit(@PathVariable Long id) {
    return ResponseEntity.ok(visitorService.recordVisit(id));
  }

  /**
   * Get all first-time visitors.
   */
  @GetMapping("/first-time")
  public ResponseEntity<List<VisitorResponse>> getFirstTimeVisitors() {
    return ResponseEntity.ok(visitorService.getFirstTimeVisitors());
  }

  /**
   * Get all visitors who have not been converted to members.
   */
  @GetMapping("/non-converted")
  public ResponseEntity<List<VisitorResponse>> getNonConvertedVisitors() {
    return ResponseEntity.ok(visitorService.getNonConvertedVisitors());
  }

  /**
   * Convert a visitor to a member.
   */
  @PostMapping("/convert")
  public ResponseEntity<MemberResponse> convertVisitorToMember(@Valid @RequestBody ConvertVisitorRequest request) {
    return ResponseEntity.ok(visitorService.convertVisitorToMember(request));
  }

  /**
   * Get visitor by phone number.
   */
  @GetMapping("/by-phone")
  public ResponseEntity<VisitorResponse> getVisitorByPhoneNumber(@RequestParam String phoneNumber) {
    return ResponseEntity.ok(visitorService.getVisitorByPhoneNumber(phoneNumber));
  }

  /**
   * Get visitor by email.
   */
  @GetMapping("/by-email")
  public ResponseEntity<VisitorResponse> getVisitorByEmail(@RequestParam String email) {
    return ResponseEntity.ok(visitorService.getVisitorByEmail(email));
  }
}
