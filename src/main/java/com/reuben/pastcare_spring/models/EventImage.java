package com.reuben.pastcare_spring.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing an image in an event's photo gallery.
 * Supports multiple images per event with captions and ordering.
 */
@Entity
@Table(name = "event_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "caption", length = 500)
    private String caption;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_cover_image", nullable = false)
    @Builder.Default
    private Boolean isCoverImage = false;

    @Column(name = "uploaded_by_id")
    private Long uploadedById;

    @Column(name = "uploaded_at", nullable = false)
    @Builder.Default
    private LocalDateTime uploadedAt = LocalDateTime.now();
}
