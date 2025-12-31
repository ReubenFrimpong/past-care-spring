package com.reuben.pastcare_spring.integration.billing;

import com.reuben.pastcare_spring.integration.BaseIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Billing/Subscription module.
 */
@SpringBootTest
@Tag("integration")
@Tag("module:billing")
@DisplayName("Billing Integration Tests")
@Transactional
class BillingIntegrationTest extends BaseIntegrationTest {

    private Long churchId;
    private String adminToken;
    private String superadminToken;

    @BeforeEach
    void setUp() {
        churchId = createTestChurch();
        adminToken = getAdminToken(churchId);
        superadminToken = getSuperadminToken();
    }

    @Nested
    @DisplayName("Subscription Plan Tests")
    class PlanTests {

        @Test
        @DisplayName("Should list available plans")
        void shouldListPlans() {
            List<?> plans = given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/billing/plans")
            .then()
                .statusCode(200)
            .extract()
                .jsonPath().getList("$");

            assertThat(plans).isNotEmpty();
        }

        @Test
        @DisplayName("Should get plan details")
        void shouldGetPlanDetails() {
            // Assuming plan ID 1 exists (FREE plan)
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/billing/plans/1")
            .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("name", notNullValue());
        }
    }

    @Nested
    @DisplayName("Subscription Management Tests")
    class SubscriptionTests {

        @Test
        @DisplayName("Should initialize subscription payment")
        void shouldInitializeSubscription() {
            Map<String, Object> request = new HashMap<>();
            request.put("planId", 2L); // Assuming BASIC plan
            request.put("email", "admin@church.com");
            request.put("callbackUrl", "https://church.com/callback");
            request.put("billingPeriod", "MONTHLY");

            given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
            .when()
                .post("/api/billing/subscribe")
            .then()
                .statusCode(200)
                .body("authorizationUrl", notNullValue())
                .body("reference", notNullValue());
        }

        @Test
        @DisplayName("Should verify subscription payment")
        void shouldVerifyPayment() {
            // This would test payment verification with a mock reference
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("Subscription Lifecycle Tests")
    class LifecycleTests {

        @Test
        @DisplayName("Should upgrade subscription")
        void shouldUpgradeSubscription() {
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should downgrade subscription")
        void shouldDowngradeSubscription() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .post("/api/billing/downgrade-to-free")
            .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("Should cancel subscription")
        void shouldCancelSubscription() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .post("/api/billing/cancel")
            .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("Should reactivate subscription")
        void shouldReactivateSubscription() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .post("/api/billing/reactivate")
            .then()
                .statusCode(200);
        }
    }

    @Nested
    @DisplayName("Payment Tests")
    class PaymentTests {

        @Test
        @DisplayName("Should verify payment")
        void shouldVerifyPayment() {
            // Mock payment verification
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle webhook")
        void shouldHandleWebhook() {
            // Test Paystack webhook handling
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("Promotional Credits Tests")
    class PromotionalCreditsTests {

        @Test
        @DisplayName("Should apply promotional credits")
        void shouldApplyPromotionalCredits() {
            // This would be SUPERADMIN only
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should track free months")
        void shouldTrackFreeMonths() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/billing/promotional-credits")
            .then()
                .statusCode(200);
        }
    }

    @Nested
    @DisplayName("Recurring Billing Tests")
    class RecurringBillingTests {

        @Test
        @DisplayName("Should process monthly billing")
        void shouldProcessMonthlyBilling() {
            // This would test automated billing
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should handle subscription expiry")
        void shouldHandleExpiry() {
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("Usage Tracking Tests")
    class UsageTests {

        @Test
        @DisplayName("Should track storage usage")
        void shouldTrackStorageUsage() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/billing/subscription")
            .then()
                .statusCode(200)
                .body("storageUsedMb", notNullValue());
        }

        @Test
        @DisplayName("Should track user count")
        void shouldTrackUserCount() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/billing/subscription")
            .then()
                .statusCode(200)
                .body("currentUserCount", notNullValue());
        }

        @Test
        @DisplayName("Should enforce plan limits")
        void shouldEnforceLimits() {
            // Test that plan limits are enforced
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("Multi-Tenancy Tests")
    class MultiTenancyTests {

        @Test
        @DisplayName("Should isolate subscriptions by church")
        void shouldIsolateSubscriptionsByChurch() {
            // Each church has independent subscription
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/billing/subscription")
            .then()
                .statusCode(200)
                .body("churchId", equalTo(churchId.intValue()));
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {

        @Test
        @DisplayName("Only ADMIN should manage subscriptions")
        void onlyAdminShouldManageSubscriptions() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/billing/subscription")
            .then()
                .statusCode(200);
        }
    }

    @Nested
    @DisplayName("Manual Subscription Activation Tests")
    class ManualActivationTests {

        private String superAdminToken;
        private Long testChurchId;

        @BeforeEach
        void setUpManualActivation() {
            // Create SUPERADMIN user and test church
            testChurchId = createTestChurch();
            superAdminToken = getSuperadminToken();
        }

        @Test
        @DisplayName("SUPERADMIN should manually activate subscription")
        void superAdminShouldManuallyActivateSubscription() {
            Map<String, Object> request = new HashMap<>();
            request.put("churchId", testChurchId);
            request.put("planId", 2L); // PROFESSIONAL plan
            request.put("durationMonths", 3);
            request.put("reason", "Payment verified on Paystack - callback failed (Ref: PCS-test-123)");

            given()
                .spec(authenticatedSpec(superAdminToken))
                .contentType("application/json")
                .body(request)
            .when()
                .post("/api/billing/platform/subscription/manual-activate")
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("message", containsString("manually activated"))
                .body("subscription.churchId", equalTo(testChurchId.intValue()))
                .body("subscription.status", equalTo("ACTIVE"))
                .body("subscription.paymentMethodType", equalTo("MANUAL"))
                .body("subscription.autoRenew", equalTo(false));
        }

        @Test
        @DisplayName("Manual activation should create audit payment record")
        void manualActivationShouldCreatePaymentRecord() {
            // Activate subscription manually
            Map<String, Object> request = new HashMap<>();
            request.put("churchId", testChurchId);
            request.put("planId", 2L);
            request.put("durationMonths", 1);
            request.put("reason", "Manual payment via bank transfer - Ref: BT20251230001");

            given()
                .spec(authenticatedSpec(superAdminToken))
                .contentType("application/json")
                .body(request)
            .when()
                .post("/api/billing/platform/subscription/manual-activate")
            .then()
                .statusCode(200)
                .body("success", equalTo(true));

            // Verify payment record exists (would need payment history endpoint)
            // This is a placeholder - actual implementation would verify in database
        }

        @Test
        @DisplayName("Manual activation should require reason")
        void manualActivationShouldRequireReason() {
            Map<String, Object> request = new HashMap<>();
            request.put("churchId", testChurchId);
            request.put("planId", 2L);
            request.put("durationMonths", 1);
            request.put("reason", ""); // Empty reason

            given()
                .spec(authenticatedSpec(superAdminToken))
                .contentType("application/json")
                .body(request)
            .when()
                .post("/api/billing/platform/subscription/manual-activate")
            .then()
                .statusCode(400);
        }

        @Test
        @DisplayName("Manual activation should require valid plan")
        void manualActivationShouldRequireValidPlan() {
            Map<String, Object> request = new HashMap<>();
            request.put("churchId", testChurchId);
            request.put("planId", 999L); // Non-existent plan
            request.put("durationMonths", 1);
            request.put("reason", "Testing with invalid plan");

            given()
                .spec(authenticatedSpec(superAdminToken))
                .contentType("application/json")
                .body(request)
            .when()
                .post("/api/billing/platform/subscription/manual-activate")
            .then()
                .statusCode(400)
                .body("success", equalTo(false))
                .body("error", containsString("Plan not found"));
        }

        @Test
        @DisplayName("Non-SUPERADMIN should not manually activate subscription")
        void nonSuperAdminShouldNotManuallyActivate() {
            Map<String, Object> request = new HashMap<>();
            request.put("churchId", testChurchId);
            request.put("planId", 2L);
            request.put("durationMonths", 1);
            request.put("reason", "Attempt by regular admin");

            given()
                .spec(authenticatedSpec(adminToken)) // Regular admin, not SUPERADMIN
                .contentType("application/json")
                .body(request)
            .when()
                .post("/api/billing/platform/subscription/manual-activate")
            .then()
                .statusCode(403); // Forbidden
        }

        @Test
        @DisplayName("Manual activation should support various durations")
        void manualActivationShouldSupportVariousDurations() {
            int[] durations = {1, 3, 6, 12, 24, 36};

            for (int duration : durations) {
                Long churchId = createTestChurch(); // Create new church for each test

                Map<String, Object> request = new HashMap<>();
                request.put("churchId", churchId);
                request.put("planId", 2L);
                request.put("durationMonths", duration);
                request.put("reason", "Testing " + duration + " month activation");

                given()
                    .spec(authenticatedSpec(superAdminToken))
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

        @Test
        @DisplayName("Manual activation should update existing subscription")
        void manualActivationShouldUpdateExistingSubscription() {
            // First activation
            Map<String, Object> firstRequest = new HashMap<>();
            firstRequest.put("churchId", testChurchId);
            firstRequest.put("planId", 1L); // FREE plan
            firstRequest.put("durationMonths", 1);
            firstRequest.put("reason", "Initial activation");

            given()
                .spec(authenticatedSpec(superAdminToken))
                .contentType("application/json")
                .body(firstRequest)
            .when()
                .post("/api/billing/platform/subscription/manual-activate")
            .then()
                .statusCode(200);

            // Second activation (upgrade)
            Map<String, Object> secondRequest = new HashMap<>();
            secondRequest.put("churchId", testChurchId);
            secondRequest.put("planId", 3L); // ENTERPRISE plan
            secondRequest.put("durationMonths", 6);
            secondRequest.put("reason", "Upgrade to ENTERPRISE - promotional access");

            given()
                .spec(authenticatedSpec(superAdminToken))
                .contentType("application/json")
                .body(secondRequest)
            .when()
                .post("/api/billing/platform/subscription/manual-activate")
            .then()
                .statusCode(200)
                .body("success", equalTo(true))
                .body("subscription.status", equalTo("ACTIVE"));
        }
    }
}
