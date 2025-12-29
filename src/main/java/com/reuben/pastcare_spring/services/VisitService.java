package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.VisitRequest;
import com.reuben.pastcare_spring.dtos.VisitResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Pastoral Care Module Phase 1: Visit Management
 * Service for managing pastoral visits
 */
@Service
@RequiredArgsConstructor
public class VisitService {

    private final VisitRepository visitRepository;
    private final MemberRepository memberRepository;
    private final CareNeedRepository careNeedRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;
    private final TenantValidationService tenantValidationService;

    /**
     * Create a new visit
     */
    @Transactional
    public VisitResponse createVisit(Long churchId, VisitRequest request, Long currentUserId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        User createdBy = userRepository.findById(currentUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + currentUserId));

        Visit visit = new Visit();
        visit.setChurch(church);
        visit.setCreatedBy(createdBy);

        updateVisitFromRequest(visit, request);

        Visit saved = visitRepository.save(visit);
        return VisitResponse.fromEntity(saved);
    }

    /**
     * Get visit by ID
     */
    public VisitResponse getVisitById(Long id) {
        Visit visit = visitRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Visit not found with id: " + id));

        // CRITICAL SECURITY: Validate visit belongs to current church
        tenantValidationService.validateVisitAccess(visit);

        return VisitResponse.fromEntity(visit);
    }

    /**
     * Get all visits for a church
     */
    public List<VisitResponse> getAllVisits(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        return visitRepository.findByChurchOrderByVisitDateDesc(church).stream()
            .map(VisitResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Update an existing visit
     */
    @Transactional
    public VisitResponse updateVisit(Long id, VisitRequest request) {
        Visit visit = visitRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Visit not found with id: " + id));

        // CRITICAL SECURITY: Validate visit belongs to current church
        tenantValidationService.validateVisitAccess(visit);

        updateVisitFromRequest(visit, request);

        Visit updated = visitRepository.save(visit);
        return VisitResponse.fromEntity(updated);
    }

    /**
     * Delete a visit
     */
    @Transactional
    public void deleteVisit(Long id) {
        Visit visit = visitRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Visit not found with id: " + id));

        // CRITICAL SECURITY: Validate visit belongs to current church
        tenantValidationService.validateVisitAccess(visit);

        visitRepository.delete(visit);
    }

    /**
     * Get visits by member
     */
    public List<VisitResponse> findByMember(Long churchId, Long memberId) {
        // Validate church exists
        churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        return visitRepository.findByMemberOrderByVisitDateDesc(member).stream()
            .map(VisitResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get visits by care need
     */
    public List<VisitResponse> findByCareNeed(Long churchId, Long careNeedId) {
        // Validate church exists
        churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        CareNeed careNeed = careNeedRepository.findById(careNeedId)
            .orElseThrow(() -> new IllegalArgumentException("Care need not found with id: " + careNeedId));

        return visitRepository.findByCareNeedOrderByVisitDateDesc(careNeed).stream()
            .map(VisitResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get upcoming visits
     */
    public List<VisitResponse> findUpcoming(Long churchId) {
        // Validate church exists
        churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        LocalDate today = LocalDate.now();
        return visitRepository.findUpcomingVisits(today).stream()
            .filter(visit -> visit.getChurch().getId().equals(churchId))
            .map(VisitResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get past visits
     */
    public List<VisitResponse> findPast(Long churchId) {
        // Validate church exists
        churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        LocalDate today = LocalDate.now();
        return visitRepository.findPastVisits(today).stream()
            .filter(visit -> visit.getChurch().getId().equals(churchId))
            .map(VisitResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get visits by date range
     */
    public List<VisitResponse> findByDateRange(Long churchId, LocalDate startDate, LocalDate endDate) {
        // Validate church exists
        churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        return visitRepository.findByDateRange(startDate, endDate).stream()
            .filter(visit -> visit.getChurch().getId().equals(churchId))
            .map(VisitResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get today's visits
     */
    public List<VisitResponse> findTodaysVisits(Long churchId) {
        // Validate church exists
        churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        LocalDate today = LocalDate.now();
        return visitRepository.findVisitsByDate(today).stream()
            .filter(visit -> visit.getChurch().getId().equals(churchId))
            .map(VisitResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Mark a visit as completed
     */
    @Transactional
    public VisitResponse markAsCompleted(Long id, String outcomes) {
        Visit visit = visitRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Visit not found with id: " + id));

        // CRITICAL SECURITY: Validate visit belongs to current church
        tenantValidationService.validateVisitAccess(visit);

        visit.setIsCompleted(true);
        if (outcomes != null && !outcomes.isEmpty()) {
            visit.setOutcomes(outcomes);
        }

        Visit updated = visitRepository.save(visit);
        return VisitResponse.fromEntity(updated);
    }

    /**
     * Find visits requiring follow-up
     */
    public List<VisitResponse> findRequiringFollowUp(Long churchId) {
        // Validate church exists
        churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        LocalDate today = LocalDate.now();
        return visitRepository.findVisitsRequiringFollowUp(today).stream()
            .filter(visit -> visit.getChurch().getId().equals(churchId))
            .map(VisitResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Helper method to update visit from request DTO
     */
    private void updateVisitFromRequest(Visit visit, VisitRequest request) {
        // Set member
        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.getMemberId()));
        visit.setMember(member);

        // Set care need (nullable)
        if (request.getCareNeedId() != null) {
            CareNeed careNeed = careNeedRepository.findById(request.getCareNeedId())
                .orElseThrow(() -> new IllegalArgumentException("Care need not found with id: " + request.getCareNeedId()));
            visit.setCareNeed(careNeed);
        } else {
            visit.setCareNeed(null);
        }

        visit.setType(request.getType());
        visit.setVisitDate(request.getVisitDate());
        visit.setStartTime(request.getStartTime());
        visit.setEndTime(request.getEndTime());

        // Set location (nullable)
        if (request.getLocationId() != null) {
            Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + request.getLocationId()));
            visit.setLocation(location);
        } else {
            visit.setLocation(null);
        }

        visit.setLocationDetails(request.getLocationDetails());

        // Set attendees (nullable)
        if (request.getAttendeeIds() != null && !request.getAttendeeIds().isEmpty()) {
            List<User> attendees = new ArrayList<>();
            for (Long attendeeId : request.getAttendeeIds()) {
                User attendee = userRepository.findById(attendeeId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + attendeeId));
                attendees.add(attendee);
            }
            visit.setAttendees(attendees);
        } else {
            visit.setAttendees(new ArrayList<>());
        }

        visit.setPurpose(request.getPurpose());
        visit.setNotes(request.getNotes());
        visit.setOutcomes(request.getOutcomes());
        visit.setFollowUpRequired(request.getFollowUpRequired() != null ? request.getFollowUpRequired() : false);
        visit.setFollowUpDate(request.getFollowUpDate());
        visit.setIsCompleted(request.getIsCompleted() != null ? request.getIsCompleted() : false);
        visit.setIsConfidential(request.getIsConfidential() != null ? request.getIsConfidential() : false);
    }
}
