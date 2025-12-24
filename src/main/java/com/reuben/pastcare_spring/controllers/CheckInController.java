package com.reuben.pastcare_spring.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.reuben.pastcare_spring.dtos.CheckInRequest;
import com.reuben.pastcare_spring.dtos.CheckInResponse;
import com.reuben.pastcare_spring.dtos.NearbySessionResponse;
import com.reuben.pastcare_spring.services.CheckInService;

import jakarta.validation.Valid;

/**
 * REST controller for check-in operations.
 * Phase 1: Enhanced Attendance Tracking
 *
 * Endpoints:
 * - POST /api/check-in - Process check-in for member or visitor
 * - GET /api/check-in/nearby-sessions - Find sessions near a location
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
@RestController
@RequestMapping("/api/check-in")
public class CheckInController {

  private final CheckInService checkInService;

  public CheckInController(CheckInService checkInService) {
    this.checkInService = checkInService;
  }

  /**
   * Process a check-in request.
   *
   * Supports multiple check-in methods:
   * - MANUAL: Direct manual check-in by admin
   * - QR_CODE: Scan QR code at session
   * - GEOFENCE: Automatic location-based check-in
   * - MOBILE_APP: Check-in via mobile app
   * - SELF_CHECKIN: Member self-check-in
   *
   * @param request The check-in request
   * @return CheckInResponse with attendance details
   */
  @PostMapping
  public ResponseEntity<CheckInResponse> checkIn(@Valid @RequestBody CheckInRequest request) {
    return ResponseEntity.ok(checkInService.checkIn(request));
  }

  /**
   * Find sessions near a user's location.
   *
   * Returns active sessions within the specified search radius,
   * ordered by distance (closest first).
   *
   * @param latitude User's current latitude
   * @param longitude User's current longitude
   * @param maxDistanceMeters Maximum search distance in meters (default 5000m)
   * @return List of nearby sessions with distance information
   */
  @GetMapping("/nearby-sessions")
  public ResponseEntity<List<NearbySessionResponse>> findNearbySessions(
      @RequestParam double latitude,
      @RequestParam double longitude,
      @RequestParam(required = false) Integer maxDistanceMeters) {
    return ResponseEntity.ok(checkInService.findNearbySessions(latitude, longitude, maxDistanceMeters));
  }
}
