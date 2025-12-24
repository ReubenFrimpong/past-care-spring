package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.CommunicationLogRequest;
import com.reuben.pastcare_spring.dtos.CommunicationLogResponse;
import com.reuben.pastcare_spring.mapper.CommunicationLogMapper;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.CommunicationLogRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing communication logs.
 * Handles all communication interactions with members (calls, emails, visits, etc.).
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@Service
@Transactional
public class CommunicationLogService {

    @Autowired
    private CommunicationLogRepository communicationLogRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChurchRepository churchRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new communication log for a member.
     */
    public CommunicationLogResponse createCommunicationLog(Long churchId, CommunicationLogRequest request, Long userId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Member member = memberRepository.findByIdAndChurch(request.memberId(), church)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + request.memberId()));

        CommunicationLog log = new CommunicationLog();
        log.setChurch(church);
        log.setMember(member);
        log.setCommunicationType(request.communicationType());
        log.setDirection(request.direction());
        log.setCommunicationDate(request.communicationDate());
        log.setDurationMinutes(request.durationMinutes());
        log.setSubject(request.subject());
        log.setNotes(request.notes());
        log.setFollowUpRequired(request.followUpRequired() != null ? request.followUpRequired() : false);
        log.setFollowUpDate(request.followUpDate());
        log.setFollowUpStatus(request.followUpStatus());
        log.setPriority(request.priority() != null ? request.priority() : CommunicationPriority.NORMAL);
        log.setOutcome(request.outcome());
        log.setIsConfidential(request.isConfidential() != null ? request.isConfidential() : false);
        log.setTags(request.tags());

        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            log.setUser(user);
        }

        CommunicationLog savedLog = communicationLogRepository.save(log);
        return CommunicationLogMapper.toResponse(savedLog);
    }

    /**
     * Update an existing communication log.
     */
    public CommunicationLogResponse updateCommunicationLog(Long churchId, Long logId, CommunicationLogRequest request, Long userId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        CommunicationLog log = communicationLogRepository.findById(logId)
            .orElseThrow(() -> new EntityNotFoundException("Communication log not found with id: " + logId));

        if (!log.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Communication log does not belong to your church");
        }

        log.setCommunicationType(request.communicationType());
        log.setDirection(request.direction());
        log.setCommunicationDate(request.communicationDate());
        log.setDurationMinutes(request.durationMinutes());
        log.setSubject(request.subject());
        log.setNotes(request.notes());
        log.setFollowUpRequired(request.followUpRequired() != null ? request.followUpRequired() : false);
        log.setFollowUpDate(request.followUpDate());
        log.setFollowUpStatus(request.followUpStatus());
        log.setPriority(request.priority() != null ? request.priority() : CommunicationPriority.NORMAL);
        log.setOutcome(request.outcome());
        log.setIsConfidential(request.isConfidential() != null ? request.isConfidential() : false);
        log.setTags(request.tags());

        CommunicationLog updatedLog = communicationLogRepository.save(log);
        return CommunicationLogMapper.toResponse(updatedLog);
    }

    /**
     * Get all communication logs for a member.
     */
    public List<CommunicationLogResponse> getMemberCommunicationLogs(Long churchId, Long memberId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Member member = memberRepository.findByIdAndChurch(memberId, church)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

        return communicationLogRepository.findByMemberOrderByCommunicationDateDesc(member).stream()
            .map(CommunicationLogMapper::toResponse)
            .toList();
    }

    /**
     * Get all communication logs for a church with pagination.
     */
    public Page<CommunicationLogResponse> getChurchCommunicationLogs(Long churchId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return communicationLogRepository.findByChurch(church, pageable)
            .map(CommunicationLogMapper::toResponse);
    }

    /**
     * Get communication logs by type.
     */
    public Page<CommunicationLogResponse> getCommunicationLogsByType(Long churchId, CommunicationType communicationType, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return communicationLogRepository.findByChurchAndCommunicationType(church, communicationType, pageable)
            .map(CommunicationLogMapper::toResponse);
    }

    /**
     * Get communication logs within a date range.
     */
    public List<CommunicationLogResponse> getCommunicationLogsByDateRange(Long churchId, LocalDateTime startDate, LocalDateTime endDate) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return communicationLogRepository.findByChurchAndCommunicationDateBetween(church, startDate, endDate).stream()
            .map(CommunicationLogMapper::toResponse)
            .toList();
    }

    /**
     * Get communication logs requiring follow-up.
     */
    public List<CommunicationLogResponse> getFollowUpRequired(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return communicationLogRepository.findByChurchAndFollowUpRequired(church).stream()
            .map(CommunicationLogMapper::toResponse)
            .toList();
    }

    /**
     * Get overdue follow-ups.
     */
    public List<CommunicationLogResponse> getOverdueFollowUps(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return communicationLogRepository.findOverdueFollowUps(church, LocalDateTime.now()).stream()
            .map(CommunicationLogMapper::toResponse)
            .toList();
    }

    /**
     * Update follow-up status.
     */
    public CommunicationLogResponse updateFollowUpStatus(Long churchId, Long logId, FollowUpStatus status) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        CommunicationLog log = communicationLogRepository.findById(logId)
            .orElseThrow(() -> new EntityNotFoundException("Communication log not found with id: " + logId));

        if (!log.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Communication log does not belong to your church");
        }

        log.setFollowUpStatus(status);
        CommunicationLog updatedLog = communicationLogRepository.save(log);
        return CommunicationLogMapper.toResponse(updatedLog);
    }

    /**
     * Delete a communication log.
     */
    public void deleteCommunicationLog(Long churchId, Long logId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        CommunicationLog log = communicationLogRepository.findById(logId)
            .orElseThrow(() -> new EntityNotFoundException("Communication log not found with id: " + logId));

        if (!log.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Communication log does not belong to your church");
        }

        communicationLogRepository.delete(log);
    }

    /**
     * Get recent communications for a member (last 30 days).
     */
    public List<CommunicationLogResponse> getRecentCommunications(Long churchId, Long memberId, int days) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Member member = memberRepository.findByIdAndChurch(memberId, church)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return communicationLogRepository.findRecentCommunications(member, since).stream()
            .map(CommunicationLogMapper::toResponse)
            .toList();
    }

    /**
     * Get a single communication log by ID.
     */
    public CommunicationLogResponse getCommunicationLogById(Long churchId, Long logId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        CommunicationLog log = communicationLogRepository.findById(logId)
            .orElseThrow(() -> new EntityNotFoundException("Communication log not found with id: " + logId));

        if (!log.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Communication log does not belong to your church");
        }

        return CommunicationLogMapper.toResponse(log);
    }
}
