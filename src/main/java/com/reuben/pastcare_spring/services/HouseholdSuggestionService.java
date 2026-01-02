package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.HouseholdSuggestion;
import com.reuben.pastcare_spring.dtos.MemberRequest;
import com.reuben.pastcare_spring.models.Household;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for providing intelligent household suggestions based on family relations.
 * Analyzes spouse and children linkages to suggest appropriate household actions.
 */
@Service
public class HouseholdSuggestionService {

    @Autowired
    private MemberRepository memberRepository;

    /**
     * Suggests household action based on the member request data.
     * Priority order:
     * 1. If linking spouse with household → suggest joining spouse's household
     * 2. If linking spouse without household → suggest creating household together
     * 3. If linking children → suggest creating household with children
     * 4. Otherwise → no suggestion
     *
     * @param request The member request containing family relation data
     * @param churchId The church ID for validation
     * @return HouseholdSuggestion with recommended action
     */
    @Transactional(readOnly = true)
    public HouseholdSuggestion suggestHouseholdForMember(MemberRequest request, Long churchId) {

        // Priority 1: Check if linking spouse
        if (request.linkSpouseId() != null) {
            return suggestBasedOnSpouse(request.linkSpouseId(), request.lastName(), churchId);
        }

        // Priority 2: Check if linking children
        if (request.linkChildIds() != null && !request.linkChildIds().isEmpty()) {
            return suggestBasedOnChildren(request.lastName(), request.linkChildIds().size());
        }

        // Priority 3: Check if linking parents (might be adult child living with parents)
        if (request.linkParentIds() != null && !request.linkParentIds().isEmpty()) {
            return suggestBasedOnParents(request.linkParentIds(), request.lastName(), churchId);
        }

        // No family relations → no suggestion
        return HouseholdSuggestion.none();
    }

    /**
     * Suggests household action when spouse is being linked.
     */
    private HouseholdSuggestion suggestBasedOnSpouse(Long spouseId, String memberLastName, Long churchId) {
        Member spouse = memberRepository.findById(spouseId).orElse(null);

        if (spouse == null) {
            return HouseholdSuggestion.none();
        }

        // Validate spouse belongs to same church
        if (!spouse.getChurch().getId().equals(churchId)) {
            return HouseholdSuggestion.none();
        }

        Household spouseHousehold = spouse.getHousehold();

        if (spouseHousehold != null) {
            // Spouse has household → suggest joining it
            return HouseholdSuggestion.joinSpouseHousehold(
                spouseHousehold.getId(),
                spouseHousehold.getHouseholdName(),
                spouse.getFirstName() + " " + spouse.getLastName()
            );
        } else {
            // Spouse has no household → suggest creating one together
            String suggestedName = (memberLastName != null ? memberLastName : spouse.getLastName()) + " Family";
            return HouseholdSuggestion.createWithSpouse(
                suggestedName,
                spouse.getFirstName() + " " + spouse.getLastName()
            );
        }
    }

    /**
     * Suggests household action when children are being linked.
     */
    private HouseholdSuggestion suggestBasedOnChildren(String memberLastName, int childrenCount) {
        String suggestedName = (memberLastName != null ? memberLastName : "New") + " Household";
        return HouseholdSuggestion.createWithChildren(suggestedName, childrenCount);
    }

    /**
     * Suggests household action when parents are being linked.
     * This handles the case of an adult child who might live with parents.
     */
    private HouseholdSuggestion suggestBasedOnParents(java.util.List<Long> parentIds, String memberLastName, Long churchId) {
        // Check if any parent has a household
        for (Long parentId : parentIds) {
            Member parent = memberRepository.findById(parentId).orElse(null);
            if (parent != null && parent.getHousehold() != null) {
                // Parent has household → suggest joining it
                Household parentHousehold = parent.getHousehold();
                return new HouseholdSuggestion(
                    "JOIN_PARENT_HOUSEHOLD",
                    parentHousehold.getId(),
                    parentHousehold.getHouseholdName(),
                    null,
                    "Join parent's household (" + parentHousehold.getHouseholdName() + ")?",
                    "Your parent is part of this household"
                );
            }
        }

        // No parent has household → no suggestion
        return HouseholdSuggestion.none();
    }

    /**
     * Suggests household action for an existing member being updated.
     * Takes into account current household status.
     */
    @Transactional(readOnly = true)
    public HouseholdSuggestion suggestHouseholdForExistingMember(Long memberId, MemberRequest request, Long churchId) {
        Member existingMember = memberRepository.findById(memberId).orElse(null);

        if (existingMember == null) {
            return HouseholdSuggestion.none();
        }

        // If member already has household and no new household action requested, keep current
        if (existingMember.getHousehold() != null &&
            request.householdId() == null &&
            !Boolean.TRUE.equals(request.createNewHousehold())) {
            return new HouseholdSuggestion(
                "KEEP_CURRENT",
                existingMember.getHousehold().getId(),
                existingMember.getHousehold().getHouseholdName(),
                null,
                "Keep current household (" + existingMember.getHousehold().getHouseholdName() + ")?",
                "This member is already part of this household"
            );
        }

        // Otherwise, use same logic as new member
        return suggestHouseholdForMember(request, churchId);
    }
}
