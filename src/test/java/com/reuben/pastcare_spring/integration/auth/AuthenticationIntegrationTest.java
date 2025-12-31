package com.reuben.pastcare_spring.integration.auth;

import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.integration.BaseIntegrationTest;
import com.reuben.pastcare_spring.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Authentication functionality.
 *
 * Tests cover:
 * - User registration
 * - Login (success and failure scenarios)
 * - Token refresh
 * - Password reset
 * - Account lockout after failed attempts
 * - Expired token handling
 */
@SpringBootTest
@DisplayName("Authentication Integration Tests")
class AuthenticationIntegrationTest extends BaseIntegrationTest {

    private Long churchId;

    @BeforeEach
    void setUp() {
        churchId = createTestChurch();
    }

    @Nested
    @DisplayName("User Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register new church and admin user successfully")
        void shouldRegisterNewChurch() {
            // Given: Registration request
            Map<String, Object> registrationRequest = new HashMap<>();
            registrationRequest.put("churchName", "New Test Church");
            registrationRequest.put("adminName", "Admin User");
            registrationRequest.put("adminEmail", "admin@newtestchurch.com");
            registrationRequest.put("password", "StrongPassword@123");
            registrationRequest.put("phoneNumber", "+254700111222");

            // When: Submit registration
            given()
                .spec(spec)
                .body(registrationRequest)
            .when()
                .post("/api/auth/register")
            .then()
                .statusCode(anyOf(is(201), is(200)))
                .body("churchId", notNullValue())
                .body("userId", notNullValue())
                .body("email", equalTo("admin@newtestchurch.com"))
                .body("role", equalTo("ADMIN"));
        }

        @Test
        @DisplayName("Should reject registration with duplicate email")
        void shouldRejectDuplicateEmail() {
            // Given: Existing user
            createAdminUser(churchId);

            // When: Try to register with same email
            Map<String, Object> registrationRequest = new HashMap<>();
            registrationRequest.put("churchName", "Another Church");
            registrationRequest.put("adminName", "Another Admin");
            registrationRequest.put("adminEmail", "admin@testchurch.com");
            registrationRequest.put("password", "Password@123");
            registrationRequest.put("phoneNumber", "+254700222333");

            // Then: Should fail with 409 Conflict
            given()
                .spec(spec)
                .body(registrationRequest)
            .when()
                .post("/api/auth/register")
            .then()
                .statusCode(409)
                .body("message", containsStringIgnoringCase("email already exists"));
        }

        @Test
        @DisplayName("Should validate password strength requirements")
        void shouldValidatePasswordStrength() {
            // Given: Weak password
            Map<String, Object> registrationRequest = new HashMap<>();
            registrationRequest.put("churchName", "Test Church");
            registrationRequest.put("adminName", "Admin");
            registrationRequest.put("adminEmail", "admin@weak.com");
            registrationRequest.put("password", "weak"); // Too weak
            registrationRequest.put("phoneNumber", "+254700333444");

            // Then: Should fail with validation error
            given()
                .spec(spec)
                .body(registrationRequest)
            .when()
                .post("/api/auth/register")
            .then()
                .statusCode(400)
                .body("message", containsStringIgnoringCase("password"));
        }
    }

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            // Given: Existing user
            User user = createAdminUser(churchId);

            // When: Login with correct credentials
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", user.getEmail());
            loginRequest.put("password", "Password@123");

            // Then: Should return access and refresh tokens
            given()
                .spec(spec)
                .body(loginRequest)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("user.email", equalTo(user.getEmail()))
                .body("user.role", equalTo("ADMIN"))
                .body("user.churchId", equalTo(churchId.intValue()));
        }

        @Test
        @DisplayName("Should reject login with wrong password")
        void shouldRejectWrongPassword() {
            // Given: Existing user
            User user = createAdminUser(churchId);

            // When: Login with wrong password
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", user.getEmail());
            loginRequest.put("password", "WrongPassword@123");

            // Then: Should fail with 401
            given()
                .spec(spec)
                .body(loginRequest)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401)
                .body("message", containsStringIgnoringCase("invalid credentials"));
        }

        @Test
        @DisplayName("Should reject login for non-existent user")
        void shouldRejectNonExistentUser() {
            // When: Login with non-existent email
            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", "nonexistent@example.com");
            loginRequest.put("password", "Password@123");

            // Then: Should fail with 401
            given()
                .spec(spec)
                .body(loginRequest)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(401)
                .body("message", containsStringIgnoringCase("invalid credentials"));
        }

        @Test
        @DisplayName("Should lock account after 5 failed login attempts")
        void shouldLockAccountAfterFailedAttempts() {
            // Given: Existing user
            User user = createAdminUser(churchId);

            Map<String, String> loginRequest = new HashMap<>();
            loginRequest.put("email", user.getEmail());
            loginRequest.put("password", "WrongPassword@123");

            // When: Attempt to login 5 times with wrong password
            for (int i = 0; i < 5; i++) {
                given()
                    .spec(spec)
                    .body(loginRequest)
                .when()
                    .post("/api/auth/login")
                .then()
                    .statusCode(401);
            }

            // Then: 6th attempt should indicate account is locked
            given()
                .spec(spec)
                .body(loginRequest)
            .when()
                .post("/api/auth/login")
            .then()
                .statusCode(423) // Locked
                .body("message", containsStringIgnoringCase("account locked"));
        }
    }

    @Nested
    @DisplayName("Token Refresh Tests")
    class TokenRefreshTests {

        @Test
        @DisplayName("Should refresh access token with valid refresh token")
        void shouldRefreshToken() {
            // Given: User with refresh token
            User user = createAdminUser(churchId);
            String refreshToken = com.reuben.pastcare_spring.testutil.TestJwtUtil
                    .generateRefreshToken(user.getId(), user.getEmail(),
                            user.getChurch().getId(), user.getRole());

            // When: Request new access token
            Map<String, String> refreshRequest = new HashMap<>();
            refreshRequest.put("refreshToken", refreshToken);

            // Then: Should return new access token
            given()
                .spec(spec)
                .body(refreshRequest)
            .when()
                .post("/api/auth/refresh")
            .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue());
        }

        @Test
        @DisplayName("Should reject expired refresh token")
        void shouldRejectExpiredRefreshToken() {
            // Given: Expired refresh token
            User user = createAdminUser(churchId);
            String expiredToken = com.reuben.pastcare_spring.testutil.TestJwtUtil
                    .generateExpiredToken(user.getId(), user.getEmail(),
                            user.getChurch().getId(), user.getRole());

            // When: Try to refresh with expired token
            Map<String, String> refreshRequest = new HashMap<>();
            refreshRequest.put("refreshToken", expiredToken);

            // Then: Should fail
            given()
                .spec(spec)
                .body(refreshRequest)
            .when()
                .post("/api/auth/refresh")
            .then()
                .statusCode(401)
                .body("message", containsStringIgnoringCase("expired"));
        }
    }

    @Nested
    @DisplayName("Password Reset Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("Should initiate password reset for valid email")
        void shouldInitiatePasswordReset() {
            // Given: Existing user
            User user = createAdminUser(churchId);

            // When: Request password reset
            Map<String, String> resetRequest = new HashMap<>();
            resetRequest.put("email", user.getEmail());

            // Then: Should accept request
            given()
                .spec(spec)
                .body(resetRequest)
            .when()
                .post("/api/auth/forgot-password")
            .then()
                .statusCode(anyOf(is(200), is(202)))
                .body("message", containsStringIgnoringCase("reset"));
        }

        @Test
        @DisplayName("Should not reveal if email doesn't exist (security)")
        void shouldNotRevealNonExistentEmail() {
            // When: Request reset for non-existent email
            Map<String, String> resetRequest = new HashMap<>();
            resetRequest.put("email", "nonexistent@example.com");

            // Then: Should still return success (don't reveal user existence)
            given()
                .spec(spec)
                .body(resetRequest)
            .when()
                .post("/api/auth/forgot-password")
            .then()
                .statusCode(anyOf(is(200), is(202)));
        }
    }

    @Nested
    @DisplayName("Cross-Church Access Tests")
    class CrossChurchAccessTests {

        @Test
        @DisplayName("Should deny access to other church's users")
        void shouldDenyCrossChurchUserAccess() {
            // Given: Two churches with users
            Long church1 = createTestChurch("Church 1");
            Long church2 = createTestChurch("Church 2");

            User church2Admin = createAdminUser(church2);
            String church1Token = getAdminToken(church1);

            // When: Church 1 admin tries to access Church 2 user
            given()
                .spec(authenticatedSpec(church1Token))
            .when()
                .get("/api/users/" + church2Admin.getId())
            .then()
                .statusCode(anyOf(is(403), is(404))); // Forbidden or Not Found (multi-tenancy hiding)
        }
    }
}
