package com.reuben.pastcare_spring.exceptions;

import lombok.Getter;

/**
 * Exception thrown when a church attempts to exceed its pricing tier's member limit.
 *
 * <p>This exception prevents tier bypass through bulk member uploads or individual additions.
 *
 * @since 2026-01-01
 */
@Getter
public class TierLimitExceededException extends RuntimeException {

    private final int currentMemberCount;
    private final Integer tierMaxMembers;
    private final int membersToAdd;
    private final int newTotalMembers;
    private final double percentageUsed;

    public TierLimitExceededException(
            String message,
            int currentMemberCount,
            Integer tierMaxMembers,
            int membersToAdd,
            int newTotalMembers,
            double percentageUsed
    ) {
        super(message);
        this.currentMemberCount = currentMemberCount;
        this.tierMaxMembers = tierMaxMembers;
        this.membersToAdd = membersToAdd;
        this.newTotalMembers = newTotalMembers;
        this.percentageUsed = percentageUsed;
    }

    /**
     * Get a detailed error message for API responses.
     *
     * @return Detailed error message with upgrade suggestions
     */
    public String getDetailedMessage() {
        return String.format(
                "Tier limit exceeded: Cannot add %d member(s). " +
                "Current: %d members, After addition: %d members, Tier max: %d members (%.1f%% of limit). " +
                "Please upgrade your subscription tier to add more members.",
                membersToAdd,
                currentMemberCount,
                newTotalMembers,
                tierMaxMembers,
                percentageUsed
        );
    }

    /**
     * Get upgrade recommendation message.
     *
     * @return Message suggesting next tier upgrade
     */
    public String getUpgradeRecommendation() {
        if (tierMaxMembers == null) {
            return "You are on the Enterprise tier with unlimited members.";
        }

        if (newTotalMembers <= 200) {
            return "Consider upgrading to Growing Church tier (201-500 members).";
        } else if (newTotalMembers <= 500) {
            return "Consider upgrading to Medium Church tier (501-1000 members).";
        } else if (newTotalMembers <= 1000) {
            return "Consider upgrading to Large Church tier (1001-2000 members).";
        } else {
            return "Consider upgrading to Enterprise tier (unlimited members).";
        }
    }
}
