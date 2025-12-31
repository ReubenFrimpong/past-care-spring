package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.PartnershipCode;
import com.reuben.pastcare_spring.security.UserPrincipal;
import com.reuben.pastcare_spring.services.PartnershipCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/partnership-codes")
public class PartnershipCodeController {

    @Autowired
    private PartnershipCodeService partnershipCodeService;

    /**
     * Apply a partnership code to get grace period
     *
     * POST /api/partnership-codes/apply
     * Body: { "code": "PARTNER2025" }
     */
    @PostMapping("/apply")
    public ResponseEntity<?> applyPartnershipCode(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        try {
            String code = request.get("code");
            if (code == null || code.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Partnership code is required"));
            }

            Long churchId = userPrincipal.getChurchId();
            ChurchSubscription subscription = partnershipCodeService.applyPartnershipCode(churchId, code.trim());

            return ResponseEntity.ok(Map.of(
                    "message", "Partnership code applied successfully",
                    "gracePeriodEnd", subscription.getCurrentPeriodEnd(),
                    "status", subscription.getStatus()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to apply partnership code"));
        }
    }

    /**
     * Validate a partnership code without applying
     *
     * GET /api/partnership-codes/validate?code=PARTNER2025
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validatePartnershipCode(@RequestParam String code) {
        try {
            PartnershipCode partnershipCode = partnershipCodeService.validateCode(code);

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "description", partnershipCode.getDescription(),
                    "gracePeriodDays", partnershipCode.getGracePeriodDays()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Check if current church has active grace period
     *
     * GET /api/partnership-codes/grace-period/status
     */
    @GetMapping("/grace-period/status")
    public ResponseEntity<?> getGracePeriodStatus(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            Long churchId = userPrincipal.getChurchId();
            boolean hasGracePeriod = partnershipCodeService.hasActiveGracePeriod(churchId);

            return ResponseEntity.ok(Map.of(
                    "hasActiveGracePeriod", hasGracePeriod
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to check grace period status"));
        }
    }
}
