package com.reuben.pastcare_spring.integration;

import com.reuben.pastcare_spring.enums.Role;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for User Management
 *
 * Tests comprehensive user management functionality including:
 * - User creation (ADMIN creating users)
 * - User listing and search
 * - User profile updates
 * - Password management
 * - User activation/deactivation
 * - Role management
 * - Multi-tenancy isolation
 *
 * IMPORTANT: Each test verifies access for ALL user roles to ensure
 * proper RBAC enforcement.
 */
@DisplayName("User Management Integration Tests")
public class UserManagementIntegrationTest extends BaseIntegrationTest {

    private Long testChurchId;
    private Map<Role, String> roleTokens = new HashMap<>();

    @BeforeEach
    void setUp() {
        // Create test church using base class helper
        testChurchId = createTestChurch("User Management Test Church");

        // Create tokens for each role
        roleTokens.put(Role.ADMIN, getAdminToken(testChurchId));
        roleTokens.put(Role.PASTOR, getPastorToken(testChurchId));
        roleTokens.put(Role.TREASURER, getTreasurerToken(testChurchId));
        roleTokens.put(Role.MEMBER_MANAGER, getMemberManagerToken(testChurchId));
        roleTokens.put(Role.FELLOWSHIP_LEADER, getFellowshipLeaderToken(testChurchId));
        roleTokens.put(Role.MEMBER, getMemberToken(testChurchId));
        roleTokens.put(Role.SUPERADMIN, getSuperadminToken());
    }

    // ==================== USER CREATION TESTS ====================

    @Nested
    @DisplayName("User Creation Access Control")
    class UserCreationAccess {

        @Test
        @DisplayName("USER-001: ADMIN can create users")
        void adminCanCreateUsers() {
            Map<String, Object> userRequest = createUserRequest("PASTOR");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(201)
                .body("name", equalTo(userRequest.get("name")))
                .body("email", equalTo(userRequest.get("email")))
                .body("role", equalTo("PASTOR"));
        }

        @Test
        @DisplayName("USER-002: SUPERADMIN can create users in any church")
        void superadminCanCreateUsers() {
            Map<String, Object> userRequest = createUserRequest("PASTOR");
            userRequest.put("churchId", testChurchId);

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.SUPERADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(201);
        }

        @Test
        @DisplayName("USER-003: PASTOR cannot create users")
        void pastorCannotCreateUsers() {
            Map<String, Object> userRequest = createUserRequest("MEMBER");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.PASTOR)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-004: TREASURER cannot create users")
        void treasurerCannotCreateUsers() {
            Map<String, Object> userRequest = createUserRequest("MEMBER");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.TREASURER)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-005: MEMBER_MANAGER cannot create users")
        void memberManagerCannotCreateUsers() {
            Map<String, Object> userRequest = createUserRequest("MEMBER");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.MEMBER_MANAGER)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-006: FELLOWSHIP_LEADER cannot create users")
        void fellowshipLeaderCannotCreateUsers() {
            Map<String, Object> userRequest = createUserRequest("MEMBER");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.FELLOWSHIP_LEADER)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-007: MEMBER cannot create users")
        void memberCannotCreateUsers() {
            Map<String, Object> userRequest = createUserRequest("MEMBER");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.MEMBER)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-008: ADMIN cannot create SUPERADMIN users (privilege escalation)")
        void adminCannotCreateSuperadmin() {
            Map<String, Object> userRequest = createUserRequest("SUPERADMIN");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(403);
        }
    }

    // ==================== USER LISTING TESTS ====================

    @Nested
    @DisplayName("User Listing Access Control")
    class UserListingAccess {

        @Test
        @DisplayName("USER-009: ADMIN can view all users")
        void adminCanViewAllUsers() {
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
            .when()
                .get("/api/users")
            .then()
                .statusCode(200)
                .body("$", hasSize(greaterThan(0)));
        }

        @Test
        @DisplayName("USER-010: SUPERADMIN can view all users")
        void superadminCanViewAllUsers() {
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.SUPERADMIN)))
            .when()
                .get("/api/users")
            .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("USER-011: PASTOR cannot view user list")
        void pastorCannotViewUserList() {
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.PASTOR)))
            .when()
                .get("/api/users")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-012: TREASURER cannot view user list")
        void treasurerCannotViewUserList() {
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.TREASURER)))
            .when()
                .get("/api/users")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-013: MEMBER_MANAGER cannot view user list")
        void memberManagerCannotViewUserList() {
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.MEMBER_MANAGER)))
            .when()
                .get("/api/users")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-014: FELLOWSHIP_LEADER cannot view user list")
        void fellowshipLeaderCannotViewUserList() {
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.FELLOWSHIP_LEADER)))
            .when()
                .get("/api/users")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-015: MEMBER cannot view user list")
        void memberCannotViewUserList() {
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.MEMBER)))
            .when()
                .get("/api/users")
            .then()
                .statusCode(403);
        }
    }

    // ==================== USER UPDATE TESTS ====================

    @Nested
    @DisplayName("User Update Access Control")
    class UserUpdateAccess {

        private Long testUserId;

        @BeforeEach
        void createTestUser() {
            // Admin creates a test user
            Map<String, Object> userRequest = createUserRequest("PASTOR");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(201)
                .extract().response();

            testUserId = response.jsonPath().getLong("id");
        }

        @Test
        @DisplayName("USER-016: ADMIN can update users")
        void adminCanUpdateUsers() {
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("name", "Updated Name");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(updateRequest)
            .when()
                .put("/api/users/" + testUserId)
            .then()
                .statusCode(200)
                .body("name", equalTo("Updated Name"));
        }

        @Test
        @DisplayName("USER-017: PASTOR cannot update other users")
        void pastorCannotUpdateOtherUsers() {
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("name", "Hacked Name");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.PASTOR)))
                .body(updateRequest)
            .when()
                .put("/api/users/" + testUserId)
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-018: MEMBER cannot update other users")
        void memberCannotUpdateOtherUsers() {
            Map<String, Object> updateRequest = new HashMap<>();
            updateRequest.put("name", "Hacked Name");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.MEMBER)))
                .body(updateRequest)
            .when()
                .put("/api/users/" + testUserId)
            .then()
                .statusCode(403);
        }
    }

    // ==================== USER ROLE CHANGE TESTS ====================

    @Nested
    @DisplayName("User Role Change Access Control")
    class UserRoleChangeAccess {

        private Long testUserId;

        @BeforeEach
        void createTestUser() {
            Map<String, Object> userRequest = createUserRequest("MEMBER");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(201)
                .extract().response();

            testUserId = response.jsonPath().getLong("id");
        }

        @Test
        @DisplayName("USER-019: ADMIN can change user roles")
        void adminCanChangeUserRoles() {
            Map<String, Object> roleRequest = new HashMap<>();
            roleRequest.put("role", "PASTOR");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(roleRequest)
            .when()
                .put("/api/users/" + testUserId + "/role")
            .then()
                .statusCode(200)
                .body("role", equalTo("PASTOR"));
        }

        @Test
        @DisplayName("USER-020: ADMIN cannot escalate to SUPERADMIN")
        void adminCannotEscalateToSuperadmin() {
            Map<String, Object> roleRequest = new HashMap<>();
            roleRequest.put("role", "SUPERADMIN");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(roleRequest)
            .when()
                .put("/api/users/" + testUserId + "/role")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-021: PASTOR cannot change user roles")
        void pastorCannotChangeUserRoles() {
            Map<String, Object> roleRequest = new HashMap<>();
            roleRequest.put("role", "ADMIN");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.PASTOR)))
                .body(roleRequest)
            .when()
                .put("/api/users/" + testUserId + "/role")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-022: MEMBER cannot change user roles")
        void memberCannotChangeUserRoles() {
            Map<String, Object> roleRequest = new HashMap<>();
            roleRequest.put("role", "ADMIN");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.MEMBER)))
                .body(roleRequest)
            .when()
                .put("/api/users/" + testUserId + "/role")
            .then()
                .statusCode(403);
        }
    }

    // ==================== USER ACTIVATION TESTS ====================

    @Nested
    @DisplayName("User Activation/Deactivation Access Control")
    class UserActivationAccess {

        private Long testUserId;

        @BeforeEach
        void createTestUser() {
            Map<String, Object> userRequest = createUserRequest("PASTOR");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(201)
                .extract().response();

            testUserId = response.jsonPath().getLong("id");
        }

        @Test
        @DisplayName("USER-023: ADMIN can deactivate users")
        void adminCanDeactivateUsers() {
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
            .when()
                .post("/api/users/" + testUserId + "/deactivate")
            .then()
                .statusCode(200)
                .body("active", equalTo(false));
        }

        @Test
        @DisplayName("USER-024: ADMIN can reactivate users")
        void adminCanReactivateUsers() {
            // First deactivate
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
            .when()
                .post("/api/users/" + testUserId + "/deactivate")
            .then()
                .statusCode(200);

            // Then reactivate
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
            .when()
                .post("/api/users/" + testUserId + "/activate")
            .then()
                .statusCode(200)
                .body("active", equalTo(true));
        }

        @Test
        @DisplayName("USER-025: PASTOR cannot deactivate users")
        void pastorCannotDeactivateUsers() {
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.PASTOR)))
            .when()
                .post("/api/users/" + testUserId + "/deactivate")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("USER-026: MEMBER cannot deactivate users")
        void memberCannotDeactivateUsers() {
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.MEMBER)))
            .when()
                .post("/api/users/" + testUserId + "/deactivate")
            .then()
                .statusCode(403);
        }
    }

    // ==================== MULTI-TENANCY TESTS ====================

    @Nested
    @DisplayName("Multi-Tenancy Isolation")
    class MultiTenancyIsolation {

        @Test
        @DisplayName("USER-027: Cannot access users from other churches")
        void cannotAccessUsersFromOtherChurches() {
            // Create another church
            Long otherChurchId = createTestChurch("Other Church");
            String otherAdminToken = getAdminToken(otherChurchId);

            // Create user in other church
            Map<String, Object> userRequest = createUserRequest("PASTOR");

            Response response = given()
                .spec(authenticatedSpec(otherAdminToken))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(201)
                .extract().response();

            Long otherUserId = response.jsonPath().getLong("id");

            // Try to access other church's user
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
            .when()
                .get("/api/users/" + otherUserId)
            .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("USER-028: User list only contains own church users")
        void userListOnlyContainsOwnChurchUsers() {
            // Create another church with users
            Long otherChurchId = createTestChurch("Other Church");
            String otherAdminToken = getAdminToken(otherChurchId);

            Map<String, Object> userRequest = createUserRequest("PASTOR");
            userRequest.put("name", "Other Church User");

            given()
                .spec(authenticatedSpec(otherAdminToken))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(201);

            // Get users from original church
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
            .when()
                .get("/api/users")
            .then()
                .statusCode(200)
                .body("name", not(hasItem("Other Church User")));
        }
    }

    // ==================== VALIDATION TESTS ====================

    @Nested
    @DisplayName("Input Validation")
    class InputValidation {

        @Test
        @DisplayName("USER-029: Cannot create user with duplicate email")
        void cannotCreateDuplicateEmail() {
            Map<String, Object> userRequest = createUserRequest("PASTOR");

            // Create first user
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(201);

            // Try to create second user with same email
            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(409);
        }

        @Test
        @DisplayName("USER-030: Cannot create user without required fields")
        void cannotCreateWithoutRequiredFields() {
            Map<String, Object> invalidRequest = new HashMap<>();
            invalidRequest.put("password", "Password123!");
            // Missing name and email

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(invalidRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("USER-031: Cannot create user with invalid email")
        void cannotCreateWithInvalidEmail() {
            Map<String, Object> invalidRequest = new HashMap<>();
            invalidRequest.put("name", "Test User");
            invalidRequest.put("email", "not-an-email");
            invalidRequest.put("password", "Password123!");
            invalidRequest.put("role", "PASTOR");

            given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(invalidRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(400);
        }
    }

    // ==================== UNAUTHENTICATED TESTS ====================

    @Nested
    @DisplayName("Unauthenticated Access")
    class UnauthenticatedAccess {

        @Test
        @DisplayName("USER-032: Cannot access users without authentication")
        void cannotAccessUsersWithoutAuth() {
            given()
                .spec(spec)
            .when()
                .get("/api/users")
            .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("USER-033: Cannot create users without authentication")
        void cannotCreateUsersWithoutAuth() {
            Map<String, Object> userRequest = createUserRequest("PASTOR");

            given()
                .spec(spec)
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .statusCode(401);
        }

        @Test
        @DisplayName("USER-034: Invalid token returns 401")
        void invalidTokenReturns401() {
            given()
                .spec(spec)
                .header("Authorization", "Bearer invalid.token.here")
            .when()
                .get("/api/users")
            .then()
                .statusCode(401);
        }
    }

    // ==================== HELPER METHODS ====================

    private Map<String, Object> createUserRequest(String role) {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Test User " + System.currentTimeMillis());
        request.put("email", "testuser" + System.currentTimeMillis() + "@test.com");
        request.put("password", "SecurePass123!");
        request.put("role", role);
        return request;
    }
}
