package com.reuben.pastcare_spring.exceptions;

import lombok.Getter;

/**
 * Exception thrown when a church exceeds its storage limit.
 *
 * <p>This triggers HTTP 413 Payload Too Large response with detailed error information
 * including current usage, limit, and suggested actions (purchase addons or delete files).
 */
@Getter
public class StorageLimitExceededException extends RuntimeException {

    private final double currentUsageMb;
    private final long limitMb;
    private final double fileSizeMb;
    private final double newTotalMb;
    private final double percentageUsed;

    public StorageLimitExceededException(
            String message,
            double currentUsageMb,
            long limitMb,
            double fileSizeMb,
            double newTotalMb,
            double percentageUsed
    ) {
        super(message);
        this.currentUsageMb = currentUsageMb;
        this.limitMb = limitMb;
        this.fileSizeMb = fileSizeMb;
        this.newTotalMb = newTotalMb;
        this.percentageUsed = percentageUsed;
    }

    /**
     * Get user-friendly error message with storage details.
     */
    public String getUserFriendlyMessage() {
        return String.format(
                "Storage limit exceeded. Current usage: %.2f MB, Limit: %d MB, " +
                "File size: %.2f MB, New total would be: %.2f MB (%.1f%% of limit). " +
                "Please purchase additional storage or delete unused files.",
                currentUsageMb, limitMb, fileSizeMb, newTotalMb, percentageUsed
        );
    }
}
