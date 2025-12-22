package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.mapper.LocationMapper;
import com.reuben.pastcare_spring.mapper.MemberMapper;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Household;
import com.reuben.pastcare_spring.models.Location;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.HouseholdRepository;
import com.reuben.pastcare_spring.repositories.LocationRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class HouseholdService {

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChurchRepository churchRepository;

    @Autowired
    private LocationRepository locationRepository;

    /**
     * Create a new household
     */
    public HouseholdResponse createHousehold(Long churchId, HouseholdRequest request) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        // Validate household name uniqueness
        if (householdRepository.existsByChurchAndHouseholdNameIgnoreCase(church, request.householdName())) {
            throw new IllegalArgumentException("Household name already exists: " + request.householdName());
        }

        // Get household head
        Member householdHead = memberRepository.findByIdAndChurch(request.householdHeadId(), church)
            .orElseThrow(() -> new EntityNotFoundException("Household head not found with id: " + request.householdHeadId()));

        Household household = new Household();
        household.setChurch(church);
        household.setHouseholdName(request.householdName());
        household.setHouseholdHead(householdHead);
        household.setNotes(request.notes());
        household.setEstablishedDate(request.establishedDate());
        household.setHouseholdEmail(request.householdEmail());
        household.setHouseholdPhone(request.householdPhone());
        household.setHouseholdImageUrl(request.householdImageUrl());

        // Set location if provided
        if (request.locationId() != null) {
            Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found with id: " + request.locationId()));
            household.setSharedLocation(location);
        }

        // Save household first
        household = householdRepository.save(household);

        // Add members to household if provided
        if (request.memberIds() != null && !request.memberIds().isEmpty()) {
            for (Long memberId : request.memberIds()) {
                Member member = memberRepository.findByIdAndChurch(memberId, church)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
                household.addMember(member);
            }
            household = householdRepository.save(household);
        }

        return mapToResponse(household);
    }

    /**
     * Update an existing household
     */
    public HouseholdResponse updateHousehold(Long churchId, Long householdId, HouseholdRequest request) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Household household = householdRepository.findByIdAndChurch(householdId, church)
            .orElseThrow(() -> new EntityNotFoundException("Household not found with id: " + householdId));

        // Check name uniqueness if name changed
        if (!household.getHouseholdName().equalsIgnoreCase(request.householdName())) {
            if (householdRepository.existsByChurchAndHouseholdNameIgnoreCase(church, request.householdName())) {
                throw new IllegalArgumentException("Household name already exists: " + request.householdName());
            }
        }

        // Update household head if changed
        if (!household.getHouseholdHead().getId().equals(request.householdHeadId())) {
            Member householdHead = memberRepository.findByIdAndChurch(request.householdHeadId(), church)
                .orElseThrow(() -> new EntityNotFoundException("Household head not found with id: " + request.householdHeadId()));
            household.setHouseholdHead(householdHead);
        }

        household.setHouseholdName(request.householdName());
        household.setNotes(request.notes());
        household.setEstablishedDate(request.establishedDate());
        household.setHouseholdEmail(request.householdEmail());
        household.setHouseholdPhone(request.householdPhone());
        household.setHouseholdImageUrl(request.householdImageUrl());

        // Update location if provided
        if (request.locationId() != null) {
            Location location = locationRepository.findById(request.locationId())
                .orElseThrow(() -> new EntityNotFoundException("Location not found with id: " + request.locationId()));
            household.setSharedLocation(location);
        } else {
            household.setSharedLocation(null);
        }

        // Update members if provided
        if (request.memberIds() != null) {
            // Remove all current members
            household.getMembers().clear();

            // Add new members
            for (Long memberId : request.memberIds()) {
                Member member = memberRepository.findByIdAndChurch(memberId, church)
                    .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));
                household.addMember(member);
            }
        }

        household = householdRepository.save(household);
        return mapToResponse(household);
    }

    /**
     * Get household by ID
     */
    @Transactional(readOnly = true)
    public HouseholdResponse getHouseholdById(Long churchId, Long householdId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Household household = householdRepository.findByIdAndChurch(householdId, church)
            .orElseThrow(() -> new EntityNotFoundException("Household not found with id: " + householdId));

        return mapToResponse(household);
    }

    /**
     * Get all households for a church
     */
    @Transactional(readOnly = true)
    public Page<HouseholdSummaryResponse> getAllHouseholds(Long churchId, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Page<Household> households = householdRepository.findByChurch(church, pageable);
        return households.map(this::mapToSummaryResponse);
    }

    /**
     * Search households by name
     */
    @Transactional(readOnly = true)
    public Page<HouseholdSummaryResponse> searchHouseholds(Long churchId, String searchTerm, Pageable pageable) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Page<Household> households = householdRepository
            .findByChurchAndHouseholdNameContainingIgnoreCase(church, searchTerm, pageable);
        return households.map(this::mapToSummaryResponse);
    }

    /**
     * Delete household
     */
    public void deleteHousehold(Long churchId, Long householdId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Household household = householdRepository.findByIdAndChurch(householdId, church)
            .orElseThrow(() -> new EntityNotFoundException("Household not found with id: " + householdId));

        // Remove household association from all members
        for (Member member : household.getMembers()) {
            member.setHousehold(null);
        }

        householdRepository.delete(household);
    }

    /**
     * Add member to household
     */
    public HouseholdResponse addMemberToHousehold(Long churchId, Long householdId, Long memberId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Household household = householdRepository.findByIdAndChurch(householdId, church)
            .orElseThrow(() -> new EntityNotFoundException("Household not found with id: " + householdId));

        Member member = memberRepository.findByIdAndChurch(memberId, church)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

        household.addMember(member);
        household = householdRepository.save(household);

        return mapToResponse(household);
    }

    /**
     * Remove member from household
     */
    public HouseholdResponse removeMemberFromHousehold(Long churchId, Long householdId, Long memberId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        Household household = householdRepository.findByIdAndChurch(householdId, church)
            .orElseThrow(() -> new EntityNotFoundException("Household not found with id: " + householdId));

        Member member = memberRepository.findByIdAndChurch(memberId, church)
            .orElseThrow(() -> new EntityNotFoundException("Member not found with id: " + memberId));

        // Don't allow removing the household head
        if (household.getHouseholdHead().getId().equals(memberId)) {
            throw new IllegalArgumentException("Cannot remove household head. Please designate a new head first.");
        }

        household.removeMember(member);
        household = householdRepository.save(household);

        return mapToResponse(household);
    }

    /**
     * Get household statistics for a church
     */
    @Transactional(readOnly = true)
    public HouseholdStatsResponse getHouseholdStats(Long churchId) {
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new EntityNotFoundException("Church not found with id: " + churchId));

        long totalHouseholds = householdRepository.countByChurch(church);
        long totalMembers = memberRepository.countByChurch(church);
        long membersInHouseholds = memberRepository.countByChurchAndHouseholdIsNotNull(church);

        double averageHouseholdSize = totalHouseholds > 0 ?
            (double) membersInHouseholds / totalHouseholds : 0;

        return new HouseholdStatsResponse(
            totalHouseholds,
            totalMembers,
            membersInHouseholds,
            averageHouseholdSize
        );
    }

    /**
     * Map Household entity to HouseholdResponse DTO
     */
    private HouseholdResponse mapToResponse(Household household) {
        MemberResponse headResponse = MemberMapper.toMemberResponse(household.getHouseholdHead());

        List<MemberResponse> memberResponses = household.getMembers().stream()
            .map(MemberMapper::toMemberResponse)
            .collect(Collectors.toList());

        LocationResponse locationResponse = household.getSharedLocation() != null ?
            LocationMapper.toLocationResponse(household.getSharedLocation()) : null;

        return new HouseholdResponse(
            household.getId(),
            household.getHouseholdName(),
            headResponse,
            locationResponse,
            memberResponses,
            household.getNotes(),
            household.getEstablishedDate(),
            household.getHouseholdImageUrl(),
            household.getHouseholdEmail(),
            household.getHouseholdPhone(),
            household.getMemberCount(),
            household.getCreatedAt(),
            household.getUpdatedAt()
        );
    }

    /**
     * Map Household entity to HouseholdSummaryResponse DTO
     */
    private HouseholdSummaryResponse mapToSummaryResponse(Household household) {
        String headName = household.getHouseholdHead().getFirstName() + " " +
            household.getHouseholdHead().getLastName();

        String locationName = household.getSharedLocation() != null ?
            household.getSharedLocation().getDisplayName() : null;

        return new HouseholdSummaryResponse(
            household.getId(),
            household.getHouseholdName(),
            headName,
            locationName,
            household.getMemberCount(),
            household.getHouseholdImageUrl(),
            household.getEstablishedDate()
        );
    }
}
