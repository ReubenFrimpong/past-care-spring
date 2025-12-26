package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.AutoDetectedCareNeed;
import com.reuben.pastcare_spring.dtos.CareNeedRequest;
import com.reuben.pastcare_spring.dtos.CareNeedResponse;
import com.reuben.pastcare_spring.dtos.CareNeedStatsResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing pastoral care needs
 */
@Service
@RequiredArgsConstructor
public class CareNeedService {

    private final CareNeedRepository careNeedRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;

    /**
     * Create a new care need
     */
    @Transactional
    public CareNeedResponse createCareNeed(Long churchId, CareNeedRequest request, Long currentUserId) {
        CareNeed careNeed = new CareNeed();

        // Set church (tenant isolation)
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        careNeed.setChurch(church);

        // Set member
        Member member = memberRepository.findById(request.memberId())
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.memberId()));
        careNeed.setMember(member);

        // Set created by user
        User createdBy = userRepository.findById(currentUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + currentUserId));
        careNeed.setCreatedBy(createdBy);

        updateCareNeedFromRequest(careNeed, request);

        CareNeed saved = careNeedRepository.save(careNeed);
        return CareNeedResponse.fromEntity(saved);
    }

    /**
     * Get care need by ID
     */
    public CareNeedResponse getCareNeedById(Long id) {
        CareNeed careNeed = careNeedRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Care need not found with id: " + id));
        return CareNeedResponse.fromEntity(careNeed);
    }

    /**
     * Get all care needs for a church with pagination
     */
    public Page<CareNeedResponse> getCareNeeds(Long churchId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return careNeedRepository.findByChurch(church, pageable)
            .map(CareNeedResponse::fromEntity);
    }

    /**
     * Update an existing care need
     */
    @Transactional
    public CareNeedResponse updateCareNeed(Long id, CareNeedRequest request) {
        CareNeed careNeed = careNeedRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Care need not found with id: " + id));

        // Update member if changed
        if (!careNeed.getMember().getId().equals(request.memberId())) {
            Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.memberId()));
            careNeed.setMember(member);
        }

        updateCareNeedFromRequest(careNeed, request);
        CareNeed updated = careNeedRepository.save(careNeed);
        return CareNeedResponse.fromEntity(updated);
    }

    /**
     * Delete a care need
     */
    @Transactional
    public void deleteCareNeed(Long id) {
        CareNeed careNeed = careNeedRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Care need not found with id: " + id));
        careNeedRepository.delete(careNeed);
    }

    /**
     * Assign a care need to a user
     */
    @Transactional
    public CareNeedResponse assignCareNeed(Long id, Long userId) {
        CareNeed careNeed = careNeedRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Care need not found with id: " + id));

        User assignedTo = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        careNeed.setAssignedTo(assignedTo);
        careNeed.setStatus(CareNeedStatus.ASSIGNED);

        CareNeed updated = careNeedRepository.save(careNeed);
        return CareNeedResponse.fromEntity(updated);
    }

    /**
     * Update the status of a care need
     */
    @Transactional
    public CareNeedResponse updateStatus(Long id, CareNeedStatus status) {
        CareNeed careNeed = careNeedRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Care need not found with id: " + id));

        careNeed.setStatus(status);

        CareNeed updated = careNeedRepository.save(careNeed);
        return CareNeedResponse.fromEntity(updated);
    }

    /**
     * Mark a care need as resolved
     */
    @Transactional
    public CareNeedResponse resolveCareNeed(Long id, String resolutionNotes) {
        CareNeed careNeed = careNeedRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Care need not found with id: " + id));

        careNeed.setStatus(CareNeedStatus.RESOLVED);
        careNeed.setResolvedDate(LocalDateTime.now());
        careNeed.setResolutionNotes(resolutionNotes);

        CareNeed updated = careNeedRepository.save(careNeed);
        return CareNeedResponse.fromEntity(updated);
    }

    /**
     * Get care needs by status
     */
    public Page<CareNeedResponse> getCareNeedsByStatus(Long churchId, CareNeedStatus status, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return careNeedRepository.findByChurchAndStatus(church, status, pageable)
            .map(CareNeedResponse::fromEntity);
    }

    /**
     * Get care needs by type
     */
    public Page<CareNeedResponse> getCareNeedsByType(Long churchId, CareNeedType type, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return careNeedRepository.findByChurchAndType(church, type, pageable)
            .map(CareNeedResponse::fromEntity);
    }

    /**
     * Get care needs assigned to a specific user
     */
    public Page<CareNeedResponse> getAssignedCareNeeds(Long churchId, Long userId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return careNeedRepository.findByChurchAndAssignedTo(church, user, pageable)
            .map(CareNeedResponse::fromEntity);
    }

    /**
     * Search care needs
     */
    public Page<CareNeedResponse> searchCareNeeds(Long churchId, String search, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return careNeedRepository.searchCareNeeds(church, search, pageable)
            .map(CareNeedResponse::fromEntity);
    }

    /**
     * Get care need statistics for a church
     */
    public CareNeedStatsResponse getCareNeedStats(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        long total = careNeedRepository.countByChurch(church);
        long pending = careNeedRepository.countByChurchAndStatus(church, CareNeedStatus.PENDING);
        long inProgress = careNeedRepository.countByChurchAndStatus(church, CareNeedStatus.IN_PROGRESS);
        long resolved = careNeedRepository.countByChurchAndStatus(church, CareNeedStatus.RESOLVED);
        long urgent = careNeedRepository.countByChurchAndPriority(church, CareNeedPriority.URGENT);

        List<CareNeed> overdue = careNeedRepository.findOverdueCareNeeds(church, LocalDateTime.now());

        return new CareNeedStatsResponse(total, pending, inProgress, resolved, urgent, overdue.size());
    }

    /**
     * Get overdue care needs
     */
    public List<CareNeedResponse> getOverdueCareNeeds(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return careNeedRepository.findOverdueCareNeeds(church, LocalDateTime.now()).stream()
            .map(CareNeedResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Auto-detect members needing care based on attendance patterns
     * Returns list of member IDs with 3+ consecutive absences (no attendance in last 21 days)
     */
    public List<Long> detectMembersNeedingCare(Long churchId) {
        return careNeedRepository.findMembersWithConsecutiveAbsences(churchId);
    }

    /**
     * Get detailed auto-detected care need suggestions
     * Returns members with 3+ weeks of absence who don't already have an active care need
     */
    public List<AutoDetectedCareNeed> getAutoDetectedCareNeeds(Long churchId) {
        List<Long> memberIds = detectMembersNeedingCare(churchId);

        return memberIds.stream()
            .map(memberId -> memberRepository.findById(memberId).orElse(null))
            .filter(member -> member != null)
            .filter(member -> {
                // Only suggest if member doesn't already have an active care need
                List<CareNeed> existingNeeds = careNeedRepository.findByMember(member);
                return existingNeeds.stream()
                    .noneMatch(cn -> cn.getStatus() != CareNeedStatus.RESOLVED &&
                                    cn.getStatus() != CareNeedStatus.CLOSED);
            })
            .map(member -> new AutoDetectedCareNeed(
                member.getId(),
                member.getFirstName() + " " + member.getLastName(),
                "No attendance recorded in the last 3 weeks",
                CareNeedType.SPIRITUAL,
                CareNeedPriority.HIGH,
                "Follow up with " + member.getFirstName() + " " + member.getLastName(),
                "Member has not attended services for 3 or more consecutive weeks. " +
                "Please reach out to check on their well-being and offer support.",
                3 // Minimum consecutive absences
            ))
            .collect(Collectors.toList());
    }

    /**
     * Helper method to update care need from request
     */
    private void updateCareNeedFromRequest(CareNeed careNeed, CareNeedRequest request) {
        careNeed.setTitle(request.title());
        careNeed.setDescription(request.description());
        careNeed.setType(request.type());
        careNeed.setPriority(request.priority() != null ? request.priority() : CareNeedPriority.NORMAL);
        careNeed.setDueDate(request.dueDate());
        careNeed.setFollowUpRequired(request.followUpRequired() != null ? request.followUpRequired() : false);
        careNeed.setFollowUpDate(request.followUpDate());

        // Handle assigned user
        if (request.assignedToUserId() != null) {
            User assignedTo = userRepository.findById(request.assignedToUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.assignedToUserId()));
            careNeed.setAssignedTo(assignedTo);
            // Auto-set status to ASSIGNED if not already set
            if (careNeed.getStatus() == CareNeedStatus.PENDING) {
                careNeed.setStatus(CareNeedStatus.ASSIGNED);
            }
        }

        // Handle tags
        if (request.tags() != null) {
            careNeed.setTags(new HashSet<>(request.tags()));
        }
    }
}
