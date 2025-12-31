package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.models.StorageAddon;
import com.reuben.pastcare_spring.repositories.StorageAddonRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class StorageAddonController {

    @Autowired
    private StorageAddonRepository storageAddonRepository;

    // ============================================================================
    // PUBLIC ENDPOINTS - Available to all authenticated users
    // ============================================================================

    /**
     * Get all active storage add-ons (for billing page)
     * GET /api/storage-addons
     */
    @GetMapping("/storage-addons")
    public ResponseEntity<List<StorageAddon>> getActiveStorageAddons() {
        List<StorageAddon> addons = storageAddonRepository.findByIsActiveTrueOrderByDisplayOrderAsc();
        return ResponseEntity.ok(addons);
    }

    // ============================================================================
    // ADMIN ENDPOINTS - SUPERADMIN only
    // ============================================================================

    /**
     * Get all storage add-ons (including inactive) - SUPERADMIN only
     * GET /api/admin/storage-addons
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @GetMapping("/admin/storage-addons")
    public ResponseEntity<List<StorageAddon>> getAllStorageAddons() {
        List<StorageAddon> addons = storageAddonRepository.findAll();
        return ResponseEntity.ok(addons);
    }

    /**
     * Get storage add-on by ID - SUPERADMIN only
     * GET /api/admin/storage-addons/{id}
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @GetMapping("/admin/storage-addons/{id}")
    public ResponseEntity<StorageAddon> getStorageAddonById(@PathVariable Long id) {
        return storageAddonRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Create new storage add-on - SUPERADMIN only
     * POST /api/admin/storage-addons
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @PostMapping("/admin/storage-addons")
    public ResponseEntity<StorageAddon> createStorageAddon(@Valid @RequestBody StorageAddon storageAddon) {
        // Ensure timestamps are set
        if (storageAddon.getCreatedAt() == null) {
            storageAddon.setCreatedAt(java.time.LocalDateTime.now());
        }
        if (storageAddon.getUpdatedAt() == null) {
            storageAddon.setUpdatedAt(java.time.LocalDateTime.now());
        }

        StorageAddon created = storageAddonRepository.save(storageAddon);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Update existing storage add-on - SUPERADMIN only
     * PUT /api/admin/storage-addons/{id}
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @PutMapping("/admin/storage-addons/{id}")
    public ResponseEntity<StorageAddon> updateStorageAddon(
            @PathVariable Long id,
            @Valid @RequestBody StorageAddon updatedAddon
    ) {
        return storageAddonRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedAddon.getName());
                    existing.setDisplayName(updatedAddon.getDisplayName());
                    existing.setDescription(updatedAddon.getDescription());
                    existing.setStorageGb(updatedAddon.getStorageGb());
                    existing.setPrice(updatedAddon.getPrice());
                    existing.setTotalStorageGb(updatedAddon.getTotalStorageGb());
                    existing.setEstimatedPhotos(updatedAddon.getEstimatedPhotos());
                    existing.setEstimatedDocuments(updatedAddon.getEstimatedDocuments());
                    existing.setIsActive(updatedAddon.getIsActive());
                    existing.setIsRecommended(updatedAddon.getIsRecommended());
                    existing.setDisplayOrder(updatedAddon.getDisplayOrder());
                    existing.setUpdatedAt(java.time.LocalDateTime.now());

                    StorageAddon saved = storageAddonRepository.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete storage add-on - SUPERADMIN only
     * DELETE /api/admin/storage-addons/{id}
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @DeleteMapping("/admin/storage-addons/{id}")
    public ResponseEntity<Void> deleteStorageAddon(@PathVariable Long id) {
        return storageAddonRepository.findById(id)
                .map(addon -> {
                    // Soft delete by setting isActive = false
                    addon.setIsActive(false);
                    storageAddonRepository.save(addon);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Toggle storage add-on active status - SUPERADMIN only
     * PATCH /api/admin/storage-addons/{id}/toggle-active
     */
    @RequirePermission(Permission.SUPERADMIN_ACCESS)
    @PatchMapping("/admin/storage-addons/{id}/toggle-active")
    public ResponseEntity<StorageAddon> toggleActiveStatus(@PathVariable Long id) {
        return storageAddonRepository.findById(id)
                .map(addon -> {
                    addon.setIsActive(!addon.getIsActive());
                    addon.setUpdatedAt(java.time.LocalDateTime.now());
                    StorageAddon saved = storageAddonRepository.save(addon);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
