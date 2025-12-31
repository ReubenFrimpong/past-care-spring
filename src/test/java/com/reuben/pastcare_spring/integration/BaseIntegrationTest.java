package com.reuben.pastcare_spring.integration;

import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.SubscriptionPlan;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import com.reuben.pastcare_spring.repositories.SubscriptionPlanRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.testutil.TestJwtUtil;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base class for all API integration tests.
 *
 * Provides:
 * - Spring Boot test context with random port
 * - REST Assured configuration
 * - JWT token generation for different roles
 * - Helper methods for creating test data (churches, users)
 * - Multi-tenancy assertion utilities
 *
 * Note: @Transactional is NOT used because HTTP requests run in separate
 * threads and would not see uncommitted test data. Instead, we use
 * @DirtiesContext to reset the database between test classes.
 *
 * Usage:
 * <pre>
 * {@code
 * @SpringBootTest
 * class MemberCrudIntegrationTest extends BaseIntegrationTest {
 *
 *     @Test
 *     void shouldCreateMember() {
 *         String token = getAdminToken(churchId);
 *         given()
 *             .spec(authenticatedSpec(token))
 *             .body(memberRequest)
 *         .when()
 *             .post("/api/members")
 *         .then()
 *             .statusCode(201);
 *     }
 * }
 * }
 * </pre>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected ChurchRepository churchRepository;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected ChurchSubscriptionRepository subscriptionRepository;

    @Autowired
    protected SubscriptionPlanRepository planRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected RequestSpecification spec;

    /**
     * Set up REST Assured before each test.
     */
    @BeforeEach
    void setUpBase() {
        RestAssured.port = port;
        RestAssured.baseURI = "http://localhost";

        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    // ============================================================================
    // Church Helper Methods
    // ============================================================================

    /**
     * Create a test church with default values.
     *
     * @return The created church ID
     */
    protected Long createTestChurch() {
        return createTestChurch("Test Church");
    }

    /**
     * Create a test church with custom name and an active subscription.
     *
     * @param churchName The church name
     * @return The created church ID
     */
    protected Long createTestChurch(String churchName) {
        Church church = new Church();
        church.setName(churchName);
        church.setEmail(churchName.toLowerCase().replace(" ", "") + "@testchurch.com");
        church.setPhoneNumber("+254700000000");
        church.setAddress("Test Address, Nairobi, Kenya");
        church.setWebsite("https://" + churchName.toLowerCase().replace(" ", "") + ".testchurch.com");
        church.setActive(true);

        Church savedChurch = churchRepository.save(church);

        // Create an active subscription for the test church
        createActiveSubscription(savedChurch.getId());

        return savedChurch.getId();
    }

    /**
     * Create a test church WITHOUT a subscription (for testing subscription-required scenarios).
     *
     * @param churchName The church name
     * @return The created church ID
     */
    protected Long createTestChurchWithoutSubscription(String churchName) {
        Church church = new Church();
        church.setName(churchName);
        church.setEmail(churchName.toLowerCase().replace(" ", "") + "@testchurch.com");
        church.setPhoneNumber("+254700000000");
        church.setAddress("Test Address, Nairobi, Kenya");
        church.setWebsite("https://" + churchName.toLowerCase().replace(" ", "") + ".testchurch.com");
        church.setActive(true);

        Church savedChurch = churchRepository.save(church);
        return savedChurch.getId();
    }

    /**
     * Create an active subscription for a church.
     * Uses the first available plan (typically FREE or the lowest tier).
     *
     * @param churchId The church ID
     */
    protected void createActiveSubscription(Long churchId) {
        // Check if subscription already exists
        if (subscriptionRepository.findByChurchId(churchId).isPresent()) {
            return;
        }

        // Get any available plan (prefer free plan if exists)
        SubscriptionPlan plan = planRepository.findByIsFreeTrueAndIsActiveTrue()
                .orElseGet(() -> planRepository.findAll().stream()
                        .filter(p -> p.getIsActive() != null && p.getIsActive())
                        .findFirst()
                        .orElse(null));

        if (plan == null) {
            // If no plans exist in database, skip subscription creation
            // This allows tests to run even without seeded plans
            return;
        }

        ChurchSubscription subscription = ChurchSubscription.builder()
                .churchId(churchId)
                .plan(plan)
                .status("ACTIVE")
                .billingPeriod("MONTHLY")
                .billingPeriodMonths(1)
                .autoRenew(true)
                .gracePeriodDays(7)
                .failedPaymentAttempts(0)
                .freeMonthsRemaining(0)
                .currentPeriodStart(LocalDate.now())
                .currentPeriodEnd(LocalDate.now().plusMonths(1))
                .nextBillingDate(LocalDate.now().plusMonths(1))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        subscriptionRepository.save(subscription);
    }

    // ============================================================================
    // User Creation Helper Methods
    // ============================================================================

    /**
     * Create a test user with specified role.
     *
     * @param churchId The church ID
     * @param email The user's email
     * @param role The user's role
     * @return The created user
     */
    protected User createTestUser(Long churchId, String email, Role role) {
        return createTestUser(churchId, email, "Test User", role);
    }

    /**
     * Create a test user with specified details.
     *
     * @param churchId The church ID (can be null for SUPERADMIN)
     * @param email The user's email
     * @param name The user's name
     * @param role The user's role
     * @return The created user
     */
    protected User createTestUser(Long churchId, String email, String name, Role role) {
        User user = new User();

        // Set church association (null for SUPERADMIN)
        if (churchId != null) {
            Church church = churchRepository.findById(churchId)
                    .orElseThrow(() -> new IllegalArgumentException("Church not found: " + churchId));
            user.setChurch(church);
        }

        user.setEmail(email);
        user.setName(name);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode("Password@123"));
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);

        return userRepository.save(user);
    }

    /**
     * Generate a unique email suffix for test isolation.
     */
    private String uniqueEmailSuffix() {
        return System.currentTimeMillis() + "_" + Thread.currentThread().getId();
    }

    /**
     * Create an ADMIN user for a church.
     *
     * @param churchId The church ID
     * @return The created admin user
     */
    protected User createAdminUser(Long churchId) {
        return createTestUser(churchId, "admin" + uniqueEmailSuffix() + "@testchurch.com", "Admin User", Role.ADMIN);
    }

    /**
     * Create a PASTOR user for a church.
     *
     * @param churchId The church ID
     * @return The created pastor user
     */
    protected User createPastorUser(Long churchId) {
        return createTestUser(churchId, "pastor" + uniqueEmailSuffix() + "@testchurch.com", "Pastor User", Role.PASTOR);
    }

    /**
     * Create a TREASURER user for a church.
     *
     * @param churchId The church ID
     * @return The created treasurer user
     */
    protected User createTreasurerUser(Long churchId) {
        return createTestUser(churchId, "treasurer" + uniqueEmailSuffix() + "@testchurch.com", "Treasurer User", Role.TREASURER);
    }

    /**
     * Create a MEMBER_MANAGER user for a church.
     *
     * @param churchId The church ID
     * @return The created member manager user
     */
    protected User createMemberManagerUser(Long churchId) {
        return createTestUser(churchId, "membermanager" + uniqueEmailSuffix() + "@testchurch.com", "Member Manager User", Role.MEMBER_MANAGER);
    }

    /**
     * Create a FELLOWSHIP_LEADER user for a church.
     *
     * @param churchId The church ID
     * @return The created fellowship leader user
     */
    protected User createFellowshipLeaderUser(Long churchId) {
        return createTestUser(churchId, "fellowshipleader" + uniqueEmailSuffix() + "@testchurch.com", "Fellowship Leader User", Role.FELLOWSHIP_LEADER);
    }

    /**
     * Create a MEMBER user for a church.
     *
     * @param churchId The church ID
     * @return The created member user
     */
    protected User createMemberUser(Long churchId) {
        return createTestUser(churchId, "member" + uniqueEmailSuffix() + "@testchurch.com", "Member User", Role.MEMBER);
    }

    /**
     * Create a SUPERADMIN user (no church association).
     *
     * @return The created superadmin user
     */
    protected User createSuperadminUser() {
        return createTestUser(null, "superadmin" + uniqueEmailSuffix() + "@pastcare.com", "Super Admin", Role.SUPERADMIN);
    }

    // ============================================================================
    // JWT Token Generation Helper Methods
    // ============================================================================

    /**
     * Generate JWT token for ADMIN role.
     *
     * @param churchId The church ID
     * @return JWT token string
     */
    protected String getAdminToken(Long churchId) {
        User admin = createAdminUser(churchId);
        return TestJwtUtil.generateAdminToken(admin.getId(), admin.getEmail(), churchId);
    }

    /**
     * Generate JWT token for PASTOR role.
     *
     * @param churchId The church ID
     * @return JWT token string
     */
    protected String getPastorToken(Long churchId) {
        User pastor = createPastorUser(churchId);
        return TestJwtUtil.generatePastorToken(pastor.getId(), pastor.getEmail(), churchId);
    }

    /**
     * Generate JWT token for TREASURER role.
     *
     * @param churchId The church ID
     * @return JWT token string
     */
    protected String getTreasurerToken(Long churchId) {
        User treasurer = createTreasurerUser(churchId);
        return TestJwtUtil.generateTreasurerToken(treasurer.getId(), treasurer.getEmail(), churchId);
    }

    /**
     * Generate JWT token for MEMBER_MANAGER role.
     *
     * @param churchId The church ID
     * @return JWT token string
     */
    protected String getMemberManagerToken(Long churchId) {
        User memberManager = createMemberManagerUser(churchId);
        return TestJwtUtil.generateMemberManagerToken(memberManager.getId(), memberManager.getEmail(), churchId);
    }

    /**
     * Generate JWT token for FELLOWSHIP_LEADER role.
     *
     * @param churchId The church ID
     * @return JWT token string
     */
    protected String getFellowshipLeaderToken(Long churchId) {
        User fellowshipLeader = createFellowshipLeaderUser(churchId);
        return TestJwtUtil.generateFellowshipLeaderToken(fellowshipLeader.getId(), fellowshipLeader.getEmail(), churchId);
    }

    /**
     * Generate JWT token for MEMBER role.
     *
     * @param churchId The church ID
     * @return JWT token string
     */
    protected String getMemberToken(Long churchId) {
        User member = createMemberUser(churchId);
        return TestJwtUtil.generateMemberToken(member.getId(), member.getEmail(), churchId);
    }

    /**
     * Generate JWT token for SUPERADMIN role.
     *
     * @return JWT token string
     */
    protected String getSuperadminToken() {
        User superadmin = createSuperadminUser();
        return TestJwtUtil.generateSuperadminToken(superadmin.getId(), superadmin.getEmail());
    }

    /**
     * Generate JWT token for a specific user.
     *
     * @param user The user
     * @return JWT token string
     */
    protected String getTokenForUser(User user) {
        Long churchId = user.getChurch() != null ? user.getChurch().getId() : null;
        return TestJwtUtil.generateToken(user.getId(), user.getEmail(), churchId, user.getRole());
    }

    // ============================================================================
    // REST Assured Specification Helper Methods
    // ============================================================================

    /**
     * Create an authenticated request specification with JWT token.
     *
     * @param token The JWT token
     * @return Authenticated request specification
     */
    protected RequestSpecification authenticatedSpec(String token) {
        return given()
                .spec(spec)
                .header("Authorization", "Bearer " + token);
    }

    // ============================================================================
    // Multi-Tenancy Assertion Helper Methods
    // ============================================================================

    /**
     * Assert that an entity belongs to the expected church (multi-tenancy check).
     *
     * @param actualChurchId The entity's church ID
     * @param expectedChurchId The expected church ID
     */
    protected void assertBelongsToChurch(Long actualChurchId, Long expectedChurchId) {
        assertThat(actualChurchId)
                .as("Entity must belong to church %d (multi-tenancy isolation)", expectedChurchId)
                .isEqualTo(expectedChurchId);
    }

    /**
     * Assert that an entity does NOT belong to a specific church.
     *
     * @param actualChurchId The entity's church ID
     * @param forbiddenChurchId The church ID it should NOT belong to
     */
    protected void assertDoesNotBelongToChurch(Long actualChurchId, Long forbiddenChurchId) {
        assertThat(actualChurchId)
                .as("Entity must NOT belong to church %d (multi-tenancy isolation)", forbiddenChurchId)
                .isNotEqualTo(forbiddenChurchId);
    }

    /**
     * Assert that a church ID is not null (for entities that must have church association).
     *
     * @param churchId The church ID to check
     */
    protected void assertHasChurchId(Long churchId) {
        assertThat(churchId)
                .as("Entity must have a church ID (multi-tenancy requirement)")
                .isNotNull();
    }
}
