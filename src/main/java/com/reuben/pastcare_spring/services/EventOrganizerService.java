package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.EventOrganizerRequest;
import com.reuben.pastcare_spring.dtos.EventOrganizerResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import com.reuben.pastcare_spring.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing event organizers.
 * Handles adding/removing organizers, managing roles, and contact person designation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventOrganizerService {

    private final EventOrganizerRepository organizerRepository;
    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    /**
     * Add organizer to event
     */
    @Transactional
    public EventOrganizerResponse addOrganizer(Long eventId, EventOrganizerRequest request, Long userId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Adding organizer to event {} in church {}", eventId, churchId);

        Event event = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        Member member = memberRepository.findById(request.getMemberId())
            .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        // Check if already an organizer
        if (organizerRepository.existsByEventAndMemberAndDeletedAtIsNull(event, member)) {
            throw new IllegalArgumentException("Member is already an organizer for this event");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // If setting as primary, remove primary status from others
        if (request.getIsPrimary() != null && request.getIsPrimary()) {
            removePrimaryStatusFromOthers(eventId);
        }

        EventOrganizer organizer = EventOrganizer.builder()
            .church(event.getChurch())
            .event(event)
            .member(member)
            .isPrimary(request.getIsPrimary() != null ? request.getIsPrimary() : false)
            .role(request.getRole())
            .isContactPerson(request.getIsContactPerson() != null ? request.getIsContactPerson() : false)
            .contactEmail(request.getContactEmail())
            .contactPhone(request.getContactPhone())
            .responsibilities(request.getResponsibilities())
            .createdBy(user)
            .build();

        organizer = organizerRepository.save(organizer);
        log.info("Organizer {} added to event {}", member.getId(), eventId);

        return EventOrganizerResponse.fromEntity(organizer);
    }

    /**
     * Update organizer
     */
    @Transactional
    public EventOrganizerResponse updateOrganizer(Long organizerId, EventOrganizerRequest request) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Updating organizer {} in church {}", organizerId, churchId);

        EventOrganizer organizer = organizerRepository.findByIdAndChurchIdAndDeletedAtIsNull(organizerId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

        // If setting as primary, remove primary status from others
        if (request.getIsPrimary() != null && request.getIsPrimary() && !organizer.getIsPrimary()) {
            removePrimaryStatusFromOthers(organizer.getEvent().getId());
        }

        // Update fields
        if (request.getIsPrimary() != null) {
            organizer.setIsPrimary(request.getIsPrimary());
        }
        organizer.setRole(request.getRole());
        if (request.getIsContactPerson() != null) {
            organizer.setIsContactPerson(request.getIsContactPerson());
        }
        organizer.setContactEmail(request.getContactEmail());
        organizer.setContactPhone(request.getContactPhone());
        organizer.setResponsibilities(request.getResponsibilities());

        organizer = organizerRepository.save(organizer);
        log.info("Organizer {} updated", organizerId);

        return EventOrganizerResponse.fromEntity(organizer);
    }

    /**
     * Remove organizer from event
     */
    @Transactional
    public void removeOrganizer(Long organizerId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Removing organizer {} in church {}", organizerId, churchId);

        EventOrganizer organizer = organizerRepository.findByIdAndChurchIdAndDeletedAtIsNull(organizerId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

        organizerRepository.delete(organizer);
        log.info("Organizer {} removed", organizerId);
    }

    /**
     * Set as primary organizer
     */
    @Transactional
    public EventOrganizerResponse setAsPrimary(Long organizerId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Setting organizer {} as primary in church {}", organizerId, churchId);

        EventOrganizer organizer = organizerRepository.findByIdAndChurchIdAndDeletedAtIsNull(organizerId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));

        // Remove primary status from others
        removePrimaryStatusFromOthers(organizer.getEvent().getId());

        // Set as primary
        organizer.setAsPrimary();
        organizer = organizerRepository.save(organizer);

        // Update event's primary organizer
        Event event = organizer.getEvent();
        event.setPrimaryOrganizer(organizer.getMember());
        eventRepository.save(event);

        log.info("Organizer {} set as primary", organizerId);
        return EventOrganizerResponse.fromEntity(organizer);
    }

    /**
     * Get organizer by ID
     */
    @Transactional(readOnly = true)
    public EventOrganizerResponse getOrganizer(Long organizerId) {
        Long churchId = TenantContext.getCurrentChurchId();
        EventOrganizer organizer = organizerRepository.findByIdAndChurchIdAndDeletedAtIsNull(organizerId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Organizer not found"));
        return EventOrganizerResponse.fromEntity(organizer);
    }

    /**
     * Get all organizers for an event
     */
    @Transactional(readOnly = true)
    public List<EventOrganizerResponse> getEventOrganizers(Long eventId) {
        return organizerRepository.findByEventId(eventId).stream()
            .map(EventOrganizerResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get events organized by a member
     */
    @Transactional(readOnly = true)
    public Page<EventOrganizerResponse> getMemberOrganizations(Long memberId, Pageable pageable) {
        return organizerRepository.findByMemberId(memberId, pageable)
            .map(EventOrganizerResponse::fromEntity);
    }

    /**
     * Get primary organizer for an event
     */
    @Transactional(readOnly = true)
    public EventOrganizerResponse getPrimaryOrganizer(Long eventId) {
        return organizerRepository.findPrimaryOrganizerByEventId(eventId)
            .map(EventOrganizerResponse::fromEntity)
            .orElse(null);
    }

    /**
     * Get contact persons for an event
     */
    @Transactional(readOnly = true)
    public List<EventOrganizerResponse> getContactPersons(Long eventId) {
        return organizerRepository.findContactPersonsByEventId(eventId).stream()
            .map(EventOrganizerResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Remove primary status from all organizers of an event
     */
    @Transactional
    private void removePrimaryStatusFromOthers(Long eventId) {
        List<EventOrganizer> organizers = organizerRepository.findByEventId(eventId);
        for (EventOrganizer org : organizers) {
            if (org.getIsPrimary()) {
                org.removePrimaryStatus();
                organizerRepository.save(org);
            }
        }
    }
}
