package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.PrayerRequestDto;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.PrayerRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PrayerRequestService {

    private final PrayerRequestRepository prayerRequestRepository;
    private final MemberRepository memberRepository;

    /**
     * Submit a new prayer request
     */
    public PrayerRequest submitPrayerRequest(Long churchId, Long memberId, PrayerRequestDto dto) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (!member.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to member");
        }

        PrayerRequest prayerRequest = new PrayerRequest();
        prayerRequest.setMember(member);
        prayerRequest.setRequest(dto.getRequest());
        prayerRequest.setCategory(dto.getCategory());
        prayerRequest.setPriority(dto.getPriority() != null ? dto.getPriority() : PrayerRequestPriority.NORMAL);
        prayerRequest.setIsAnonymous(dto.getIsAnonymous() != null ? dto.getIsAnonymous() : false);
        prayerRequest.setIsUrgent(dto.getIsUrgent() != null ? dto.getIsUrgent() : false);
        prayerRequest.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false);
        prayerRequest.setStatus(PrayerRequestStatus.PENDING);
        prayerRequest.setChurchId(churchId);

        // Set expiry date if not provided (default 30 days)
        if (dto.getExpiresAt() != null) {
            prayerRequest.setExpiresAt(dto.getExpiresAt());
        } else {
            prayerRequest.setExpiresAt(LocalDateTime.now().plusDays(30));
        }

        PrayerRequest saved = prayerRequestRepository.save(prayerRequest);
        log.info("Prayer request submitted by member: {} (ID: {})", member.getFirstName(), memberId);

        return saved;
    }

    /**
     * Get all prayer requests for a church
     */
    @Transactional(readOnly = true)
    public List<PrayerRequest> getAllPrayerRequests(Long churchId) {
        return prayerRequestRepository.findByChurchIdOrderByCreatedAtDesc(churchId);
    }

    /**
     * Get prayer requests by member
     */
    @Transactional(readOnly = true)
    public List<PrayerRequest> getMemberPrayerRequests(Long memberId) {
        return prayerRequestRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    /**
     * Get prayer requests by status
     */
    @Transactional(readOnly = true)
    public List<PrayerRequest> getPrayerRequestsByStatus(Long churchId, PrayerRequestStatus status) {
        return prayerRequestRepository.findByChurchIdAndStatusOrderByCreatedAtDesc(churchId, status);
    }

    /**
     * Get public prayer requests (for prayer team/church)
     */
    @Transactional(readOnly = true)
    public List<PrayerRequest> getPublicPrayerRequests(Long churchId) {
        return prayerRequestRepository.findPublicPrayerRequests(churchId, PrayerRequestStatus.ACTIVE);
    }

    /**
     * Get urgent prayer requests
     */
    @Transactional(readOnly = true)
    public List<PrayerRequest> getUrgentPrayerRequests(Long churchId) {
        return prayerRequestRepository.findByChurchIdAndIsUrgentTrueOrderByCreatedAtDesc(churchId);
    }

    /**
     * Update prayer request status
     */
    public PrayerRequest updateStatus(Long churchId, Long prayerRequestId, PrayerRequestStatus status) {
        PrayerRequest prayerRequest = prayerRequestRepository.findById(prayerRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Prayer request not found"));

        if (!prayerRequest.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to prayer request");
        }

        prayerRequest.setStatus(status);

        if (status == PrayerRequestStatus.ANSWERED) {
            prayerRequest.setAnsweredAt(LocalDateTime.now());
        }

        return prayerRequestRepository.save(prayerRequest);
    }

    /**
     * Add testimony for answered prayer
     */
    public PrayerRequest addTestimony(Long churchId, Long prayerRequestId, Long memberId, String testimony) {
        PrayerRequest prayerRequest = prayerRequestRepository.findById(prayerRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Prayer request not found"));

        if (!prayerRequest.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to prayer request");
        }

        // Only the member who submitted can add testimony
        if (!prayerRequest.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("Only the prayer request owner can add testimony");
        }

        prayerRequest.setTestimony(testimony);
        prayerRequest.setStatus(PrayerRequestStatus.ANSWERED);
        prayerRequest.setAnsweredAt(LocalDateTime.now());

        PrayerRequest updated = prayerRequestRepository.save(prayerRequest);
        log.info("Testimony added for prayer request: {}", prayerRequestId);

        return updated;
    }

    /**
     * Get prayer testimonies
     */
    @Transactional(readOnly = true)
    public List<PrayerRequest> getPrayerTestimonies(Long churchId) {
        return prayerRequestRepository.findByChurchIdAndStatusAndTestimonyIsNotNullOrderByAnsweredAtDesc(
            churchId, PrayerRequestStatus.ANSWERED
        );
    }

    /**
     * Archive expired prayer requests
     */
    public int archiveExpiredPrayerRequests() {
        List<PrayerRequest> expired = prayerRequestRepository.findExpiredPrayerRequests(
            LocalDateTime.now(), PrayerRequestStatus.ARCHIVED
        );

        for (PrayerRequest request : expired) {
            request.setStatus(PrayerRequestStatus.ARCHIVED);
        }

        prayerRequestRepository.saveAll(expired);
        log.info("Archived {} expired prayer requests", expired.size());

        return expired.size();
    }

    /**
     * Delete prayer request (soft delete by archiving)
     */
    public void deletePrayerRequest(Long churchId, Long prayerRequestId, Long memberId) {
        PrayerRequest prayerRequest = prayerRequestRepository.findById(prayerRequestId)
            .orElseThrow(() -> new IllegalArgumentException("Prayer request not found"));

        if (!prayerRequest.getChurchId().equals(churchId)) {
            throw new IllegalArgumentException("Unauthorized access to prayer request");
        }

        // Only the member who submitted can delete
        if (!prayerRequest.getMember().getId().equals(memberId)) {
            throw new IllegalArgumentException("Only the prayer request owner can delete");
        }

        prayerRequest.setStatus(PrayerRequestStatus.ARCHIVED);
        prayerRequestRepository.save(prayerRequest);
        log.info("Prayer request archived: {}", prayerRequestId);
    }
}
