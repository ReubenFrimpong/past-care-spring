package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.AutoDetectedCareNeed;
import com.reuben.pastcare_spring.dtos.CareNeedRequest;
import com.reuben.pastcare_spring.dtos.CareNeedResponse;
import com.reuben.pastcare_spring.dtos.CareNeedStatsResponse;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.models.CareNeedStatus;
import com.reuben.pastcare_spring.models.CareNeedType;
import com.reuben.pastcare_spring.services.CareNeedService;
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
 * REST controller for care need management
 */
@RestController
@RequestMapping("/api/care-needs")
@RequiredArgsConstructor
@Tag(name = "Care Needs", description = "Pastoral care needs management endpoints")
public class CareNeedController {

    private final CareNeedService careNeedService;
    private final RequestContextUtil requestContextUtil;

    /**
     * Create a new care need
     */
    @PostMapping
    @RequirePermission(Permission.CARE_NEED_CREATE)
    @Operation(summary = "Create care need", description = "Creates a new pastoral care need")
    public ResponseEntity<CareNeedResponse> createCareNeed(
            @Valid @RequestBody CareNeedRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long currentUserId = requestContextUtil.extractUserId(httpRequest);
        CareNeedResponse careNeed = careNeedService.createCareNeed(churchId, request, currentUserId);
        return ResponseEntity.ok(careNeed);
    }

    /**
     * Get care need by ID
     */
    @GetMapping("/{id}")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get care need by ID", description = "Returns a single care need by ID")
    public ResponseEntity<CareNeedResponse> getCareNeedById(@PathVariable Long id) {
        CareNeedResponse careNeed = careNeedService.getCareNeedById(id);
        return ResponseEntity.ok(careNeed);
    }

    /**
     * Get all care needs with pagination
     */
    @GetMapping
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get all care needs", description = "Returns paginated list of care needs")
    public ResponseEntity<Page<CareNeedResponse>> getCareNeeds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CareNeedResponse> careNeeds = careNeedService.getCareNeeds(churchId, pageable);
        return ResponseEntity.ok(careNeeds);
    }

    /**
     * Update an existing care need
     */
    @PutMapping("/{id}")
    @RequirePermission(Permission.CARE_NEED_EDIT)
    @Operation(summary = "Update care need", description = "Updates an existing care need")
    public ResponseEntity<CareNeedResponse> updateCareNeed(
            @PathVariable Long id,
            @Valid @RequestBody CareNeedRequest request) {
        CareNeedResponse updated = careNeedService.updateCareNeed(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * Delete a care need
     */
    @DeleteMapping("/{id}")
    @RequirePermission(Permission.CARE_NEED_EDIT)
    @Operation(summary = "Delete care need", description = "Deletes a care need")
    public ResponseEntity<Void> deleteCareNeed(@PathVariable Long id) {
        careNeedService.deleteCareNeed(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Assign a care need to a user
     */
    @PatchMapping("/{id}/assign")
    @RequirePermission(Permission.CARE_NEED_EDIT)
    @Operation(summary = "Assign care need", description = "Assigns a care need to a user")
    public ResponseEntity<CareNeedResponse> assignCareNeed(
            @PathVariable Long id,
            @RequestBody Map<String, Long> payload) {
        Long userId = payload.get("userId");
        CareNeedResponse updated = careNeedService.assignCareNeed(id, userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * Update care need status
     */
    @PatchMapping("/{id}/status")
    @RequirePermission(Permission.CARE_NEED_EDIT)
    @Operation(summary = "Update status", description = "Updates the status of a care need")
    public ResponseEntity<CareNeedResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, CareNeedStatus> payload) {
        CareNeedStatus status = payload.get("status");
        CareNeedResponse updated = careNeedService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Mark a care need as resolved
     */
    @PatchMapping("/{id}/resolve")
    @RequirePermission(Permission.CARE_NEED_EDIT)
    @Operation(summary = "Resolve care need", description = "Marks a care need as resolved")
    public ResponseEntity<CareNeedResponse> resolveCareNeed(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String resolutionNotes = payload.get("resolutionNotes");
        CareNeedResponse updated = careNeedService.resolveCareNeed(id, resolutionNotes);
        return ResponseEntity.ok(updated);
    }

    /**
     * Get care needs by status
     */
    @GetMapping("/status/{status}")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get by status", description = "Returns care needs filtered by status")
    public ResponseEntity<Page<CareNeedResponse>> getCareNeedsByStatus(
            @PathVariable CareNeedStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CareNeedResponse> careNeeds = careNeedService.getCareNeedsByStatus(churchId, status, pageable);
        return ResponseEntity.ok(careNeeds);
    }

    /**
     * Get care needs by type
     */
    @GetMapping("/type/{type}")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get by type", description = "Returns care needs filtered by type")
    public ResponseEntity<Page<CareNeedResponse>> getCareNeedsByType(
            @PathVariable CareNeedType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CareNeedResponse> careNeeds = careNeedService.getCareNeedsByType(churchId, type, pageable);
        return ResponseEntity.ok(careNeeds);
    }

    /**
     * Get care needs assigned to a user
     */
    @GetMapping("/assigned/{userId}")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get assigned", description = "Returns care needs assigned to a specific user")
    public ResponseEntity<Page<CareNeedResponse>> getAssignedCareNeeds(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CareNeedResponse> careNeeds = careNeedService.getAssignedCareNeeds(churchId, userId, pageable);
        return ResponseEntity.ok(careNeeds);
    }

    /**
     * Search care needs
     */
    @GetMapping("/search")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Search care needs", description = "Searches care needs by title, description, or member name")
    public ResponseEntity<Page<CareNeedResponse>> searchCareNeeds(
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CareNeedResponse> careNeeds = careNeedService.searchCareNeeds(churchId, search, pageable);
        return ResponseEntity.ok(careNeeds);
    }

    /**
     * Get care need statistics
     */
    @GetMapping("/stats")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get statistics", description = "Returns care need statistics")
    public ResponseEntity<CareNeedStatsResponse> getCareNeedStats(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        CareNeedStatsResponse stats = careNeedService.getCareNeedStats(churchId);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get overdue care needs
     */
    @GetMapping("/overdue")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get overdue", description = "Returns overdue care needs")
    public ResponseEntity<List<CareNeedResponse>> getOverdueCareNeeds(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<CareNeedResponse> careNeeds = careNeedService.getOverdueCareNeeds(churchId);
        return ResponseEntity.ok(careNeeds);
    }

    /**
     * Get urgent care needs
     */
    @GetMapping("/urgent")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get urgent", description = "Returns urgent care needs")
    public ResponseEntity<List<CareNeedResponse>> getUrgentCareNeeds(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<CareNeedResponse> careNeeds = careNeedService.getUrgentCareNeeds(churchId);
        return ResponseEntity.ok(careNeeds);
    }

    /**
     * Get unassigned care needs
     */
    @GetMapping("/unassigned")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get unassigned", description = "Returns unassigned care needs")
    public ResponseEntity<List<CareNeedResponse>> getUnassignedCareNeeds(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<CareNeedResponse> careNeeds = careNeedService.getUnassignedCareNeeds(churchId);
        return ResponseEntity.ok(careNeeds);
    }

    /**
     * Get care needs for the current user (assigned to me)
     */
    @GetMapping("/assigned-to-me")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get my assigned care needs", description = "Returns care needs assigned to the current user")
    public ResponseEntity<List<CareNeedResponse>> getMyAssignedCareNeeds(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long userId = requestContextUtil.extractUserId(httpRequest);
        List<CareNeedResponse> careNeeds = careNeedService.getAssignedCareNeeds(churchId, userId, org.springframework.data.domain.Pageable.unpaged())
            .getContent();
        return ResponseEntity.ok(careNeeds);
    }

    /**
     * Get care needs by member
     */
    @GetMapping("/member/{memberId}")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Get by member", description = "Returns care needs for a specific member")
    public ResponseEntity<List<CareNeedResponse>> getCareNeedsByMember(
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<CareNeedResponse> careNeeds = careNeedService.getCareNeedsByMember(churchId, memberId);
        return ResponseEntity.ok(careNeeds);
    }

    /**
     * Auto-detect members needing care
     */
    @GetMapping("/detect-needs")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Detect needs", description = "Auto-detects members needing care based on attendance patterns")
    public ResponseEntity<List<Long>> detectMembersNeedingCare(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<Long> memberIds = careNeedService.detectMembersNeedingCare(churchId);
        return ResponseEntity.ok(memberIds);
    }

    /**
     * Get auto-detected care need suggestions
     */
    @GetMapping("/auto-detect")
    @RequirePermission(Permission.CARE_NEED_VIEW_ALL)
    @Operation(summary = "Auto-detect suggestions", description = "Returns detailed care need suggestions for members with irregular attendance")
    public ResponseEntity<List<AutoDetectedCareNeed>> getAutoDetectedCareNeeds(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<AutoDetectedCareNeed> suggestions = careNeedService.getAutoDetectedCareNeeds(churchId);
        return ResponseEntity.ok(suggestions);
    }
}
