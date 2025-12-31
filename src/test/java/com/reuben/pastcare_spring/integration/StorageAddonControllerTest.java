package com.reuben.pastcare_spring.integration;

import com.reuben.pastcare_spring.models.StorageAddon;
import com.reuben.pastcare_spring.repositories.StorageAddonRepository;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Storage Add-on endpoints.
 * Tests both public endpoints (for authenticated users) and admin endpoints (SUPERADMIN only).
 */
@DisplayName("Storage Add-on API Integration Tests")
public class StorageAddonControllerTest extends BaseIntegrationTest {

    @Autowired
    private StorageAddonRepository storageAddonRepository;

    private Long churchId;
    private StorageAddon testAddon;

    @BeforeEach
    void setUp() {
        // Clean up any existing test data
        storageAddonRepository.deleteAll();

        // Create a test church for multi-role testing (unique name per test)
        String uniqueChurchName = "Storage Addon Test Church " + System.currentTimeMillis();
        churchId = createTestChurch(uniqueChurchName);

        // Create a test storage add-on
        testAddon = new StorageAddon();
        testAddon.setName("TEST_5GB_ADDON");
        testAddon.setDisplayName("+5 GB Storage");
        testAddon.setDescription("Test add-on for integration tests");
        testAddon.setStorageGb(5);
        testAddon.setPrice(new BigDecimal("37.50"));
        testAddon.setTotalStorageGb(7); // 2GB base + 5GB addon
        testAddon.setEstimatedPhotos(2000);
        testAddon.setEstimatedDocuments(1500);
        testAddon.setIsActive(true);
        testAddon.setIsRecommended(false);
        testAddon.setDisplayOrder(1);
        testAddon = storageAddonRepository.save(testAddon);
    }

    // ============================================================================
    // PUBLIC ENDPOINT TESTS (Authenticated Users)
    // ============================================================================

    @Test
    @DisplayName("GET /api/storage-addons - ADMIN - Should return all active add-ons")
    void getActiveStorageAddons_asAdmin_shouldReturnActiveAddons() {
        // Create another active addon
        StorageAddon activeAddon = createAddon("ACTIVE_10GB", "+10 GB", true, 2);

        // Create an inactive addon (should not be returned)
        createAddon("INACTIVE_20GB", "+20 GB", false, 3);

        String token = getAdminToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/storage-addons")
        .then()
            .statusCode(200)
            .body("size()", is(2))
            .body("[0].name", equalTo("TEST_5GB_ADDON"))
            .body("[0].displayName", equalTo("+5 GB Storage"))
            .body("[0].price", is(37.50f))
            .body("[0].isActive", is(true))
            .body("[1].name", equalTo("ACTIVE_10GB"));
    }

    @Test
    @DisplayName("GET /api/storage-addons - PASTOR - Should return active add-ons")
    void getActiveStorageAddons_asPastor_shouldReturnActiveAddons() {
        String token = getPastorToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/storage-addons")
        .then()
            .statusCode(200)
            .body("size()", is(1))
            .body("[0].name", equalTo("TEST_5GB_ADDON"));
    }

    @Test
    @DisplayName("GET /api/storage-addons - TREASURER - Should return active add-ons")
    void getActiveStorageAddons_asTreasurer_shouldReturnActiveAddons() {
        String token = getTreasurerToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/storage-addons")
        .then()
            .statusCode(200)
            .body("size()", is(1));
    }

    @Test
    @DisplayName("GET /api/storage-addons - MEMBER - Should return active add-ons")
    void getActiveStorageAddons_asMember_shouldReturnActiveAddons() {
        String token = getMemberToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/storage-addons")
        .then()
            .statusCode(200)
            .body("size()", is(1));
    }

    @Test
    @DisplayName("GET /api/storage-addons - Unauthenticated - Should return 403")
    void getActiveStorageAddons_unauthenticated_shouldReturn403() {
        given()
            .spec(spec)
        .when()
            .get("/api/storage-addons")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("GET /api/storage-addons - Should return add-ons sorted by display order")
    void getActiveStorageAddons_shouldReturnSortedByDisplayOrder() {
        // Create add-ons with different display orders
        createAddon("ADDON_3", "+30 GB", true, 3);
        createAddon("ADDON_2", "+20 GB", true, 2);
        createAddon("ADDON_4", "+40 GB", true, 4);

        String token = getAdminToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/storage-addons")
        .then()
            .statusCode(200)
            .body("size()", is(4))
            .body("[0].displayOrder", is(1))
            .body("[1].displayOrder", is(2))
            .body("[2].displayOrder", is(3))
            .body("[3].displayOrder", is(4));
    }

    // ============================================================================
    // ADMIN ENDPOINT TESTS (SUPERADMIN Only)
    // ============================================================================

    @Test
    @DisplayName("GET /api/admin/storage-addons - SUPERADMIN - Should return all add-ons")
    void getAllStorageAddons_asSuperadmin_shouldReturnAllAddons() {
        // Create active and inactive add-ons
        createAddon("ACTIVE_ADDON", "+10 GB", true, 2);
        createAddon("INACTIVE_ADDON", "+20 GB", false, 3);

        String token = getSuperadminToken();

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/admin/storage-addons")
        .then()
            .statusCode(200)
            .body("size()", is(3)) // Should include inactive
            .body("name", hasItems("TEST_5GB_ADDON", "ACTIVE_ADDON", "INACTIVE_ADDON"));
    }

    @Test
    @DisplayName("GET /api/admin/storage-addons - ADMIN - Should return 403")
    void getAllStorageAddons_asAdmin_shouldReturn403() {
        String token = getAdminToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/admin/storage-addons")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("GET /api/admin/storage-addons - PASTOR - Should return 403")
    void getAllStorageAddons_asPastor_shouldReturn403() {
        String token = getPastorToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/admin/storage-addons")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("GET /api/admin/storage-addons/{id} - SUPERADMIN - Should return specific add-on")
    void getStorageAddonById_asSuperadmin_shouldReturnAddon() {
        String token = getSuperadminToken();

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/admin/storage-addons/" + testAddon.getId())
        .then()
            .statusCode(200)
            .body("id", equalTo(testAddon.getId().intValue()))
            .body("name", equalTo("TEST_5GB_ADDON"))
            .body("displayName", equalTo("+5 GB Storage"))
            .body("storageGb", is(5))
            .body("price", is(37.50f))
            .body("totalStorageGb", is(7))
            .body("estimatedPhotos", is(2000))
            .body("estimatedDocuments", is(1500));
    }

    @Test
    @DisplayName("GET /api/admin/storage-addons/{id} - ADMIN - Should return 403")
    void getStorageAddonById_asAdmin_shouldReturn403() {
        String token = getAdminToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/admin/storage-addons/" + testAddon.getId())
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("GET /api/admin/storage-addons/{id} - Non-existent ID - Should return 404")
    void getStorageAddonById_nonExistent_shouldReturn404() {
        String token = getSuperadminToken();

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/admin/storage-addons/99999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("POST /api/admin/storage-addons - SUPERADMIN - Should create new add-on")
    void createStorageAddon_asSuperadmin_shouldCreateAddon() {
        String token = getSuperadminToken();

        StorageAddon newAddon = new StorageAddon();
        newAddon.setName("NEW_15GB_ADDON");
        newAddon.setDisplayName("+15 GB Storage");
        newAddon.setDescription("Brand new add-on");
        newAddon.setStorageGb(15);
        newAddon.setPrice(new BigDecimal("75.00"));
        newAddon.setTotalStorageGb(17);
        newAddon.setEstimatedPhotos(7500);
        newAddon.setEstimatedDocuments(5000);
        newAddon.setIsActive(true);
        newAddon.setIsRecommended(true);
        newAddon.setDisplayOrder(5);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(newAddon)
        .when()
            .post("/api/admin/storage-addons")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("name", equalTo("NEW_15GB_ADDON"))
            .body("displayName", equalTo("+15 GB Storage"))
            .body("price", is(75.00f))
            .body("isRecommended", is(true))
            .body("createdAt", notNullValue());
    }

    @Test
    @DisplayName("POST /api/admin/storage-addons - ADMIN - Should return 403")
    void createStorageAddon_asAdmin_shouldReturn403() {
        String token = getAdminToken(churchId);

        StorageAddon newAddon = new StorageAddon();
        newAddon.setName("FORBIDDEN_ADDON");
        newAddon.setDisplayName("+100 GB");

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(newAddon)
        .when()
            .post("/api/admin/storage-addons")
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("POST /api/admin/storage-addons - Duplicate name - Should return 400")
    void createStorageAddon_duplicateName_shouldReturn400() {
        String token = getSuperadminToken();

        StorageAddon duplicateAddon = new StorageAddon();
        duplicateAddon.setName("TEST_5GB_ADDON"); // Same as testAddon
        duplicateAddon.setDisplayName("+5 GB Duplicate");
        duplicateAddon.setStorageGb(5);
        duplicateAddon.setPrice(new BigDecimal("37.50"));
        duplicateAddon.setTotalStorageGb(7);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(duplicateAddon)
        .when()
            .post("/api/admin/storage-addons")
        .then()
            .statusCode(400);
    }

    @Test
    @DisplayName("PUT /api/admin/storage-addons/{id} - SUPERADMIN - Should update add-on")
    void updateStorageAddon_asSuperadmin_shouldUpdateAddon() {
        String token = getSuperadminToken();

        testAddon.setDisplayName("+5 GB Storage (Updated)");
        testAddon.setPrice(new BigDecimal("40.00"));
        testAddon.setDescription("Updated description");
        testAddon.setIsRecommended(true);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(testAddon)
        .when()
            .put("/api/admin/storage-addons/" + testAddon.getId())
        .then()
            .statusCode(200)
            .body("id", equalTo(testAddon.getId().intValue()))
            .body("displayName", equalTo("+5 GB Storage (Updated)"))
            .body("price", is(40.00f))
            .body("description", equalTo("Updated description"))
            .body("isRecommended", is(true))
            .body("updatedAt", notNullValue());
    }

    @Test
    @DisplayName("PUT /api/admin/storage-addons/{id} - ADMIN - Should return 403")
    void updateStorageAddon_asAdmin_shouldReturn403() {
        String token = getAdminToken(churchId);

        testAddon.setPrice(new BigDecimal("100.00"));

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(testAddon)
        .when()
            .put("/api/admin/storage-addons/" + testAddon.getId())
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("PUT /api/admin/storage-addons/{id} - Non-existent ID - Should return 404")
    void updateStorageAddon_nonExistent_shouldReturn404() {
        String token = getSuperadminToken();

        StorageAddon nonExistent = new StorageAddon();
        nonExistent.setId(99999L);
        nonExistent.setName("NON_EXISTENT");

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(nonExistent)
        .when()
            .put("/api/admin/storage-addons/99999")
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /api/admin/storage-addons/{id} - SUPERADMIN - Should delete add-on")
    void deleteStorageAddon_asSuperadmin_shouldDeleteAddon() {
        String token = getSuperadminToken();

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/admin/storage-addons/" + testAddon.getId())
        .then()
            .statusCode(204);

        // Verify deletion
        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/api/admin/storage-addons/" + testAddon.getId())
        .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("DELETE /api/admin/storage-addons/{id} - ADMIN - Should return 403")
    void deleteStorageAddon_asAdmin_shouldReturn403() {
        String token = getAdminToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/admin/storage-addons/" + testAddon.getId())
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("DELETE /api/admin/storage-addons/{id} - PASTOR - Should return 403")
    void deleteStorageAddon_asPastor_shouldReturn403() {
        String token = getPastorToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/api/admin/storage-addons/" + testAddon.getId())
        .then()
            .statusCode(403);
    }

    @Test
    @DisplayName("PATCH /api/admin/storage-addons/{id}/toggle-active - SUPERADMIN - Should toggle active status")
    void toggleActiveStatus_asSuperadmin_shouldToggleStatus() {
        String token = getSuperadminToken();

        // Initial state: active
        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .patch("/api/admin/storage-addons/" + testAddon.getId() + "/toggle-active")
        .then()
            .statusCode(200)
            .body("isActive", is(false));

        // Toggle again
        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .patch("/api/admin/storage-addons/" + testAddon.getId() + "/toggle-active")
        .then()
            .statusCode(200)
            .body("isActive", is(true));
    }

    @Test
    @DisplayName("PATCH /api/admin/storage-addons/{id}/toggle-active - ADMIN - Should return 403")
    void toggleActiveStatus_asAdmin_shouldReturn403() {
        String token = getAdminToken(churchId);

        given()
            .spec(spec)
            .header("Authorization", "Bearer " + token)
        .when()
            .patch("/api/admin/storage-addons/" + testAddon.getId() + "/toggle-active")
        .then()
            .statusCode(403);
    }

    // ============================================================================
    // HELPER METHODS
    // ============================================================================

    private StorageAddon createAddon(String name, String displayName, boolean isActive, int displayOrder) {
        StorageAddon addon = new StorageAddon();
        addon.setName(name);
        addon.setDisplayName(displayName);
        addon.setDescription("Test addon: " + name);
        addon.setStorageGb(10);
        addon.setPrice(new BigDecimal("50.00"));
        addon.setTotalStorageGb(12);
        addon.setEstimatedPhotos(5000);
        addon.setEstimatedDocuments(3000);
        addon.setIsActive(isActive);
        addon.setIsRecommended(false);
        addon.setDisplayOrder(displayOrder);
        return storageAddonRepository.save(addon);
    }
}
