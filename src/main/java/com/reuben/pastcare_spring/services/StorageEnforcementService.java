package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.exceptions.StorageLimitExceededException;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for enforcing storage limits with HARD BLOCK on uploads.
 *
 * <p>Prevents file uploads when church exceeds storage limit.
 * Uses denormalized Church.totalStorageLimitMb for fast checks.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StorageEnforcementService {

    private final ChurchRepository churchRepository;
    private final StorageCalculationService storageCalculationService;

    /**
     * Technical buffer in MB to account for metadata and small discrepancies.
     * Allows upload if new total would be within limit + buffer.
     */
    private static final long TECHNICAL_BUFFER_MB = 10L;

    /**
     * Check if church can upload a file of given size.
     *
     * <p>HARD BLOCK: Returns false if upload would exceed limit.
     *
     * @param churchId Church attempting upload
     * @param fileSizeBytes File size in bytes
     * @return StorageCheckResult with allowed flag and detailed metrics
     */
    public StorageCheckResult canUploadFile(Long churchId, long fileSizeBytes) {
        // Get church's total storage limit (base + active addons)
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalStateException("Church not found: " + churchId));

        long limitMb = church.getTotalStorageLimitMb();

        // UNLIMITED STORAGE: If limit is -1, always allow upload
        if (limitMb == -1) {
            double currentUsageMb = storageCalculationService.getLatestStorageUsage(churchId).getTotalStorageMb();
            double fileSizeMb = fileSizeBytes / (1024.0 * 1024.0);
            double newTotalMb = currentUsageMb + fileSizeMb;

            log.debug("Unlimited storage plan for church {}: uploading {} MB (current: {} MB, new total: {} MB)",
                    churchId, fileSizeMb, currentUsageMb, newTotalMb);

            return new StorageCheckResult(
                    true,           // Always allowed
                    currentUsageMb,
                    -1,             // Unlimited
                    fileSizeMb,
                    newTotalMb,
                    0.0             // 0% usage for unlimited
            );
        }

        // Get current storage usage
        double currentUsageMb = storageCalculationService.getLatestStorageUsage(churchId).getTotalStorageMb();

        // Convert file size to MB
        double fileSizeMb = fileSizeBytes / (1024.0 * 1024.0);

        // Calculate new total if upload proceeds
        double newTotalMb = currentUsageMb + fileSizeMb;

        // Calculate percentage
        double percentageUsed = (newTotalMb / limitMb) * 100.0;

        // Allow upload if within limit + technical buffer
        boolean allowed = newTotalMb <= (limitMb + TECHNICAL_BUFFER_MB);

        if (!allowed) {
            log.warn("Storage limit exceeded for church {}: current={} MB, limit={} MB, file={} MB, new total={} MB",
                    churchId, currentUsageMb, limitMb, fileSizeMb, newTotalMb);
        }

        return new StorageCheckResult(
                allowed,
                currentUsageMb,
                limitMb,
                fileSizeMb,
                newTotalMb,
                percentageUsed
        );
    }

    /**
     * Check if church can create a new member (estimates 1KB per member).
     *
     * @param churchId Church attempting to create member
     * @return StorageCheckResult
     */
    public StorageCheckResult canCreateMember(Long churchId) {
        // Estimate 1KB per member for database storage
        return canUploadFile(churchId, 1024L);
    }

    /**
     * Check if church can create a new record (generic, estimates 1KB).
     *
     * @param churchId Church attempting to create record
     * @return StorageCheckResult
     */
    public StorageCheckResult canCreateRecord(Long churchId) {
        return canUploadFile(churchId, 1024L);
    }

    /**
     * Throw exception if upload not allowed.
     *
     * @param churchId Church attempting upload
     * @param fileSizeBytes File size in bytes
     * @throws StorageLimitExceededException if upload would exceed limit
     */
    public void enforceStorageLimit(Long churchId, long fileSizeBytes) {
        StorageCheckResult result = canUploadFile(churchId, fileSizeBytes);
        if (!result.isAllowed()) {
            throw new StorageLimitExceededException(
                    result.getErrorMessage(),
                    result.getCurrentUsageMb(),
                    result.getLimitMb(),
                    result.getFileSizeMb(),
                    result.getNewTotalMb(),
                    result.getPercentageUsed()
            );
        }
    }

    /**
     * Result of storage check with detailed metrics.
     */
    @Getter
    public static class StorageCheckResult {
        private final boolean allowed;
        private final double currentUsageMb;
        private final long limitMb;
        private final double fileSizeMb;
        private final double newTotalMb;
        private final double percentageUsed;

        public StorageCheckResult(
                boolean allowed,
                double currentUsageMb,
                long limitMb,
                double fileSizeMb,
                double newTotalMb,
                double percentageUsed
        ) {
            this.allowed = allowed;
            this.currentUsageMb = currentUsageMb;
            this.limitMb = limitMb;
            this.fileSizeMb = fileSizeMb;
            this.newTotalMb = newTotalMb;
            this.percentageUsed = percentageUsed;
        }

        /**
         * Get user-friendly error message.
         */
        public String getErrorMessage() {
            return String.format(
                    "Storage limit exceeded. Current usage: %.2f MB, Limit: %d MB, " +
                    "File size: %.2f MB, New total would be: %.2f MB (%.1f%% of limit). " +
                    "Please purchase additional storage or delete unused files.",
                    currentUsageMb, limitMb, fileSizeMb, newTotalMb, percentageUsed
            );
        }

        /**
         * Get warning message if approaching limit (>80%).
         */
        public String getWarningMessage() {
            if (percentageUsed > 80.0) {
                return String.format(
                        "Warning: Storage usage at %.1f%% of limit (%.2f MB / %d MB). " +
                        "Consider purchasing additional storage.",
                        percentageUsed, currentUsageMb, limitMb
                );
            }
            return null;
        }

        /**
         * Check if usage is approaching limit (>80%).
         */
        public boolean isApproachingLimit() {
            return percentageUsed > 80.0;
        }
    }
}
