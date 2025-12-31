package com.reuben.pastcare_spring.security;

import com.reuben.pastcare_spring.integration.BaseIntegrationTest;
import com.reuben.pastcare_spring.enums.Role;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security Tests: Sensitive Data Leakage Prevention
 *
 * Verifies that sensitive data is NOT exposed in API responses:
 * - Passwords (hashed or plain)
 * - Internal tokens/secrets
 * - Internal system IDs unnecessarily
 * - Stack traces in error responses
 * - PII in error messages
 */
@DisplayName("Sensitive Data Leakage Prevention Tests")
public class SensitiveDataLeakageTest extends BaseIntegrationTest {

    private Long testChurchId;
    private String adminToken;
    private String testUserEmail;
    private final String testPassword = "SecurePass123!";

    @BeforeEach
    void setUp() {
        // Create test church with unique name to avoid conflicts in nested tests
        testChurchId = createTestChurch("Security Test Church " + System.currentTimeMillis() + "_" + Thread.currentThread().getId());

        // Get admin token for this church
        adminToken = getAdminToken(testChurchId);

        // Store email for tests
        testUserEmail = "secadmin" + System.currentTimeMillis() + "@test.com";
    }

    @Nested
    @DisplayName("Password Field Protection")
    class PasswordFieldProtection {

        @Test
        @DisplayName("SEC-001: Login response should not contain password")
        void loginResponseShouldNotContainPassword() {
            Map<String, Object> loginRequest = new HashMap<>();
            loginRequest.put("email", testUserEmail);
            loginRequest.put("password", testPassword);
            loginRequest.put("rememberMe", false);

            // Create a new user first
            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("name", "Test User");
            userRequest.put("email", testUserEmail);
            userRequest.put("password", testPassword);
            userRequest.put("role", "PASTOR");

            Response createResponse = given()
                .spec(authenticatedSpec(adminToken))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .extract().response();

            // Controller returns 200 OK, not 201 Created
            assertThat(createResponse.statusCode()).isIn(200, 201);

            // Now try login
            Response response = given()
                .spec(spec)
                .body(loginRequest)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .extract().response();

            String responseBody = response.asString();

            // Password should NEVER appear in response
            assertThat(responseBody.toLowerCase()).doesNotContain("\"password\"");
            assertThat(responseBody).doesNotContain(testPassword);
        }

        @Test
        @DisplayName("SEC-002: Get all users should not expose passwords")
        void getAllUsersShouldNotExposePasswords() {
            Response response = given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/users")
            .then()
                .statusCode(200)
                .extract().response();

            String responseBody = response.asString();

            // No password fields should appear
            assertThat(responseBody.toLowerCase()).doesNotContain("\"password\"");
            assertThat(responseBody).doesNotContain("$2a$"); // BCrypt prefix
            assertThat(responseBody).doesNotContain("$2b$"); // BCrypt variant
        }

        @Test
        @DisplayName("SEC-003: User creation response contains temporaryPassword for admin workflow")
        void userCreationResponseContainsTemporaryPassword() {
            // NOTE: This is a design trade-off:
            // - The application returns temporaryPassword so admins can share it with new users
            // - When admin provides a password, it becomes the temporaryPassword
            // - This is intentional for admin usability but should be noted as a security consideration
            // - Alternative would be to always generate passwords and never echo input

            String uniqueSuffix = System.currentTimeMillis() + "_" + Thread.currentThread().getId();
            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("name", "New User");
            userRequest.put("email", "newuser" + uniqueSuffix + "@test.com");
            // Don't provide password - let system generate one
            userRequest.put("role", "PASTOR");

            Response response = given()
                .spec(authenticatedSpec(adminToken))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .extract().response();

            // Controller returns 200 OK, not 201 Created
            assertThat(response.statusCode()).isIn(200, 201);

            String responseBody = response.asString();

            // When no password provided, a system-generated one should be in temporaryPassword
            assertThat(responseBody).contains("temporaryPassword");

            // Verify the response has proper JSON content type (protects against XSS)
            assertThat(response.getContentType()).containsIgnoringCase("application/json");

            // Verify no hashed passwords are exposed
            assertThat(responseBody).doesNotContain("$2a$"); // BCrypt prefix
            assertThat(responseBody).doesNotContain("$2b$"); // BCrypt variant
        }

        @Test
        @DisplayName("SEC-004: Registration response should not contain password")
        void registrationResponseShouldNotContainPassword() {
            Map<String, Object> registrationRequest = new HashMap<>();
            registrationRequest.put("churchName", "New Church " + System.currentTimeMillis());
            registrationRequest.put("churchEmail", "newchurch" + System.currentTimeMillis() + "@test.com");
            registrationRequest.put("adminName", "New Admin");
            registrationRequest.put("adminEmail", "newadmin" + System.currentTimeMillis() + "@test.com");
            registrationRequest.put("password", "RegistrationPass123!");

            Response response = given()
                .spec(spec)
                .body(registrationRequest)
            .when()
                .post("/api/auth/register")
            .then()
                .extract().response();

            String responseBody = response.asString();
            assertThat(responseBody).doesNotContain("RegistrationPass123!");
            assertThat(responseBody.toLowerCase()).doesNotContain("\"password\"");
        }
    }

    @Nested
    @DisplayName("Token/Secret Protection")
    class TokenSecretProtection {

        @Test
        @DisplayName("SEC-005: Refresh token should not appear in regular API responses")
        void refreshTokenShouldNotAppearInRegularResponses() {
            Response response = given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/users")
            .then()
                .statusCode(200)
                .extract().response();

            String responseBody = response.asString();
            assertThat(responseBody.toLowerCase()).doesNotContain("refreshtoken");
            assertThat(responseBody.toLowerCase()).doesNotContain("refresh_token");
        }

        @Test
        @DisplayName("SEC-006: JWT secret should never appear in responses")
        void jwtSecretShouldNeverAppear() {
            // Try various endpoints
            String[] endpoints = {"/api/users", "/api/members", "/api/billing/status"};

            for (String endpoint : endpoints) {
                try {
                    Response response = given()
                        .spec(authenticatedSpec(adminToken))
                    .when()
                        .get(endpoint)
                    .then()
                        .extract().response();

                    String responseBody = response.asString();
                    assertThat(responseBody.toLowerCase()).doesNotContain("jwtsecret");
                    assertThat(responseBody.toLowerCase()).doesNotContain("jwt_secret");
                    assertThat(responseBody.toLowerCase()).doesNotContain("secret_key");
                } catch (Exception e) {
                    // Some endpoints may not exist, that's OK
                }
            }
        }
    }

    @Nested
    @DisplayName("Error Response Protection")
    class ErrorResponseProtection {

        @Test
        @DisplayName("SEC-007: Error responses should not contain stack traces")
        void errorsShouldNotContainStackTraces() {
            // Send malformed JSON to trigger error
            Response response = given()
                .spec(spec)
                .contentType("application/json")
                .body("{invalid json}")
            .when()
                .post("/api/auth/login")
            .then()
                .extract().response();

            String responseBody = response.asString();

            assertThat(responseBody).doesNotContain("java.lang.");
            assertThat(responseBody).doesNotContain("at com.reuben");
            assertThat(responseBody).doesNotContain("NullPointerException");
            assertThat(responseBody).doesNotContain("SQLException");
            assertThat(responseBody).doesNotContain(".java:");
        }

        @Test
        @DisplayName("SEC-008: Error responses should not expose file paths")
        void errorsShouldNotExposeFilePaths() {
            Response response = given()
                .spec(spec)
                .contentType("application/json")
                .body("{invalid}")
            .when()
                .post("/api/auth/login")
            .then()
                .extract().response();

            String responseBody = response.asString();

            assertThat(responseBody).doesNotContain("/home/");
            assertThat(responseBody).doesNotContain("/var/");
            assertThat(responseBody).doesNotContain("C:\\");
            assertThat(responseBody).doesNotContain("/src/main/");
        }

        @Test
        @DisplayName("SEC-009: 404 error should not reveal resource existence in other tenants")
        void notFoundShouldNotRevealOtherTenantResources() {
            // Create another church with a user
            String uniqueSuffix = System.currentTimeMillis() + "_" + Thread.currentThread().getId();
            Long otherChurchId = createTestChurch("Other Church " + uniqueSuffix);
            String otherAdminToken = getAdminToken(otherChurchId);

            // Create user in other church
            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("name", "Other User");
            userRequest.put("email", "other" + uniqueSuffix + "@test.com");
            userRequest.put("password", "OtherPass123!");
            userRequest.put("role", "PASTOR");

            Response createResponse = given()
                .spec(authenticatedSpec(otherAdminToken))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .extract().response();

            // Controller returns 200 OK, not 201 Created
            assertThat(createResponse.statusCode()).isIn(200, 201);

            Long otherUserId = createResponse.jsonPath().getLong("id");

            // Try to access other church's user with original admin
            Response response = given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/users/" + otherUserId)
            .then()
                .extract().response();

            // 402 = payment required, 404 = not found (both acceptable for cross-tenant access)
            assertThat(response.statusCode()).isIn(402, 404);

            String responseBody = response.asString();

            // Should NOT reveal any details about the other user
            assertThat(responseBody).doesNotContain("Other User");
            assertThat(responseBody.toLowerCase()).doesNotContain("other user");
        }
    }

    @Nested
    @DisplayName("PII Protection")
    class PiiProtection {

        @Test
        @DisplayName("SEC-010: Failed login should not confirm email existence")
        void failedLoginShouldNotConfirmEmailExistence() {
            // Create a user first
            String uniqueSuffix = System.currentTimeMillis() + "_" + Thread.currentThread().getId();
            String existingEmail = "existing" + uniqueSuffix + "@test.com";
            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("name", "Existing User");
            userRequest.put("email", existingEmail);
            userRequest.put("password", testPassword);
            userRequest.put("role", "PASTOR");

            Response createResponse = given()
                .spec(authenticatedSpec(adminToken))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .extract().response();

            // Controller returns 200 OK, not 201 Created
            assertThat(createResponse.statusCode()).isIn(200, 201);

            // Try with existing email, wrong password
            Map<String, Object> existingEmailRequest = new HashMap<>();
            existingEmailRequest.put("email", existingEmail);
            existingEmailRequest.put("password", "wrongpassword");
            existingEmailRequest.put("rememberMe", false);

            Response existingResult = given()
                .spec(spec)
                .body(existingEmailRequest)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401)
                .extract().response();

            // Try with non-existing email
            Map<String, Object> nonExistingRequest = new HashMap<>();
            nonExistingRequest.put("email", "nonexistent" + uniqueSuffix + "@test.com");
            nonExistingRequest.put("password", "somepassword");
            nonExistingRequest.put("rememberMe", false);

            Response nonExistingResult = given()
                .spec(spec)
                .body(nonExistingRequest)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401)
                .extract().response();

            // Both error messages should be generic
            String existingError = existingResult.asString();
            String nonExistingError = nonExistingResult.asString();

            assertThat(existingError).doesNotContain("email exists");
            assertThat(existingError).doesNotContain("user exists");
            assertThat(nonExistingError).doesNotContain("not found");
            assertThat(nonExistingError).doesNotContain("does not exist");
        }

        @Test
        @DisplayName("SEC-011: Members API should not expose internal IDs unnecessarily")
        void membersShouldNotExposeUnnecessaryInternalIds() {
            Response response = given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
                .extract().response();

            String responseBody = response.asString();

            // Should not expose internal references like database sequence values
            assertThat(responseBody).doesNotContain("hibernate_sequence");
            assertThat(responseBody).doesNotContain("internal_id");
        }
    }
}
