package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.SavedSearch;
import com.reuben.pastcare_spring.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for SavedSearch entity.
 * Handles database operations for saved member searches.
 */
@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {

    /**
     * Find all saved searches for a church (both public and user's private).
     *
     * @param church Church to filter by
     * @param user   Current user
     * @param pageable Pagination parameters
     * @return Page of saved searches
     */
    @Query("SELECT s FROM SavedSearch s WHERE s.church = :church " +
           "AND (s.isPublic = true OR s.createdBy = :user) " +
           "ORDER BY s.updatedAt DESC")
    Page<SavedSearch> findAccessibleSearches(
        @Param("church") Church church,
        @Param("user") User user,
        Pageable pageable
    );

    /**
     * Find all searches created by a specific user.
     *
     * @param church Church to filter by
     * @param user   User who created the searches
     * @param pageable Pagination parameters
     * @return Page of saved searches
     */
    Page<SavedSearch> findByChurchAndCreatedBy(Church church, User user, Pageable pageable);

    /**
     * Find all public searches for a church.
     *
     * @param church Church to filter by
     * @param pageable Pagination parameters
     * @return Page of public saved searches
     */
    Page<SavedSearch> findByChurchAndIsPublicTrue(Church church, Pageable pageable);

    /**
     * Find a saved search by ID and church (for security).
     *
     * @param id       Search ID
     * @param church   Church to filter by
     * @return Optional containing the search if found
     */
    @Query("SELECT s FROM SavedSearch s WHERE s.id = :id AND s.church = :church")
    java.util.Optional<SavedSearch> findByIdAndChurch(
        @Param("id") Long id,
        @Param("church") Church church
    );

    /**
     * Count saved searches by church.
     *
     * @param church Church to count for
     * @return Number of saved searches
     */
    long countByChurch(Church church);
}
