package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.LifecycleEventRequest;
import com.reuben.pastcare_spring.dtos.LifecycleEventResponse;
import com.reuben.pastcare_spring.mapper.LifecycleEventMapper;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.LifecycleEventRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing lifecycle events.
 * Handles baptisms, confirmations, memberships, and other spiritual milestones.
 *
 * Phase 4: Lifecycle & Communication Tracking
 */
@Service
@Transactional
public class LifecycleEventService {

    @Autowired
    private LifecycleEventRepository lifecycleEventRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChurchRepository churchRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new lifecycle event for a member.
     */
    public LifecycleEventResponse createLifecycleEvent(Long churchId, LifecycleEventRequest request, Long userId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Member member = memberRepository.findByIdAndChurch(request.memberId(), church)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + request.memberId()));

        LifecycleEvent event = new LifecycleEvent();
        event.setChurch(church);
        event.setMember(member);
        event.setEventType(request.eventType());
        event.setEventDate(request.eventDate());
        event.setLocation(request.location());
        event.setOfficiatingMinister(request.officiatingMinister());
        event.setCertificateNumber(request.certificateNumber());
        event.setNotes(request.notes());
        event.setDocumentUrl(request.documentUrl());
        event.setWitnesses(request.witnesses());
        event.setIsVerified(request.isVerified() != null ? request.isVerified() : false);

        if (Boolean.TRUE.equals(request.isVerified()) && userId != null) {
            User verifier = userRepository.findById(userId).orElse(null);
            event.setVerifiedBy(verifier);
        }

        LifecycleEvent savedEvent = lifecycleEventRepository.save(event);
        return LifecycleEventMapper.toResponse(savedEvent);
    }

    /**
     * Update an existing lifecycle event.
     */
    public LifecycleEventResponse updateLifecycleEvent(Long churchId, Long eventId, LifecycleEventRequest request, Long userId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        LifecycleEvent event = lifecycleEventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Lifecycle event not found with id: " + eventId));

        if (!event.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Lifecycle event does not belong to your church");
        }

        event.setEventType(request.eventType());
        event.setEventDate(request.eventDate());
        event.setLocation(request.location());
        event.setOfficiatingMinister(request.officiatingMinister());
        event.setCertificateNumber(request.certificateNumber());
        event.setNotes(request.notes());
        event.setDocumentUrl(request.documentUrl());
        event.setWitnesses(request.witnesses());

        if (request.isVerified() != null && Boolean.TRUE.equals(request.isVerified()) && !Boolean.TRUE.equals(event.getIsVerified())) {
            event.setIsVerified(true);
            if (userId != null) {
                User verifier = userRepository.findById(userId).orElse(null);
                event.setVerifiedBy(verifier);
            }
        }

        LifecycleEvent updatedEvent = lifecycleEventRepository.save(event);
        return LifecycleEventMapper.toResponse(updatedEvent);
    }

    /**
     * Get all lifecycle events for a member.
     */
    public List<LifecycleEventResponse> getMemberLifecycleEvents(Long churchId, Long memberId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Member member = memberRepository.findByIdAndChurch(memberId, church)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

        return lifecycleEventRepository.findByMemberOrderByEventDateDesc(member).stream()
            .map(LifecycleEventMapper::toResponse)
            .toList();
    }

    /**
     * Get all lifecycle events for a church with pagination.
     */
    public Page<LifecycleEventResponse> getChurchLifecycleEvents(Long churchId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return lifecycleEventRepository.findByChurch(church, pageable)
            .map(LifecycleEventMapper::toResponse);
    }

    /**
     * Get lifecycle events by type.
     */
    public Page<LifecycleEventResponse> getLifecycleEventsByType(Long churchId, LifecycleEventType eventType, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return lifecycleEventRepository.findByChurchAndEventType(church, eventType, pageable)
            .map(LifecycleEventMapper::toResponse);
    }

    /**
     * Get lifecycle events within a date range.
     */
    public List<LifecycleEventResponse> getLifecycleEventsByDateRange(Long churchId, LocalDate startDate, LocalDate endDate) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        return lifecycleEventRepository.findByChurchAndEventDateBetween(church, startDate, endDate).stream()
            .map(LifecycleEventMapper::toResponse)
            .toList();
    }

    /**
     * Verify a lifecycle event.
     */
    public LifecycleEventResponse verifyLifecycleEvent(Long churchId, Long eventId, Long userId) {
        churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        LifecycleEvent event = lifecycleEventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Lifecycle event not found with id: " + eventId));

        if (!event.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Lifecycle event does not belong to your church");
        }

        event.setIsVerified(true);
        if (userId != null) {
            User verifier = userRepository.findById(userId).orElse(null);
            event.setVerifiedBy(verifier);
        }

        LifecycleEvent verifiedEvent = lifecycleEventRepository.save(event);
        return LifecycleEventMapper.toResponse(verifiedEvent);
    }

    /**
     * Delete a lifecycle event.
     */
    public void deleteLifecycleEvent(Long churchId, Long eventId) {
        churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        LifecycleEvent event = lifecycleEventRepository.findById(eventId)
            .orElseThrow(() -> new EntityNotFoundException("Lifecycle event not found with id: " + eventId));

        if (!event.getChurch().getId().equals(churchId)) {
            throw new IllegalArgumentException("Lifecycle event does not belong to your church");
        }

        lifecycleEventRepository.delete(event);
    }

    /**
     * Check if a member has a specific lifecycle event type.
     */
    public boolean memberHasLifecycleEvent(Long churchId, Long memberId, LifecycleEventType eventType) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Member member = memberRepository.findByIdAndChurch(memberId, church)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

        return lifecycleEventRepository.existsByMemberAndEventType(member, eventType);
    }

}
