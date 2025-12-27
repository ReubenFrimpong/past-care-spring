package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.PrayerRequestRequest;
import com.reuben.pastcare_spring.dtos.PrayerRequestResponse;
import com.reuben.pastcare_spring.dtos.PrayerRequestStatsResponse;
import com.reuben.pastcare_spring.models.PrayerCategory;
import com.reuben.pastcare_spring.models.PrayerRequestStatus;
import com.reuben.pastcare_spring.services.PrayerRequestService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for prayer request management
 */
@RestController
@RequestMapping("/api/prayer-requests")
@RequiredArgsConstructor
@Tag(name = "Prayer Requests", description = "Prayer request management endpoints")
public class PrayerRequestController {

    private final PrayerRequestService prayerRequestService;
    private final RequestContextUtil requestContextUtil;

    /**
     * Submit a new prayer request
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Submit prayer request", description = "Submits a new prayer request")
    public ResponseEntity<PrayerRequestResponse> createPrayerRequest(
            @Valid @RequestBody PrayerRequestRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long currentUserId = requestContextUtil.extractUserId(httpRequest);
        PrayerRequestResponse prayerRequest = prayerRequestService.createPrayerRequest(churchId, request, currentUserId);
        return ResponseEntity.ok(prayerRequest);
    }

    /**
     * Get prayer request by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get prayer request by ID", description = "Returns a single prayer request by ID")
    public ResponseEntity<PrayerRequestResponse> getPrayerRequestById(@PathVariable Long id) {
        PrayerRequestResponse prayerRequest = prayerRequestService.getPrayerRequestById(id);
        return ResponseEntity.ok(prayerRequest);
    }

    /**
     * Get all prayer requests with pagination
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all prayer requests", description = "Returns paginated list of prayer requests")
    public ResponseEntity<Page<PrayerRequestResponse>> getPrayerRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PrayerRequestResponse> prayerRequests = prayerRequestService.getPrayerRequests(churchId, pageable);
        return ResponseEntity.ok(prayerRequests);
    }

    /**
     * Update an existing prayer request
     */
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update prayer request", description = "Updates an existing prayer request")
    public ResponseEntity<PrayerRequestResponse> updatePrayerRequest(
            @PathVariable Long id,
            @Valid @RequestBody PrayerRequestRequest request) {
        PrayerRequestResponse updated = prayerRequestService.updatePrayerRequest(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a prayer request
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete prayer request", description = "Deletes a prayer request")
    public ResponseEntity<Void> deletePrayerRequest(@PathVariable Long id) {
        prayerRequestService.deletePrayerRequest(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Increment prayer count (when someone prays)
     */
    @PostMapping("/{id}/pray")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark as prayed", description = "Increments the prayer count for a request")
    public ResponseEntity<PrayerRequestResponse> incrementPrayerCount(@PathVariable Long id) {
        PrayerRequestResponse updated = prayerRequestService.incrementPrayerCount(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Mark prayer request as answered with testimony
     */
    @PostMapping("/{id}/answer")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Mark as answered", description = "Marks a prayer request as answered with testimony")
    public ResponseEntity<PrayerRequestResponse> markAsAnswered(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String testimony = payload.get("testimony");
        PrayerRequestResponse updated = prayerRequestService.markAsAnswered(id, testimony);
        return ResponseEntity.ok(updated);
    }

    /**
     * Archive a prayer request
     */
    @PostMapping("/{id}/archive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Archive prayer request", description = "Archives/expires a prayer request")
    public ResponseEntity<PrayerRequestResponse> archivePrayerRequest(@PathVariable Long id) {
        PrayerRequestResponse updated = prayerRequestService.archivePrayerRequest(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get active prayer requests
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get active prayers", description = "Returns active public prayer requests")
    public ResponseEntity<List<PrayerRequestResponse>> getActivePrayerRequests(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<PrayerRequestResponse> prayerRequests = prayerRequestService.getActivePrayerRequests(churchId);
        return ResponseEntity.ok(prayerRequests);
    }

    /**
     * Get urgent prayer requests
     */
    @GetMapping("/urgent")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get urgent prayers", description = "Returns urgent prayer requests")
    public ResponseEntity<List<PrayerRequestResponse>> getUrgentPrayerRequests(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<PrayerRequestResponse> prayerRequests = prayerRequestService.getUrgentPrayerRequests(churchId);
        return ResponseEntity.ok(prayerRequests);
    }

    /**
     * Get prayer requests submitted by current user
     */
    @GetMapping("/my-requests")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my requests", description = "Returns prayer requests submitted by the current user")
    public ResponseEntity<Page<PrayerRequestResponse>> getMyPrayerRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PrayerRequestResponse> prayerRequests = prayerRequestService.getMyPrayerRequests(churchId, userId, pageable);
        return ResponseEntity.ok(prayerRequests);
    }

    /**
     * Get prayer requests by status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get by status", description = "Returns prayer requests filtered by status")
    public ResponseEntity<Page<PrayerRequestResponse>> getPrayerRequestsByStatus(
            @PathVariable PrayerRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PrayerRequestResponse> prayerRequests = prayerRequestService.getPrayerRequestsByStatus(churchId, status, pageable);
        return ResponseEntity.ok(prayerRequests);
    }

    /**
     * Get prayer requests by category
     */
    @GetMapping("/category/{category}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get by category", description = "Returns prayer requests filtered by category")
    public ResponseEntity<Page<PrayerRequestResponse>> getPrayerRequestsByCategory(
            @PathVariable PrayerCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PrayerRequestResponse> prayerRequests = prayerRequestService.getPrayerRequestsByCategory(churchId, category, pageable);
        return ResponseEntity.ok(prayerRequests);
    }

    /**
     * Get answered prayer requests with testimonies
     */
    @GetMapping("/answered")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get answered prayers", description = "Returns answered prayer requests with testimonies")
    public ResponseEntity<Page<PrayerRequestResponse>> getAnsweredPrayerRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("answeredDate").descending());
        Page<PrayerRequestResponse> prayerRequests = prayerRequestService.getAnsweredPrayerRequests(churchId, pageable);
        return ResponseEntity.ok(prayerRequests);
    }

    /**
     * Get public prayer requests (for member portal)
     */
    @GetMapping("/public")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get public prayers", description = "Returns public prayer requests for member portal")
    public ResponseEntity<Page<PrayerRequestResponse>> getPublicPrayerRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size);
        Page<PrayerRequestResponse> prayerRequests = prayerRequestService.getPublicPrayerRequests(churchId, pageable);
        return ResponseEntity.ok(prayerRequests);
    }

    /**
     * Search prayer requests
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Search prayer requests", description = "Searches prayer requests by title, description, or tags")
    public ResponseEntity<Page<PrayerRequestResponse>> searchPrayerRequests(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<PrayerRequestResponse> prayerRequests = prayerRequestService.searchPrayerRequests(churchId, search, pageable);
        return ResponseEntity.ok(prayerRequests);
    }

    /**
     * Get prayer request statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get statistics", description = "Returns prayer request statistics")
    public ResponseEntity<PrayerRequestStatsResponse> getPrayerRequestStats(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        PrayerRequestStatsResponse stats = prayerRequestService.getPrayerRequestStats(churchId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get prayer requests expiring soon
     */
    @GetMapping("/expiring-soon")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get expiring soon", description = "Returns prayer requests expiring within next 7 days")
    public ResponseEntity<List<PrayerRequestResponse>> getExpiringSoon(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<PrayerRequestResponse> prayerRequests = prayerRequestService.getExpiringSoon(churchId);
        return ResponseEntity.ok(prayerRequests);
    }

    /**
     * Auto-archive expired prayer requests
     */
    @PostMapping("/auto-archive")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Auto-archive expired", description = "Automatically archives expired prayer requests")
    public ResponseEntity<Map<String, Integer>> autoArchiveExpiredRequests(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        int archivedCount = prayerRequestService.autoArchiveExpiredRequests(churchId);
        return ResponseEntity.ok(Map.of("archivedCount", archivedCount));
    }
}
