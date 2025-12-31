package com.reuben.pastcare_spring.integration.members;

import com.reuben.pastcare_spring.dtos.MemberRequest;
import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.integration.BaseIntegrationTest;
import com.reuben.pastcare_spring.models.MemberStatus;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Member CRUD operations.
 *
 * Tests cover:
 * - Create member (full profile, quick add)
 * - Read member (by ID, list with pagination)
 * - Update member (profile completeness recalculation)
 * - Delete member
 * - Multi-tenancy isolation
 * - Permission-based access control
 */
@SpringBootTest
@Tag("integration")
@Tag("module:members")
@DisplayName("Member CRUD Integration Tests")
@Transactional
class MemberCrudIntegrationTest extends BaseIntegrationTest {

    private Long churchId;
    private String adminToken;

    @BeforeEach
    void setUp() {
        churchId = createTestChurch();
        adminToken = getAdminToken(churchId);
    }

    @Nested
    @DisplayName("Create Member Tests")
    class CreateMemberTests {

        @Test
        @DisplayName("Should create member with full profile and calculate profile completeness")
        void shouldCreateMemberWithFullProfile() {
            // Given: Full member profile
            MemberRequest request = new MemberRequest(
                null,
                "John",
                "Kwame",
                "Doe",
                "Mr.",
                "male",
                churchId,
                null, // fellowshipIds
                LocalDate.of(1990, 5, 15),
                "GH",
                "Africa/Accra",
                "+233244123456",
                "john.doe@example.com",
                "+233244123456",
                "+233200987654",
                null, // coordinates
                null, // nominatimAddress
                null, // profileImageUrl
                "single",
                null, // spouseId
                "Software Engineer",
                YearMonth.of(2020, 1),
                "Jane Doe",
                "+233244999888",
                "Very active member",
                Set.of("youth", "tech-team")
            );

            // When: Create member
            MemberResponse response = given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
            .when()
                .post("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .as(MemberResponse.class);

            // Then: Verify member created with all fields
            assertThat(response.id()).isNotNull();
            assertThat(response.firstName()).isEqualTo("John");
            assertThat(response.otherName()).isEqualTo("Kwame");
            assertThat(response.lastName()).isEqualTo("Doe");
            assertThat(response.title()).isEqualTo("Mr.");
            assertThat(response.sex()).isEqualTo("male");
            assertThat(response.email()).isEqualTo("john.doe@example.com");
            assertThat(response.phoneNumber()).isEqualTo("+233244123456");
            assertThat(response.occupation()).isEqualTo("Software Engineer");
            assertThat(response.maritalStatus()).isEqualTo("single");
            assertThat(response.profileCompleteness()).isGreaterThan(70); // Full profile should have high completeness
            assertBelongsToChurch(response.churchId(), churchId);
        }

        @Test
        @DisplayName("Should create member with quick add (minimal fields)")
        void shouldCreateMemberWithQuickAdd() {
            // Given: Minimal member profile
            MemberRequest request = new MemberRequest(
                null,
                "Jane",
                null,
                "Smith",
                null,
                "female",
                churchId,
                null,
                null,
                null,
                null,
                "+233244567890",
                null,
                null,
                null,
                null,
                null,
                null,
                "single",
                null,
                null,
                null,
                null,
                null,
                null,
                null
            );

            // When: Create member with minimal fields
            MemberResponse response = given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
            .when()
                .post("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .as(MemberResponse.class);

            // Then: Verify member created
            assertThat(response.id()).isNotNull();
            assertThat(response.firstName()).isEqualTo("Jane");
            assertThat(response.lastName()).isEqualTo("Smith");
            assertThat(response.phoneNumber()).isEqualTo("+233244567890");
            assertThat(response.profileCompleteness()).isLessThan(50); // Minimal profile should have low completeness
            assertBelongsToChurch(response.churchId(), churchId);
        }

        @Test
        @DisplayName("Should reject member creation with invalid phone number format")
        void shouldRejectInvalidPhoneNumber() {
            // Given: Member with invalid phone number
            MemberRequest request = new MemberRequest(
                null, "John", null, "Doe", null, "male", churchId, null, null, null, null,
                "0244123456", // Invalid: missing country code
                null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            // When: Try to create member
            // Then: Should return 400 Bad Request
            given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
            .when()
                .post("/api/members")
            .then()
                .statusCode(400)
                .body("message", containsString("phone number"));
        }

        @Test
        @DisplayName("Should reject member creation with duplicate phone number")
        void shouldRejectDuplicatePhoneNumber() {
            // Given: Existing member with phone number
            String phone = "+233244111222";
            MemberRequest firstRequest = new MemberRequest(
                null, "First", null, "Member", null, "male", churchId, null, null, null, null,
                phone, null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            given()
                .spec(authenticatedSpec(adminToken))
                .body(firstRequest)
            .when()
                .post("/api/members")
            .then()
                .statusCode(200);

            // When: Try to create another member with same phone number
            MemberRequest duplicateRequest = new MemberRequest(
                null, "Second", null, "Member", null, "female", churchId, null, null, null, null,
                phone, null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            // Then: Should return 409 Conflict
            given()
                .spec(authenticatedSpec(adminToken))
                .body(duplicateRequest)
            .when()
                .post("/api/members")
            .then()
                .statusCode(409)
                .body("message", containsString("Phone number already taken"));
        }
    }

    @Nested
    @DisplayName("Get Member Tests")
    class GetMemberTests {

        @Test
        @DisplayName("Should get member by ID")
        void shouldGetMemberById() {
            // Given: Existing member
            MemberRequest request = new MemberRequest(
                null, "Test", null, "Member", null, "male", churchId, null, null, null, null,
                "+233244333444", "test@example.com", null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            MemberResponse created = given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
                .post("/api/members")
                .as(MemberResponse.class);

            // When: Get member by ID
            MemberResponse response = given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/members/" + created.id())
            .then()
                .statusCode(200)
            .extract()
                .as(MemberResponse.class);

            // Then: Verify member details
            assertThat(response.id()).isEqualTo(created.id());
            assertThat(response.firstName()).isEqualTo("Test");
            assertThat(response.email()).isEqualTo("test@example.com");
            assertBelongsToChurch(response.churchId(), churchId);
        }

        @Test
        @DisplayName("Should return 404 when member not found")
        void shouldReturn404WhenMemberNotFound() {
            // When: Get non-existent member
            // Then: Should return 404
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/members/99999")
            .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should get paginated list of members")
        void shouldGetPaginatedMembers() {
            // Given: Multiple members
            for (int i = 1; i <= 5; i++) {
                MemberRequest request = new MemberRequest(
                    null, "Member" + i, null, "Test" + i, null, "male", churchId, null, null, null, null,
                    "+23324400000" + i, null, null, null, null, null, null, "single", null, null, null, null, null, null, null
                );
                given()
                    .spec(authenticatedSpec(adminToken))
                    .body(request)
                    .post("/api/members");
            }

            // When: Get members with pagination
            given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("page", 0)
                .queryParam("size", 3)
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
                .body("content.size()", equalTo(3))
                .body("totalElements", greaterThanOrEqualTo(5))
                .body("number", equalTo(0))
                .body("size", equalTo(3));
        }
    }

    @Nested
    @DisplayName("Update Member Tests")
    class UpdateMemberTests {

        @Test
        @DisplayName("Should update member and recalculate profile completeness")
        void shouldUpdateMemberAndRecalculateCompleteness() {
            // Given: Member with minimal profile
            MemberRequest initialRequest = new MemberRequest(
                null, "Update", null, "Test", null, "male", churchId, null, null, null, null,
                "+233244555666", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            MemberResponse created = given()
                .spec(authenticatedSpec(adminToken))
                .body(initialRequest)
                .post("/api/members")
                .as(MemberResponse.class);

            Integer initialCompleteness = created.profileCompleteness();

            // When: Update member with more fields
            MemberRequest updateRequest = new MemberRequest(
                created.id(), "Update", "Middle", "Test", "Dr.", "male", churchId, null,
                LocalDate.of(1985, 3, 20), "NG", "Africa/Lagos",
                "+233244555666", "update@example.com", null, null, null, null, null,
                "married", null, "Doctor", YearMonth.of(2015, 6), "Emergency Contact", "+233244777888",
                "Updated notes", Set.of("medical", "leadership")
            );

            MemberResponse updated = given()
                .spec(authenticatedSpec(adminToken))
                .body(updateRequest)
            .when()
                .put("/api/members/" + created.id())
            .then()
                .statusCode(200)
            .extract()
                .as(MemberResponse.class);

            // Then: Verify updates and profile completeness increased
            assertThat(updated.otherName()).isEqualTo("Middle");
            assertThat(updated.title()).isEqualTo("Dr.");
            assertThat(updated.email()).isEqualTo("update@example.com");
            assertThat(updated.occupation()).isEqualTo("Doctor");
            assertThat(updated.profileCompleteness()).isGreaterThan(initialCompleteness);
        }

        @Test
        @DisplayName("Should reject update to non-existent member")
        void shouldRejectUpdateToNonExistentMember() {
            // Given: Update request for non-existent member
            MemberRequest request = new MemberRequest(
                99999L, "Ghost", null, "Member", null, "male", churchId, null, null, null, null,
                "+233244888999", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            // When: Try to update
            // Then: Should return 404
            given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
            .when()
                .put("/api/members/99999")
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("Delete Member Tests")
    class DeleteMemberTests {

        @Test
        @DisplayName("Should delete member")
        void shouldDeleteMember() {
            // Given: Existing member
            MemberRequest request = new MemberRequest(
                null, "Delete", null, "Me", null, "male", churchId, null, null, null, null,
                "+233244101010", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            MemberResponse created = given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
                .post("/api/members")
                .as(MemberResponse.class);

            // When: Delete member
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .delete("/api/members/" + created.id())
            .then()
                .statusCode(204);

            // Then: Member should not be found
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/members/" + created.id())
            .then()
                .statusCode(404);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent member")
        void shouldReturn404WhenDeletingNonExistentMember() {
            // When: Try to delete non-existent member
            // Then: Should return 404
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .delete("/api/members/99999")
            .then()
                .statusCode(404);
        }
    }

    @Nested
    @DisplayName("Multi-Tenancy Tests")
    class MultiTenancyTests {

        @Test
        @DisplayName("Should only return members from current church")
        void shouldIsolateMembersByChurch() {
            // Given: Two churches with members
            Long church1 = createTestChurch("Church 1");
            Long church2 = createTestChurch("Church 2");
            String token1 = getAdminToken(church1);
            String token2 = getAdminToken(church2);

            // Create member in church1
            MemberRequest request1 = new MemberRequest(
                null, "Church1", null, "Member", null, "male", church1, null, null, null, null,
                "+233244201010", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );
            given().spec(authenticatedSpec(token1)).body(request1).post("/api/members");

            // Create member in church2
            MemberRequest request2 = new MemberRequest(
                null, "Church2", null, "Member", null, "female", church2, null, null, null, null,
                "+233244202020", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );
            given().spec(authenticatedSpec(token2)).body(request2).post("/api/members");

            // When: Church 1 admin queries members
            List<MemberResponse> church1Members = given()
                .spec(authenticatedSpec(token1))
            .when()
                .get("/api/members")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("content", MemberResponse.class);

            // Then: Should only see church1 members
            assertThat(church1Members).isNotEmpty();
            assertThat(church1Members).allMatch(m -> m.churchId().equals(church1));
            assertThat(church1Members).noneMatch(m -> m.churchId().equals(church2));
        }

        @Test
        @DisplayName("Should prevent access to member from different church")
        void shouldPreventCrossChurchAccess() {
            // Given: Member in church1
            Long church1 = createTestChurch("Church 1");
            Long church2 = createTestChurch("Church 2");
            String token1 = getAdminToken(church1);
            String token2 = getAdminToken(church2);

            MemberRequest request = new MemberRequest(
                null, "Protected", null, "Member", null, "male", church1, null, null, null, null,
                "+233244301010", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            MemberResponse created = given()
                .spec(authenticatedSpec(token1))
                .body(request)
                .post("/api/members")
                .as(MemberResponse.class);

            // When: Church 2 admin tries to access church1 member
            // Then: Should return 404 (hiding existence) or 403
            given()
                .spec(authenticatedSpec(token2))
            .when()
                .get("/api/members/" + created.id())
            .then()
                .statusCode(anyOf(equalTo(404), equalTo(403)));
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {

        @Test
        @DisplayName("ADMIN should be able to create members")
        void adminShouldCreateMembers() {
            // Given: Admin token
            String token = getAdminToken(churchId);
            MemberRequest request = new MemberRequest(
                null, "Admin", null, "Created", null, "male", churchId, null, null, null, null,
                "+233244401010", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            // When: Admin creates member
            // Then: Should succeed
            given()
                .spec(authenticatedSpec(token))
                .body(request)
            .when()
                .post("/api/members")
            .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("MEMBER_MANAGER should be able to create members")
        void memberManagerShouldCreateMembers() {
            // Given: Member Manager token
            String token = getMemberManagerToken(churchId);
            MemberRequest request = new MemberRequest(
                null, "Manager", null, "Created", null, "female", churchId, null, null, null, null,
                "+233244501010", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            // When: Member Manager creates member
            // Then: Should succeed
            given()
                .spec(authenticatedSpec(token))
                .body(request)
            .when()
                .post("/api/members")
            .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("MEMBER should NOT be able to create members")
        void memberShouldNotCreateMembers() {
            // Given: Regular member token
            String token = getMemberToken(churchId);
            MemberRequest request = new MemberRequest(
                null, "Unauthorized", null, "Member", null, "male", churchId, null, null, null, null,
                "+233244601010", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            // When: Regular member tries to create member
            // Then: Should return 403 Forbidden
            given()
                .spec(authenticatedSpec(token))
                .body(request)
            .when()
                .post("/api/members")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("PASTOR should be able to view all members")
        void pastorShouldViewAllMembers() {
            // Given: Pastor token
            String token = getPastorToken(churchId);

            // When: Pastor queries members
            // Then: Should succeed
            given()
                .spec(authenticatedSpec(token))
            .when()
                .get("/api/members")
            .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("TREASURER should NOT be able to create members")
        void treasurerShouldNotCreateMembers() {
            // Given: Treasurer token
            String token = getTreasurerToken(churchId);
            MemberRequest request = new MemberRequest(
                null, "Treasurer", null, "Attempt", null, "male", churchId, null, null, null, null,
                "+233244701010", null, null, null, null, null, null, "single", null, null, null, null, null, null, null
            );

            // When: Treasurer tries to create member
            // Then: Should return 403 Forbidden
            given()
                .spec(authenticatedSpec(token))
                .body(request)
            .when()
                .post("/api/members")
            .then()
                .statusCode(403);
        }
    }
}
