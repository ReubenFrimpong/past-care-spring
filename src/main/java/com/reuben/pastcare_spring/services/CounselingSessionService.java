package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.CounselingSessionRequest;
import com.reuben.pastcare_spring.dtos.CounselingSessionResponse;
import com.reuben.pastcare_spring.dtos.CounselingSessionStatsResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing counseling sessions
 */
@Service
@RequiredArgsConstructor
public class CounselingSessionService {

    private final CounselingSessionRepository counselingSessionRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;
    private final CareNeedRepository careNeedRepository;

    /**
     * Create a new counseling session
     */
    @Transactional
    public CounselingSessionResponse createSession(Long churchId, CounselingSessionRequest request) {
        CounselingSession session = new CounselingSession();

        // Set church (tenant isolation)
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        session.setChurch(church);

        // Set member
        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.getMemberId()));
        session.setMember(member);

        // Set counselor
        User counselor = userRepository.findById(request.getCounselorId())
            .orElseThrow(() -> new IllegalArgumentException("Counselor not found with id: " + request.getCounselorId()));
        session.setCounselor(counselor);

        updateSessionFromRequest(session, request);

        CounselingSession saved = counselingSessionRepository.save(session);
        return CounselingSessionResponse.fromEntity(saved);
    }

    /**
     * Get counseling session by ID
     */
    public CounselingSessionResponse getSessionById(Long id) {
        CounselingSession session = counselingSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Counseling session not found with id: " + id));
        return CounselingSessionResponse.fromEntity(session);
    }

    /**
     * Get all counseling sessions for a church with pagination
     */
    public Page<CounselingSessionResponse> getSessions(Long churchId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return counselingSessionRepository.findByChurch(church, pageable)
            .map(CounselingSessionResponse::fromEntity);
    }

    /**
     * Update an existing counseling session
     */
    @Transactional
    public CounselingSessionResponse updateSession(Long id, CounselingSessionRequest request) {
        CounselingSession session = counselingSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Counseling session not found with id: " + id));

        // Update member if changed
        if (!session.getMember().getId().equals(request.getMemberId())) {
            Member member = memberRepository.findById(request.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.getMemberId()));
            session.setMember(member);
        }

        // Update counselor if changed
        if (!session.getCounselor().getId().equals(request.getCounselorId())) {
            User counselor = userRepository.findById(request.getCounselorId())
                .orElseThrow(() -> new IllegalArgumentException("Counselor not found with id: " + request.getCounselorId()));
            session.setCounselor(counselor);
        }

        updateSessionFromRequest(session, request);
        CounselingSession updated = counselingSessionRepository.save(session);
        return CounselingSessionResponse.fromEntity(updated);
    }

    /**
     * Delete a counseling session
     */
    @Transactional
    public void deleteSession(Long id) {
        CounselingSession session = counselingSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Counseling session not found with id: " + id));
        counselingSessionRepository.delete(session);
    }

    /**
     * Complete a counseling session and add outcome
     */
    @Transactional
    public CounselingSessionResponse completeSession(Long id, String outcome, SessionOutcome sessionOutcome) {
        CounselingSession session = counselingSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Counseling session not found with id: " + id));

        session.setStatus(CounselingStatus.COMPLETED);
        session.setOutcome(outcome);
        session.setSessionOutcome(sessionOutcome);

        CounselingSession updated = counselingSessionRepository.save(session);
        return CounselingSessionResponse.fromEntity(updated);
    }

    /**
     * Schedule follow-up for a session
     */
    @Transactional
    public CounselingSessionResponse scheduleFollowUp(Long id, LocalDateTime followUpDate, String followUpNotes) {
        CounselingSession session = counselingSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Counseling session not found with id: " + id));

        session.setFollowUpRequired(true);
        session.setFollowUpDate(followUpDate);
        session.setFollowUpNotes(followUpNotes);

        CounselingSession updated = counselingSessionRepository.save(session);
        return CounselingSessionResponse.fromEntity(updated);
    }

    /**
     * Create a referral for a session
     */
    @Transactional
    public CounselingSessionResponse createReferral(Long id, String referredTo, String referralOrganization,
                                                    String referralPhone, String referralNotes) {
        CounselingSession session = counselingSessionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Counseling session not found with id: " + id));

        session.setIsReferralNeeded(true);
        session.setReferredTo(referredTo);
        session.setReferralOrganization(referralOrganization);
        session.setReferralPhone(referralPhone);
        session.setReferralNotes(referralNotes);
        session.setReferralDate(LocalDateTime.now());

        CounselingSession updated = counselingSessionRepository.save(session);
        return CounselingSessionResponse.fromEntity(updated);
    }

    /**
     * Get sessions by counselor
     */
    public Page<CounselingSessionResponse> getSessionsByCounselor(Long churchId, Long counselorId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        User counselor = userRepository.findById(counselorId)
            .orElseThrow(() -> new IllegalArgumentException("Counselor not found with id: " + counselorId));
        return counselingSessionRepository.findByChurchAndCounselor(church, counselor, pageable)
            .map(CounselingSessionResponse::fromEntity);
    }

    /**
     * Get sessions by status
     */
    public Page<CounselingSessionResponse> getSessionsByStatus(Long churchId, CounselingStatus status, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return counselingSessionRepository.findByChurchAndStatus(church, status, pageable)
            .map(CounselingSessionResponse::fromEntity);
    }

    /**
     * Get sessions by type
     */
    public Page<CounselingSessionResponse> getSessionsByType(Long churchId, CounselingType type, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return counselingSessionRepository.findByChurchAndType(church, type, pageable)
            .map(CounselingSessionResponse::fromEntity);
    }

    /**
     * Get upcoming sessions
     */
    public List<CounselingSessionResponse> getUpcomingSessions(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return counselingSessionRepository.findUpcomingSessions(church, LocalDateTime.now()).stream()
            .map(CounselingSessionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get upcoming sessions for a counselor
     */
    public List<CounselingSessionResponse> getUpcomingSessionsByCounselor(Long counselorId) {
        User counselor = userRepository.findById(counselorId)
            .orElseThrow(() -> new IllegalArgumentException("Counselor not found with id: " + counselorId));
        return counselingSessionRepository.findUpcomingSessionsByCounselor(counselor, LocalDateTime.now()).stream()
            .map(CounselingSessionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get sessions requiring follow-up
     */
    public List<CounselingSessionResponse> getSessionsRequiringFollowUp(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return counselingSessionRepository.findSessionsRequiringFollowUp(church, LocalDateTime.now()).stream()
            .map(CounselingSessionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get sessions by member
     */
    public List<CounselingSessionResponse> getSessionsByMember(Long churchId, Long memberId) {
        // Validate church exists
        churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        return counselingSessionRepository.findByMemberOrderBySessionDateDesc(member).stream()
            .map(CounselingSessionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get sessions by care need
     */
    public List<CounselingSessionResponse> getSessionsByCareNeed(Long churchId, Long careNeedId) {
        // Validate church exists
        churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        CareNeed careNeed = careNeedRepository.findById(careNeedId)
            .orElseThrow(() -> new IllegalArgumentException("Care need not found with id: " + careNeedId));

        return counselingSessionRepository.findByCareNeedOrderBySessionDateDesc(careNeed).stream()
            .map(CounselingSessionResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Search counseling sessions
     */
    public Page<CounselingSessionResponse> searchSessions(Long churchId, String search, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return counselingSessionRepository.searchSessions(church, search, pageable)
            .map(CounselingSessionResponse::fromEntity);
    }

    /**
     * Get counseling session statistics for a church
     */
    public CounselingSessionStatsResponse getSessionStats(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        long total = counselingSessionRepository.countByChurch(church);
        long scheduled = counselingSessionRepository.countByChurchAndStatus(church, CounselingStatus.SCHEDULED);
        long completed = counselingSessionRepository.countByChurchAndStatus(church, CounselingStatus.COMPLETED);
        long cancelled = counselingSessionRepository.countByChurchAndStatus(church, CounselingStatus.CANCELLED);
        long followUpsNeeded = counselingSessionRepository.countSessionsRequiringFollowUp(church, LocalDateTime.now());
        long referralsMade = counselingSessionRepository.countSessionsWithReferrals(church);
        long individual = counselingSessionRepository.countByChurchAndType(church, CounselingType.INDIVIDUAL);
        long group = counselingSessionRepository.countByChurchAndType(church, CounselingType.GROUP);
        long crisis = counselingSessionRepository.countByChurchAndType(church, CounselingType.CRISIS);

        return new CounselingSessionStatsResponse(total, scheduled, completed, cancelled,
            followUpsNeeded, referralsMade, individual, group, crisis);
    }

    /**
     * Helper method to update session from request
     */
    private void updateSessionFromRequest(CounselingSession session, CounselingSessionRequest request) {
        session.setTitle(request.getTitle());
        session.setSessionNotes(request.getSessionNotes());
        session.setType(request.getType());
        session.setSessionDate(request.getSessionDate());
        session.setDurationMinutes(request.getDurationMinutes());
        session.setLocation(request.getLocation());

        // Handle status
        if (request.getStatus() != null) {
            session.setStatus(request.getStatus());
        } else if (session.getStatus() == null) {
            session.setStatus(CounselingStatus.SCHEDULED);
        }

        // Handle care need
        if (request.getCareNeedId() != null) {
            CareNeed careNeed = careNeedRepository.findById(request.getCareNeedId())
                .orElseThrow(() -> new IllegalArgumentException("Care need not found with id: " + request.getCareNeedId()));
            session.setCareNeed(careNeed);
        }

        // Referral fields
        session.setIsReferralNeeded(request.getIsReferralNeeded() != null ? request.getIsReferralNeeded() : false);
        session.setReferredTo(request.getReferredTo());
        session.setReferralOrganization(request.getReferralOrganization());
        session.setReferralPhone(request.getReferralPhone());
        session.setReferralNotes(request.getReferralNotes());
        session.setReferralDate(request.getReferralDate());

        // Follow-up
        session.setFollowUpRequired(request.getFollowUpRequired() != null ? request.getFollowUpRequired() : false);
        session.setFollowUpDate(request.getFollowUpDate());
        session.setFollowUpNotes(request.getFollowUpNotes());

        // Confidentiality
        session.setIsConfidential(request.getIsConfidential() != null ? request.getIsConfidential() : true);

        // Outcome
        session.setOutcome(request.getOutcome());
        session.setSessionOutcome(request.getSessionOutcome());
    }
}
