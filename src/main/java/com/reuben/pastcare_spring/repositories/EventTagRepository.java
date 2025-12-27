package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Event;
import com.reuben.pastcare_spring.models.EventTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for EventTag entity.
 * Provides custom queries for tag-based event categorization and filtering.
 */
@Repository
public interface EventTagRepository extends JpaRepository<EventTag, Long> {

    // Basic queries
    Optional<EventTag> findByIdAndChurchId(Long id, Long churchId);

    List<EventTag> findByChurchId(Long churchId);

    // Tags for an event
    List<EventTag> findByEvent(Event event);

    @Query("SELECT t FROM EventTag t WHERE t.event.id = :eventId " +
           "ORDER BY t.tag ASC")
    List<EventTag> findByEventId(@Param("eventId") Long eventId);

    // Find tag by event and tag name
    @Query("SELECT t FROM EventTag t WHERE t.event.id = :eventId " +
           "AND LOWER(t.tag) = LOWER(:tag)")
    Optional<EventTag> findByEventIdAndTag(
        @Param("eventId") Long eventId,
        @Param("tag") String tag
    );

    // Check if tag exists for event
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
           "FROM EventTag t WHERE t.event.id = :eventId " +
           "AND LOWER(t.tag) = LOWER(:tag)")
    boolean existsByEventIdAndTag(
        @Param("eventId") Long eventId,
        @Param("tag") String tag
    );

    // Events with a specific tag
    @Query("SELECT t.event FROM EventTag t WHERE t.church.id = :churchId " +
           "AND LOWER(t.tag) = LOWER(:tag) " +
           "AND t.event.deletedAt IS NULL " +
           "ORDER BY t.event.startDate DESC")
    List<Event> findEventsByTag(
        @Param("churchId") Long churchId,
        @Param("tag") String tag
    );

    // Events with any of the specified tags
    @Query("SELECT DISTINCT t.event FROM EventTag t WHERE t.church.id = :churchId " +
           "AND LOWER(t.tag) IN :tags " +
           "AND t.event.deletedAt IS NULL " +
           "ORDER BY t.event.startDate DESC")
    List<Event> findEventsByTags(
        @Param("churchId") Long churchId,
        @Param("tags") List<String> tags
    );

    // All unique tags for a church
    @Query("SELECT DISTINCT t.tag FROM EventTag t WHERE t.church.id = :churchId " +
           "ORDER BY t.tag ASC")
    List<String> findDistinctTagsByChurchId(@Param("churchId") Long churchId);

    // Tag usage count
    @Query("SELECT t.tag, COUNT(t) FROM EventTag t " +
           "WHERE t.church.id = :churchId " +
           "GROUP BY t.tag " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> countTagUsage(@Param("churchId") Long churchId);

    // Popular tags (most used)
    @Query("SELECT t.tag, COUNT(t) FROM EventTag t " +
           "WHERE t.church.id = :churchId " +
           "GROUP BY t.tag " +
           "ORDER BY COUNT(t) DESC")
    List<Object[]> findPopularTags(
        @Param("churchId") Long churchId,
        org.springframework.data.domain.Pageable pageable
    );

    // Tags with their colors
    @Query("SELECT DISTINCT t.tag, t.tagColor FROM EventTag t " +
           "WHERE t.church.id = :churchId " +
           "ORDER BY t.tag ASC")
    List<Object[]> findDistinctTagsWithColors(@Param("churchId") Long churchId);

    // Count tags for event
    @Query("SELECT COUNT(t) FROM EventTag t WHERE t.event.id = :eventId")
    long countByEventId(@Param("eventId") Long eventId);

    // Delete tag by event and tag name
    @Query("DELETE FROM EventTag t WHERE t.event.id = :eventId " +
           "AND LOWER(t.tag) = LOWER(:tag)")
    void deleteByEventIdAndTag(
        @Param("eventId") Long eventId,
        @Param("tag") String tag
    );

    // Delete all tags for event
    void deleteByEvent(Event event);

    @Query("DELETE FROM EventTag t WHERE t.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);

    // Search tags
    @Query("SELECT DISTINCT t.tag FROM EventTag t " +
           "WHERE t.church.id = :churchId " +
           "AND LOWER(t.tag) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY t.tag ASC")
    List<String> searchTags(
        @Param("churchId") Long churchId,
        @Param("searchTerm") String searchTerm
    );
}
