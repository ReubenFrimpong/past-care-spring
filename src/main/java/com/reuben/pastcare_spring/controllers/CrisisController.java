package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.CrisisSeverity;
import com.reuben.pastcare_spring.models.CrisisStatus;
import com.reuben.pastcare_spring.models.CrisisType;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.services.CrisisService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for crisis and emergency management
 */
@RestController
@RequestMapping("/api/crises")
@RequiredArgsConstructor
@Tag(name = "Crises", description = "Crisis and emergency management endpoints")
public class CrisisController {

    private final CrisisService crisisService;
    private final RequestContextUtil requestContextUtil;

    /**
     * Report a new crisis
     */
    @PostMapping
    @RequirePermission(Permission.VISIT_CREATE)
    @Operation(summary = "Report crisis", description = "Reports a new crisis or emergency situation")
    public ResponseEntity<CrisisResponse> reportCrisis(
            @Valid @RequestBody CrisisRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long currentUserId = requestContextUtil.extractUserId(httpRequest);
        CrisisResponse crisis = crisisService.reportCrisis(churchId, request, currentUserId);
        return ResponseEntity.ok(crisis);
    }

    /**
     * Get crisis by ID
     */
    @GetMapping("/{id}")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get crisis by ID", description = "Returns a single crisis by ID")
    public ResponseEntity<CrisisResponse> getCrisisById(@PathVariable Long id) {
        CrisisResponse crisis = crisisService.getCrisisById(id);
        return ResponseEntity.ok(crisis);
    }

    /**
     * Get all crises with pagination
     */
    @GetMapping
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get all crises", description = "Returns paginated list of crises")
    public ResponseEntity<Page<CrisisResponse>> getCrises(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "reportedDate") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CrisisResponse> crises = crisisService.getCrises(churchId, pageable);
        return ResponseEntity.ok(crises);
    }

    /**
     * Update an existing crisis
     */
    @PutMapping("/{id}")
    @RequirePermission(Permission.VISIT_EDIT)
    @Operation(summary = "Update crisis", description = "Updates an existing crisis")
    public ResponseEntity<CrisisResponse> updateCrisis(
            @PathVariable Long id,
            @Valid @RequestBody CrisisRequest request) {
        CrisisResponse updated = crisisService.updateCrisis(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a crisis
     */
    @DeleteMapping("/{id}")
    @RequirePermission(Permission.VISIT_EDIT)
    @Operation(summary = "Delete crisis", description = "Deletes a crisis")
    public ResponseEntity<Void> deleteCrisis(@PathVariable Long id) {
        crisisService.deleteCrisis(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add affected member to crisis
     */
    @PostMapping("/{id}/affected-members")
    @RequirePermission(Permission.VISIT_CREATE)
    @Operation(summary = "Add affected member", description = "Adds an affected member to a crisis")
    public ResponseEntity<CrisisAffectedMemberResponse> addAffectedMember(
            @PathVariable Long id,
            @Valid @RequestBody CrisisAffectedMemberRequest request) {
        CrisisAffectedMemberResponse affectedMember = crisisService.addAffectedMember(id, request);
        return ResponseEntity.ok(affectedMember);
    }

    /**
     * Bulk add affected members to crisis
     */
    @PostMapping("/{id}/affected-members/bulk")
    @RequirePermission(Permission.VISIT_CREATE)
    @Operation(summary = "Bulk add affected members", description = "Adds multiple affected members to a crisis at once")
    public ResponseEntity<List<CrisisAffectedMemberResponse>> bulkAddAffectedMembers(
            @PathVariable Long id,
            @Valid @RequestBody BulkCrisisAffectedMembersRequest request) {
        List<CrisisAffectedMemberResponse> affectedMembers = crisisService.bulkAddAffectedMembers(id, request);
        return ResponseEntity.ok(affectedMembers);
    }

    /**
     * Remove affected member from crisis
     */
    @DeleteMapping("/{id}/affected-members/{memberId}")
    @RequirePermission(Permission.VISIT_EDIT)
    @Operation(summary = "Remove affected member", description = "Removes an affected member from a crisis")
    public ResponseEntity<Void> removeAffectedMember(
            @PathVariable Long id,
            @PathVariable Long memberId) {
        crisisService.removeAffectedMember(id, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mobilize resources for a crisis
     */
    @PostMapping("/{id}/mobilize")
    @RequirePermission(Permission.VISIT_CREATE)
    @Operation(summary = "Mobilize resources", description = "Mobilizes resources for a crisis response")
    public ResponseEntity<CrisisResponse> mobilizeResources(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String resources = payload.get("resources");
        CrisisResponse updated = crisisService.mobilizeResources(id, resources);
        return ResponseEntity.ok(updated);
    }

    /**
     * Send emergency notifications
     */
    @PostMapping("/{id}/notify")
    @RequirePermission(Permission.VISIT_CREATE)
    @Operation(summary = "Send notifications", description = "Sends emergency notifications for a crisis")
    public ResponseEntity<CrisisResponse> sendEmergencyNotifications(@PathVariable Long id) {
        CrisisResponse updated = crisisService.sendEmergencyNotifications(id);
        return ResponseEntity.ok(updated);
    }

    /**
     * Resolve a crisis
     */
    @PostMapping("/{id}/resolve")
    @RequirePermission(Permission.VISIT_CREATE)
    @Operation(summary = "Resolve crisis", description = "Marks a crisis as resolved")
    public ResponseEntity<CrisisResponse> resolveCrisis(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String resolutionNotes = payload.get("resolutionNotes");
        CrisisResponse updated = crisisService.resolveCrisis(id, resolutionNotes);
        return ResponseEntity.ok(updated);
    }

    /**
     * Update crisis status
     */
    @PatchMapping("/{id}/status")
    @RequirePermission(Permission.VISIT_EDIT)
    @Operation(summary = "Update status", description = "Updates the status of a crisis")
    public ResponseEntity<CrisisResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, CrisisStatus> payload) {
        CrisisStatus status = payload.get("status");
        CrisisResponse updated = crisisService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get active crises
     */
    @GetMapping("/active")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get active crises", description = "Returns all active crises")
    public ResponseEntity<List<CrisisResponse>> getActiveCrises(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<CrisisResponse> crises = crisisService.getActiveCrises(churchId);
        return ResponseEntity.ok(crises);
    }

    /**
     * Get critical crises
     */
    @GetMapping("/critical")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get critical crises", description = "Returns all critical severity crises")
    public ResponseEntity<List<CrisisResponse>> getCriticalCrises(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<CrisisResponse> crises = crisisService.getCriticalCrises(churchId);
        return ResponseEntity.ok(crises);
    }

    /**
     * Get crises by status
     */
    @GetMapping("/status/{status}")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get by status", description = "Returns crises filtered by status")
    public ResponseEntity<Page<CrisisResponse>> getCrisesByStatus(
            @PathVariable CrisisStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportedDate").descending());
        Page<CrisisResponse> crises = crisisService.getCrisesByStatus(churchId, status, pageable);
        return ResponseEntity.ok(crises);
    }

    /**
     * Get crises by type
     */
    @GetMapping("/type/{type}")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get by type", description = "Returns crises filtered by type")
    public ResponseEntity<Page<CrisisResponse>> getCrisesByType(
            @PathVariable CrisisType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportedDate").descending());
        Page<CrisisResponse> crises = crisisService.getCrisesByType(churchId, type, pageable);
        return ResponseEntity.ok(crises);
    }

    /**
     * Get crises by severity
     */
    @GetMapping("/severity/{severity}")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get by severity", description = "Returns crises filtered by severity")
    public ResponseEntity<Page<CrisisResponse>> getCrisesBySeverity(
            @PathVariable CrisisSeverity severity,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportedDate").descending());
        Page<CrisisResponse> crises = crisisService.getCrisesBySeverity(churchId, severity, pageable);
        return ResponseEntity.ok(crises);
    }

    /**
     * Search crises
     */
    @GetMapping("/search")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Search crises", description = "Searches crises by title, description, or location")
    public ResponseEntity<Page<CrisisResponse>> searchCrises(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("reportedDate").descending());
        Page<CrisisResponse> crises = crisisService.searchCrises(churchId, search, pageable);
        return ResponseEntity.ok(crises);
    }

    /**
     * Get crisis statistics
     */
    @GetMapping("/stats")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Get statistics", description = "Returns crisis statistics")
    public ResponseEntity<CrisisStatsResponse> getCrisisStats(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        CrisisStatsResponse stats = crisisService.getCrisisStats(churchId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Auto-detect and add affected members based on geographic location
     */
    @PostMapping("/{id}/auto-detect-members")
    @RequirePermission(Permission.VISIT_CREATE)
    @Operation(summary = "Auto-detect affected members", description = "Automatically detects and adds members in the affected geographic area")
    public ResponseEntity<List<Member>> autoDetectAffectedMembers(@PathVariable Long id) {
        List<Member> affectedMembers = crisisService.autoDetectAffectedMembers(id);
        return ResponseEntity.ok(affectedMembers);
    }

    /**
     * Preview members that would be affected by geographic criteria (without adding them)
     */
    @GetMapping("/preview-affected-members")
    @RequirePermission(Permission.VISIT_VIEW_ALL)
    @Operation(summary = "Preview affected members", description = "Preview which members would be affected based on geographic criteria")
    public ResponseEntity<List<Member>> previewAffectedMembers(
            @RequestParam(required = false) String suburb,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String countryCode) {
        List<Member> members = crisisService.previewAffectedMembers(suburb, city, district, region, countryCode);
        return ResponseEntity.ok(members);
    }
}
