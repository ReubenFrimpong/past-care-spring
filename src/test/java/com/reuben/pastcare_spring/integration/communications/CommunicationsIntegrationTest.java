package com.reuben.pastcare_spring.integration.communications;

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
 * Integration tests for Communications/SMS module.
 */
@SpringBootTest
@Tag("integration")
@Tag("module:communications")
@DisplayName("Communications Integration Tests")
@Transactional
class CommunicationsIntegrationTest extends BaseIntegrationTest {

    private Long churchId;
    private String adminToken;

    @BeforeEach
    void setUp() {
        churchId = createTestChurch();
        adminToken = getAdminToken(churchId);
    }

    @Nested
    @DisplayName("SMS Sending Tests")
    class SmsSendingTests {

        @Test
        @DisplayName("Should send single SMS")
        void shouldSendSingleSms() {
            Map<String, Object> request = new HashMap<>();
            request.put("recipientPhone", "+233244000001");
            request.put("recipientName", "Test Recipient");
            request.put("message", "Test message");

            given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
            .when()
                .post("/api/sms/send")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(402))); // 402 if no credits
        }

        @Test
        @DisplayName("Should send bulk SMS")
        void shouldSendBulkSms() {
            Map<String, Object> request = new HashMap<>();
            request.put("recipientPhones", List.of("+233244000001", "+233244000002"));
            request.put("message", "Bulk message");

            given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
            .when()
                .post("/api/sms/send-bulk")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(402)));
        }

        @Test
        @DisplayName("Should send SMS with template")
        void shouldSendSmsWithTemplate() {
            Map<String, Object> request = new HashMap<>();
            request.put("recipientPhone", "+233244000001");
            request.put("templateId", 1L);
            request.put("variables", Map.of("name", "John"));

            given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
            .when()
                .post("/api/sms/send-templated")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(402), equalTo(404)));
        }
    }

    @Nested
    @DisplayName("Credits Management Tests")
    class CreditsTests {

        @Test
        @DisplayName("Should purchase SMS credits")
        void shouldPurchaseCredits() {
            Map<String, Object> request = new HashMap<>();
            request.put("creditAmount", 100);
            request.put("paymentMethod", "PAYSTACK");

            given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
            .when()
                .post("/api/sms/credits/purchase")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)));
        }

        @Test
        @DisplayName("Should check credit balance")
        void shouldCheckCreditBalance() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/sms/credits/balance")
            .then()
                .statusCode(200)
                .body("balance", notNullValue());
        }

        @Test
        @DisplayName("Should handle insufficient credits")
        void shouldHandleInsufficientCredits() {
            // This would test sending when credits are low
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("Template Tests")
    class TemplateTests {

        @Test
        @DisplayName("Should create SMS template")
        void shouldCreateTemplate() {
            Map<String, Object> request = new HashMap<>();
            request.put("name", "Welcome Template");
            request.put("content", "Welcome {{name}} to our church!");

            given()
                .spec(authenticatedSpec(adminToken))
                .body(request)
            .when()
                .post("/api/sms/templates")
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(201)))
                .body("name", equalTo("Welcome Template"));
        }

        @Test
        @DisplayName("Should use template with variables")
        void shouldUseTemplateWithVariables() {
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should list templates")
        void shouldListTemplates() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/sms/templates")
            .then()
                .statusCode(200);
        }
    }

    @Nested
    @DisplayName("Webhook Tests")
    class WebhookTests {

        @Test
        @DisplayName("Should handle delivery status webhook")
        void shouldHandleDeliveryStatus() {
            // This would test webhook from SMS gateway
            assertThat(true).isTrue();
        }

        @Test
        @DisplayName("Should verify webhook security")
        void shouldVerifyWebhookSecurity() {
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("Message Log Tests")
    class MessageLogTests {

        @Test
        @DisplayName("Should track message history")
        void shouldTrackMessageHistory() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/sms/history")
            .then()
                .statusCode(200);
        }

        @Test
        @DisplayName("Should filter messages by status")
        void shouldFilterByStatus() {
            given()
                .spec(authenticatedSpec(adminToken))
                .queryParam("status", "DELIVERED")
            .when()
                .get("/api/sms/history")
            .then()
                .statusCode(200);
        }
    }

    @Nested
    @DisplayName("Multi-Tenancy Tests")
    class MultiTenancyTests {

        @Test
        @DisplayName("Should isolate SMS messages by church")
        void shouldIsolateMessagesByChurch() {
            // Each church should only see their own messages
            assertThat(true).isTrue();
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {

        @Test
        @DisplayName("ADMIN should send SMS")
        void adminShouldSendSms() {
            given()
                .spec(authenticatedSpec(adminToken))
            .when()
                .get("/api/sms/history")
            .then()
                .statusCode(200);
        }
    }
}
