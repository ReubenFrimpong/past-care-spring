package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.services.ChurchSettingsService;
import com.reuben.pastcare_spring.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for managing church settings
 *
 * Handles notification preferences, system preferences, and other church-specific configurations.
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChurchSettingsController {

    private final ChurchSettingsService churchSettingsService;

    /**
     * Get all settings for the current church
     */
    @GetMapping
    @RequirePermission(Permission.CHURCH_SETTINGS_VIEW)
    public ResponseEntity<Map<String, String>> getSettings() {
        Long churchId = TenantContext.getCurrentChurchId();
        Map<String, String> settings = churchSettingsService.getSettings(churchId);
        return ResponseEntity.ok(settings);
    }

    /**
     * Get a specific setting by key
     */
    @GetMapping("/{key}")
    @RequirePermission(Permission.CHURCH_SETTINGS_VIEW)
    public ResponseEntity<String> getSetting(@PathVariable String key) {
        Long churchId = TenantContext.getCurrentChurchId();
        String value = churchSettingsService.getSetting(churchId, key);
        if (value != null) {
            return ResponseEntity.ok(value);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Save or update a single setting
     */
    @PutMapping("/{key}")
    @RequirePermission(Permission.CHURCH_SETTINGS_EDIT)
    public ResponseEntity<Void> saveSetting(
            @PathVariable String key,
            @RequestBody Map<String, String> body) {
        Long churchId = TenantContext.getCurrentChurchId();
        String value = body.get("value");
        churchSettingsService.saveSetting(churchId, key, value);
        return ResponseEntity.ok().build();
    }

    /**
     * Save or update multiple settings at once
     */
    @PutMapping
    @RequirePermission(Permission.CHURCH_SETTINGS_EDIT)
    public ResponseEntity<Void> saveSettings(@RequestBody Map<String, String> settings) {
        Long churchId = TenantContext.getCurrentChurchId();
        churchSettingsService.saveSettings(churchId, settings);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a setting
     */
    @DeleteMapping("/{key}")
    @RequirePermission(Permission.CHURCH_SETTINGS_EDIT)
    public ResponseEntity<Void> deleteSetting(@PathVariable String key) {
        Long churchId = TenantContext.getCurrentChurchId();
        churchSettingsService.deleteSetting(churchId, key);
        return ResponseEntity.ok().build();
    }
}
