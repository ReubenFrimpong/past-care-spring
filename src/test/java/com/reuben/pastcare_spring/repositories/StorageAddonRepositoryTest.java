package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.StorageAddon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for StorageAddonRepository.
 * Tests custom query methods and basic CRUD operations.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("StorageAddonRepository Tests")
class StorageAddonRepositoryTest {

    @Autowired
    private StorageAddonRepository storageAddonRepository;

    @BeforeEach
    void setUp() {
        storageAddonRepository.deleteAll();
    }

    // ============================================================================
    // Basic CRUD Tests
    // ============================================================================

    @Test
    @DisplayName("Should save and retrieve storage add-on")
    void saveAndRetrieve_shouldWork() {
        // Given
        StorageAddon addon = createTestAddon("3GB_ADDON", "+3 GB Storage", true, 1);

        // When
        StorageAddon saved = storageAddonRepository.save(addon);
        Optional<StorageAddon> retrieved = storageAddonRepository.findById(saved.getId());

        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getName()).isEqualTo("3GB_ADDON");
        assertThat(retrieved.get().getDisplayName()).isEqualTo("+3 GB Storage");
        assertThat(retrieved.get().getPrice()).isEqualByComparingTo(new BigDecimal("37.50"));
        assertThat(retrieved.get().getStorageGb()).isEqualTo(3);
        assertThat(retrieved.get().getTotalStorageGb()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should update storage add-on")
    void update_shouldModifyAddon() {
        // Given
        StorageAddon addon = createTestAddon("5GB_ADDON", "+5 GB Storage", true, 1);
        StorageAddon saved = storageAddonRepository.save(addon);

        // When
        saved.setDisplayName("+5 GB Storage (Updated)");
        saved.setPrice(new BigDecimal("40.00"));
        saved.setIsRecommended(true);
        StorageAddon updated = storageAddonRepository.save(saved);

        // Then
        assertThat(updated.getId()).isEqualTo(saved.getId());
        assertThat(updated.getDisplayName()).isEqualTo("+5 GB Storage (Updated)");
        assertThat(updated.getPrice()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(updated.getIsRecommended()).isTrue();
    }

    @Test
    @DisplayName("Should delete storage add-on")
    void delete_shouldRemoveAddon() {
        // Given
        StorageAddon addon = createTestAddon("10GB_ADDON", "+10 GB Storage", true, 1);
        StorageAddon saved = storageAddonRepository.save(addon);

        // When
        storageAddonRepository.deleteById(saved.getId());

        // Then
        Optional<StorageAddon> retrieved = storageAddonRepository.findById(saved.getId());
        assertThat(retrieved).isEmpty();
    }

    // ============================================================================
    // Custom Query Tests
    // ============================================================================

    @Test
    @DisplayName("findByIsActiveTrueOrderByDisplayOrderAsc - Should return only active add-ons sorted by display order")
    void findByIsActiveTrueOrderByDisplayOrderAsc_shouldReturnActiveSorted() {
        // Given
        createTestAddon("ADDON_1", "+3 GB", true, 3);
        createTestAddon("ADDON_2", "+5 GB", true, 1);
        createTestAddon("ADDON_3", "+8 GB", false, 2); // Inactive
        createTestAddon("ADDON_4", "+10 GB", true, 2);

        // When
        List<StorageAddon> activeAddons = storageAddonRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        // Then
        assertThat(activeAddons).hasSize(3);
        assertThat(activeAddons.get(0).getName()).isEqualTo("ADDON_2"); // displayOrder = 1
        assertThat(activeAddons.get(1).getName()).isEqualTo("ADDON_4"); // displayOrder = 2
        assertThat(activeAddons.get(2).getName()).isEqualTo("ADDON_1"); // displayOrder = 3
    }

    @Test
    @DisplayName("findByIsActiveTrueOrderByDisplayOrderAsc - Should return empty list when no active add-ons")
    void findByIsActiveTrueOrderByDisplayOrderAsc_noActive_shouldReturnEmpty() {
        // Given
        createTestAddon("INACTIVE_1", "+3 GB", false, 1);
        createTestAddon("INACTIVE_2", "+5 GB", false, 2);

        // When
        List<StorageAddon> activeAddons = storageAddonRepository.findByIsActiveTrueOrderByDisplayOrderAsc();

        // Then
        assertThat(activeAddons).isEmpty();
    }

    @Test
    @DisplayName("findByName - Should find add-on by exact name")
    void findByName_exactMatch_shouldReturnAddon() {
        // Given
        createTestAddon("3GB_ADDON", "+3 GB Storage", true, 1);
        createTestAddon("5GB_ADDON", "+5 GB Storage", true, 2);

        // When
        Optional<StorageAddon> found = storageAddonRepository.findByName("3GB_ADDON");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("3GB_ADDON");
        assertThat(found.get().getDisplayName()).isEqualTo("+3 GB Storage");
    }

    @Test
    @DisplayName("findByName - Should return empty for non-existent name")
    void findByName_noMatch_shouldReturnEmpty() {
        // Given
        createTestAddon("3GB_ADDON", "+3 GB Storage", true, 1);

        // When
        Optional<StorageAddon> found = storageAddonRepository.findByName("NON_EXISTENT");

        // Then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByName - Should be case-sensitive")
    void findByName_caseSensitive() {
        // Given
        createTestAddon("3GB_ADDON", "+3 GB Storage", true, 1);

        // When
        Optional<StorageAddon> upperCase = storageAddonRepository.findByName("3GB_ADDON");
        Optional<StorageAddon> lowerCase = storageAddonRepository.findByName("3gb_addon");

        // Then
        assertThat(upperCase).isPresent();
        assertThat(lowerCase).isEmpty();
    }

    // ============================================================================
    // Field Validation Tests
    // ============================================================================

    @Test
    @DisplayName("Should handle all optional fields as null")
    void save_withNullOptionalFields_shouldWork() {
        // Given
        StorageAddon addon = new StorageAddon();
        addon.setName("MINIMAL_ADDON");
        addon.setDisplayName("+1 GB");
        addon.setStorageGb(1);
        addon.setPrice(new BigDecimal("10.00"));
        addon.setTotalStorageGb(3);
        addon.setIsActive(true);
        addon.setIsRecommended(false);
        addon.setDisplayOrder(1);
        // description, estimatedPhotos, estimatedDocuments are null

        // When
        StorageAddon saved = storageAddonRepository.save(addon);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getDescription()).isNull();
        assertThat(saved.getEstimatedPhotos()).isNull();
        assertThat(saved.getEstimatedDocuments()).isNull();
    }

    @Test
    @DisplayName("Should handle recommended flag correctly")
    void save_isRecommended_shouldPersist() {
        // Given
        StorageAddon recommended = createTestAddon("RECOMMENDED", "+8 GB", true, 1);
        recommended.setIsRecommended(true);

        StorageAddon notRecommended = createTestAddon("NOT_RECOMMENDED", "+3 GB", true, 2);
        notRecommended.setIsRecommended(false);

        // When
        StorageAddon savedRecommended = storageAddonRepository.save(recommended);
        StorageAddon savedNotRecommended = storageAddonRepository.save(notRecommended);

        // Then
        assertThat(savedRecommended.getIsRecommended()).isTrue();
        assertThat(savedNotRecommended.getIsRecommended()).isFalse();
    }

    @Test
    @DisplayName("Should auto-generate timestamps on create")
    void save_shouldAutoGenerateTimestamps() {
        // Given
        StorageAddon addon = createTestAddon("TIMESTAMP_TEST", "+5 GB", true, 1);

        // When
        StorageAddon saved = storageAddonRepository.save(addon);

        // Then
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update updatedAt timestamp on modification")
    void update_shouldUpdateTimestamp() throws InterruptedException {
        // Given
        StorageAddon addon = createTestAddon("UPDATE_TEST", "+5 GB", true, 1);
        StorageAddon saved = storageAddonRepository.save(addon);

        // Small delay to ensure timestamp difference
        Thread.sleep(100);

        // When
        saved.setPrice(new BigDecimal("50.00"));
        StorageAddon updated = storageAddonRepository.save(saved);

        // Then
        assertThat(updated.getUpdatedAt()).isAfter(saved.getCreatedAt());
    }

    // ============================================================================
    // Constraint Tests
    // ============================================================================

    @Test
    @DisplayName("Should enforce unique constraint on name")
    void save_duplicateName_shouldFail() {
        // Given
        createTestAddon("DUPLICATE_NAME", "+5 GB", true, 1);

        // When & Then
        StorageAddon duplicate = new StorageAddon();
        duplicate.setName("DUPLICATE_NAME"); // Same name - should fail
        duplicate.setDisplayName("+10 GB");
        duplicate.setStorageGb(10);
        duplicate.setPrice(new BigDecimal("50.00"));
        duplicate.setTotalStorageGb(12);
        duplicate.setIsActive(true);
        duplicate.setIsRecommended(false);
        duplicate.setDisplayOrder(2);

        try {
            storageAddonRepository.save(duplicate);
            storageAddonRepository.flush(); // Force constraint check
            assertThat(true).as("Should have thrown exception for duplicate name").isFalse();
        } catch (Exception e) {
            // Expected - unique constraint violation
            assertThat(e.getMessage()).containsAnyOf("Unique", "unique", "constraint", "Duplicate");
        }
    }

    @Test
    @DisplayName("Should allow multiple add-ons with same display order")
    void save_sameDisplayOrder_shouldWork() {
        // Given
        StorageAddon addon1 = createTestAddon("ADDON_1", "+3 GB", true, 1);
        StorageAddon addon2 = createTestAddon("ADDON_2", "+5 GB", true, 1); // Same display order

        // When
        StorageAddon saved1 = storageAddonRepository.save(addon1);
        StorageAddon saved2 = storageAddonRepository.save(addon2);

        // Then
        assertThat(saved1.getDisplayOrder()).isEqualTo(saved2.getDisplayOrder());
    }

    // ============================================================================
    // Helper Methods
    // ============================================================================

    private StorageAddon createTestAddon(String name, String displayName, boolean isActive, int displayOrder) {
        StorageAddon addon = new StorageAddon();
        addon.setName(name);
        addon.setDisplayName(displayName);
        addon.setDescription("Test description for " + name);
        addon.setStorageGb(3);
        addon.setPrice(new BigDecimal("37.50"));
        addon.setTotalStorageGb(5); // 2GB base + 3GB addon
        addon.setEstimatedPhotos(1500);
        addon.setEstimatedDocuments(1000);
        addon.setIsActive(isActive);
        addon.setIsRecommended(false);
        addon.setDisplayOrder(displayOrder);
        return storageAddonRepository.save(addon);
    }
}
