package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.HouseholdRequest;
import com.reuben.pastcare_spring.dtos.HouseholdResponse;
import com.reuben.pastcare_spring.dtos.HouseholdStatsResponse;
import com.reuben.pastcare_spring.dtos.HouseholdSummaryResponse;
import com.reuben.pastcare_spring.services.HouseholdService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for household management operations
 */
@RestController
@RequestMapping("/api/households")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class HouseholdController {

    @Autowired
    private HouseholdService householdService;

    @Autowired
    private RequestContextUtil requestContextUtil;

    /**
     * Create a new household
     * POST /api/households
     */
    @PostMapping
    public ResponseEntity<HouseholdResponse> createHousehold(
            @Valid @RequestBody HouseholdRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        HouseholdResponse response = householdService.createHousehold(churchId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Update an existing household
     * PUT /api/households/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<HouseholdResponse> updateHousehold(
            @PathVariable Long id,
            @Valid @RequestBody HouseholdRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        HouseholdResponse response = householdService.updateHousehold(churchId, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get household by ID
     * GET /api/households/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<HouseholdResponse> getHouseholdById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        HouseholdResponse response = householdService.getHouseholdById(churchId, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all households with pagination and optional search
     * GET /api/households
     * Query params:
     * - page (default: 0)
     * - size (default: 20)
     * - sort (default: householdName,asc)
     * - search (optional)
     */
    @GetMapping
    public ResponseEntity<Page<HouseholdSummaryResponse>> getAllHouseholds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "householdName,asc") String[] sort,
            @RequestParam(required = false) String search,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);

        // Parse sort parameters
        String sortField = sort[0];
        String sortDirection = sort.length > 1 ? sort[1] : "asc";
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        Page<HouseholdSummaryResponse> households;
        if (search != null && !search.trim().isEmpty()) {
            households = householdService.searchHouseholds(churchId, search, pageable);
        } else {
            households = householdService.getAllHouseholds(churchId, pageable);
        }

        return ResponseEntity.ok(households);
    }

    /**
     * Delete household
     * DELETE /api/households/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHousehold(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        householdService.deleteHousehold(churchId, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add member to household
     * POST /api/households/{id}/members/{memberId}
     */
    @PostMapping("/{id}/members/{memberId}")
    public ResponseEntity<HouseholdResponse> addMemberToHousehold(
            @PathVariable Long id,
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        HouseholdResponse response = householdService.addMemberToHousehold(churchId, id, memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * Remove member from household
     * DELETE /api/households/{id}/members/{memberId}
     */
    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<HouseholdResponse> removeMemberFromHousehold(
            @PathVariable Long id,
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        HouseholdResponse response = householdService.removeMemberFromHousehold(churchId, id, memberId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get household statistics for the church
     * GET /api/households/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<HouseholdStatsResponse> getHouseholdStats(
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        HouseholdStatsResponse stats = householdService.getHouseholdStats(churchId);
        return ResponseEntity.ok(stats);
    }
}
