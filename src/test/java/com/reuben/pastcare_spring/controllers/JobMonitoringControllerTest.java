package com.reuben.pastcare_spring.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.models.ScheduledJobExecution;
import com.reuben.pastcare_spring.models.ScheduledJobExecution.JobStatus;
import com.reuben.pastcare_spring.repositories.ScheduledJobExecutionRepository;
import com.reuben.pastcare_spring.services.JobMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for JobMonitoringController
 * Tests all job monitoring endpoints with proper authorization
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class JobMonitoringControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ScheduledJobExecutionRepository executionRepository;

    @Autowired
    private JobMonitoringService jobMonitoringService;

    private ScheduledJobExecution testExecution;

    @BeforeEach
    void setUp() {
        executionRepository.deleteAll();

        // Create a test job execution
        testExecution = jobMonitoringService.startJobExecution(
            "sendDailyEventReminders",
            "Send event reminders for upcoming events",
            false,
            null
        );
    }

    // ==================== GET /api/platform/jobs/available ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getAvailableJobs_AsSuperAdmin_ReturnsAllJobs() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/available"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(6)))
            .andExpect(jsonPath("$[0].jobName", notNullValue()))
            .andExpect(jsonPath("$[0].displayName", notNullValue()))
            .andExpect(jsonPath("$[0].description", notNullValue()))
            .andExpect(jsonPath("$[0].cronExpression", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAvailableJobs_AsAdmin_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/available"))
            .andExpect(status().isForbidden());
    }

    @Test
    void getAvailableJobs_Unauthenticated_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/available"))
            .andExpect(status().isUnauthorized());
    }

    // ==================== GET /api/platform/jobs/executions ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getRecentExecutions_AsSuperAdmin_ReturnsExecutions() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/executions")
                .param("hoursBack", "24"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
            .andExpect(jsonPath("$[0].id", notNullValue()))
            .andExpect(jsonPath("$[0].jobName", is("sendDailyEventReminders")))
            .andExpect(jsonPath("$[0].status", is("RUNNING")));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getRecentExecutions_WithCustomHours_ReturnsCorrectExecutions() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/executions")
                .param("hoursBack", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @WithMockUser(roles = "PASTOR")
    void getRecentExecutions_AsPastor_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/executions"))
            .andExpect(status().isForbidden());
    }

    // ==================== GET /api/platform/jobs/running ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getRunningJobs_AsSuperAdmin_ReturnsRunningJobs() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/running"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].status", is("RUNNING")))
            .andExpect(jsonPath("$[0].endTime", nullValue()));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getRunningJobs_WhenNoRunningJobs_ReturnsEmptyList() throws Exception {
        // Mark the job as completed
        jobMonitoringService.markJobCompleted(testExecution.getId(), 10, 0);

        mockMvc.perform(get("/api/platform/jobs/running"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== GET /api/platform/jobs/failed ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getFailedJobs_AsSuperAdmin_ReturnsFailedJobs() throws Exception {
        // Mark the job as failed
        Exception testError = new RuntimeException("Test error");
        jobMonitoringService.markJobFailed(testExecution.getId(), testError);

        mockMvc.perform(get("/api/platform/jobs/failed"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].status", is("FAILED")))
            .andExpect(jsonPath("$[0].errorMessage", is("Test error")))
            .andExpect(jsonPath("$[0].retryCount", is(0)));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getFailedJobs_ExcludesRetriedJobs_ReturnsOnlyNonRetriedFailures() throws Exception {
        // Create a failed job with retry count > 0
        ScheduledJobExecution retriedExecution = jobMonitoringService.startJobExecution(
            "weeklyCleanup",
            "Cleanup old data",
            false,
            null
        );
        jobMonitoringService.markJobFailed(retriedExecution.getId(), new RuntimeException("Error"));
        jobMonitoringService.incrementRetryCount(retriedExecution.getId());

        // Create a failed job with retry count = 0
        ScheduledJobExecution failedExecution = jobMonitoringService.startJobExecution(
            "processSubscriptionRenewals",
            "Process renewals",
            false,
            null
        );
        jobMonitoringService.markJobFailed(failedExecution.getId(), new RuntimeException("Error"));

        mockMvc.perform(get("/api/platform/jobs/failed"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].jobName", is("processSubscriptionRenewals")));
    }

    // ==================== GET /api/platform/jobs/executions/{id} ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getExecutionDetails_AsSuperAdmin_ReturnsFullDetails() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/executions/" + testExecution.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(testExecution.getId().intValue())))
            .andExpect(jsonPath("$.jobName", is("sendDailyEventReminders")))
            .andExpect(jsonPath("$.description", notNullValue()))
            .andExpect(jsonPath("$.status", is("RUNNING")))
            .andExpect(jsonPath("$.startTime", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getExecutionDetails_WithFailedJob_ReturnsErrorDetails() throws Exception {
        Exception testError = new RuntimeException("Test failure");
        jobMonitoringService.markJobFailed(testExecution.getId(), testError);

        mockMvc.perform(get("/api/platform/jobs/executions/" + testExecution.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("FAILED")))
            .andExpect(jsonPath("$.errorMessage", is("Test failure")))
            .andExpect(jsonPath("$.stackTrace", notNullValue()));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getExecutionDetails_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/executions/999999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "TREASURER")
    void getExecutionDetails_AsTreasurer_ReturnsForbidden() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/executions/" + testExecution.getId()))
            .andExpect(status().isForbidden());
    }

    // ==================== POST /api/platform/jobs/execute ====================

    @Test
    @WithMockUser(username = "superadmin@test.com", roles = "SUPERADMIN")
    void executeJob_AsSuperAdmin_StartsJobSuccessfully() throws Exception {
        Map<String, String> request = Map.of("jobName", "weeklyCleanup");

        mockMvc.perform(post("/api/platform/jobs/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("successfully")));

        // Verify job execution was tracked
        var executions = executionRepository.findByJobNameOrderByStartTimeDesc("weeklyCleanup");
        assert executions.size() > 0;
        assert executions.get(0).getManuallyTriggered();
        assert executions.get(0).getTriggeredBy().equals("superadmin@test.com");
    }

    @Test
    @WithMockUser(username = "superadmin@test.com", roles = "SUPERADMIN")
    void executeJob_WithInvalidJobName_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of("jobName", "invalidJobName");

        mockMvc.perform(post("/api/platform/jobs/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void executeJob_AsAdmin_ReturnsForbidden() throws Exception {
        Map<String, String> request = Map.of("jobName", "weeklyCleanup");

        mockMvc.perform(post("/api/platform/jobs/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    // ==================== POST /api/platform/jobs/executions/{id}/retry ====================

    @Test
    @WithMockUser(username = "superadmin@test.com", roles = "SUPERADMIN")
    void retryFailedJob_AsSuperAdmin_StartsRetrySuccessfully() throws Exception {
        // Mark the job as failed
        jobMonitoringService.markJobFailed(testExecution.getId(), new RuntimeException("Test error"));

        mockMvc.perform(post("/api/platform/jobs/executions/" + testExecution.getId() + "/retry"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("retry")));

        // Verify retry count was incremented
        var execution = executionRepository.findById(testExecution.getId()).orElseThrow();
        assert execution.getRetryCount() == 1;
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void retryFailedJob_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/platform/jobs/executions/999999/retry"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MEMBER_MANAGER")
    void retryFailedJob_AsMemberManager_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/platform/jobs/executions/" + testExecution.getId() + "/retry"))
            .andExpect(status().isForbidden());
    }

    // ==================== POST /api/platform/jobs/executions/{id}/cancel ====================

    @Test
    @WithMockUser(username = "superadmin@test.com", roles = "SUPERADMIN")
    void cancelJob_AsSuperAdmin_CancelsSuccessfully() throws Exception {
        mockMvc.perform(post("/api/platform/jobs/executions/" + testExecution.getId() + "/cancel"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("canceled")));

        // Verify job was marked as canceled
        var execution = executionRepository.findById(testExecution.getId()).orElseThrow();
        assert execution.getStatus() == JobStatus.CANCELED;
        assert execution.getCanceled();
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void cancelJob_NonExistentId_ReturnsNotFound() throws Exception {
        mockMvc.perform(post("/api/platform/jobs/executions/999999/cancel"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "FELLOWSHIP_LEADER")
    void cancelJob_AsFellowshipLeader_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/api/platform/jobs/executions/" + testExecution.getId() + "/cancel"))
            .andExpect(status().isForbidden());
    }

    // ==================== GET /api/platform/jobs/executions/by-job/{jobName} ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getExecutionsByJobName_AsSuperAdmin_ReturnsJobExecutions() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/executions/by-job/sendDailyEventReminders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].jobName", is("sendDailyEventReminders")));
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void getExecutionsByJobName_WithNonExistentJob_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/executions/by-job/nonExistentJob"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    // ==================== Authorization Tests for All Roles ====================

    @Test
    @WithMockUser(roles = "MEMBER")
    void allEndpoints_AsMember_ReturnForbidden() throws Exception {
        // Test all GET endpoints
        mockMvc.perform(get("/api/platform/jobs/available"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/platform/jobs/executions"))
            .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/platform/jobs/running"))
            .andExpect(status().isForbidden());

        // Test all POST endpoints
        Map<String, String> request = Map.of("jobName", "weeklyCleanup");
        mockMvc.perform(post("/api/platform/jobs/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }

    @Test
    void allEndpoints_Unauthenticated_ReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/platform/jobs/available"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/platform/jobs/executions"))
            .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/platform/jobs/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("jobName", "test"))))
            .andExpect(status().isUnauthorized());
    }

    // ==================== Data Validation Tests ====================

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void executeJob_WithNullJobName_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/platform/jobs/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPERADMIN")
    void executeJob_WithEmptyJobName_ReturnsBadRequest() throws Exception {
        Map<String, String> request = Map.of("jobName", "");

        mockMvc.perform(post("/api/platform/jobs/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
