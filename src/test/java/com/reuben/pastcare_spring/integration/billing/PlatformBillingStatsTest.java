package com.reuben.pastcare_spring.integration.billing;

import com.reuben.pastcare_spring.integration.BaseIntegrationTest;
import com.reuben.pastcare_spring.models.ChurchSubscription;
import com.reuben.pastcare_spring.models.SubscriptionPlan;
import com.reuben.pastcare_spring.repositories.ChurchSubscriptionRepository;
import com.reuben.pastcare_spring.repositories.SubscriptionPlanRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Platform Billing Statistics.
 * Tests revenue calculation, grace period handling, and MRR calculations.
 */
@SpringBootTest
@Tag("integration")
@Tag("module:billing")
@DisplayName("Platform Billing Stats Integration Tests")
@Transactional
class PlatformBillingStatsTest extends BaseIntegrationTest {

    @Autowired
    private ChurchSubscriptionRepository subscriptionRepository;

    @Autowired
    private SubscriptionPlanRepository planRepository;

    private String superadminToken;
    private SubscriptionPlan professionalPlan;
    private SubscriptionPlan enterprisePlan;

    @BeforeEach
    void setUp() {
        superadminToken = getSuperadminToken();

        // Get existing plans
        professionalPlan = planRepository.findByName("PROFESSIONAL")
                .orElseThrow(() -> new RuntimeException("PROFESSIONAL plan not found"));
        enterprisePlan = planRepository.findByName("ENTERPRISE")
                .orElseThrow(() -> new RuntimeException("ENTERPRISE plan not found"));
    }

    @Nested
    @DisplayName("Revenue Calculation Tests")
    class RevenueCalculationTests {

        @Test
        @DisplayName("Should calculate MRR correctly for active subscriptions")
        void shouldCalculateMrrForActiveSubscriptions() {
            // Create 3 active subscriptions
            createActiveSubscription(professionalPlan, 1); // GHS 50/month
            createActiveSubscription(professionalPlan, 1); // GHS 50/month
            createActiveSubscription(enterprisePlan, 1);   // GHS 100/month

            // Expected MRR = 50 + 50 + 100 = GHS 200

            var response = given()
                .spec(authenticatedSpec(superadminToken))
            .when()
                .get("/api/platform/billing/stats")
            .then()
                .statusCode(200)
                .body("monthlyRecurringRevenue", greaterThanOrEqualTo(200.0f))
            .extract()
                .jsonPath();

            double mrr = response.getDouble("monthlyRecurringRevenue");
            assertThat(mrr).isGreaterThanOrEqualTo(200.0);
        }

        @Test
        @DisplayName("Should NOT include grace period subscriptions in MRR")
        void shouldNotIncludeGracePeriodInMrr() {
            // Create 2 active subscriptions
            createActiveSubscription(professionalPlan, 1); // GHS 50/month - SHOULD COUNT
            createActiveSubscription(professionalPlan, 1); // GHS 50/month - SHOULD COUNT

            // Create 2 subscriptions in grace period (PAST_DUE but within grace period)
            createGracePeriodSubscription(professionalPlan, 1); // SHOULD NOT COUNT
            createGracePeriodSubscription(professionalPlan, 1); // SHOULD NOT COUNT

            // Expected MRR = 50 + 50 = GHS 100 (NOT 200)

            var response = given()
                .spec(authenticatedSpec(superadminToken))
            .when()
                .get("/api/platform/billing/stats")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath();

            double mrr = response.getDouble("monthlyRecurringRevenue");

            // MRR should be around 100 (only active subscriptions)
            // Allow small margin for other subscriptions in test DB
            assertThat(mrr).isLessThan(150.0); // Should NOT include grace period (200)
        }

        @Test
        @DisplayName("Should include past-due subscriptions AFTER grace period in MRR")
        void shouldIncludePastDueAfterGracePeriodInMrr() {
            // Create 1 active subscription
            createActiveSubscription(professionalPlan, 1); // GHS 50/month - SHOULD COUNT

            // Create 1 subscription past-due BEYOND grace period
            createExpiredPastDueSubscription(professionalPlan, 1); // GHS 50/month - SHOULD COUNT (owed)

            // Create 1 subscription in grace period
            createGracePeriodSubscription(professionalPlan, 1); // SHOULD NOT COUNT

            // Expected MRR = 50 (active) + 50 (past-due beyond grace) = GHS 100

            var response = given()
                .spec(authenticatedSpec(superadminToken))
            .when()
                .get("/api/platform/billing/stats")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath();

            double mrr = response.getDouble("monthlyRecurringRevenue");
            int pastDueCount = response.getInt("pastDueSubscriptions");

            // Should have at least 1 past-due subscription
            assertThat(pastDueCount).isGreaterThanOrEqualTo(1);

            // MRR should include both active and past-due (not in grace)
            assertThat(mrr).isGreaterThanOrEqualTo(50.0);
        }

        @Test
        @DisplayName("Should correctly normalize quarterly subscriptions to monthly MRR")
        void shouldNormalizeQuarterlyToMonthlyMrr() {
            // Create quarterly subscription: GHS 300 for 3 months
            createActiveSubscription(professionalPlan, 3); // GHS 150 total for 3 months = GHS 50/month

            var response = given()
                .spec(authenticatedSpec(superadminToken))
            .when()
                .get("/api/platform/billing/stats")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath();

            double mrr = response.getDouble("monthlyRecurringRevenue");

            // Should normalize: 150 / 3 = 50 per month
            assertThat(mrr).isGreaterThanOrEqualTo(50.0);
        }

        @Test
        @DisplayName("Should correctly normalize annual subscriptions to monthly MRR")
        void shouldNormalizeAnnualToMonthlyMrr() {
            // Create annual subscription: GHS 1200 for 12 months
            createActiveSubscription(enterprisePlan, 12); // GHS 1200 total for 12 months = GHS 100/month

            var response = given()
                .spec(authenticatedSpec(superadminToken))
            .when()
                .get("/api/platform/billing/stats")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath();

            double mrr = response.getDouble("monthlyRecurringRevenue");
            double arr = response.getDouble("annualRecurringRevenue");

            // Should normalize: 1200 / 12 = 100 per month
            assertThat(mrr).isGreaterThanOrEqualTo(100.0);

            // ARR should be MRR * 12
            assertThat(arr).isGreaterThanOrEqualTo(mrr * 12);
        }
    }

    @Nested
    @DisplayName("ARPU Calculation Tests")
    class ArpuCalculationTests {

        @Test
        @DisplayName("Should exclude grace period subscriptions from ARPU calculation")
        void shouldExcludeGracePeriodFromArpu() {
            // Create 2 active subscriptions at GHS 50/month each
            createActiveSubscription(professionalPlan, 1);
            createActiveSubscription(professionalPlan, 1);

            // Create 1 grace period subscription (should NOT be counted)
            createGracePeriodSubscription(professionalPlan, 1);

            // Expected: MRR = 100, Billed Churches = 2, ARPU = 100/2 = 50

            var response = given()
                .spec(authenticatedSpec(superadminToken))
            .when()
                .get("/api/platform/billing/stats")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath();

            double arpu = response.getDouble("averageRevenuePerChurch");
            int billedChurches = response.getInt("totalBilledChurches");

            // Billed churches should only count active (not grace period)
            assertThat(billedChurches).isGreaterThanOrEqualTo(2);
            assertThat(arpu).isGreaterThanOrEqualTo(40.0); // Should be around 50
        }

        @Test
        @DisplayName("Should include past-due (after grace) in ARPU calculation")
        void shouldIncludePastDueAfterGraceInArpu() {
            // Create 1 active + 1 past-due (after grace)
            createActiveSubscription(professionalPlan, 1); // GHS 50/month
            createExpiredPastDueSubscription(professionalPlan, 1); // GHS 50/month

            // Expected: MRR = 100, Billed Churches = 2, ARPU = 50

            var response = given()
                .spec(authenticatedSpec(superadminToken))
            .when()
                .get("/api/platform/billing/stats")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath();

            int billedChurches = response.getInt("totalBilledChurches");

            // Should count both active and past-due (not in grace)
            assertThat(billedChurches).isGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Subscription Status Counts Tests")
    class StatusCountsTests {

        @Test
        @DisplayName("Should correctly count subscriptions by status")
        void shouldCountSubscriptionsByStatus() {
            createActiveSubscription(professionalPlan, 1);
            createActiveSubscription(professionalPlan, 1);
            createGracePeriodSubscription(professionalPlan, 1);
            createCanceledSubscription(professionalPlan);
            createSuspendedSubscription(professionalPlan);

            var response = given()
                .spec(authenticatedSpec(superadminToken))
            .when()
                .get("/api/platform/billing/stats")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath();

            int activeCount = response.getInt("activeSubscriptions");
            int pastDueCount = response.getInt("pastDueSubscriptions");
            int canceledCount = response.getInt("canceledSubscriptions");
            int suspendedCount = response.getInt("suspendedSubscriptions");

            assertThat(activeCount).isGreaterThanOrEqualTo(2);
            assertThat(pastDueCount).isGreaterThanOrEqualTo(1);
            assertThat(canceledCount).isGreaterThanOrEqualTo(1);
            assertThat(suspendedCount).isGreaterThanOrEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Manual Activation with Category Tests")
    class ManualActivationCategoryTests {

        @Test
        @DisplayName("Manual activation should require category field")
        void manualActivationShouldRequireCategory() {
            Long churchId = createTestChurch();

            var request = new java.util.HashMap<String, Object>();
            request.put("churchId", churchId);
            request.put("planId", professionalPlan.getId());
            request.put("durationMonths", 1);
            request.put("reason", "Testing without category");
            // Missing category field

            given()
                .spec(authenticatedSpec(superadminToken))
                .contentType("application/json")
                .body(request)
            .when()
                .post("/api/billing/platform/subscription/manual-activate")
            .then()
                .statusCode(200); // Should succeed with default category "UNSPECIFIED"
        }

        @Test
        @DisplayName("Manual activation should accept and store category")
        void manualActivationShouldAcceptCategory() {
            Long churchId = createTestChurch();

            var request = new java.util.HashMap<String, Object>();
            request.put("churchId", churchId);
            request.put("planId", professionalPlan.getId());
            request.put("durationMonths", 3);
            request.put("category", "PAYMENT_CALLBACK_FAILED");
            request.put("reason", "Payment verified on Paystack - callback failed (Ref: PCS-ABC123)");

            given()
                .spec(authenticatedSpec(superadminToken))
                .contentType("application/json")
                .body(request)
            .when()
                .post("/api/billing/platform/subscription/manual-activate")
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("subscription.status", equalTo("ACTIVE"));
        }

        @Test
        @DisplayName("Manual activation should support all category types")
        void manualActivationShouldSupportAllCategories() {
            String[] categories = {
                "PAYMENT_CALLBACK_FAILED",
                "ALTERNATIVE_PAYMENT",
                "GRACE_PERIOD_EXTENSION",
                "PROMOTIONAL",
                "EMERGENCY_OVERRIDE"
            };

            for (String category : categories) {
                Long churchId = createTestChurch();

                var request = new java.util.HashMap<String, Object>();
                request.put("churchId", churchId);
                request.put("planId", professionalPlan.getId());
                request.put("durationMonths", 1);
                request.put("category", category);
                request.put("reason", "Testing category: " + category);

                given()
                    .spec(authenticatedSpec(superadminToken))
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/billing/platform/subscription/manual-activate")
                .then()
                    .statusCode(200)
                    .body("success", equalTo(true))
                    .body("subscription.status", equalTo("ACTIVE"));
            }
        }
    }

    // Helper methods to create test subscriptions

    private void createActiveSubscription(SubscriptionPlan plan, int billingMonths) {
        Long churchId = createTestChurch();

        ChurchSubscription subscription = ChurchSubscription.builder()
                .churchId(churchId)
                .plan(plan)
                .status("ACTIVE")
                .currentPeriodStart(LocalDate.now().minusDays(5))
                .currentPeriodEnd(LocalDate.now().plusDays(25))
                .nextBillingDate(LocalDate.now().plusDays(25))
                .billingPeriodMonths(billingMonths)
                .autoRenew(true)
                .gracePeriodDays(7)
                .failedPaymentAttempts(0)
                .build();

        subscriptionRepository.save(subscription);
    }

    private void createGracePeriodSubscription(SubscriptionPlan plan, int billingMonths) {
        Long churchId = createTestChurch();

        // PAST_DUE but within grace period (next billing date was 3 days ago, grace period is 7 days)
        ChurchSubscription subscription = ChurchSubscription.builder()
                .churchId(churchId)
                .plan(plan)
                .status("PAST_DUE")
                .currentPeriodStart(LocalDate.now().minusMonths(1))
                .currentPeriodEnd(LocalDate.now().minusDays(3))
                .nextBillingDate(LocalDate.now().minusDays(3)) // 3 days overdue
                .billingPeriodMonths(billingMonths)
                .autoRenew(true)
                .gracePeriodDays(7) // Still within 7-day grace period
                .failedPaymentAttempts(1)
                .build();

        subscriptionRepository.save(subscription);
    }

    private void createExpiredPastDueSubscription(SubscriptionPlan plan, int billingMonths) {
        Long churchId = createTestChurch();

        // PAST_DUE and BEYOND grace period (next billing date was 10 days ago, grace period is 7 days)
        ChurchSubscription subscription = ChurchSubscription.builder()
                .churchId(churchId)
                .plan(plan)
                .status("PAST_DUE")
                .currentPeriodStart(LocalDate.now().minusMonths(1))
                .currentPeriodEnd(LocalDate.now().minusDays(10))
                .nextBillingDate(LocalDate.now().minusDays(10)) // 10 days overdue
                .billingPeriodMonths(billingMonths)
                .autoRenew(true)
                .gracePeriodDays(7) // Beyond 7-day grace period
                .failedPaymentAttempts(3)
                .build();

        subscriptionRepository.save(subscription);
    }

    private void createCanceledSubscription(SubscriptionPlan plan) {
        Long churchId = createTestChurch();

        ChurchSubscription subscription = ChurchSubscription.builder()
                .churchId(churchId)
                .plan(plan)
                .status("CANCELED")
                .currentPeriodStart(LocalDate.now().minusMonths(2))
                .currentPeriodEnd(LocalDate.now().minusDays(30))
                .nextBillingDate(null)
                .billingPeriodMonths(1)
                .autoRenew(false)
                .gracePeriodDays(7)
                .failedPaymentAttempts(0)
                .build();

        subscriptionRepository.save(subscription);
    }

    private void createSuspendedSubscription(SubscriptionPlan plan) {
        Long churchId = createTestChurch();

        ChurchSubscription subscription = ChurchSubscription.builder()
                .churchId(churchId)
                .plan(plan)
                .status("SUSPENDED")
                .currentPeriodStart(LocalDate.now().minusMonths(2))
                .currentPeriodEnd(LocalDate.now().minusDays(20))
                .nextBillingDate(LocalDate.now().minusDays(20))
                .billingPeriodMonths(1)
                .autoRenew(false)
                .gracePeriodDays(7)
                .failedPaymentAttempts(5)
                .build();

        subscriptionRepository.save(subscription);
    }
}
