package com.reuben.pastcare_spring.dtos;

/**
 * DTO for providing smart household suggestions to the frontend
 * based on family relations (spouse, children) being linked.
 */
public record HouseholdSuggestion(

    /**
     * Suggested action type:
     * - NONE: No household action suggested
     * - JOIN_SPOUSE_HOUSEHOLD: Suggest joining spouse's existing household
     * - CREATE_WITH_SPOUSE: Suggest creating new household with spouse (spouse has no household)
     * - CREATE_WITH_CHILDREN: Suggest creating household with children
     * - CREATE_SINGLE: Suggest creating household for single member
     */
    String action,

    /**
     * ID of existing household (for JOIN_SPOUSE_HOUSEHOLD action)
     */
    Long householdId,

    /**
     * Name of existing household (for JOIN_SPOUSE_HOUSEHOLD action)
     */
    String householdName,

    /**
     * Suggested name for new household (for CREATE_* actions)
     */
    String suggestedName,

    /**
     * User-friendly message to display in the UI
     * Examples:
     * - "Join John Doe's household?"
     * - "Create household with Mary and 2 children?"
     * - "Create Smith Family household?"
     */
    String message,

    /**
     * Additional context information
     */
    String details

) {

    /**
     * Factory method for no suggestion
     */
    public static HouseholdSuggestion none() {
        return new HouseholdSuggestion(
            "NONE",
            null,
            null,
            null,
            "No household suggestion available",
            null
        );
    }

    /**
     * Factory method for joining spouse's household
     */
    public static HouseholdSuggestion joinSpouseHousehold(Long householdId, String householdName, String spouseName) {
        return new HouseholdSuggestion(
            "JOIN_SPOUSE_HOUSEHOLD",
            householdId,
            householdName,
            null,
            "Join " + spouseName + "'s household (" + householdName + ")?",
            "Your spouse is already part of this household"
        );
    }

    /**
     * Factory method for creating household with spouse
     */
    public static HouseholdSuggestion createWithSpouse(String suggestedName, String spouseName) {
        return new HouseholdSuggestion(
            "CREATE_WITH_SPOUSE",
            null,
            null,
            suggestedName,
            "Create household with " + spouseName + "?",
            "You and your spouse will be part of this new household"
        );
    }

    /**
     * Factory method for creating household with children
     */
    public static HouseholdSuggestion createWithChildren(String suggestedName, int childrenCount) {
        return new HouseholdSuggestion(
            "CREATE_WITH_CHILDREN",
            null,
            null,
            suggestedName,
            "Create household with " + childrenCount + " " + (childrenCount == 1 ? "child" : "children") + "?",
            "This member and their children will be part of this household"
        );
    }

    /**
     * Factory method for creating household for single member
     */
    public static HouseholdSuggestion createSingle(String suggestedName) {
        return new HouseholdSuggestion(
            "CREATE_SINGLE",
            null,
            null,
            suggestedName,
            "Create household for this member?",
            "A new household will be created with this member"
        );
    }
}
