package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.exceptions.TenantViolationException;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.security.TenantContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service for validating tenant (church) access to resources.
 * Prevents cross-tenant data access by checking that resources belong to the current church.
 *
 * This provides defense in depth alongside:
 * - TenantContextFilter (validates JWT churchId)
 * - Hibernate filters (automatic query filtering)
 * - Repository methods (church-scoped queries)
 */
@Service
public class TenantValidationService {

    private static final Logger logger = LoggerFactory.getLogger(TenantValidationService.class);

    /**
     * Validate that a member belongs to the current church.
     * Throws TenantViolationException if not.
     */
    public void validateMemberAccess(Member member) {
        if (member == null) {
            return; // Let the calling code handle null
        }

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        // SUPERADMIN can access any church
        if (TenantContext.isSuperadmin()) {
            logger.debug("SUPERADMIN access granted for member {} from church {}",
                    member.getId(), member.getChurch().getId());
            return;
        }

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        Long memberChurchId = member.getChurch().getId();
        if (!currentChurchId.equals(memberChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    userId,
                    memberChurchId,
                    currentChurchId,
                    "Member:" + member.getId()
            );
        }
    }

    /**
     * Validate that a donation belongs to the current church.
     */
    public void validateDonationAccess(Donation donation) {
        if (donation == null) return;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        if (TenantContext.isSuperadmin()) return;

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        Long donationChurchId = donation.getChurch().getId();
        if (!currentChurchId.equals(donationChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    userId,
                    donationChurchId,
                    currentChurchId,
                    "Donation:" + donation.getId()
            );
        }
    }

    /**
     * Validate that an event belongs to the current church.
     */
    public void validateEventAccess(Event event) {
        if (event == null) return;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        if (TenantContext.isSuperadmin()) return;

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        Long eventChurchId = event.getChurch().getId();
        if (!currentChurchId.equals(eventChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    userId,
                    eventChurchId,
                    currentChurchId,
                    "Event:" + event.getId()
            );
        }
    }

    /**
     * Validate that a visit belongs to the current church.
     */
    public void validateVisitAccess(Visit visit) {
        if (visit == null) return;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        if (TenantContext.isSuperadmin()) return;

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        Long visitChurchId = visit.getChurch().getId();
        if (!currentChurchId.equals(visitChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    userId,
                    visitChurchId,
                    currentChurchId,
                    "Visit:" + visit.getId()
            );
        }
    }

    /**
     * Validate that a household belongs to the current church.
     */
    public void validateHouseholdAccess(Household household) {
        if (household == null) return;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        if (TenantContext.isSuperadmin()) return;

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        Long householdChurchId = household.getChurch().getId();
        if (!currentChurchId.equals(householdChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    userId,
                    householdChurchId,
                    currentChurchId,
                    "Household:" + household.getId()
            );
        }
    }

    /**
     * Validate that a fellowship belongs to the current church.
     */
    public void validateFellowshipAccess(Fellowship fellowship) {
        if (fellowship == null) return;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        if (TenantContext.isSuperadmin()) return;

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        Long fellowshipChurchId = fellowship.getChurch().getId();
        if (!currentChurchId.equals(fellowshipChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    userId,
                    fellowshipChurchId,
                    currentChurchId,
                    "Fellowship:" + fellowship.getId()
            );
        }
    }

    /**
     * Validate that a campaign belongs to the current church.
     */
    public void validateCampaignAccess(Campaign campaign) {
        if (campaign == null) return;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        if (TenantContext.isSuperadmin()) return;

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        Long campaignChurchId = campaign.getChurch().getId();
        if (!currentChurchId.equals(campaignChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    userId,
                    campaignChurchId,
                    currentChurchId,
                    "Campaign:" + campaign.getId()
            );
        }
    }

    /**
     * Validate that a care need belongs to the current church.
     */
    public void validateCareNeedAccess(CareNeed careNeed) {
        if (careNeed == null) return;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        if (TenantContext.isSuperadmin()) return;

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        Long careNeedChurchId = careNeed.getChurch().getId();
        if (!currentChurchId.equals(careNeedChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    userId,
                    careNeedChurchId,
                    currentChurchId,
                    "CareNeed:" + careNeed.getId()
            );
        }
    }

    /**
     * Validate that a prayer request belongs to the current church.
     */
    public void validatePrayerRequestAccess(PrayerRequest prayerRequest) {
        if (prayerRequest == null) return;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        if (TenantContext.isSuperadmin()) return;

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        Long prayerRequestChurchId = prayerRequest.getChurch().getId();
        if (!currentChurchId.equals(prayerRequestChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    userId,
                    prayerRequestChurchId,
                    currentChurchId,
                    "PrayerRequest:" + prayerRequest.getId()
            );
        }
    }

    /**
     * Validate that an attendance session belongs to the current church.
     */
    public void validateAttendanceSessionAccess(AttendanceSession attendanceSession) {
        if (attendanceSession == null) return;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        if (TenantContext.isSuperadmin()) return;

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        Long sessionChurchId = attendanceSession.getChurch().getId();
        if (!currentChurchId.equals(sessionChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    userId,
                    sessionChurchId,
                    currentChurchId,
                    "AttendanceSession:" + attendanceSession.getId()
            );
        }
    }

    /**
     * Generic validation for any entity with a church field.
     * Uses reflection to get church ID.
     */
    public void validateChurchAccess(Object entity, String entityType) {
        if (entity == null) return;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        Long userId = TenantContext.getCurrentUserId();

        if (TenantContext.isSuperadmin()) return;

        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        try {
            // Try to get church using reflection
            java.lang.reflect.Method getChurch = entity.getClass().getMethod("getChurch");
            Church church = (Church) getChurch.invoke(entity);

            if (church != null) {
                Long entityChurchId = church.getId();
                if (!currentChurchId.equals(entityChurchId)) {
                    throw new TenantViolationException(
                            "Cross-tenant access denied",
                            userId,
                            entityChurchId,
                            currentChurchId,
                            entityType
                    );
                }
            }
        } catch (Exception e) {
            logger.warn("Could not validate church access for entity type {}: {}",
                    entityType, e.getMessage());
        }
    }

    /**
     * Check if a resource belongs to the current user's church.
     * This is the primary validation - users can only access resources from their own church.
     */
    public boolean isFromCurrentChurch(Church entityChurch) {
        if (entityChurch == null) return false;

        Long currentChurchId = TenantContext.getCurrentChurchId();
        if (currentChurchId == null) return false;

        return currentChurchId.equals(entityChurch.getId());
    }

    /**
     * Validate that the current user has access to a specific church.
     * SUPERADMIN can access any church.
     * Other users can only access their own church.
     */
    public void validateChurchId(Long requestedChurchId) {
        if (requestedChurchId == null) {
            throw new TenantViolationException("Church ID is required");
        }

        if (TenantContext.isSuperadmin()) {
            return; // SUPERADMIN can access any church
        }

        Long currentChurchId = TenantContext.getCurrentChurchId();
        if (currentChurchId == null) {
            throw new TenantViolationException("No church context set for current user");
        }

        if (!currentChurchId.equals(requestedChurchId)) {
            throw new TenantViolationException(
                    "Cross-tenant access denied",
                    TenantContext.getCurrentUserId(),
                    requestedChurchId,
                    currentChurchId,
                    "Church:" + requestedChurchId
            );
        }
    }
}
