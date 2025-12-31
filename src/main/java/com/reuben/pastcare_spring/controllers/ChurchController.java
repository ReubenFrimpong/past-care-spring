package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.services.ChurchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for managing church profile and settings
 */
@RestController
@RequestMapping("/api/churches")
@RequiredArgsConstructor
public class ChurchController {

    private final ChurchService churchService;

    /**
     * Get church by ID
     */
    @GetMapping("/{id}")
    @RequirePermission(Permission.CHURCH_SETTINGS_VIEW)
    public ResponseEntity<Church> getChurch(@PathVariable Long id) {
        Church church = churchService.getChurchById(id);
        return ResponseEntity.ok(church);
    }

    /**
     * Update church profile
     */
    @PutMapping("/{id}")
    @RequirePermission(Permission.CHURCH_SETTINGS_EDIT)
    public ResponseEntity<Church> updateChurch(
            @PathVariable Long id,
            @RequestBody Church churchRequest) {
        Church updatedChurch = churchService.updateChurch(id, churchRequest);
        return ResponseEntity.ok(updatedChurch);
    }

    /**
     * Upload church logo
     */
    @PostMapping("/{id}/logo")
    @RequirePermission(Permission.CHURCH_SETTINGS_EDIT)
    public ResponseEntity<Map<String, String>> uploadLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {

        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Validate file size (max 2MB)
        long maxSize = 2 * 1024 * 1024; // 2MB in bytes
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size must not exceed 2MB");
        }

        // Upload logo and update church
        String logoUrl = churchService.uploadLogo(id, file);

        Map<String, String> response = new HashMap<>();
        response.put("logoUrl", logoUrl);
        response.put("message", "Logo uploaded successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Delete church logo
     */
    @DeleteMapping("/{id}/logo")
    @RequirePermission(Permission.CHURCH_SETTINGS_EDIT)
    public ResponseEntity<Map<String, String>> deleteLogo(@PathVariable Long id) {
        churchService.deleteLogo(id);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Logo deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Get church logo (public endpoint for landing page and favicon)
     * Returns the church logo URL if available
     */
    @GetMapping("/public/logo")
    public ResponseEntity<Map<String, String>> getChurchLogo() {
        // For now, get the first active church
        // In a multi-tenant setup, you might want to use domain-based routing
        String logoUrl = churchService.getFirstActiveChurchLogoUrl();

        Map<String, String> response = new HashMap<>();
        if (logoUrl != null && !logoUrl.isEmpty()) {
            response.put("logoUrl", logoUrl);
        } else {
            response.put("logoUrl", null);
        }

        return ResponseEntity.ok(response);
    }
}
