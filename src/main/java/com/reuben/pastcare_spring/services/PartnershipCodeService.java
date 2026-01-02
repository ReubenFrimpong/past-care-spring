package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.PartnershipCode;
import com.reuben.pastcare_spring.models.PartnershipCodeUsage;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import com.reuben.pastcare_spring.repositories.PartnershipCodeRepository;
import com.reuben.pastcare_spring.repositories.PartnershipCodeUsageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PartnershipCodeService {

    @Autowired
    private PartnershipCodeRepository partnershipCodeRepository;

    @Autowired
    private ChurchSubscriptionRepository subscriptionRepository;

    @Autowired
    private PartnershipCodeUsageRepository partnershipCodeUsageRepository;

    /**
     * Apply a partnership code to a church subscription to grant grace period
     *
     * @param churchId The church ID
     * @param code The partnership code
     * @return The updated subscription
     * @throws IllegalArgumentException if code is invalid or already used
     */
    @Transactional
    public ChurchSubscription applyPartnershipCode(Long churchId, String code) {
        // Find the partnership code
        PartnershipCode partnershipCode = partnershipCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid partnership code"));

        // Validate the code
        if (!partnershipCode.isValid()) {
            if (!partnershipCode.getIsActive()) {
                throw new IllegalArgumentException("This partnership code is no longer active");
            }
            if (partnershipCode.getExpiresAt() != null && LocalDateTime.now().isAfter(partnershipCode.getExpiresAt())) {
                throw new IllegalArgumentException("This partnership code has expired");
            }
            if (partnershipCode.getMaxUses() != null && partnershipCode.getCurrentUses() >= partnershipCode.getMaxUses()) {
                throw new IllegalArgumentException("This partnership code has reached its usage limit");
            }
            throw new IllegalArgumentException("Invalid partnership code");
        }

        // Check if this church has already used this code (per-church limit)
        if (partnershipCode.getMaxUsesPerChurch() != null) {
            long churchUsageCount = partnershipCodeUsageRepository.countByPartnershipCodeIdAndChurchId(
                    partnershipCode.getId(), churchId);

            if (churchUsageCount >= partnershipCode.getMaxUsesPerChurch()) {
                throw new IllegalArgumentException("Your church has already used this partnership code the maximum number of times allowed");
            }
        }

        // Find the subscription
        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        // Check if church already has an active subscription or grace period
        if ("ACTIVE".equals(subscription.getStatus())) {
            // If they have Paystack subscription, they don't need grace period
            if (subscription.getPaystackSubscriptionCode() != null) {
                throw new IllegalArgumentException("You already have an active paid subscription");
            }
        }

        // Apply the grace period
        LocalDateTime gracePeriodEnd = LocalDateTime.now().plusDays(partnershipCode.getGracePeriodDays());
        subscription.setStatus("ACTIVE");

        // Note: These fields need to be added to ChurchSubscription model
        // For now, we'll use the existing fields creatively
        // subscription.setPartnershipCodeId(partnershipCode.getId());
        // subscription.setGracePeriodEnd(gracePeriodEnd);

        // Temporary workaround: store in current period dates
        subscription.setCurrentPeriodStart(LocalDateTime.now().toLocalDate());
        subscription.setCurrentPeriodEnd(gracePeriodEnd.toLocalDate());
        subscription.setNextBillingDate(gracePeriodEnd.toLocalDate().plusDays(1));

        subscription = subscriptionRepository.save(subscription);

        // Track the usage for this church
        PartnershipCodeUsage usage = new PartnershipCodeUsage();
        usage.setPartnershipCodeId(partnershipCode.getId());
        usage.setChurchId(churchId);
        usage.setGracePeriodDaysGranted(partnershipCode.getGracePeriodDays());
        partnershipCodeUsageRepository.save(usage);

        // Increment code usage
        partnershipCode.incrementUsage();
        partnershipCodeRepository.save(partnershipCode);

        return subscription;
    }

    /**
     * Validate a partnership code without applying it
     */
    public PartnershipCode validateCode(String code) {
        PartnershipCode partnershipCode = partnershipCodeRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid partnership code"));

        if (!partnershipCode.isValid()) {
            throw new IllegalArgumentException("This partnership code is not valid");
        }

        return partnershipCode;
    }

    /**
     * Check if a church has an active grace period
     */
    public boolean hasActiveGracePeriod(Long churchId) {
        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
                .orElse(null);

        if (subscription == null) {
            return false;
        }

        // Check if they have active status but no Paystack subscription (grace period)
        return "ACTIVE".equals(subscription.getStatus())
                && subscription.getPaystackSubscriptionCode() == null
                && subscription.getCurrentPeriodEnd() != null
                && subscription.getCurrentPeriodEnd().isAfter(LocalDateTime.now().toLocalDate());
    }

    // ============================================================================
    // SUPERADMIN CRUD Operations
    // ============================================================================

    /**
     * Get all partnership codes (SUPERADMIN only)
     */
    public List<PartnershipCode> getAllCodes() {
        return partnershipCodeRepository.findAll();
    }

    /**
     * Get partnership code by ID (SUPERADMIN only)
     */
    public PartnershipCode getCodeById(Long id) {
        return partnershipCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Partnership code not found with ID: " + id));
    }

    /**
     * Create new partnership code (SUPERADMIN only)
     */
    @Transactional
    public PartnershipCode createCode(PartnershipCode partnershipCode) {
        // Validate code doesn't already exist
        if (partnershipCodeRepository.findByCodeIgnoreCase(partnershipCode.getCode()).isPresent()) {
            throw new IllegalArgumentException("Partnership code already exists: " + partnershipCode.getCode());
        }

        // Set initial values
        partnershipCode.setCurrentUses(0);
        partnershipCode.setCreatedAt(LocalDateTime.now());

        return partnershipCodeRepository.save(partnershipCode);
    }

    /**
     * Update existing partnership code (SUPERADMIN only)
     */
    @Transactional
    public PartnershipCode updateCode(Long id, PartnershipCode updatedCode) {
        PartnershipCode existingCode = getCodeById(id);

        // Update fields (code itself cannot be changed)
        existingCode.setDescription(updatedCode.getDescription());
        existingCode.setGracePeriodDays(updatedCode.getGracePeriodDays());
        existingCode.setMaxUses(updatedCode.getMaxUses());
        existingCode.setMaxUsesPerChurch(updatedCode.getMaxUsesPerChurch());
        existingCode.setExpiresAt(updatedCode.getExpiresAt());
        existingCode.setIsActive(updatedCode.getIsActive());

        return partnershipCodeRepository.save(existingCode);
    }

    /**
     * Deactivate partnership code (SUPERADMIN only)
     */
    @Transactional
    public void deactivateCode(Long id) {
        PartnershipCode code = getCodeById(id);
        code.setIsActive(false);
        partnershipCodeRepository.save(code);
    }

    /**
     * Get partnership code usage statistics (SUPERADMIN only)
     */
    public Map<String, Object> getCodeStats(Long id) {
        PartnershipCode code = getCodeById(id);
        long uniqueChurches = partnershipCodeUsageRepository.countUniqueChurchesByPartnershipCodeId(id);

        Map<String, Object> stats = new HashMap<>();
        stats.put("code", code.getCode());
        stats.put("description", code.getDescription());
        stats.put("totalUses", code.getCurrentUses() != null ? code.getCurrentUses() : 0);
        stats.put("uniqueChurches", uniqueChurches);
        stats.put("maxUses", code.getMaxUses() != null ? code.getMaxUses() : "Unlimited");
        stats.put("maxUsesPerChurch", code.getMaxUsesPerChurch() != null ? code.getMaxUsesPerChurch() : "Unlimited");
        stats.put("gracePeriodDays", code.getGracePeriodDays());
        stats.put("isActive", code.getIsActive());
        stats.put("isExpired", code.getExpiresAt() != null && LocalDateTime.now().isAfter(code.getExpiresAt()));
        stats.put("createdAt", code.getCreatedAt());
        stats.put("expiresAt", code.getExpiresAt() != null ? code.getExpiresAt() : "Never");

        return stats;
    }
}
