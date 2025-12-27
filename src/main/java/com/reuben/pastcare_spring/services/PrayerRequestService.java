package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.PrayerRequestRequest;
import com.reuben.pastcare_spring.dtos.PrayerRequestResponse;
import com.reuben.pastcare_spring.dtos.PrayerRequestStatsResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing prayer requests
 */
@Service
@RequiredArgsConstructor
public class PrayerRequestService {

    private final PrayerRequestRepository prayerRequestRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;

    /**
     * Create a new prayer request (submit request)
     */
    @Transactional
    public PrayerRequestResponse createPrayerRequest(Long churchId, PrayerRequestRequest request, Long currentUserId) {
        PrayerRequest prayerRequest = new PrayerRequest();

        // Set church (tenant isolation)
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        prayerRequest.setChurch(church);

        // Set member
        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.getMemberId()));
        prayerRequest.setMember(member);

        // Set submitted by user
        User submittedBy = userRepository.findById(currentUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + currentUserId));
        prayerRequest.setSubmittedBy(submittedBy);

        updatePrayerRequestFromRequest(prayerRequest, request);

        PrayerRequest saved = prayerRequestRepository.save(prayerRequest);
        return PrayerRequestResponse.fromEntity(saved);
    }

    /**
     * Get prayer request by ID
     */
    public PrayerRequestResponse getPrayerRequestById(Long id) {
        PrayerRequest prayerRequest = prayerRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Prayer request not found with id: " + id));
        return PrayerRequestResponse.fromEntity(prayerRequest);
    }

    /**
     * Get all prayer requests for a church with pagination
     */
    public Page<PrayerRequestResponse> getPrayerRequests(Long churchId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return prayerRequestRepository.findByChurch(church, pageable)
            .map(PrayerRequestResponse::fromEntity);
    }

    /**
     * Update an existing prayer request
     */
    @Transactional
    public PrayerRequestResponse updatePrayerRequest(Long id, PrayerRequestRequest request) {
        PrayerRequest prayerRequest = prayerRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Prayer request not found with id: " + id));

        // Update member if changed
        if (!prayerRequest.getMember().getId().equals(request.getMemberId())) {
            Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.getMemberId()));
            prayerRequest.setMember(member);
        }

        updatePrayerRequestFromRequest(prayerRequest, request);
        PrayerRequest updated = prayerRequestRepository.save(prayerRequest);
        return PrayerRequestResponse.fromEntity(updated);
    }

    /**
     * Delete a prayer request
     */
    @Transactional
    public void deletePrayerRequest(Long id) {
        PrayerRequest prayerRequest = prayerRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Prayer request not found with id: " + id));
        prayerRequestRepository.delete(prayerRequest);
    }

    /**
     * Increment prayer count (when someone prays)
     */
    @Transactional
    public PrayerRequestResponse incrementPrayerCount(Long id) {
        PrayerRequest prayerRequest = prayerRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Prayer request not found with id: " + id));

        prayerRequest.incrementPrayerCount();
        PrayerRequest updated = prayerRequestRepository.save(prayerRequest);
        return PrayerRequestResponse.fromEntity(updated);
    }

    /**
     * Mark prayer request as answered with testimony
     */
    @Transactional
    public PrayerRequestResponse markAsAnswered(Long id, String testimony) {
        PrayerRequest prayerRequest = prayerRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Prayer request not found with id: " + id));

        prayerRequest.setStatus(PrayerRequestStatus.ANSWERED);
        prayerRequest.setAnsweredDate(LocalDateTime.now());

        if (testimony != null && !testimony.isEmpty()) {
            prayerRequest.setTestimony(testimony);
        }

        PrayerRequest updated = prayerRequestRepository.save(prayerRequest);
        return PrayerRequestResponse.fromEntity(updated);
    }

    /**
     * Archive/expire prayer requests
     */
    @Transactional
    public PrayerRequestResponse archivePrayerRequest(Long id) {
        PrayerRequest prayerRequest = prayerRequestRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Prayer request not found with id: " + id));

        prayerRequest.setStatus(PrayerRequestStatus.ARCHIVED);
        PrayerRequest updated = prayerRequestRepository.save(prayerRequest);
        return PrayerRequestResponse.fromEntity(updated);
    }

    /**
     * Get active prayer requests
     */
    public List<PrayerRequestResponse> getActivePrayerRequests(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return prayerRequestRepository.findActivePrayerRequests(church).stream()
            .map(PrayerRequestResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get urgent prayer requests
     */
    public List<PrayerRequestResponse> getUrgentPrayerRequests(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return prayerRequestRepository.findUrgentPrayerRequests(church).stream()
            .map(PrayerRequestResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get prayer requests by status
     */
    public Page<PrayerRequestResponse> getPrayerRequestsByStatus(Long churchId, PrayerRequestStatus status, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return prayerRequestRepository.findByChurchAndStatus(church, status, pageable)
            .map(PrayerRequestResponse::fromEntity);
    }

    /**
     * Get prayer requests by category
     */
    public Page<PrayerRequestResponse> getPrayerRequestsByCategory(Long churchId, PrayerCategory category, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return prayerRequestRepository.findByChurchAndCategory(church, category, pageable)
            .map(PrayerRequestResponse::fromEntity);
    }

    /**
     * Get prayer requests submitted by a specific user
     */
    public Page<PrayerRequestResponse> getMyPrayerRequests(Long churchId, Long userId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
        return prayerRequestRepository.findByChurchAndSubmittedBy(church, user, pageable)
            .map(PrayerRequestResponse::fromEntity);
    }

    /**
     * Get answered prayer requests with testimonies
     */
    public Page<PrayerRequestResponse> getAnsweredPrayerRequests(Long churchId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return prayerRequestRepository.findAnsweredPrayerRequests(church, pageable)
            .map(PrayerRequestResponse::fromEntity);
    }

    /**
     * Get public prayer requests (for member portal)
     */
    public Page<PrayerRequestResponse> getPublicPrayerRequests(Long churchId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return prayerRequestRepository.findPublicPrayerRequests(church, pageable)
            .map(PrayerRequestResponse::fromEntity);
    }

    /**
     * Search prayer requests
     */
    public Page<PrayerRequestResponse> searchPrayerRequests(Long churchId, String search, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return prayerRequestRepository.searchPrayerRequests(church, search, pageable)
            .map(PrayerRequestResponse::fromEntity);
    }

    /**
     * Get prayer request statistics for a church
     */
    public PrayerRequestStatsResponse getPrayerRequestStats(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        long total = prayerRequestRepository.countByChurch(church);
        long pending = prayerRequestRepository.countByChurchAndStatus(church, PrayerRequestStatus.PENDING);
        long active = prayerRequestRepository.countByChurchAndStatus(church, PrayerRequestStatus.ACTIVE);
        long answered = prayerRequestRepository.countByChurchAndStatus(church, PrayerRequestStatus.ANSWERED);
        long urgent = prayerRequestRepository.countUrgent(church);
        long publicRequests = prayerRequestRepository.countActivePublic(church);

        return new PrayerRequestStatsResponse(total, pending, active, answered, urgent, publicRequests);
    }

    /**
     * Get prayer requests expiring soon
     */
    public List<PrayerRequestResponse> getExpiringSoon(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        LocalDate today = LocalDate.now();
        LocalDate nextWeek = today.plusDays(7);
        return prayerRequestRepository.findExpiringSoon(church, today, nextWeek).stream()
            .map(PrayerRequestResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Auto-archive expired prayer requests
     */
    @Transactional
    public int autoArchiveExpiredRequests(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        List<PrayerRequest> expired = prayerRequestRepository.findExpiredPrayerRequests(church, LocalDate.now());

        for (PrayerRequest pr : expired) {
            pr.setStatus(PrayerRequestStatus.ARCHIVED);
        }

        prayerRequestRepository.saveAll(expired);
        return expired.size();
    }

    /**
     * Helper method to update prayer request from request DTO
     */
    private void updatePrayerRequestFromRequest(PrayerRequest prayerRequest, PrayerRequestRequest request) {
        prayerRequest.setTitle(request.getTitle());
        prayerRequest.setDescription(request.getDescription());
        prayerRequest.setCategory(request.getCategory());
        prayerRequest.setPriority(request.getPriority() != null ? request.getPriority() : PrayerPriority.NORMAL);
        prayerRequest.setIsAnonymous(request.getIsAnonymous() != null ? request.getIsAnonymous() : false);
        prayerRequest.setIsUrgent(request.getIsUrgent() != null ? request.getIsUrgent() : false);
        prayerRequest.setExpirationDate(request.getExpirationDate());
        prayerRequest.setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : true);
        prayerRequest.setTags(request.getTags());

        // Handle status if provided
        if (request.getStatus() != null) {
            prayerRequest.setStatus(request.getStatus());
        } else if (prayerRequest.getStatus() == null) {
            prayerRequest.setStatus(PrayerRequestStatus.PENDING);
        }
    }
}
