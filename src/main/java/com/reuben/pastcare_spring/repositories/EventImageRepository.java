package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage, Long> {

    /**
     * Find all images for an event, ordered by display order
     */
    List<EventImage> findByEventIdOrderByDisplayOrder(Long eventId);

    /**
     * Find the cover image for an event
     */
    Optional<EventImage> findByEventIdAndIsCoverImageTrue(Long eventId);

    /**
     * Count images for an event
     */
    long countByEventId(Long eventId);

    /**
     * Delete all images for an event
     */
    void deleteByEventId(Long eventId);

    /**
     * Update display order
     */
    @Modifying
    @Query("UPDATE EventImage ei SET ei.displayOrder = :displayOrder WHERE ei.id = :id")
    void updateDisplayOrder(@Param("id") Long id, @Param("displayOrder") Integer displayOrder);

    /**
     * Clear cover image flag for all images of an event
     */
    @Modifying
    @Query("UPDATE EventImage ei SET ei.isCoverImage = false WHERE ei.event.id = :eventId")
    void clearCoverImage(@Param("eventId") Long eventId);

    /**
     * Find images for multiple events
     */
    @Query("SELECT ei FROM EventImage ei WHERE ei.event.id IN :eventIds ORDER BY ei.event.id, ei.displayOrder")
    List<EventImage> findByEventIdIn(@Param("eventIds") List<Long> eventIds);
}
