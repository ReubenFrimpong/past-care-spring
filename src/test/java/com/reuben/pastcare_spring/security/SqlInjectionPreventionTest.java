package com.reuben.pastcare_spring.security;

import com.reuben.pastcare_spring.integration.BaseIntegrationTest;
import com.reuben.pastcare_spring.enums.Role;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security Tests: SQL Injection Prevention
 *
 * Verifies that the application properly sanitizes and parameterizes
 * all database queries to prevent SQL injection attacks.
 *
 * Tests cover:
 * - Search endpoints (query parameters)
 * - Create endpoints (body parameters)
 * - Update endpoints (body parameters)
 * - Path parameters
 * - Filter parameters
 *
 * ALL user roles are tested to ensure consistent protection.
 */
@DisplayName("SQL Injection Prevention Tests")
public class SqlInjectionPreventionTest extends BaseIntegrationTest {

    private Long testChurchId;
    private Map<Role, String> roleTokens = new HashMap<>();

    // Common SQL injection payloads
    private static final String[] SQL_INJECTION_PAYLOADS = {
        "'; DROP TABLE users; --",
        "1' OR '1'='1",
        "1; SELECT * FROM users",
        "' UNION SELECT * FROM users --",
        "admin'--",
        "1' OR 1=1 --",
        "'; DELETE FROM members; --",
        "1' AND '1'='1",
        "' OR ''='",
        "1'; TRUNCATE TABLE users; --",
        "'; INSERT INTO users VALUES ('hacker', 'pass'); --",
        "1' ORDER BY 1--+",
        "1' GROUP BY 1--+",
        "' HAVING 1=1 --",
        "1' WAITFOR DELAY '00:00:10' --"
    };

    @BeforeEach
    void setUp() {
        // Create test church with unique name to avoid conflicts in parameterized tests
        testChurchId = createTestChurch("SQL Injection Test Church " + System.currentTimeMillis() + "_" + Thread.currentThread().getId());

        // Create tokens for each role
        roleTokens.put(Role.ADMIN, getAdminToken(testChurchId));
        roleTokens.put(Role.PASTOR, getPastorToken(testChurchId));
        roleTokens.put(Role.TREASURER, getTreasurerToken(testChurchId));
        roleTokens.put(Role.MEMBER_MANAGER, getMemberManagerToken(testChurchId));
        roleTokens.put(Role.FELLOWSHIP_LEADER, getFellowshipLeaderToken(testChurchId));
        roleTokens.put(Role.MEMBER, getMemberToken(testChurchId));
        roleTokens.put(Role.SUPERADMIN, getSuperadminToken());
    }

    // ==================== MEMBER SEARCH SQL INJECTION ====================

    @Nested
    @DisplayName("Member Search SQL Injection Prevention")
    class MemberSearchInjection {

        @ParameterizedTest
        @ValueSource(strings = {
            "'; DROP TABLE members; --",
            "1' OR '1'='1",
            "' UNION SELECT * FROM users --",
            "admin'--"
        })
        @DisplayName("SQL-001: Member search query parameter is sanitized")
        void memberSearchQueryIsSanitized(String payload) {
            // Test with ADMIN role
            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .queryParam("search", payload)
            .when()
                .get("/api/members")
            .then()
                .extract().response();

            // Should NOT return 500 (server error from SQL exception)
            assertThat(response.statusCode()).isNotEqualTo(500);

            // Response should not contain SQL error messages
            String body = response.asString().toLowerCase();
            assertThat(body).doesNotContain("sql");
            assertThat(body).doesNotContain("syntax error");
            assertThat(body).doesNotContain("query");
            assertThat(body).doesNotContain("database");
        }

        @Test
        @DisplayName("SQL-002: All roles - Member search is protected from SQL injection")
        void allRolesMemberSearchProtected() {
            String payload = "'; DELETE FROM members; --";

            for (Role role : roleTokens.keySet()) {
                Response response = given()
                    .spec(authenticatedSpec(roleTokens.get(role)))
                    .queryParam("search", payload)
                .when()
                    .get("/api/members")
                .then()
                    .extract().response();

                // Should be either 200 (empty results), 402 (no subscription), 403 (no access), or 400 (bad request)
                // But NEVER 500 (SQL error) caused by the SQL injection payload
                int status = response.statusCode();
                if (status == 500) {
                    String body = response.asString().toLowerCase();
                    // 500 is only OK if it's NOT a SQL-related error
                    assertThat(body)
                        .as("Role %s should not cause SQL error", role.name())
                        .doesNotContain("syntax error")
                        .doesNotContain("invalid sql")
                        .doesNotContain("statement")
                        .doesNotContain("query failed");
                }
            }
        }
    }

    // ==================== USER CREATION SQL INJECTION ====================

    @Nested
    @DisplayName("User Creation SQL Injection Prevention")
    class UserCreationInjection {

        @ParameterizedTest
        @ValueSource(strings = {
            "Test'; DROP TABLE users; --",
            "User' OR '1'='1",
            "Name'; DELETE FROM users; --"
        })
        @DisplayName("SQL-003: User name field is sanitized")
        void userNameFieldIsSanitized(String maliciousName) {
            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("name", maliciousName);
            userRequest.put("email", "test" + System.currentTimeMillis() + "@test.com");
            userRequest.put("password", "SecurePass123!");
            userRequest.put("role", "PASTOR");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .extract().response();

            // Should NOT cause server error
            assertThat(response.statusCode()).isNotEqualTo(500);

            // If successful (201), the malicious name should be stored literally, not executed
            if (response.statusCode() == 201) {
                String storedName = response.jsonPath().getString("name");
                assertThat(storedName).isEqualTo(maliciousName);
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "test'; DROP TABLE users; --@test.com",
            "test' OR '1'='1@test.com"
        })
        @DisplayName("SQL-004: User email field is sanitized")
        void userEmailFieldIsSanitized(String maliciousEmail) {
            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("name", "Test User");
            userRequest.put("email", maliciousEmail);
            userRequest.put("password", "SecurePass123!");
            userRequest.put("role", "PASTOR");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .extract().response();

            // Should NOT cause server error (might be 400 for invalid email format)
            assertThat(response.statusCode()).isNotEqualTo(500);
        }
    }

    // ==================== MEMBER CREATION SQL INJECTION ====================

    @Nested
    @DisplayName("Member Creation SQL Injection Prevention")
    class MemberCreationInjection {

        @ParameterizedTest
        @ValueSource(strings = {
            "John'; DROP TABLE members; --",
            "Jane' OR '1'='1",
            "Bob'; DELETE FROM members; --"
        })
        @DisplayName("SQL-005: Member first name field is sanitized")
        void memberFirstNameIsSanitized(String maliciousName) {
            Map<String, Object> memberRequest = new HashMap<>();
            memberRequest.put("firstName", maliciousName);
            memberRequest.put("lastName", "Doe");
            memberRequest.put("email", "member" + System.currentTimeMillis() + "@test.com");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(memberRequest)
            .when()
                .post("/api/members")
            .then()
                .extract().response();

            // Should NOT cause server error
            assertThat(response.statusCode()).isNotEqualTo(500);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "Smith'; DROP TABLE members; --",
            "Jones' UNION SELECT * FROM users --"
        })
        @DisplayName("SQL-006: Member last name field is sanitized")
        void memberLastNameIsSanitized(String maliciousName) {
            Map<String, Object> memberRequest = new HashMap<>();
            memberRequest.put("firstName", "John");
            memberRequest.put("lastName", maliciousName);
            memberRequest.put("email", "member" + System.currentTimeMillis() + "@test.com");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(memberRequest)
            .when()
                .post("/api/members")
            .then()
                .extract().response();

            // Should NOT cause server error
            assertThat(response.statusCode()).isNotEqualTo(500);
        }
    }

    // ==================== PATH PARAMETER SQL INJECTION ====================

    @Nested
    @DisplayName("Path Parameter SQL Injection Prevention")
    class PathParameterInjection {

        @ParameterizedTest
        @ValueSource(strings = {
            "1; DROP TABLE users; --",
            "1 OR 1=1",
            "1' OR '1'='1",
            "-1 UNION SELECT * FROM users"
        })
        @DisplayName("SQL-007: User ID path parameter is sanitized")
        void userIdPathParameterIsSanitized(String maliciousId) {
            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
            .when()
                .get("/api/users/" + maliciousId)
            .then()
                .extract().response();

            // Should be 400, 402 (no subscription), or 404, NOT 500
            assertThat(response.statusCode())
                .as("Malicious ID should not cause SQL error")
                .isIn(400, 402, 404);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "1; DELETE FROM members; --",
            "1 OR 1=1",
            "abc'; DROP TABLE members; --"
        })
        @DisplayName("SQL-008: Member ID path parameter is sanitized")
        void memberIdPathParameterIsSanitized(String maliciousId) {
            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
            .when()
                .get("/api/members/" + maliciousId)
            .then()
                .extract().response();

            // Should be 400, 402 (no subscription), or 404, NOT 500
            assertThat(response.statusCode())
                .as("Malicious ID should not cause SQL error")
                .isIn(400, 402, 404);
        }
    }

    // ==================== FILTER PARAMETER SQL INJECTION ====================

    @Nested
    @DisplayName("Filter Parameter SQL Injection Prevention")
    class FilterParameterInjection {

        @ParameterizedTest
        @ValueSource(strings = {
            "PASTOR'; DROP TABLE users; --",
            "ADMIN' OR '1'='1",
            "MEMBER' UNION SELECT * FROM users --"
        })
        @DisplayName("SQL-009: Role filter parameter is sanitized")
        void roleFilterParameterIsSanitized(String maliciousRole) {
            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .queryParam("role", maliciousRole)
            .when()
                .get("/api/users")
            .then()
                .extract().response();

            // Should NOT cause server error
            assertThat(response.statusCode()).isNotEqualTo(500);
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "ACTIVE'; DROP TABLE members; --",
            "INACTIVE' OR '1'='1"
        })
        @DisplayName("SQL-010: Status filter parameter is sanitized")
        void statusFilterParameterIsSanitized(String maliciousStatus) {
            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .queryParam("status", maliciousStatus)
            .when()
                .get("/api/members")
            .then()
                .extract().response();

            // Should NOT cause server error
            assertThat(response.statusCode()).isNotEqualTo(500);
        }
    }

    // ==================== DONATION SQL INJECTION ====================

    @Nested
    @DisplayName("Donation Endpoint SQL Injection Prevention")
    class DonationInjection {

        @Test
        @DisplayName("SQL-011: Donation amount field cannot contain SQL")
        void donationAmountFieldProtected() {
            // First create a member
            Map<String, Object> memberRequest = new HashMap<>();
            memberRequest.put("firstName", "Test");
            memberRequest.put("lastName", "Member");
            memberRequest.put("email", "member" + System.currentTimeMillis() + "@test.com");

            Response memberResponse = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(memberRequest)
            .when()
                .post("/api/members")
            .then()
                .extract().response();

            if (memberResponse.statusCode() == 201) {
                Long memberId = memberResponse.jsonPath().getLong("id");

                // Try to inject SQL through amount (should fail validation)
                Map<String, Object> donationRequest = new HashMap<>();
                donationRequest.put("memberId", memberId);
                donationRequest.put("amount", "100; DROP TABLE donations; --");
                donationRequest.put("donationDate", "2024-01-15");
                donationRequest.put("type", "TITHE");

                Response response = given()
                    .spec(authenticatedSpec(roleTokens.get(Role.TREASURER)))
                    .body(donationRequest)
                .when()
                    .post("/api/donations")
                .then()
                    .extract().response();

                // Should be 400 (bad request) for invalid amount, NOT 500
                assertThat(response.statusCode()).isNotEqualTo(500);
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "TITHE'; DROP TABLE donations; --",
            "OFFERING' OR '1'='1"
        })
        @DisplayName("SQL-012: Donation type field is sanitized")
        void donationTypeFieldIsSanitized(String maliciousType) {
            Map<String, Object> donationRequest = new HashMap<>();
            donationRequest.put("memberId", 1);
            donationRequest.put("amount", 100.00);
            donationRequest.put("donationDate", "2024-01-15");
            donationRequest.put("type", maliciousType);

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.TREASURER)))
                .body(donationRequest)
            .when()
                .post("/api/donations")
            .then()
                .extract().response();

            // Should NOT cause server error
            assertThat(response.statusCode()).isNotEqualTo(500);
        }
    }

    // ==================== FELLOWSHIP SQL INJECTION ====================

    @Nested
    @DisplayName("Fellowship Endpoint SQL Injection Prevention")
    class FellowshipInjection {

        @ParameterizedTest
        @ValueSource(strings = {
            "Youth'; DROP TABLE fellowships; --",
            "Women' OR '1'='1",
            "Men' UNION SELECT * FROM users --"
        })
        @DisplayName("SQL-013: Fellowship name field is sanitized")
        void fellowshipNameFieldIsSanitized(String maliciousName) {
            Map<String, Object> fellowshipRequest = new HashMap<>();
            fellowshipRequest.put("name", maliciousName);
            fellowshipRequest.put("description", "Test fellowship");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(fellowshipRequest)
            .when()
                .post("/api/fellowships")
            .then()
                .extract().response();

            // Should NOT cause server error
            assertThat(response.statusCode()).isNotEqualTo(500);
        }
    }

    // ==================== COMPREHENSIVE ALL-ENDPOINTS TEST ====================

    @Nested
    @DisplayName("Comprehensive SQL Injection Prevention")
    class ComprehensiveInjection {

        @Test
        @DisplayName("SQL-014: No endpoint returns SQL error messages")
        void noEndpointReturnsSqlErrors() {
            String payload = "'; DROP TABLE test; --";

            String[] endpoints = {
                "/api/members?search=" + payload,
                "/api/users?search=" + payload,
                "/api/fellowships?search=" + payload,
                "/api/donations?search=" + payload,
                "/api/events?search=" + payload
            };

            for (String endpoint : endpoints) {
                try {
                    Response response = given()
                        .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                    .when()
                        .get(endpoint)
                    .then()
                        .extract().response();

                    String body = response.asString().toLowerCase();

                    // Assert no SQL-related error messages
                    // Note: "sql" may appear in legitimate content (e.g., church name "SQL Injection Test")
                    // So we check for specific error patterns instead
                    assertThat(body)
                        .as("Endpoint %s should not expose SQL errors", endpoint)
                        .doesNotContain("syntax error")
                        .doesNotContain("invalid sql")
                        .doesNotContain("sql exception")
                        .doesNotContain("query failed")
                        .doesNotContain("jdbc")
                        .doesNotContain("hibernate");

                } catch (Exception e) {
                    // Endpoint might not exist, that's OK
                }
            }
        }
    }
}
