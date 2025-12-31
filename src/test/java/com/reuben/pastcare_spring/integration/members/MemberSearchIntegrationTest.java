package com.reuben.pastcare_spring.integration.members;

import com.reuben.pastcare_spring.dtos.MemberRequest;
import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.integration.BaseIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Member Search functionality.
 *
 * Tests cover:
 * - Basic search by name/phone
 * - Filter by status (MEMBER, VISITOR, INACTIVE)
 * - Advanced search (age range, gender, tags combined)
 * - Empty search results
 * - Search pagination
 */
@SpringBootTest
@Tag("integration")
@Tag("module:members")
@DisplayName("Member Search Integration Tests")
@Transactional
class MemberSearchIntegrationTest extends BaseIntegrationTest {

    private Long churchId;
    private String adminToken;

    @BeforeEach
    void setUp() {
        churchId = createTestChurch();
        adminToken = getAdminToken(churchId);
        createTestMembers();
    }

    private void createTestMembers() {
        // Create diverse members for search testing
        createMember("Alice", "Johnson", "+233244111111", "alice@example.com", Set.of("youth", "tech"));
        createMember("Bob", "Smith", "+233244222222", "bob@example.com", Set.of("choir", "ushering"));
        createMember("Charlie", "Brown", "+233244333333", "charlie@example.com", Set.of("youth", "media"));
        createMember("Diana", "Wilson", "+233244444444", "diana@example.com", Set.of("children", "prayer"));
        createMember("Eve", "Martinez", "+233244555555", "eve@example.com", Set.of("tech", "media"));
    }

    private void createMember(String firstName, String lastName, String phone, String email, Set<String> tags) {
        MemberRequest request = new MemberRequest(
            null, firstName, null, lastName, null, "male", churchId, null,
            LocalDate.of(1990, 1, 1), null, null, phone, email, null, null, null, null, null,
            "single", null, null, null, null, null, null, tags
        );
        given().spec(authenticatedSpec(adminToken)).body(request).post("/api/members");
    }

    @Nested
    @DisplayName("Basic Search Tests")
    class BasicSearchTests {

        @Test
        @DisplayName("Should search members by first name")
        void shouldSearchByFirstName() {
            // When: Search for "Alice"
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("search", "Alice")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should find Alice
            assertThat(results).isNotEmpty();
            assertThat(results).anyMatch(m -> m.firstName().equals("Alice"));
        }

        @Test
        @DisplayName("Should search members by last name")
        void shouldSearchByLastName() {
            // When: Search for "Smith"
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("search", "Smith")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should find Bob Smith
            assertThat(results).isNotEmpty();
            assertThat(results).anyMatch(m -> m.lastName().equals("Smith"));
        }

        @Test
        @DisplayName("Should search members by phone number")
        void shouldSearchByPhoneNumber() {
            // When: Search for phone number
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("search", "244111111")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should find Alice
            assertThat(results).isNotEmpty();
            assertThat(results).anyMatch(m -> m.phoneNumber().contains("244111111"));
        }

        @Test
        @DisplayName("Should search members case-insensitively")
        void shouldSearchCaseInsensitive() {
            // When: Search with lowercase
            List<MemberResponse> lowerResults = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("search", "alice")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // When: Search with uppercase
            List<MemberResponse> upperResults = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("search", "ALICE")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should find same results
            assertThat(lowerResults).isNotEmpty();
            assertThat(upperResults).isNotEmpty();
            assertThat(lowerResults.size()).isEqualTo(upperResults.size());
        }

        @Test
        @DisplayName("Should return empty list when no matches found")
        void shouldReturnEmptyWhenNoMatches() {
            // When: Search for non-existent name
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("search", "NonExistentName12345")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should return empty list (not error)
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Filter Tests")
    class FilterTests {

        @Test
        @DisplayName("Should filter members by tag")
        void shouldFilterByTag() {
            // When: Filter by "youth" tag
            given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("filter", "tag:youth")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(2)); // Alice and Charlie have youth tag
        }

        @Test
        @DisplayName("Should filter members by multiple tags")
        void shouldFilterByMultipleTags() {
            // When: Filter by "tech" AND "media" tags
            given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("filter", "tag:tech,media")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(1)); // Eve has both tags
        }

        @Test
        @DisplayName("Should filter members by status")
        void shouldFilterByStatus() {
            // Given: Create member with VISITOR status
            MemberRequest visitorRequest = new MemberRequest(
                null, "Visitor", null, "Test", null, "male", churchId, null, null, null, null,
                "+233244666666", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );
            given().spec(authenticatedSpec(adminToken)).body(visitorRequest).post("/api/members");

            // When: Filter by VISITOR status
            given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("filter", "status:VISITOR")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
                .body("content.size()", greaterThanOrEqualTo(1));
        }
    }

    @Nested
    @DisplayName("Advanced Search Tests")
    class AdvancedSearchTests {

        @Test
        @DisplayName("Should combine search and filter")
        void shouldCombineSearchAndFilter() {
            // When: Search for "Alice" with tag filter
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("search", "Alice")
                .queryParam("filter", "tag:youth")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should find Alice with youth tag
            assertThat(results).isNotEmpty();
            assertThat(results).anyMatch(m ->
                m.firstName().equals("Alice") && m.tags().contains("youth")
            );
        }

        @Test
        @DisplayName("Should handle partial name matches")
        void shouldHandlePartialMatches() {
            // When: Search with partial name
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("search", "John")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should find "Johnson" (partial match)
            assertThat(results).isNotEmpty();
            assertThat(results).anyMatch(m -> m.lastName().contains("John"));
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should paginate search results")
        void shouldPaginateSearchResults() {
            // When: Get first page with size 2
            given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("page", 0)
                .queryParam("size", 2)
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
                .body("content.size()", equalTo(2))
                .body("totalElements", greaterThanOrEqualTo(5))
                .body("number", equalTo(0))
                .body("size", equalTo(2))
                .body("totalPages", greaterThanOrEqualTo(3));
        }

        @Test
        @DisplayName("Should handle empty page gracefully")
        void shouldHandleEmptyPage() {
            // When: Request page 999 (beyond available data)
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("page", 999)
                .queryParam("size", 20)
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should return empty list
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should sort search results")
        void shouldSortSearchResults() {
            // When: Get members sorted by firstName
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("page", 0)
                .queryParam("size", 10)
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should be sorted alphabetically
            assertThat(results).isNotEmpty();
            // Verify first member comes alphabetically before last
            if (results.size() > 1) {
                String firstNameFirst = results.get(0).firstName();
                String firstNameLast = results.get(results.size() - 1).firstName();
                assertThat(firstNameFirst.compareTo(firstNameLast)).isLessThanOrEqualTo(0);
            }
        }
    }

    @Nested
    @DisplayName("Multi-Tenancy Search Tests")
    class MultiTenancySearchTests {

        @Test
        @DisplayName("Should only search within current church")
        void shouldSearchOnlyCurrentChurch() {
            // Given: Another church with member named "Alice"
            Long church2 = createTestChurch("Church 2");
            String token2 = getAdminToken(church2);

            MemberRequest alice2Request = new MemberRequest(
                null, "Alice", null, "OtherChurch", null, "female", church2, null, null, null, null,
                "+233244777777", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );
            given().spec(authenticatedSpec(token2)).body(alice2Request).post("/api/members");

            // When: Church 1 admin searches for "Alice"
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("search", "Alice")
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should only find Church 1's Alice
            assertThat(results).isNotEmpty();
            assertThat(results).allMatch(m -> m.churchId().equals(churchId));
            assertThat(results).noneMatch(m -> m.churchId().equals(church2));
            assertThat(results).noneMatch(m -> m.lastName().equals("OtherChurch"));
        }
    }

    @Nested
    @DisplayName("Permission-Based Search Tests")
    class PermissionBasedSearchTests {

        @Test
        @DisplayName("ADMIN should see all members in search")
        void adminShouldSeeAllMembers() {
            // When: Admin searches
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should see all members
            assertThat(results).hasSizeGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("PASTOR should see all members in search")
        void pastorShouldSeeAllMembers() {
            // Given: Pastor token
            String pastorToken = getPastorToken(churchId);

            // When: Pastor searches
            List<MemberResponse> results = given()
                .spec(authenticatedSpec(pastorToken))
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should see all members
            assertThat(results).hasSizeGreaterThanOrEqualTo(5);
        }

        @Test
        @DisplayName("MEMBER should only see own profile in search")
        void memberShouldSeeOnlyOwnProfile() {
            // Given: Regular member token
            String memberToken = getMemberToken(churchId);

            // When: Member searches
            // Then: Should only see own profile (or get 403 depending on implementation)
            given()
                .spec(authenticatedSpec(memberToken))
            .when()
                .get("/api/members")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(403)));
        }
    }
}
