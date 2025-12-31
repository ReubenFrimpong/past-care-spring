package com.reuben.pastcare_spring.integration.giving;

import com.reuben.pastcare_spring.integration.BaseIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Giving module (Donations, Campaigns, Pledges).
 *
 * TEMPLATE FILE - Demonstrates comprehensive test patterns for:
 * - Donation recording (cash, online, mobile money)
 * - Paystack payment integration
 * - Campaigns and pledges
 * - Receipt generation
 * - Giving analytics
 */
@SpringBootTest
@Tag("integration")
@Tag("module:giving")
@DisplayName("Giving Integration Tests")
@Transactional
class GivingIntegrationTestTemplate extends BaseIntegrationTest {

    private Long churchId;
    private String adminToken;
    private String treasurerToken;

    @BeforeEach
    void setUp() {
        churchId = createTestChurch();
        adminToken = getAdminToken(churchId);
        treasurerToken = getTreasurerToken(churchId);
    }

    @Nested
    @DisplayName("Donation Recording Tests")
    class DonationRecordingTests {

        @Test
        @DisplayName("Should record cash donation")
        void shouldRecordCashDonation() {
            // Given: Cash donation request
            Long memberId = createTestMember();
            String donationPayload = """
                {
                    "memberId": %d,
                    "amount": 100.00,
                    "donationType": "TITHE",
                    "paymentMethod": "CASH",
                    "donationDate": "%s",
                    "notes": "Sunday offering"
                }
                """.formatted(memberId, LocalDate.now());

            // When: Treasurer records donation
            given()
                .spec(authenticatedSpec(treasurerToken))
                .body(donationPayload)
            .when()
                .post("/api/donations")
            .then()
                .statusCode(201)
                .body("amount", equalTo(100.00f))
                .body("donationType", equalTo("TITHE"))
                .body("paymentMethod", equalTo("CASH"))
                .body("memberId", equalTo(memberId.intValue()));
        }

        @Test
        @DisplayName("Should record anonymous donation")
        void shouldRecordAnonymousDonation() {
            // Given: Anonymous donation (no memberId)
            String donationPayload = """
                {
                    "amount": 500.00,
                    "donationType": "OFFERING",
                    "paymentMethod": "CASH",
                    "donationDate": "%s",
                    "isAnonymous": true
                }
                """.formatted(LocalDate.now());

            // When: Record anonymous donation
            given()
                .spec(authenticatedSpec(treasurerToken))
                .body(donationPayload)
            .when()
                .post("/api/donations")
            .then()
                .statusCode(201)
                .body("isAnonymous", equalTo(true))
                .body("memberId", nullValue());
        }

        @Test
        @DisplayName("Should reject donation with negative amount")
        void shouldRejectNegativeAmount() {
            // Given: Invalid donation (negative amount)
            String donationPayload = """
                {
                    "amount": -100.00,
                    "donationType": "TITHE",
                    "paymentMethod": "CASH",
                    "donationDate": "%s"
                }
                """.formatted(LocalDate.now());

            // When: Try to record
            // Then: Should return 400 Bad Request
            given()
                .spec(authenticatedSpec(treasurerToken))
                .body(donationPayload)
            .when()
                .post("/api/donations")
            .then()
                .statusCode(400)
                .body("message", containsString("Amount cannot be negative"));
        }

        @Test
        @DisplayName("Should reject donation with zero amount")
        void shouldRejectZeroAmount() {
            // Given: Invalid donation (zero amount)
            String donationPayload = """
                {
                    "amount": 0.00,
                    "donationType": "TITHE",
                    "paymentMethod": "CASH",
                    "donationDate": "%s"
                }
                """.formatted(LocalDate.now());

            // When: Try to record
            // Then: Should return 400 Bad Request
            given()
                .spec(authenticatedSpec(treasurerToken))
                .body(donationPayload)
            .when()
                .post("/api/donations")
            .then()
                .statusCode(400)
                .body("message", containsString("Amount must be greater than 0"));
        }
    }

    @Nested
    @DisplayName("Paystack Integration Tests")
    class PaystackIntegrationTests {

        @Test
        @DisplayName("Should initialize online payment")
        void shouldInitializeOnlinePayment() {
            // Given: Payment initialization request
            Long memberId = createTestMember();
            String paymentPayload = """
                {
                    "memberId": %d,
                    "amount": 1000.00,
                    "email": "donor@example.com",
                    "donationType": "TITHE"
                }
                """.formatted(memberId);

            // When: Initialize payment
            given()
                .spec(authenticatedSpec(treasurerToken))
                .body(paymentPayload)
            .when()
                .post("/api/donations/paystack/initialize")
            .then()
                .statusCode(200)
                .body("authorizationUrl", notNullValue())
                .body("reference", notNullValue());
        }

        @Test
        @DisplayName("Should verify successful payment")
        void shouldVerifySuccessfulPayment() {
            // Given: Payment reference (mock successful payment)
            String reference = "MOCK-PAYMENT-REF-123";

            // When: Verify payment
            given()
                .spec(authenticatedSpec(treasurerToken))
                .queryParam("reference", reference)
            .when()
                .get("/api/donations/paystack/verify")
            .then()
                .statusCode(200)
                .body("status", anyOf(equalTo("success"), equalTo("VERIFIED")));
        }

        @Test
        @DisplayName("Should handle Paystack webhook")
        void shouldHandlePaystackWebhook() {
            // Given: Paystack webhook payload
            String webhookPayload = """
                {
                    "event": "charge.success",
                    "data": {
                        "reference": "WEBHOOK-REF-456",
                        "amount": 50000,
                        "customer": {
                            "email": "donor@example.com"
                        },
                        "metadata": {
                            "churchId": %d,
                            "donationType": "TITHE"
                        }
                    }
                }
                """.formatted(churchId);

            // When: Webhook received
            given()
                .spec(spec)
                .header("x-paystack-signature", "mock-signature")
                .body(webhookPayload)
            .when()
                .post("/api/webhooks/paystack")
            .then()
                .statusCode(200);

            // Then: Donation should be created
            // (Verify in database or query donations endpoint)
        }
    }

    @Nested
    @DisplayName("Campaign Tests")
    class CampaignTests {

        @Test
        @DisplayName("Should create fundraising campaign")
        void shouldCreateCampaign() {
            // Given: Campaign request
            String campaignPayload = """
                {
                    "name": "Building Fund 2025",
                    "description": "New sanctuary construction",
                    "goal": 100000.00,
                    "startDate": "%s",
                    "endDate": "%s"
                }
                """.formatted(LocalDate.now(), LocalDate.now().plusMonths(6));

            // When: Create campaign
            given()
                .spec(authenticatedSpec(adminToken))
                .body(campaignPayload)
            .when()
                .post("/api/campaigns")
            .then()
                .statusCode(201)
                .body("name", equalTo("Building Fund 2025"))
                .body("goal", equalTo(100000.00f))
                .body("currentAmount", equalTo(0.0f));
        }

        @Test
        @DisplayName("Should donate to campaign and update progress")
        void shouldDonateToCandidate() {
            // Given: Existing campaign
            Long campaignId = createTestCampaign(BigDecimal.valueOf(50000));

            // When: Donate to campaign
            Long memberId = createTestMember();
            String donationPayload = """
                {
                    "memberId": %d,
                    "campaignId": %d,
                    "amount": 5000.00,
                    "paymentMethod": "CASH",
                    "donationDate": "%s"
                }
                """.formatted(memberId, campaignId, LocalDate.now());

            given()
                .spec(authenticatedSpec(treasurerToken))
                .body(donationPayload)
                .post("/api/donations");

            // Then: Campaign progress should update
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/campaigns/" + campaignId)
            .then()
                .statusCode(200)
                .body("currentAmount", greaterThanOrEqualTo(5000.0f))
                .body("percentageComplete", greaterThan(0.0f));
        }

        @Test
        @DisplayName("Should get campaign donor list")
        void shouldGetCampaignDonors() {
            // Given: Campaign with donations
            Long campaignId = createTestCampaign(BigDecimal.valueOf(50000));
            donateToCampaign(campaignId, 1000);
            donateToCampaign(campaignId, 2000);

            // When: Get donor list
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/campaigns/" + campaignId + "/donors")
            .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(2));
        }

        @Test
        @DisplayName("Should close completed campaign")
        void shouldCloseCompletedCampaign() {
            // Given: Campaign that reached goal
            Long campaignId = createTestCampaign(BigDecimal.valueOf(10000));
            donateToCampaign(campaignId, 10000);

            // When: Close campaign
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .post("/api/campaigns/" + campaignId + "/close")
            .then()
                .statusCode(200)
                .body("status", equalTo("COMPLETED"));
        }
    }

    @Nested
    @DisplayName("Pledge Tests")
    class PledgeTests {

        @Test
        @DisplayName("Should create pledge")
        void shouldCreatePledge() {
            // Given: Pledge request
            Long memberId = createTestMember();
            String pledgePayload = """
                {
                    "memberId": %d,
                    "amount": 12000.00,
                    "frequency": "MONTHLY",
                    "duration": 12,
                    "startDate": "%s"
                }
                """.formatted(memberId, LocalDate.now());

            // When: Create pledge
            given()
                .spec(authenticatedSpec(treasurerToken))
                .body(pledgePayload)
            .when()
                .post("/api/pledges")
            .then()
                .statusCode(201)
                .body("amount", equalTo(12000.00f))
                .body("frequency", equalTo("MONTHLY"))
                .body("remainingBalance", equalTo(12000.00f));
        }

        @Test
        @DisplayName("Should record pledge payment and reduce balance")
        void shouldRecordPledgePayment() {
            // Given: Existing pledge
            Long pledgeId = createTestPledge(BigDecimal.valueOf(12000));

            // When: Record pledge payment
            String paymentPayload = """
                {
                    "pledgeId": %d,
                    "amount": 1000.00,
                    "paymentMethod": "CASH",
                    "paymentDate": "%s"
                }
                """.formatted(pledgeId, LocalDate.now());

            given()
                .spec(authenticatedSpec(treasurerToken))
                .body(paymentPayload)
                .post("/api/pledges/" + pledgeId + "/payments");

            // Then: Balance should reduce
            given()
                .spec(authenticatedSpec(treasurerToken))
            .when()
                .get("/api/pledges/" + pledgeId)
            .then()
                .statusCode(200)
                .body("remainingBalance", equalTo(11000.00f))
                .body("paidAmount", equalTo(1000.00f));
        }

        @Test
        @DisplayName("Should mark pledge as fulfilled when fully paid")
        void shouldMarkPledgeFulfilled() {
            // Given: Pledge with $1000 remaining
            Long pledgeId = createTestPledge(BigDecimal.valueOf(1000));

            // When: Pay full amount
            String paymentPayload = """
                {
                    "pledgeId": %d,
                    "amount": 1000.00,
                    "paymentMethod": "CASH",
                    "paymentDate": "%s"
                }
                """.formatted(pledgeId, LocalDate.now());

            given()
                .spec(authenticatedSpec(treasurerToken))
                .body(paymentPayload)
                .post("/api/pledges/" + pledgeId + "/payments");

            // Then: Status should be FULFILLED
            given()
                .spec(authenticatedSpec(treasurerToken))
            .when()
                .get("/api/pledges/" + pledgeId)
            .then()
                .statusCode(200)
                .body("status", equalTo("FULFILLED"))
                .body("remainingBalance", equalTo(0.0f));
        }
    }

    @Nested
    @DisplayName("Donation Analytics Tests")
    class DonationAnalyticsTests {

        @Test
        @DisplayName("Should get giving summary")
        void shouldGetGivingSummary() {
            // Given: Multiple donations
            recordDonation(BigDecimal.valueOf(1000));
            recordDonation(BigDecimal.valueOf(2000));
            recordDonation(BigDecimal.valueOf(500));

            // When: Get summary
            given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("startDate", LocalDate.now().minusDays(7))
                .queryParam("endDate", LocalDate.now())
            .when()
                .get("/api/donations/summary")
            .then()
                .statusCode(200)
                .body("totalAmount", greaterThanOrEqualTo(3500.0f))
                .body("transactionCount", greaterThanOrEqualTo(3));
        }

        @Test
        @DisplayName("Should get top donors list")
        void shouldGetTopDonors() {
            // Given: Donations from multiple members
            // (Implementation specific)

            // When: Get top donors
            given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("limit", 10)
            .when()
                .get("/api/donations/top-donors")
            .then()
                .statusCode(200)
                .body("size()", lessThanOrEqualTo(10));
        }

        @Test
        @DisplayName("Should export donation report")
        void shouldExportDonationReport() {
            // When: Export to CSV
            given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("format", "csv")
                .queryParam("startDate", LocalDate.now().minusMonths(1))
                .queryParam("endDate", LocalDate.now())
            .when()
                .get("/api/donations/export")
            .then()
                .statusCode(200)
                .header("Content-Type", containsString("csv"))
                .header("Content-Disposition", containsString("attachment"));
        }

        @Test
        @DisplayName("Should generate donation receipt PDF")
        void shouldGenerateDonationReceipt() {
            // Given: Existing donation
            Long donationId = recordDonation(BigDecimal.valueOf(500));

            // When: Generate receipt
            given()
                .spec(authenticatedSpec(treasurerToken))
            .when()
                .get("/api/donations/" + donationId + "/receipt")
            .then()
                .statusCode(200)
                .header("Content-Type", containsString("pdf"));
        }
    }

    @Nested
    @DisplayName("Multi-Tenancy Tests")
    class MultiTenancyTests {

        @Test
        @DisplayName("Should isolate donations by church")
        void shouldIsolateDonationsByChurch() {
            // Given: Two churches with donations
            Long church1 = createTestChurch("Church 1");
            Long church2 = createTestChurch("Church 2");
            String token1 = getTreasurerToken(church1);
            String token2 = getTreasurerToken(church2);

            recordDonationForChurch(church1, token1, 1000);
            recordDonationForChurch(church2, token2, 2000);

            // When: Church 1 gets donations
            given()
                .spec(authenticatedSpec(token1))
            .when()
                .get("/api/donations")
            .then()
                .statusCode(200)
                .body("content.findAll { it.amount == 2000 }.size()", equalTo(0)); // Church2's donation not visible
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {

        @Test
        @DisplayName("TREASURER should record donations")
        void treasurerShouldRecordDonations() {
            // Given: Treasurer token
            String donationPayload = """
                {
                    "amount": 100.00,
                    "donationType": "OFFERING",
                    "paymentMethod": "CASH",
                    "donationDate": "%s"
                }
                """.formatted(LocalDate.now());

            // When: Treasurer records donation
            given()
                .spec(authenticatedSpec(treasurerToken))
                .body(donationPayload)
            .when()
                .post("/api/donations")
            .then()
                .statusCode(201);
        }

        @Test
        @DisplayName("MEMBER should NOT record donations")
        void memberShouldNotRecordDonations() {
            // Given: Regular member token
            String memberToken = getMemberToken(churchId);
            String donationPayload = """
                {
                    "amount": 100.00,
                    "donationType": "OFFERING",
                    "paymentMethod": "CASH",
                    "donationDate": "%s"
                }
                """.formatted(LocalDate.now());

            // When: Member tries to record donation
            given()
                .spec(authenticatedSpec(memberToken))
                .body(donationPayload)
            .when()
                .post("/api/donations")
            .then()
                .statusCode(403);
        }

        @Test
        @DisplayName("ADMIN should view giving reports")
        void adminShouldViewReports() {
            // When: Admin gets summary
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/donations/summary")
            .then()
                .statusCode(200);
        }
    }

    // Helper Methods
    private Long createTestMember() {
        String payload = """
            {
                "firstName": "Test",
                "lastName": "Donor",
                "sex": "male",
                "phoneNumber": "+23324400%05d",
                "maritalStatus": "single"
            }
            """.formatted((int)(Math.random() * 100000));

        return Long.valueOf(given().spec(authenticatedSpec(adminToken))
            .body(payload)
            .post("/api/members")
            .jsonPath()
            .getInt("id"));
    }

    private Long createTestCampaign(BigDecimal goal) {
        String payload = """
            {
                "name": "Test Campaign",
                "goal": %s,
                "startDate": "%s",
                "endDate": "%s"
            }
            """.formatted(goal, LocalDate.now(), LocalDate.now().plusMonths(3));

        return Long.valueOf(given().spec(authenticatedSpec(adminToken))
            .body(payload)
            .post("/api/campaigns")
            .jsonPath()
            .getInt("id"));
    }

    private Long createTestPledge(BigDecimal amount) {
        Long memberId = createTestMember();
        String payload = """
            {
                "memberId": %d,
                "amount": %s,
                "frequency": "MONTHLY",
                "startDate": "%s"
            }
            """.formatted(memberId, amount, LocalDate.now());

        return Long.valueOf(given().spec(authenticatedSpec(treasurerToken))
            .body(payload)
            .post("/api/pledges")
            .jsonPath()
            .getInt("id"));
    }

    private Long recordDonation(BigDecimal amount) {
        Long memberId = createTestMember();
        String payload = """
            {
                "memberId": %d,
                "amount": %s,
                "donationType": "OFFERING",
                "paymentMethod": "CASH",
                "donationDate": "%s"
            }
            """.formatted(memberId, amount, LocalDate.now());

        return Long.valueOf(given().spec(authenticatedSpec(treasurerToken))
            .body(payload)
            .post("/api/donations")
            .jsonPath()
            .getInt("id"));
    }

    private void donateToCampaign(Long campaignId, double amount) {
        Long memberId = createTestMember();
        String payload = """
            {
                "memberId": %d,
                "campaignId": %d,
                "amount": %s,
                "paymentMethod": "CASH",
                "donationDate": "%s"
            }
            """.formatted(memberId, campaignId, amount, LocalDate.now());

        given().spec(authenticatedSpec(treasurerToken))
            .body(payload)
            .post("/api/donations");
    }

    private void recordDonationForChurch(Long churchId, String token, double amount) {
        String payload = """
            {
                "amount": %s,
                "donationType": "OFFERING",
                "paymentMethod": "CASH",
                "donationDate": "%s"
            }
            """.formatted(amount, LocalDate.now());

        given().spec(authenticatedSpec(token))
            .body(payload)
            .post("/api/donations");
    }
}
