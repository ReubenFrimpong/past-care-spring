package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.EventResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import com.reuben.pastcare_spring.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing event tags.
 * Handles tag creation, deletion, and tag-based event discovery.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EventTagService {

    private final EventTagRepository tagRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    /**
     * Add tag to event
     */
    @Transactional
    public void addTag(Long eventId, String tag, String tagColor, Long userId) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Adding tag '{}' to event {} in church {}", tag, eventId, churchId);

        Event event = eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        // Check if tag already exists
        if (tagRepository.existsByEventIdAndTag(eventId, tag)) {
            throw new IllegalArgumentException("Tag '" + tag + "' already exists for this event");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        EventTag eventTag = EventTag.builder()
            .church(event.getChurch())
            .event(event)
            .tag(tag.trim().toLowerCase())
            .tagColor(tagColor != null ? tagColor : "#3B82F6")
            .createdBy(user)
            .build();

        eventTag.normalizeTag();
        eventTag.setDefaultColorIfNeeded();

        tagRepository.save(eventTag);
        log.info("Tag '{}' added to event {}", tag, eventId);
    }

    /**
     * Remove tag from event
     */
    @Transactional
    public void removeTag(Long eventId, String tag) {
        Long churchId = TenantContext.getCurrentChurchId();
        log.info("Removing tag '{}' from event {} in church {}", tag, eventId, churchId);

        // Verify event exists
        eventRepository.findByIdAndChurchIdAndDeletedAtIsNull(eventId, churchId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found"));

        tagRepository.deleteByEventIdAndTag(eventId, tag);
        log.info("Tag '{}' removed from event {}", tag, eventId);
    }

    /**
     * Get all tags for an event
     */
    @Transactional(readOnly = true)
    public List<String> getEventTags(Long eventId) {
        return tagRepository.findByEventId(eventId).stream()
            .map(EventTag::getTag)
            .collect(Collectors.toList());
    }

    /**
     * Get all unique tags for a church
     */
    @Transactional(readOnly = true)
    public List<String> getAllTags() {
        Long churchId = TenantContext.getCurrentChurchId();
        return tagRepository.findDistinctTagsByChurchId(churchId);
    }

    /**
     * Get tags with their colors
     */
    @Transactional(readOnly = true)
    public Map<String, String> getTagsWithColors() {
        Long churchId = TenantContext.getCurrentChurchId();
        List<Object[]> results = tagRepository.findDistinctTagsWithColors(churchId);

        return results.stream()
            .collect(Collectors.toMap(
                r -> (String) r[0],
                r -> (String) r[1]
            ));
    }

    /**
     * Find events by tag
     */
    @Transactional(readOnly = true)
    public List<EventResponse> findEventsByTag(String tag) {
        Long churchId = TenantContext.getCurrentChurchId();
        return tagRepository.findEventsByTag(churchId, tag).stream()
            .map(EventResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Find events by multiple tags
     */
    @Transactional(readOnly = true)
    public List<EventResponse> findEventsByTags(List<String> tags) {
        Long churchId = TenantContext.getCurrentChurchId();
        return tagRepository.findEventsByTags(churchId, tags).stream()
            .map(EventResponse::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get tag usage statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Long> getTagUsageStats() {
        Long churchId = TenantContext.getCurrentChurchId();
        List<Object[]> results = tagRepository.countTagUsage(churchId);

        return results.stream()
            .collect(Collectors.toMap(
                r -> (String) r[0],
                r -> (Long) r[1]
            ));
    }

    /**
     * Search tags (autocomplete)
     */
    @Transactional(readOnly = true)
    public List<String> searchTags(String searchTerm) {
        Long churchId = TenantContext.getCurrentChurchId();
        return tagRepository.searchTags(churchId, searchTerm);
    }
}
