package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.models.PartnershipCode;
import com.reuben.pastcare_spring.services.PartnershipCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-only endpoints for managing partnership codes (SUPERADMIN)
 */
@RestController
@RequestMapping("/api/admin/partnership-codes")
public class AdminPartnershipCodeController {

    @Autowired
    private PartnershipCodeService partnershipCodeService;

    /**
     * Get all partnership codes (SUPERADMIN only)
     *
     * GET /api/admin/partnership-codes
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @GetMapping
    public ResponseEntity<List<PartnershipCode>> getAllPartnershipCodes() {
        List<PartnershipCode> codes = partnershipCodeService.getAllCodes();
        return ResponseEntity.ok(codes);
    }

    /**
     * Get partnership code by ID (SUPERADMIN only)
     *
     * GET /api/admin/partnership-codes/{id}
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @GetMapping("/{id}")
    public ResponseEntity<PartnershipCode> getPartnershipCodeById(@PathVariable Long id) {
        PartnershipCode code = partnershipCodeService.getCodeById(id);
        return ResponseEntity.ok(code);
    }

    /**
     * Create new partnership code (SUPERADMIN only)
     *
     * POST /api/admin/partnership-codes
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @PostMapping
    public ResponseEntity<PartnershipCode> createPartnershipCode(
            @Valid @RequestBody PartnershipCode partnershipCode
    ) {
        PartnershipCode createdCode = partnershipCodeService.createCode(partnershipCode);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCode);
    }

    /**
     * Update existing partnership code (SUPERADMIN only)
     *
     * PUT /api/admin/partnership-codes/{id}
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @PutMapping("/{id}")
    public ResponseEntity<PartnershipCode> updatePartnershipCode(
            @PathVariable Long id,
            @Valid @RequestBody PartnershipCode partnershipCode
    ) {
        PartnershipCode updatedCode = partnershipCodeService.updateCode(id, partnershipCode);
        return ResponseEntity.ok(updatedCode);
    }

    /**
     * Deactivate partnership code (SUPERADMIN only)
     *
     * DELETE /api/admin/partnership-codes/{id}
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivatePartnershipCode(@PathVariable Long id) {
        partnershipCodeService.deactivateCode(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get code usage statistics (SUPERADMIN only)
     *
     * GET /api/admin/partnership-codes/{id}/stats
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @GetMapping("/{id}/stats")
    public ResponseEntity<?> getCodeStats(@PathVariable Long id) {
        var stats = partnershipCodeService.getCodeStats(id);
        return ResponseEntity.ok(stats);
    }
}
