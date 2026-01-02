package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.PaymentInitializationResponse;
import com.reuben.pastcare_spring.dtos.TierUpgradePreviewRequest;
import com.reuben.pastcare_spring.dtos.TierUpgradePreviewResponse;
import com.reuben.pastcare_spring.dtos.TierUpgradeRequest;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.CongregationPricingTier;
import com.reuben.pastcare_spring.services.TierUpgradeService;
import com.reuben.pastcare_spring.services.TierValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for subscription tier upgrades.
 *
 * <p>Provides endpoints for:
 * <ul>
 *   <li>Validating tier selections</li>
 *   <li>Previewing upgrade calculations</li>
 *   <li>Initiating tier upgrade payments</li>
 *   <li>Verifying and completing upgrades</li>
 *   <li>Canceling pending upgrades</li>
 * </ul>
 *
 * <p><b>Security:</b> All endpoints require MANAGE_BILLING permission on the church.
 *
 * @since 2026-01-02
 */
@RestController
@RequestMapping("/api/billing/tier-upgrade")
@RequiredArgsConstructor
@Slf4j
public class TierUpgradeController {

    private final TierUpgradeService tierUpgradeService;
    private final TierValidationService tierValidation;

    /**
     * GET /api/billing/tier-upgrade/valid-tiers
     *
     * <p>Get list of valid tiers for church based on current member count.
     *
     * <p>Only tiers that can accommodate the church's member count are returned.
     *
     * @param churchId Church ID
     * @return List of valid pricing tiers
     */
    @GetMapping("/valid-tiers")
    @PreAuthorize("hasPermission(#churchId, 'Church', 'MANAGE_BILLING')")
    public ResponseEntity<List<CongregationPricingTier>> getValidTiers(
            @RequestParam Long churchId) {

        log.info("Fetching valid tiers for church {}", churchId);
        List<CongregationPricingTier> validTiers = tierValidation.getValidTiersForChurch(churchId);
        return ResponseEntity.ok(validTiers);
    }

    /**
     * GET /api/billing/tier-upgrade/can-select/{tierId}
     *
     * <p>Check if a specific tier can be selected by the church.
     *
     * <p>Validates that the tier's min/max member range includes the church's current member count.
     *
     * @param churchId Church ID
     * @param tierId Tier ID to check
     * @return Map with "canSelect" boolean
     */
    @GetMapping("/can-select/{tierId}")
    @PreAuthorize("hasPermission(#churchId, 'Church', 'MANAGE_BILLING')")
    public ResponseEntity<Map<String, Boolean>> canSelectTier(
            @RequestParam Long churchId,
            @PathVariable Long tierId) {

        log.info("Checking if church {} can select tier {}", churchId, tierId);
        boolean canSelect = tierValidation.canSelectTier(churchId, tierId);
        return ResponseEntity.ok(Map.of("canSelect", canSelect));
    }

    /**
     * POST /api/billing/tier-upgrade/preview
     *
     * <p>Preview tier upgrade calculation without initiating payment.
     *
     * <p>Returns complete financial breakdown including:
     * <ul>
     *   <li>Current vs new tier/interval</li>
     *   <li>Days remaining in billing period</li>
     *   <li>Unused credit from current tier</li>
     *   <li>Prorated charge for new tier</li>
     *   <li>Net amount to pay (GHS)</li>
     *   <li>New billing date</li>
     * </ul>
     *
     * @param request Preview request
     * @return Upgrade preview with financial details
     */
    @PostMapping("/preview")
    @PreAuthorize("hasPermission(#request.churchId, 'Church', 'MANAGE_BILLING')")
    public ResponseEntity<TierUpgradePreviewResponse> previewUpgrade(
            @RequestBody @Valid TierUpgradePreviewRequest request) {

        log.info("Previewing tier upgrade for church {}: tier {} → {}, interval {}",
                request.getChurchId(), request.getNewTierId(), request.getNewIntervalId());

        TierUpgradePreviewResponse preview = tierUpgradeService.previewUpgrade(request);
        return ResponseEntity.ok(preview);
    }

    /**
     * POST /api/billing/tier-upgrade/initiate
     *
     * <p>Initiate tier upgrade payment via Paystack.
     *
     * <p>Creates:
     * <ul>
     *   <li>Tier change history record (PENDING)</li>
     *   <li>Payment record (PENDING)</li>
     *   <li>Paystack payment session</li>
     * </ul>
     *
     * <p>Returns Paystack authorization URL for payment redirection.
     *
     * @param request Upgrade request with payment details
     * @return Paystack payment initialization response
     */
    @PostMapping("/initiate")
    @PreAuthorize("hasPermission(#request.churchId, 'Church', 'MANAGE_BILLING')")
    public ResponseEntity<PaymentInitializationResponse> initiateUpgrade(
            @RequestBody @Valid TierUpgradeRequest request) {

        log.info("Initiating tier upgrade payment for church {}: tier {}, interval {}",
                request.getChurchId(), request.getNewTierId(), request.getNewIntervalId());

        PaymentInitializationResponse response = tierUpgradeService.initiateUpgrade(request);

        log.info("Tier upgrade payment initialized: reference {}, authorization URL: {}",
                response.getReference(), response.getAuthorizationUrl());

        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/billing/tier-upgrade/verify/{reference}
     *
     * <p>Verify and complete tier upgrade after Paystack payment.
     *
     * <p>Actions performed:
     * <ul>
     *   <li>Verify payment with Paystack</li>
     *   <li>Update subscription tier and billing dates</li>
     *   <li>Mark tier change history as COMPLETED</li>
     *   <li>Clear pending upgrade flags</li>
     * </ul>
     *
     * @param reference Payment reference from Paystack
     * @return Updated subscription
     */
    @PostMapping("/verify/{reference}")
    public ResponseEntity<ChurchSubscription> verifyUpgrade(
            @PathVariable String reference) {

        log.info("Verifying tier upgrade payment: reference {}", reference);

        ChurchSubscription subscription = tierUpgradeService.completeUpgrade(reference);

        log.info("Tier upgrade verified and completed: church {}, tier {}",
                subscription.getChurchId(), subscription.getPricingTier().getTierName());

        return ResponseEntity.ok(subscription);
    }

    /**
     * POST /api/billing/tier-upgrade/cancel
     *
     * <p>Cancel pending tier upgrade.
     *
     * <p>Used when payment fails or user abandons the upgrade.
     *
     * @param churchId Church ID
     * @return Success response
     */
    @PostMapping("/cancel")
    @PreAuthorize("hasPermission(#churchId, 'Church', 'MANAGE_BILLING')")
    public ResponseEntity<Void> cancelPendingUpgrade(@RequestParam Long churchId) {

        log.info("Canceling pending tier upgrade for church {}", churchId);
        tierUpgradeService.cancelPendingUpgrade(churchId);

        return ResponseEntity.ok().build();
    }
}
