package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.security.TenantContext;
import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing crisis and emergency situations
 */
@Service
@RequiredArgsConstructor
public class CrisisService {

    private final CrisisRepository crisisRepository;
    private final CrisisAffectedMemberRepository crisisAffectedMemberRepository;
    private final CrisisAffectedLocationRepository crisisAffectedLocationRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;

    /**
     * Report a new crisis
     */
    @Transactional
    public CrisisResponse reportCrisis(Long churchId, CrisisRequest request, Long currentUserId) {
        Crisis crisis = new Crisis();

        // Set church (tenant isolation)
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        crisis.setChurch(church);

        // Set reported by user
        User reportedBy = userRepository.findById(currentUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + currentUserId));
        crisis.setReportedBy(reportedBy);
        crisis.setReportedDate(LocalDateTime.now());

        updateCrisisFromRequest(crisis, request);

        Crisis saved = crisisRepository.save(crisis);

        // Add affected members if provided
        if (request.getAffectedMemberIds() != null && !request.getAffectedMemberIds().isEmpty()) {
            for (Long memberId : request.getAffectedMemberIds()) {
                addAffectedMemberToCrisis(saved.getId(), memberId);
            }
            // Update affected members count
            saved.setAffectedMembersCount(request.getAffectedMemberIds().size());
            saved = crisisRepository.save(saved);
        }

        return convertToResponseWithAffectedMembers(saved);
    }

    /**
     * Get crisis by ID
     */
    public CrisisResponse getCrisisById(Long id) {
        Crisis crisis = crisisRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + id));
        return convertToResponseWithAffectedMembers(crisis);
    }

    /**
     * Get all crises for a church with pagination
     */
    public Page<CrisisResponse> getCrises(Long churchId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return crisisRepository.findByChurch(church, pageable)
            .map(this::convertToResponseWithAffectedMembers);
    }

    /**
     * Update an existing crisis
     */
    @Transactional
    public CrisisResponse updateCrisis(Long id, CrisisRequest request) {
        Crisis crisis = crisisRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + id));

        updateCrisisFromRequest(crisis, request);
        Crisis updated = crisisRepository.save(crisis);
        return convertToResponseWithAffectedMembers(updated);
    }

    /**
     * Delete a crisis
     */
    @Transactional
    public void deleteCrisis(Long id) {
        Crisis crisis = crisisRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + id));
        crisisRepository.delete(crisis);
    }

    /**
     * Add affected member to crisis
     */
    @Transactional
    public CrisisAffectedMemberResponse addAffectedMember(Long crisisId, CrisisAffectedMemberRequest request) {
        Crisis crisis = crisisRepository.findById(crisisId)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + crisisId));

        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.getMemberId()));

        // Check if member is already affected
        if (crisisAffectedMemberRepository.findByCrisisAndMember(crisis, member).isPresent()) {
            throw new IllegalArgumentException("Member is already added to this crisis");
        }

        CrisisAffectedMember affectedMember = new CrisisAffectedMember(crisis, member);
        affectedMember.setNotes(request.getNotes());
        affectedMember.setIsPrimaryContact(request.getIsPrimaryContact() != null ? request.getIsPrimaryContact() : false);

        CrisisAffectedMember saved = crisisAffectedMemberRepository.save(affectedMember);

        // Update affected members count
        Long count = crisisAffectedMemberRepository.countByCrisis(crisis);
        crisis.setAffectedMembersCount(count.intValue());
        crisisRepository.save(crisis);

        return CrisisAffectedMemberResponse.fromEntity(saved);
    }

    /**
     * Internal method to add affected member by ID only
     */
    @Transactional
    private void addAffectedMemberToCrisis(Long crisisId, Long memberId) {
        Crisis crisis = crisisRepository.findById(crisisId)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + crisisId));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        // Check if member is already affected
        if (crisisAffectedMemberRepository.findByCrisisAndMember(crisis, member).isEmpty()) {
            CrisisAffectedMember affectedMember = new CrisisAffectedMember(crisis, member);
            crisisAffectedMemberRepository.save(affectedMember);
        }
    }

    /**
     * Bulk add affected members to crisis
     * Use case: Church-wide crises like COVID-19, natural disasters, etc.
     */
    @Transactional
    public List<CrisisAffectedMemberResponse> bulkAddAffectedMembers(Long crisisId, BulkCrisisAffectedMembersRequest request) {
        Crisis crisis = crisisRepository.findById(crisisId)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + crisisId));

        List<CrisisAffectedMemberResponse> addedMembers = new java.util.ArrayList<>();

        for (Long memberId : request.getMemberIds()) {
            try {
                Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

                // Check if member is already affected
                if (crisisAffectedMemberRepository.findByCrisisAndMember(crisis, member).isEmpty()) {
                    CrisisAffectedMember affectedMember = new CrisisAffectedMember(crisis, member);
                    affectedMember.setNotes(request.getNotes());
                    affectedMember.setIsPrimaryContact(request.getIsPrimaryContact() != null ? request.getIsPrimaryContact() : false);

                    CrisisAffectedMember saved = crisisAffectedMemberRepository.save(affectedMember);
                    addedMembers.add(CrisisAffectedMemberResponse.fromEntity(saved));
                }
                // Silently skip members already affected
            } catch (Exception e) {
                // Log error but continue with other members
                System.err.println("Failed to add member " + memberId + " to crisis " + crisisId + ": " + e.getMessage());
            }
        }

        // Update affected members count
        Long count = crisisAffectedMemberRepository.countByCrisis(crisis);
        crisis.setAffectedMembersCount(count.intValue());
        crisisRepository.save(crisis);

        return addedMembers;
    }

    /**
     * Remove affected member from crisis
     */
    @Transactional
    public void removeAffectedMember(Long crisisId, Long memberId) {
        Crisis crisis = crisisRepository.findById(crisisId)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + crisisId));

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

        crisisAffectedMemberRepository.deleteByCrisisAndMember(crisis, member);

        // Update affected members count
        Long count = crisisAffectedMemberRepository.countByCrisis(crisis);
        crisis.setAffectedMembersCount(count.intValue());
        crisisRepository.save(crisis);
    }

    /**
     * Mobilize resources for a crisis
     */
    @Transactional
    public CrisisResponse mobilizeResources(Long id, String resources) {
        Crisis crisis = crisisRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + id));

        crisis.setResourcesMobilized(resources);

        // Update status to IN_RESPONSE if currently ACTIVE
        if (crisis.getStatus() == CrisisStatus.ACTIVE) {
            crisis.setStatus(CrisisStatus.IN_RESPONSE);
        }

        Crisis updated = crisisRepository.save(crisis);
        return convertToResponseWithAffectedMembers(updated);
    }

    /**
     * Send emergency notifications
     */
    @Transactional
    public CrisisResponse sendEmergencyNotifications(Long id) {
        Crisis crisis = crisisRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + id));

        crisis.setCommunicationSent(true);
        crisis.setEmergencyContactNotified(true);

        Crisis updated = crisisRepository.save(crisis);
        return convertToResponseWithAffectedMembers(updated);
    }

    /**
     * Resolve a crisis
     */
    @Transactional
    public CrisisResponse resolveCrisis(Long id, String resolutionNotes) {
        Crisis crisis = crisisRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + id));

        crisis.setStatus(CrisisStatus.RESOLVED);
        crisis.setResolvedDate(LocalDateTime.now());

        if (resolutionNotes != null && !resolutionNotes.isEmpty()) {
            crisis.setResolutionNotes(resolutionNotes);
        }

        Crisis updated = crisisRepository.save(crisis);
        return convertToResponseWithAffectedMembers(updated);
    }

    /**
     * Update crisis status
     */
    @Transactional
    public CrisisResponse updateStatus(Long id, CrisisStatus status) {
        Crisis crisis = crisisRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found with id: " + id));

        crisis.setStatus(status);

        // Set resolved date if status is RESOLVED or CLOSED
        if ((status == CrisisStatus.RESOLVED || status == CrisisStatus.CLOSED) && crisis.getResolvedDate() == null) {
            crisis.setResolvedDate(LocalDateTime.now());
        }

        Crisis updated = crisisRepository.save(crisis);
        return convertToResponseWithAffectedMembers(updated);
    }

    /**
     * Get active crises
     */
    public List<CrisisResponse> getActiveCrises(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return crisisRepository.findActiveCrises(church).stream()
            .map(this::convertToResponseWithAffectedMembers)
            .collect(Collectors.toList());
    }

    /**
     * Get critical crises
     */
    public List<CrisisResponse> getCriticalCrises(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return crisisRepository.findCriticalCrises(church).stream()
            .map(this::convertToResponseWithAffectedMembers)
            .collect(Collectors.toList());
    }

    /**
     * Get crises by status
     */
    public Page<CrisisResponse> getCrisesByStatus(Long churchId, CrisisStatus status, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return crisisRepository.findByChurchAndStatus(church, status, pageable)
            .map(this::convertToResponseWithAffectedMembers);
    }

    /**
     * Get crises by type
     */
    public Page<CrisisResponse> getCrisesByType(Long churchId, CrisisType type, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return crisisRepository.findByChurchAndCrisisType(church, type, pageable)
            .map(this::convertToResponseWithAffectedMembers);
    }

    /**
     * Get crises by severity
     */
    public Page<CrisisResponse> getCrisesBySeverity(Long churchId, CrisisSeverity severity, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return crisisRepository.findByChurchAndSeverity(church, severity, pageable)
            .map(this::convertToResponseWithAffectedMembers);
    }

    /**
     * Search crises
     */
    public Page<CrisisResponse> searchCrises(Long churchId, String search, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
        return crisisRepository.searchCrises(church, search, pageable)
            .map(this::convertToResponseWithAffectedMembers);
    }

    /**
     * Get crisis statistics for a church
     */
    public CrisisStatsResponse getCrisisStats(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

        long total = crisisRepository.countByChurch(church);
        long active = crisisRepository.countByChurchAndStatus(church, CrisisStatus.ACTIVE);
        long inResponse = crisisRepository.countByChurchAndStatus(church, CrisisStatus.IN_RESPONSE);
        long resolved = crisisRepository.countByChurchAndStatus(church, CrisisStatus.RESOLVED);
        long critical = crisisRepository.countByChurchAndSeverity(church, CrisisSeverity.CRITICAL);
        long highSeverity = crisisRepository.countByChurchAndSeverity(church, CrisisSeverity.HIGH);

        // Calculate total affected members across all crises
        List<Crisis> allCrises = crisisRepository.findByChurch(church, Pageable.unpaged()).getContent();
        long totalAffectedMembers = allCrises.stream()
            .mapToLong(c -> c.getAffectedMembersCount() != null ? c.getAffectedMembersCount() : 0)
            .sum();

        return new CrisisStatsResponse(total, active, inResponse, resolved, critical, highSeverity, totalAffectedMembers);
    }

    /**
     * Helper method to update crisis from request
     */
    private void updateCrisisFromRequest(Crisis crisis, CrisisRequest request) {
        crisis.setTitle(request.getTitle());
        crisis.setDescription(request.getDescription());
        crisis.setCrisisType(request.getCrisisType());
        crisis.setSeverity(request.getSeverity());
        crisis.setIncidentDate(request.getIncidentDate());
        crisis.setLocation(request.getLocation());

        // Set geographic fields for auto-detection (legacy single location support)
        crisis.setAffectedSuburb(request.getAffectedSuburb());
        crisis.setAffectedCity(request.getAffectedCity());
        crisis.setAffectedDistrict(request.getAffectedDistrict());
        crisis.setAffectedRegion(request.getAffectedRegion());
        crisis.setAffectedCountryCode(request.getAffectedCountryCode());

        // Handle multiple affected locations
        if (request.getAffectedLocations() != null && !request.getAffectedLocations().isEmpty()) {
            // Clear existing locations
            crisis.clearAffectedLocations();

            // Add new locations
            for (AffectedLocationRequest locationReq : request.getAffectedLocations()) {
                CrisisAffectedLocation location = new CrisisAffectedLocation(
                    crisis,
                    locationReq.getSuburb(),
                    locationReq.getCity(),
                    locationReq.getDistrict(),
                    locationReq.getRegion(),
                    locationReq.getCountryCode()
                );
                crisis.addAffectedLocation(location);
            }
        }

        crisis.setResponseTeamNotes(request.getResponseTeamNotes());
        crisis.setResolutionNotes(request.getResolutionNotes());
        crisis.setFollowUpRequired(request.getFollowUpRequired() != null ? request.getFollowUpRequired() : false);
        crisis.setFollowUpDate(request.getFollowUpDate());
        crisis.setResourcesMobilized(request.getResourcesMobilized());
        crisis.setCommunicationSent(request.getCommunicationSent() != null ? request.getCommunicationSent() : false);
        crisis.setEmergencyContactNotified(request.getEmergencyContactNotified() != null ? request.getEmergencyContactNotified() : false);

        // Handle affected members count if provided
        if (request.getAffectedMembersCount() != null) {
            crisis.setAffectedMembersCount(request.getAffectedMembersCount());
        }

        // Handle status if provided
        if (request.getStatus() != null) {
            crisis.setStatus(request.getStatus());
            // Set resolved date if status is RESOLVED or CLOSED and not already set
            if ((request.getStatus() == CrisisStatus.RESOLVED || request.getStatus() == CrisisStatus.CLOSED)
                && crisis.getResolvedDate() == null) {
                crisis.setResolvedDate(LocalDateTime.now());
            }
        } else if (crisis.getStatus() == null) {
            crisis.setStatus(CrisisStatus.ACTIVE);
        }
    }

    /**
     * Helper method to convert Crisis entity to response with affected members
     */
    private CrisisResponse convertToResponseWithAffectedMembers(Crisis crisis) {
        CrisisResponse response = CrisisResponse.fromEntity(crisis);

        // Load affected members
        List<CrisisAffectedMember> affectedMembers = crisisAffectedMemberRepository.findByCrisis(crisis);
        List<CrisisAffectedMemberResponse> affectedMemberResponses = affectedMembers.stream()
            .map(CrisisAffectedMemberResponse::fromEntity)
            .collect(Collectors.toList());

        response.setAffectedMembers(affectedMemberResponses);

        // Load affected locations
        List<CrisisAffectedLocation> affectedLocations = crisis.getAffectedLocationsList();
        if (affectedLocations != null && !affectedLocations.isEmpty()) {
            List<AffectedLocationResponse> locationResponses = affectedLocations.stream()
                .map(AffectedLocationResponse::fromEntity)
                .collect(Collectors.toList());
            response.setAffectedLocations(locationResponses);
        }

        return response;
    }

    /**
     * Auto-detect members in the affected geographic area and add them to the crisis.
     * Supports both legacy single location and new multi-location functionality.
     */
    @Transactional
    public List<Member> autoDetectAffectedMembers(Long crisisId) {
        Crisis crisis = crisisRepository.findById(crisisId)
            .orElseThrow(() -> new IllegalArgumentException("Crisis not found"));

        Church church = crisis.getChurch();

        Set<Member> allAffectedMembers = new java.util.HashSet<>();

        // Check if crisis has multiple locations defined
        List<CrisisAffectedLocation> locations = crisis.getAffectedLocationsList();

        if (locations != null && !locations.isEmpty()) {
            // Multi-location: find members for each location
            for (CrisisAffectedLocation location : locations) {
                List<Member> membersInLocation = memberRepository.findByGeographicLocation(
                    church,
                    location.getSuburb(),
                    location.getCity(),
                    location.getDistrict(),
                    location.getRegion(),
                    location.getCountryCode()
                );
                allAffectedMembers.addAll(membersInLocation);
            }
        } else {
            // Legacy single location: use the crisis's geographic fields
            List<Member> membersInLocation = memberRepository.findByGeographicLocation(
                church,
                crisis.getAffectedSuburb(),
                crisis.getAffectedCity(),
                crisis.getAffectedDistrict(),
                crisis.getAffectedRegion(),
                crisis.getAffectedCountryCode()
            );
            allAffectedMembers.addAll(membersInLocation);
        }

        // Add them to the crisis (avoiding duplicates)
        List<CrisisAffectedMember> existing = crisisAffectedMemberRepository.findByCrisis(crisis);
        Set<Long> existingMemberIds = existing.stream()
            .filter(cam -> cam.getMember() != null) // Filter out orphaned records
            .map(cam -> cam.getMember().getId())
            .collect(Collectors.toSet());

        for (Member member : allAffectedMembers) {
            if (!existingMemberIds.contains(member.getId())) {
                CrisisAffectedMember affectedMember = new CrisisAffectedMember(crisis, member);
                crisisAffectedMemberRepository.save(affectedMember);
            }
        }

        // Update affected members count
        Long count = crisisAffectedMemberRepository.countByCrisis(crisis);
        crisis.setAffectedMembersCount(count != null ? count.intValue() : 0);
        crisisRepository.save(crisis);

        return new java.util.ArrayList<>(allAffectedMembers);
    }

    /**
     * Get list of members that would be affected based on geographic criteria (preview mode)
     */
    public List<Member> previewAffectedMembers(String suburb, String city, String district, String region, String countryCode) {
        Long churchId = TenantContext.getCurrentChurchId();
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalArgumentException("Church not found"));
        return memberRepository.findByGeographicLocation(church, suburb, city, district, region, countryCode);
    }
}
