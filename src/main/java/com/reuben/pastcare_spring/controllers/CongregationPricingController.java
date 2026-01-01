package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.models.CongregationPricingTier;
import com.reuben.pastcare_spring.models.SubscriptionBillingInterval;
import com.reuben.pastcare_spring.services.CongregationPricingService;
import com.reuben.pastcare_spring.services.CurrencyConversionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * REST controller for congregation-based pricing tier management.
 *
 * <p>Public endpoints:
 * <ul>
 *   <li>GET /api/pricing/tiers - Get all active tiers</li>
 *   <li>GET /api/pricing/tiers/{id} - Get specific tier</li>
 *   <li>GET /api/pricing/calculate - Calculate price for member count and interval</li>
 * </ul>
 *
 * <p>Church-specific endpoints (authenticated):
 * <ul>
 *   <li>GET /api/pricing/church/current - Get current tier for authenticated church</li>
 *   <li>GET /api/pricing/church/upgrade-options - Get available upgrade options</li>
 *   <li>GET /api/pricing/church/tier-check - Check if tier upgrade required</li>
 * </ul>
 *
 * <p>SUPERADMIN endpoints:
 * <ul>
 *   <li>POST /api/pricing/tiers - Create new tier</li>
 *   <li>PUT /api/pricing/tiers/{id} - Update existing tier</li>
 *   <li>DELETE /api/pricing/tiers/{id} - Deactivate tier</li>
 * </ul>
 *
 * @since 2026-01-01
 */
@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
@Slf4j
@Validated
public class CongregationPricingController {

    private final CongregationPricingService pricingService;
    private final CurrencyConversionService currencyService;

    // ==================== PUBLIC ENDPOINTS ====================

    /**
     * Get all active pricing tiers.
     *
     * <p><b>Public endpoint</b> - No authentication required.
     *
     * @return List of active tiers sorted by min_members ASC
     */
    @GetMapping("/tiers")
    public ResponseEntity<List<CongregationPricingTier>> getAllActiveTiers() {
        log.debug("GET /api/pricing/tiers - Fetching all active pricing tiers");
        List<CongregationPricingTier> tiers = pricingService.getAllActiveTiers();
        return ResponseEntity.ok(tiers);
    }

    /**
     * Get specific pricing tier by ID.
     *
     * <p><b>Public endpoint</b> - No authentication required.
     *
     * @param tierId Tier ID
     * @return Pricing tier details
     */
    @GetMapping("/tiers/{tierId}")
    public ResponseEntity<CongregationPricingTier> getTierById(@PathVariable Long tierId) {
        log.debug("GET /api/pricing/tiers/{} - Fetching tier details", tierId);
        CongregationPricingTier tier = pricingService.getTierById(tierId);
        return ResponseEntity.ok(tier);
    }

    /**
     * Get all billing intervals (MONTHLY, QUARTERLY, BIANNUAL, ANNUAL).
     *
     * <p><b>Public endpoint</b> - No authentication required.
     *
     * @return List of billing intervals
     */
    @GetMapping("/billing-intervals")
    public ResponseEntity<List<SubscriptionBillingInterval>> getAllBillingIntervals() {
        log.debug("GET /api/pricing/billing-intervals - Fetching all billing intervals");
        List<SubscriptionBillingInterval> intervals = pricingService.getAllBillingIntervals();
        return ResponseEntity.ok(intervals);
    }

    /**
     * Calculate pricing for given member count and billing interval.
     *
     * <p><b>Public endpoint</b> - No authentication required.
     * Used during signup to show pricing before church registration.
     *
     * <p>Example: GET /api/pricing/calculate?memberCount=350&billingInterval=QUARTERLY
     *
     * @param memberCount Number of members
     * @param billingInterval Billing interval (MONTHLY, QUARTERLY, BIANNUAL, ANNUAL)
     * @return Pricing calculation with dual currency display
     */
    @GetMapping("/calculate")
    public ResponseEntity<PricingCalculationResponse> calculatePricing(
            @RequestParam @Min(1) int memberCount,
            @RequestParam @NotBlank String billingInterval) {

        log.debug("GET /api/pricing/calculate - memberCount={}, billingInterval={}",
                memberCount, billingInterval);

        CongregationPricingTier tier = pricingService.getPricingTierForMemberCount(memberCount);
        BigDecimal priceUsd = tier.getPriceForInterval(billingInterval);
        BigDecimal priceGhs = currencyService.convertUsdToGhs(priceUsd);

        String dualCurrencyDisplay = currencyService.formatDualCurrency(priceUsd);

        PricingCalculationResponse response = new PricingCalculationResponse(
                tier.getId(),
                tier.getTierName(),
                tier.getDisplayName(),
                tier.getDescription(),
                memberCount,
                tier.getMinMembers(),
                tier.getMaxMembers(),
                billingInterval,
                priceUsd,
                priceGhs,
                dualCurrencyDisplay,
                tier.getDiscountForInterval(billingInterval),
                tier.getFeatures()
        );

        return ResponseEntity.ok(response);
    }

    // ==================== CHURCH-SPECIFIC ENDPOINTS (AUTHENTICATED) ====================

    /**
     * Get current pricing tier for authenticated church.
     *
     * <p><b>Authentication required</b> - Any church role.
     *
     * @param churchId Church ID from authentication context
     * @return Current tier information
     */
    @GetMapping("/church/current")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PASTOR', 'TREASURER', 'MEMBER_MANAGER', 'FELLOWSHIP_LEADER', 'MEMBER')")
    public ResponseEntity<CongregationPricingService.ChurchTierInfo> getCurrentTierForChurch(
            @RequestParam Long churchId) {

        log.debug("GET /api/pricing/church/current - churchId={}", churchId);
        CongregationPricingService.ChurchTierInfo tierInfo = pricingService.getChurchTierInfo(churchId);
        return ResponseEntity.ok(tierInfo);
    }

    /**
     * Get available tier upgrade options for church.
     *
     * <p><b>Authentication required</b> - ADMIN or TREASURER only.
     *
     * @param churchId Church ID from authentication context
     * @return Upgrade options
     */
    @GetMapping("/church/upgrade-options")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TREASURER')")
    public ResponseEntity<CongregationPricingService.TierUpgradeInfo> getUpgradeOptions(
            @RequestParam Long churchId) {

        log.debug("GET /api/pricing/church/upgrade-options - churchId={}", churchId);
        CongregationPricingService.TierUpgradeInfo upgradeInfo = pricingService.getTierUpgradeInfo(churchId);
        return ResponseEntity.ok(upgradeInfo);
    }

    /**
     * Check if tier upgrade is required for church.
     *
     * <p><b>Authentication required</b> - Any church role.
     * Used to show upgrade warnings in UI.
     *
     * @param churchId Church ID from authentication context
     * @return Tier upgrade status
     */
    @GetMapping("/church/tier-check")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'PASTOR', 'TREASURER', 'MEMBER_MANAGER', 'FELLOWSHIP_LEADER', 'MEMBER')")
    public ResponseEntity<TierCheckResponse> checkTierUpgradeRequired(
            @RequestParam Long churchId) {

        log.debug("GET /api/pricing/church/tier-check - churchId={}", churchId);

        boolean upgradeRequired = pricingService.checkTierUpgradeRequired(churchId);
        CongregationPricingService.ChurchTierInfo tierInfo = pricingService.getChurchTierInfo(churchId);

        TierCheckResponse response = new TierCheckResponse(
                upgradeRequired,
                tierInfo.currentMemberCount(),
                tierInfo.currentTier().getMaxMembers(),
                tierInfo.percentageUsed(),
                tierInfo.membersRemaining(),
                upgradeRequired ? "Tier upgrade required - member limit exceeded" :
                        tierInfo.percentageUsed() > 80 ? "Approaching tier limit" : "Within tier limits"
        );

        return ResponseEntity.ok(response);
    }

    // ==================== SUPERADMIN ENDPOINTS ====================

    /**
     * Create a new pricing tier.
     *
     * <p><b>SUPERADMIN only</b> - Requires SUPERADMIN authority.
     *
     * @param request Tier creation request
     * @return Created tier
     */
    @PostMapping("/tiers")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<CongregationPricingTier> createTier(
            @Valid @RequestBody TierCreateRequest request) {

        log.info("POST /api/pricing/tiers - Creating new tier: {}", request.tierName());

        CongregationPricingTier tier = pricingService.createTier(
                request.tierName(),
                request.displayName(),
                request.description(),
                request.minMembers(),
                request.maxMembers(),
                request.monthlyPriceUsd(),
                request.quarterlyPriceUsd(),
                request.biannualPriceUsd(),
                request.annualPriceUsd(),
                request.features(),
                request.displayOrder()
        );

        return ResponseEntity.ok(tier);
    }

    /**
     * Update an existing pricing tier.
     *
     * <p><b>SUPERADMIN only</b> - Requires SUPERADMIN authority.
     *
     * @param tierId Tier ID to update
     * @param request Tier update request
     * @return Updated tier
     */
    @PutMapping("/tiers/{tierId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<CongregationPricingTier> updateTier(
            @PathVariable Long tierId,
            @Valid @RequestBody TierUpdateRequest request) {

        log.info("PUT /api/pricing/tiers/{} - Updating tier", tierId);

        CongregationPricingTier tier = pricingService.updateTier(
                tierId,
                request.displayName(),
                request.description(),
                request.monthlyPriceUsd(),
                request.quarterlyPriceUsd(),
                request.biannualPriceUsd(),
                request.annualPriceUsd(),
                request.features(),
                request.displayOrder()
        );

        return ResponseEntity.ok(tier);
    }

    /**
     * Deactivate a pricing tier.
     *
     * <p><b>SUPERADMIN only</b> - Requires SUPERADMIN authority.
     * Does not delete tier, just marks it as inactive.
     *
     * @param tierId Tier ID to deactivate
     * @return Success message
     */
    @DeleteMapping("/tiers/{tierId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> deactivateTier(@PathVariable Long tierId) {

        log.info("DELETE /api/pricing/tiers/{} - Deactivating tier", tierId);

        pricingService.deactivateTier(tierId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tier deactivated successfully",
                "tierId", tierId
        ));
    }

    // ==================== DTOs ====================

    /**
     * Response for pricing calculation.
     */
    public record PricingCalculationResponse(
            Long tierId,
            String tierName,
            String tierDisplayName,
            String tierDescription,
            int memberCount,
            int tierMinMembers,
            Integer tierMaxMembers,
            String billingInterval,
            BigDecimal priceUsd,
            BigDecimal priceGhs,
            String dualCurrencyDisplay,
            BigDecimal discountPercentage,
            String features
    ) {}

    /**
     * Response for tier upgrade check.
     */
    public record TierCheckResponse(
            boolean upgradeRequired,
            int currentMemberCount,
            Integer tierMaxMembers,
            double percentageUsed,
            Integer membersRemaining,
            String message
    ) {}

    /**
     * Request to create a new pricing tier.
     */
    public record TierCreateRequest(
            @NotBlank String tierName,
            @NotBlank String displayName,
            String description,
            @Min(1) int minMembers,
            Integer maxMembers, // null for unlimited
            BigDecimal monthlyPriceUsd,
            BigDecimal quarterlyPriceUsd,
            BigDecimal biannualPriceUsd,
            BigDecimal annualPriceUsd,
            String features, // JSON array
            int displayOrder
    ) {}

    /**
     * Request to update an existing pricing tier.
     */
    public record TierUpdateRequest(
            String displayName,
            String description,
            BigDecimal monthlyPriceUsd,
            BigDecimal quarterlyPriceUsd,
            BigDecimal biannualPriceUsd,
            BigDecimal annualPriceUsd,
            String features, // JSON array
            Integer displayOrder
    ) {}
}
