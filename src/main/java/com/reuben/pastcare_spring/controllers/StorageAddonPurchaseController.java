package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.PaymentInitializationResponse;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.models.ChurchStorageAddon;
import com.reuben.pastcare_spring.repositories.ChurchStorageAddonRepository;
import com.reuben.pastcare_spring.security.UserPrincipal;
import com.reuben.pastcare_spring.services.StorageAddonBillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for storage addon purchase management.
 *
 * Provides endpoints for:
 * - Purchasing storage addons (with prorated billing)
 * - Listing church's active addons
 * - Canceling addons (remains active until period end)
 *
 * All endpoints require MANAGE_BILLING permission.
 */
@RestController
@RequestMapping("/api/storage-addons")
@RequiredArgsConstructor
@Slf4j
public class StorageAddonPurchaseController {

    private final StorageAddonBillingService storageAddonBillingService;
    private final ChurchStorageAddonRepository churchStorageAddonRepository;

    /**
     * Purchase a storage addon with prorated first-month billing.
     *
     * Flow:
     * 1. Validates subscription is active/grace period
     * 2. Checks for duplicate purchase
     * 3. Calculates prorated charge based on days remaining in billing period
     * 4. Creates Payment record with ADDON-{UUID} reference
     * 5. Initializes Paystack payment
     * 6. Returns payment authorization URL
     *
     * Activation happens via webhook after payment success.
     *
     * @param request Purchase request with storageAddonId, email, callbackUrl
     * @param principal Current authenticated user (for churchId extraction)
     * @return Paystack payment initialization response with authorization_url
     */
    @PostMapping("/purchase")
    @RequirePermission(Permission.BILLING_MANAGE)
    public ResponseEntity<PaymentInitializationResponse> purchaseAddon(
            @RequestBody StorageAddonPurchaseRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Church {} purchasing storage addon {}",
            principal.getChurchId(), request.getStorageAddonId());

        try {
            PaymentInitializationResponse response = storageAddonBillingService.purchaseStorageAddon(
                principal.getChurchId(),
                request.getStorageAddonId(),
                request.getEmail(),
                request.getCallbackUrl()
            );

            log.info("Storage addon purchase initiated for church {}, reference: {}",
                principal.getChurchId(), response.getReference());

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            log.warn("Storage addon purchase failed for church {}: {}",
                principal.getChurchId(), e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * List all storage addons for the authenticated church.
     *
     * Returns:
     * - Active addons (currently providing storage)
     * - Canceled addons (active until period end)
     * - Suspended addons (subscription past due)
     *
     * Does NOT include:
     * - Expired addons (after cancellation period ended)
     *
     * @param principal Current authenticated user
     * @return List of church's storage addons with details
     */
    @GetMapping("/my-addons")
    @RequirePermission(Permission.BILLING_MANAGE)
    public ResponseEntity<List<ChurchStorageAddonResponse>> getMyAddons(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Fetching storage addons for church {}", principal.getChurchId());

        List<ChurchStorageAddon> addons = churchStorageAddonRepository
            .findByChurchId(principal.getChurchId());

        List<ChurchStorageAddonResponse> response = addons.stream()
            .map(this::mapToResponse)
            .toList();

        return ResponseEntity.ok(response);
    }

    /**
     * Cancel a storage addon.
     *
     * Important: Addon remains ACTIVE until the current billing period ends.
     * This means:
     * - Storage capacity remains available until period end
     * - No refund for remaining days
     * - Addon will NOT auto-renew at next billing date
     * - Status changes to CANCELED (not deleted)
     *
     * @param addonId ID of the church_storage_addon to cancel
     * @param request Cancellation request with reason
     * @param principal Current authenticated user
     * @return Success message
     */
    @PostMapping("/{addonId}/cancel")
    @RequirePermission(Permission.BILLING_MANAGE)
    public ResponseEntity<String> cancelAddon(
            @PathVariable Long addonId,
            @RequestBody AddonCancellationRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Church {} canceling storage addon {}, reason: {}",
            principal.getChurchId(), addonId, request.getReason());

        try {
            storageAddonBillingService.cancelAddon(
                principal.getChurchId(),
                addonId,
                request.getReason()
            );

            return ResponseEntity.ok("Addon canceled successfully. " +
                "Storage capacity will remain available until the end of your current billing period.");

        } catch (IllegalStateException e) {
            log.warn("Addon cancellation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
        }
    }

    /**
     * Maps ChurchStorageAddon entity to DTO response.
     */
    private ChurchStorageAddonResponse mapToResponse(ChurchStorageAddon addon) {
        return ChurchStorageAddonResponse.builder()
            .id(addon.getId())
            .addonName(addon.getStorageAddon().getName())
            .storageGb(addon.getStorageAddon().getStorageGb())
            .purchasePrice(addon.getPurchasePrice())
            .purchasedAt(addon.getPurchasedAt())
            .isProrated(addon.getIsProrated())
            .proratedAmount(addon.getProratedAmount())
            .currentPeriodStart(addon.getCurrentPeriodStart())
            .currentPeriodEnd(addon.getCurrentPeriodEnd())
            .nextRenewalDate(addon.getNextRenewalDate())
            .status(addon.getStatus())
            .canceledAt(addon.getCanceledAt())
            .cancellationReason(addon.getCancellationReason())
            .build();
    }

    /**
     * Request DTO for purchasing storage addon.
     */
    @lombok.Data
    public static class StorageAddonPurchaseRequest {
        private Long storageAddonId;
        private String email;
        private String callbackUrl;
    }

    /**
     * Request DTO for canceling storage addon.
     */
    @lombok.Data
    public static class AddonCancellationRequest {
        private String reason;
    }

    /**
     * Response DTO for church storage addon details.
     */
    @lombok.Data
    @lombok.Builder
    public static class ChurchStorageAddonResponse {
        private Long id;
        private String addonName;
        private Integer storageGb;
        private java.math.BigDecimal purchasePrice;
        private java.time.LocalDateTime purchasedAt;
        private Boolean isProrated;
        private java.math.BigDecimal proratedAmount;
        private java.time.LocalDate currentPeriodStart;
        private java.time.LocalDate currentPeriodEnd;
        private java.time.LocalDate nextRenewalDate;
        private String status;
        private java.time.LocalDateTime canceledAt;
        private String cancellationReason;
    }
}
