package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.SavedSearch;
import com.reuben.pastcare_spring.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for SavedSearchRepository.
 * Tests database queries for saved searches.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false"
})
@DisplayName("Saved Search Repository Tests")
class SavedSearchRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SavedSearchRepository savedSearchRepository;

    private Church church;
    private User user1;
    private User user2;
    private SavedSearch publicSearch;
    private SavedSearch privateSearch;

    @BeforeEach
    void setUp() {
        // Create church
        church = new Church();
        church.setName("Test Church");
        church.setEmail("test@church.com");
        church.setPhoneNumber("+233241234567");
        entityManager.persist(church);

        // Create users
        user1 = new User();
        user1.setName("John Doe");
        user1.setEmail("john@example.com");
        user1.setPassword("password");
        user1.setChurch(church);
        user1.setRole(Role.ADMIN);
        entityManager.persist(user1);

        user2 = new User();
        user2.setName("Jane Smith");
        user2.setEmail("jane@example.com");
        user2.setPassword("password");
        user2.setChurch(church);
        user2.setRole(Role.ADMIN);
        entityManager.persist(user2);

        // Create public saved search
        publicSearch = SavedSearch.builder()
            .church(church)
            .createdBy(user1)
            .searchName("Public Active Members")
            .searchCriteria("{\"filterGroups\":[]}")
            .isPublic(true)
            .isDynamic(false)
            .description("Public search for active members")
            .build();
        entityManager.persist(publicSearch);

        // Create private saved search
        privateSearch = SavedSearch.builder()
            .church(church)
            .createdBy(user1)
            .searchName("Private Search")
            .searchCriteria("{\"filterGroups\":[]}")
            .isPublic(false)
            .isDynamic(true)
            .description("Private search")
            .build();
        entityManager.persist(privateSearch);

        entityManager.flush();
    }

    // ========== Find Accessible Searches ==========

    @Test
    @DisplayName("Should find all public searches and user's private searches")
    void testFindAccessibleSearches() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When - user1 should see both (creator of both)
        Page<SavedSearch> result = savedSearchRepository.findAccessibleSearches(church, user1, pageable);

        // Then
        assertEquals(2, result.getTotalElements());
        assertTrue(result.getContent().stream().anyMatch(s -> s.getSearchName().equals("Public Active Members")));
        assertTrue(result.getContent().stream().anyMatch(s -> s.getSearchName().equals("Private Search")));
    }

    @Test
    @DisplayName("Should find only public searches for non-creator")
    void testFindAccessibleSearches_NonCreator() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When - user2 should only see public search
        Page<SavedSearch> result = savedSearchRepository.findAccessibleSearches(church, user2, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("Public Active Members", result.getContent().get(0).getSearchName());
    }

    // ========== Find By Church And Created By ==========

    @Test
    @DisplayName("Should find searches by creator")
    void testFindByChurchAndCreatedBy() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<SavedSearch> result = savedSearchRepository.findByChurchAndCreatedBy(church, user1, pageable);

        // Then
        assertEquals(2, result.getTotalElements());
    }

    // ========== Find Public Searches ==========

    @Test
    @DisplayName("Should find all public searches")
    void testFindByChurchAndIsPublicTrue() {
        // Given
        Pageable pageable = PageRequest.of(0, 20);

        // When
        Page<SavedSearch> result = savedSearchRepository.findByChurchAndIsPublicTrue(church, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("Public Active Members", result.getContent().get(0).getSearchName());
    }

    // ========== Find By ID And Church ==========

    @Test
    @DisplayName("Should find search by ID and church")
    void testFindByIdAndChurch() {
        // When
        Optional<SavedSearch> result = savedSearchRepository.findByIdAndChurch(publicSearch.getId(), church);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Public Active Members", result.get().getSearchName());
    }

    @Test
    @DisplayName("Should return empty when church does not match")
    void testFindByIdAndChurch_WrongChurch() {
        // Given
        Church anotherChurch = new Church();
        anotherChurch.setName("Another Church");
        anotherChurch.setEmail("another@church.com");
        anotherChurch.setPhoneNumber("+233241234568");
        entityManager.persist(anotherChurch);
        entityManager.flush();

        // When
        Optional<SavedSearch> result = savedSearchRepository.findByIdAndChurch(publicSearch.getId(), anotherChurch);

        // Then
        assertFalse(result.isPresent());
    }

    // ========== Count By Church ==========

    @Test
    @DisplayName("Should count searches by church")
    void testCountByChurch() {
        // When
        long count = savedSearchRepository.countByChurch(church);

        // Then
        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should return zero count for church with no searches")
    void testCountByChurch_NoSearches() {
        // Given
        Church anotherChurch = new Church();
        anotherChurch.setName("Another Church");
        anotherChurch.setEmail("another@church.com");
        anotherChurch.setPhoneNumber("+233241234568");
        entityManager.persist(anotherChurch);
        entityManager.flush();

        // When
        long count = savedSearchRepository.countByChurch(anotherChurch);

        // Then
        assertEquals(0, count);
    }
}
