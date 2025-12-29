package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.EventImageRequest;
import com.reuben.pastcare_spring.dtos.EventImageResponse;
import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.EventImage;
import com.reuben.pastcare_spring.repositories.EventImageRepository;
import com.reuben.pastcare_spring.repositories.EventRepository;
import com.reuben.pastcare_spring.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventImageService {

    private final EventImageRepository eventImageRepository;
    private final EventRepository eventRepository;
    private final ImageService imageService;

    /**
     * Get all images for an event
     */
    public List<EventImageResponse> getEventImages(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Verify church access
        if (!event.getChurch().getId().equals(TenantContext.getCurrentChurchId())) {
            throw new RuntimeException("Unauthorized access to event");
        }

        return eventImageRepository.findByEventIdOrderByDisplayOrder(eventId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Upload a new image to an event's gallery
     */
    @Transactional
    public EventImageResponse uploadEventImage(Long eventId, MultipartFile file,
                                                String caption, Boolean isCoverImage,
                                                Long uploadedById) throws IOException {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Verify church access
        if (!event.getChurch().getId().equals(TenantContext.getCurrentChurchId())) {
            throw new RuntimeException("Unauthorized access to event");
        }

        // Upload image
        String imageUrl = imageService.uploadEventImage(file, null);

        // If this is set as cover image, clear existing cover image
        if (Boolean.TRUE.equals(isCoverImage)) {
            eventImageRepository.clearCoverImage(eventId);
        }

        // Get next display order
        long imageCount = eventImageRepository.countByEventId(eventId);

        // Create event image
        EventImage eventImage = EventImage.builder()
                .event(event)
                .imageUrl(imageUrl)
                .caption(caption)
                .displayOrder((int) imageCount)
                .isCoverImage(Boolean.TRUE.equals(isCoverImage))
                .uploadedById(uploadedById)
                .uploadedAt(LocalDateTime.now())
                .build();

        EventImage saved = eventImageRepository.save(eventImage);

        return mapToResponse(saved);
    }

    /**
     * Update image details (caption, display order, cover image flag)
     */
    @Transactional
    public EventImageResponse updateEventImage(Long eventId, Long imageId, EventImageRequest request) {
        EventImage eventImage = eventImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Event image not found with id: " + imageId));

        // Verify event access
        if (!eventImage.getEvent().getId().equals(eventId)) {
            throw new RuntimeException("Image does not belong to this event");
        }

        // Verify church access
        if (!eventImage.getEvent().getChurch().getId().equals(TenantContext.getCurrentChurchId())) {
            throw new RuntimeException("Unauthorized access to event image");
        }

        // Update caption
        if (request.getCaption() != null) {
            eventImage.setCaption(request.getCaption());
        }

        // Update display order
        if (request.getDisplayOrder() != null) {
            eventImage.setDisplayOrder(request.getDisplayOrder());
        }

        // Update cover image flag
        if (Boolean.TRUE.equals(request.getIsCoverImage())) {
            eventImageRepository.clearCoverImage(eventId);
            eventImage.setIsCoverImage(true);
        }

        EventImage updated = eventImageRepository.save(eventImage);

        return mapToResponse(updated);
    }

    /**
     * Delete an event image
     */
    @Transactional
    public void deleteEventImage(Long eventId, Long imageId) {
        EventImage eventImage = eventImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Event image not found with id: " + imageId));

        // Verify event access
        if (!eventImage.getEvent().getId().equals(eventId)) {
            throw new RuntimeException("Image does not belong to this event");
        }

        // Verify church access
        if (!eventImage.getEvent().getChurch().getId().equals(TenantContext.getCurrentChurchId())) {
            throw new RuntimeException("Unauthorized access to event image");
        }

        // Delete image file
        imageService.deleteImage(eventImage.getImageUrl());

        // Delete database record
        eventImageRepository.delete(eventImage);
    }

    /**
     * Reorder event images
     */
    @Transactional
    public void reorderImages(Long eventId, List<Long> imageIds) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        // Verify church access
        if (!event.getChurch().getId().equals(TenantContext.getCurrentChurchId())) {
            throw new RuntimeException("Unauthorized access to event");
        }

        for (int i = 0; i < imageIds.size(); i++) {
            eventImageRepository.updateDisplayOrder(imageIds.get(i), i);
        }
    }

    /**
     * Map entity to response
     */
    private EventImageResponse mapToResponse(EventImage eventImage) {
        return EventImageResponse.builder()
                .id(eventImage.getId())
                .eventId(eventImage.getEvent().getId())
                .imageUrl(eventImage.getImageUrl())
                .caption(eventImage.getCaption())
                .displayOrder(eventImage.getDisplayOrder())
                .isCoverImage(eventImage.getIsCoverImage())
                .uploadedById(eventImage.getUploadedById())
                .uploadedAt(eventImage.getUploadedAt())
                .build();
    }
}
