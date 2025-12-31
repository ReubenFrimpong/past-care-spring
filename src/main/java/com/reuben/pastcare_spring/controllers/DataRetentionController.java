package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.ExtendRetentionRequest;
import com.reuben.pastcare_spring.dtos.PendingDeletionResponse;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static com.reuben.pastcare_spring.enums.Permission.PLATFORM_MANAGE_CHURCHES;

/**
 * Controller for SUPERADMIN data retention management.
 * Allows viewing pending deletions, extending retention periods, and canceling deletions.
 */
@RestController
@RequestMapping("/api/platform/data-retention")
@RequiredArgsConstructor
@Slf4j
public class DataRetentionController {

    private final ChurchSubscriptionRepository subscriptionRepository;
    private final ChurchRepository churchRepository;
    private final UserRepository userRepository;

    /**
     * Get all churches with pending data deletion.
     * SUPERADMIN only.
     *
     * @return List of churches scheduled for deletion
     */
    @GetMapping("/pending-deletions")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<List<PendingDeletionResponse>> getPendingDeletions() {
        log.info("SUPERADMIN requesting pending deletions list");

        // Find all SUSPENDED subscriptions with retention end dates
        List<ChurchSubscription> suspendedSubscriptions = subscriptionRepository.findByStatus("SUSPENDED")
            .stream()
            .filter(sub -> sub.getDataRetentionEndDate() != null)
            .collect(Collectors.toList());

        List<PendingDeletionResponse> pendingDeletions = suspendedSubscriptions.stream()
            .map(this::mapToPendingDeletionResponse)
            .sorted((a, b) -> Long.compare(a.getDaysUntilDeletion(), b.getDaysUntilDeletion())) // Most urgent first
            .collect(Collectors.toList());

        log.info("Found {} churches with pending data deletion", pendingDeletions.size());
        return ResponseEntity.ok(pendingDeletions);
    }

    /**
     * Get details for a specific church's pending deletion.
     * SUPERADMIN only.
     *
     * @param churchId Church ID
     * @return Pending deletion details
     */
    @GetMapping("/pending-deletions/{churchId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<PendingDeletionResponse> getPendingDeletionDetails(@PathVariable Long churchId) {
        log.info("SUPERADMIN requesting pending deletion details for church {}", churchId);

        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
            .orElseThrow(() -> new IllegalArgumentException("No subscription found for church: " + churchId));

        if (!subscription.isSuspended() || subscription.getDataRetentionEndDate() == null) {
            throw new IllegalArgumentException("Church " + churchId + " is not pending deletion");
        }

        PendingDeletionResponse response = mapToPendingDeletionResponse(subscription);
        return ResponseEntity.ok(response);
    }

    /**
     * Extend data retention period for a church.
     * SUPERADMIN only.
     *
     * @param churchId Church ID
     * @param request Extension request with days and note
     * @param authentication Current user authentication
     * @return Updated pending deletion details
     */
    @PostMapping("/{churchId}/extend")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<PendingDeletionResponse> extendRetention(
            @PathVariable Long churchId,
            @Valid @RequestBody ExtendRetentionRequest request,
            Authentication authentication) {

        log.info("SUPERADMIN extending retention for church {} by {} days. Reason: {}",
                churchId, request.getExtensionDays(), request.getNote());

        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
            .orElseThrow(() -> new IllegalArgumentException("No subscription found for church: " + churchId));

        if (!subscription.isSuspended()) {
            throw new IllegalArgumentException("Can only extend retention for suspended subscriptions");
        }

        // Extend retention period
        subscription.extendRetentionPeriod(request.getExtensionDays(), request.getNote());
        subscriptionRepository.save(subscription);

        log.info("✅ Extended retention for church {} by {} days. New deletion date: {}",
                churchId, request.getExtensionDays(), subscription.getDataRetentionEndDate());

        PendingDeletionResponse response = mapToPendingDeletionResponse(subscription);
        return ResponseEntity.ok(response);
    }

    /**
     * Cancel deletion and reactivate subscription (requires manual activation).
     * SUPERADMIN only - typically done when church renews or special circumstances.
     *
     * @param churchId Church ID
     * @param authentication Current user authentication
     * @return Success message
     */
    @DeleteMapping("/{churchId}/cancel-deletion")
    @PreAuthorize("hasRole('SUPERADMIN')")
    @RequirePermission(PLATFORM_MANAGE_CHURCHES)
    public ResponseEntity<String> cancelDeletion(
            @PathVariable Long churchId,
            Authentication authentication) {

        log.warn("SUPERADMIN canceling deletion for church {}", churchId);

        ChurchSubscription subscription = subscriptionRepository.findByChurchId(churchId)
            .orElseThrow(() -> new IllegalArgumentException("No subscription found for church: " + churchId));

        if (!subscription.isSuspended()) {
            throw new IllegalArgumentException("Church " + churchId + " is not suspended");
        }

        // Cancel deletion tracking
        subscription.cancelDeletion();

        // Note: Subscription remains SUSPENDED until SUPERADMIN manually activates it
        // This prevents automatic reactivation without payment
        subscriptionRepository.save(subscription);

        log.warn("✅ Deletion canceled for church {}. Subscription remains SUSPENDED - manual activation required.", churchId);

        return ResponseEntity.ok("Deletion canceled successfully. Church subscription remains SUSPENDED - use manual activation if needed.");
    }

    /**
     * Helper method to map ChurchSubscription to PendingDeletionResponse
     */
    private PendingDeletionResponse mapToPendingDeletionResponse(ChurchSubscription subscription) {
        Church church = churchRepository.findById(subscription.getChurchId())
            .orElse(null);

        long daysUntilDeletion = subscription.getDaysUntilDeletion();
        long daysUntilWarning = subscription.getDataRetentionEndDate() != null
            ? java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), subscription.getDataRetentionEndDate().minusDays(7))
            : -1;

        // Count admin users for this church
        long adminCount = userRepository.findAll().stream()
            .filter(user -> user.getChurch() != null && user.getChurch().getId().equals(subscription.getChurchId()))
            .filter(user -> user.getRole() == com.reuben.pastcare_spring.enums.Role.ADMIN)
            .count();

        // Determine urgency level
        String urgencyLevel;
        if (daysUntilDeletion <= 0) {
            urgencyLevel = "OVERDUE"; // Should be deleted already
        } else if (daysUntilDeletion <= 3) {
            urgencyLevel = "CRITICAL"; // 3 days or less
        } else if (daysUntilDeletion <= 7) {
            urgencyLevel = "HIGH"; // Within warning period
        } else if (daysUntilDeletion <= 14) {
            urgencyLevel = "MEDIUM"; // 2 weeks or less
        } else {
            urgencyLevel = "LOW"; // More than 2 weeks
        }

        return PendingDeletionResponse.builder()
            .churchId(subscription.getChurchId())
            .churchName(church != null ? church.getName() : "Unknown Church")
            .status(subscription.getStatus())
            .suspendedAt(subscription.getSuspendedAt())
            .dataRetentionEndDate(subscription.getDataRetentionEndDate())
            .daysUntilDeletion(daysUntilDeletion)
            .deletionWarningSentAt(subscription.getDeletionWarningSentAt())
            .daysUntilWarning(daysUntilWarning)
            .warningSent(subscription.getDeletionWarningSentAt() != null)
            .retentionExtensionDays(subscription.getRetentionExtensionDays())
            .retentionExtensionNote(subscription.getRetentionExtensionNote())
            .urgencyLevel(urgencyLevel)
            .churchEmail(church != null ? church.getEmail() : null)
            .adminCount((int) adminCount)
            .build();
    }
}
