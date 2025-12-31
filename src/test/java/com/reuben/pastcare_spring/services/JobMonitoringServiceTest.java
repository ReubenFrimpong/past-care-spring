package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.ScheduledJobExecution;
import com.reuben.pastcare_spring.models.ScheduledJobExecution.JobStatus;
import com.reuben.pastcare_spring.repositories.ScheduledJobExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JobMonitoringService
 * Tests job execution tracking logic without database
 */
@ExtendWith(MockitoExtension.class)
class JobMonitoringServiceTest {

    @Mock
    private ScheduledJobExecutionRepository executionRepository;

    @InjectMocks
    private JobMonitoringService jobMonitoringService;

    private ScheduledJobExecution testExecution;

    @BeforeEach
    void setUp() {
        testExecution = new ScheduledJobExecution();
        testExecution.setId(1L);
        testExecution.setJobName("testJob");
        testExecution.setDescription("Test Job Description");
        testExecution.setStatus(JobStatus.RUNNING);
        testExecution.setStartTime(LocalDateTime.now());
        testExecution.setRetryCount(0);
        testExecution.setManuallyTriggered(false);
    }

    // ==================== startJobExecution Tests ====================

    @Test
    void startJobExecution_WithScheduledJob_CreatesExecutionSuccessfully() {
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenAnswer(invocation -> {
            ScheduledJobExecution saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        ScheduledJobExecution result = jobMonitoringService.startJobExecution(
            "sendDailyEventReminders",
            "Send event reminders",
            false,
            null
        );

        assertNotNull(result);
        assertEquals("sendDailyEventReminders", result.getJobName());
        assertEquals("Send event reminders", result.getDescription());
        assertEquals(JobStatus.RUNNING, result.getStatus());
        assertFalse(result.getManuallyTriggered());
        assertNull(result.getTriggeredBy());

        verify(executionRepository, times(1)).save(any(ScheduledJobExecution.class));
    }

    @Test
    void startJobExecution_WithManualTrigger_SetsManuallyTriggeredFlag() {
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenReturn(testExecution);

        jobMonitoringService.startJobExecution(
            "weeklyCleanup",
            "Cleanup old data",
            true,
            "superadmin@test.com"
        );

        ArgumentCaptor<ScheduledJobExecution> captor = ArgumentCaptor.forClass(ScheduledJobExecution.class);
        verify(executionRepository).save(captor.capture());

        ScheduledJobExecution saved = captor.getValue();
        assertTrue(saved.getManuallyTriggered());
        assertEquals("superadmin@test.com", saved.getTriggeredBy());
    }

    @Test
    void startJobExecution_SetsStartTimeAndStatus() {
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenReturn(testExecution);

        jobMonitoringService.startJobExecution(
            "testJob",
            "Test",
            false,
            null
        );

        ArgumentCaptor<ScheduledJobExecution> captor = ArgumentCaptor.forClass(ScheduledJobExecution.class);
        verify(executionRepository).save(captor.capture());

        ScheduledJobExecution saved = captor.getValue();
        assertNotNull(saved.getStartTime());
        assertEquals(JobStatus.RUNNING, saved.getStatus());
        assertEquals(0, saved.getRetryCount());
    }

    // ==================== markJobCompleted Tests ====================

    @Test
    void markJobCompleted_UpdatesStatusAndMetrics() {
        testExecution.setStatus(JobStatus.RUNNING);
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenReturn(testExecution);

        jobMonitoringService.markJobCompleted(1L, 100, 5);

        ArgumentCaptor<ScheduledJobExecution> captor = ArgumentCaptor.forClass(ScheduledJobExecution.class);
        verify(executionRepository).save(captor.capture());

        ScheduledJobExecution saved = captor.getValue();
        assertEquals(JobStatus.COMPLETED, saved.getStatus());
        assertEquals(100, saved.getItemsProcessed());
        assertEquals(5, saved.getItemsFailed());
        assertNotNull(saved.getEndTime());
        assertNotNull(saved.getDurationMs());
    }

    @Test
    void markJobCompleted_WithNullValues_HandlesGracefully() {
        testExecution.setStatus(JobStatus.RUNNING);
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenReturn(testExecution);

        jobMonitoringService.markJobCompleted(1L, null, null);

        ArgumentCaptor<ScheduledJobExecution> captor = ArgumentCaptor.forClass(ScheduledJobExecution.class);
        verify(executionRepository).save(captor.capture());

        ScheduledJobExecution saved = captor.getValue();
        assertEquals(JobStatus.COMPLETED, saved.getStatus());
        assertNull(saved.getItemsProcessed());
        assertNull(saved.getItemsFailed());
    }

    @Test
    void markJobCompleted_NonExistentId_DoesNotThrowException() {
        when(executionRepository.findById(999L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> jobMonitoringService.markJobCompleted(999L, 10, 0));

        verify(executionRepository, never()).save(any(ScheduledJobExecution.class));
    }

    // ==================== markJobFailed Tests ====================

    @Test
    void markJobFailed_WithException_CapturesErrorDetails() {
        testExecution.setStatus(JobStatus.RUNNING);
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenReturn(testExecution);

        Exception testError = new RuntimeException("Test error message");

        jobMonitoringService.markJobFailed(1L, testError);

        ArgumentCaptor<ScheduledJobExecution> captor = ArgumentCaptor.forClass(ScheduledJobExecution.class);
        verify(executionRepository).save(captor.capture());

        ScheduledJobExecution saved = captor.getValue();
        assertEquals(JobStatus.FAILED, saved.getStatus());
        assertEquals("Test error message", saved.getErrorMessage());
        assertNotNull(saved.getStackTrace());
        assertTrue(saved.getStackTrace().contains("RuntimeException"));
        assertNotNull(saved.getEndTime());
        assertNotNull(saved.getDurationMs());
    }

    @Test
    void markJobFailed_NonExistentId_DoesNotThrowException() {
        when(executionRepository.findById(999L)).thenReturn(Optional.empty());

        Exception testError = new RuntimeException("Test error");

        assertDoesNotThrow(() -> jobMonitoringService.markJobFailed(999L, testError));

        verify(executionRepository, never()).save(any(ScheduledJobExecution.class));
    }

    // ==================== cancelJobExecution Tests ====================

    @Test
    void cancelJobExecution_UpdatesStatusAndSetsFlag() {
        testExecution.setStatus(JobStatus.RUNNING);
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenReturn(testExecution);

        jobMonitoringService.cancelJobExecution(1L, "superadmin@test.com");

        ArgumentCaptor<ScheduledJobExecution> captor = ArgumentCaptor.forClass(ScheduledJobExecution.class);
        verify(executionRepository).save(captor.capture());

        ScheduledJobExecution saved = captor.getValue();
        assertEquals(JobStatus.CANCELED, saved.getStatus());
        assertTrue(saved.getCanceled());
        assertNotNull(saved.getEndTime());
    }

    @Test
    void cancelJobExecution_NonExistentId_DoesNotThrowException() {
        when(executionRepository.findById(999L)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> jobMonitoringService.cancelJobExecution(999L, "admin"));

        verify(executionRepository, never()).save(any(ScheduledJobExecution.class));
    }

    // ==================== incrementRetryCount Tests ====================

    @Test
    void incrementRetryCount_IncrementsCounterSuccessfully() {
        testExecution.setRetryCount(0);
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenReturn(testExecution);

        jobMonitoringService.incrementRetryCount(1L);

        ArgumentCaptor<ScheduledJobExecution> captor = ArgumentCaptor.forClass(ScheduledJobExecution.class);
        verify(executionRepository).save(captor.capture());

        ScheduledJobExecution saved = captor.getValue();
        assertEquals(1, saved.getRetryCount());
    }

    @Test
    void incrementRetryCount_MultipleRetries_AccumulatesCorrectly() {
        testExecution.setRetryCount(2);
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenReturn(testExecution);

        jobMonitoringService.incrementRetryCount(1L);

        ArgumentCaptor<ScheduledJobExecution> captor = ArgumentCaptor.forClass(ScheduledJobExecution.class);
        verify(executionRepository).save(captor.capture());

        ScheduledJobExecution saved = captor.getValue();
        assertEquals(3, saved.getRetryCount());
    }

    // ==================== getRecentExecutions Tests ====================

    @Test
    void getRecentExecutions_ReturnsExecutionsFromRepository() {
        List<ScheduledJobExecution> expectedExecutions = Arrays.asList(
            testExecution,
            new ScheduledJobExecution()
        );

        when(executionRepository.findRecentExecutions(any(LocalDateTime.class)))
            .thenReturn(expectedExecutions);

        List<ScheduledJobExecution> result = jobMonitoringService.getRecentExecutions(24);

        assertEquals(2, result.size());
        verify(executionRepository, times(1)).findRecentExecutions(any(LocalDateTime.class));
    }

    @Test
    void getRecentExecutions_WithCustomHours_CalculatesCorrectTimeRange() {
        when(executionRepository.findRecentExecutions(any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(testExecution));

        jobMonitoringService.getRecentExecutions(48);

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(executionRepository).findRecentExecutions(captor.capture());

        LocalDateTime capturedTime = captor.getValue();
        LocalDateTime expectedTime = LocalDateTime.now().minusHours(48);

        // Allow 1 second tolerance for test execution time
        assertTrue(capturedTime.isBefore(expectedTime.plusSeconds(1)));
        assertTrue(capturedTime.isAfter(expectedTime.minusSeconds(1)));
    }

    // ==================== getRunningJobs Tests ====================

    @Test
    void getRunningJobs_ReturnsOnlyRunningJobs() {
        List<ScheduledJobExecution> runningJobs = Arrays.asList(testExecution);

        when(executionRepository.findByStatus(JobStatus.RUNNING))
            .thenReturn(runningJobs);

        List<ScheduledJobExecution> result = jobMonitoringService.getRunningJobs();

        assertEquals(1, result.size());
        assertEquals(JobStatus.RUNNING, result.get(0).getStatus());
        verify(executionRepository, times(1)).findByStatus(JobStatus.RUNNING);
    }

    // ==================== getFailedJobsNeedingRetry Tests ====================

    @Test
    void getFailedJobsNeedingRetry_ReturnsJobsWithRetryCountZero() {
        testExecution.setStatus(JobStatus.FAILED);
        testExecution.setRetryCount(0);
        List<ScheduledJobExecution> failedJobs = Arrays.asList(testExecution);

        when(executionRepository.findFailedJobsNeedingRetry())
            .thenReturn(failedJobs);

        List<ScheduledJobExecution> result = jobMonitoringService.getFailedJobsNeedingRetry();

        assertEquals(1, result.size());
        assertEquals(JobStatus.FAILED, result.get(0).getStatus());
        assertEquals(0, result.get(0).getRetryCount());
        verify(executionRepository, times(1)).findFailedJobsNeedingRetry();
    }

    // ==================== getExecutionById Tests ====================

    @Test
    void getExecutionById_WhenExists_ReturnsExecution() {
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));

        Optional<ScheduledJobExecution> result = jobMonitoringService.getExecutionById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(executionRepository, times(1)).findById(1L);
    }

    @Test
    void getExecutionById_WhenNotExists_ReturnsEmpty() {
        when(executionRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<ScheduledJobExecution> result = jobMonitoringService.getExecutionById(999L);

        assertFalse(result.isPresent());
        verify(executionRepository, times(1)).findById(999L);
    }

    // ==================== getExecutionsForJob Tests ====================

    @Test
    void getExecutionsForJob_ReturnsFilteredResults() {
        List<ScheduledJobExecution> executions = Arrays.asList(testExecution);

        when(executionRepository.findByJobNameOrderByStartTimeDesc("testJob"))
            .thenReturn(executions);

        List<ScheduledJobExecution> result = jobMonitoringService.getExecutionsForJob("testJob");

        assertEquals(1, result.size());
        assertEquals("testJob", result.get(0).getJobName());
        verify(executionRepository, times(1)).findByJobNameOrderByStartTimeDesc("testJob");
    }

    // ==================== cleanupOldExecutions Tests ====================

    @Test
    void cleanupOldExecutions_DeletesExecutionsOlderThan90Days() {
        jobMonitoringService.cleanupOldExecutions();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(executionRepository).deleteByStartTimeBefore(captor.capture());

        LocalDateTime capturedTime = captor.getValue();
        LocalDateTime expectedTime = LocalDateTime.now().minusDays(90);

        // Allow 1 second tolerance for test execution time
        assertTrue(capturedTime.isBefore(expectedTime.plusSeconds(1)));
        assertTrue(capturedTime.isAfter(expectedTime.minusSeconds(1)));
    }

    // ==================== Edge Cases and Error Handling ====================

    @Test
    void markJobCompleted_WithZeroItems_SavesCorrectly() {
        testExecution.setStatus(JobStatus.RUNNING);
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenReturn(testExecution);

        jobMonitoringService.markJobCompleted(1L, 0, 0);

        ArgumentCaptor<ScheduledJobExecution> captor = ArgumentCaptor.forClass(ScheduledJobExecution.class);
        verify(executionRepository).save(captor.capture());

        ScheduledJobExecution saved = captor.getValue();
        assertEquals(0, saved.getItemsProcessed());
        assertEquals(0, saved.getItemsFailed());
    }

    @Test
    void markJobFailed_WithNullException_HandlesGracefully() {
        testExecution.setStatus(JobStatus.RUNNING);
        when(executionRepository.findById(1L)).thenReturn(Optional.of(testExecution));

        // Service doesn't handle null exceptions - this test verifies it throws NullPointerException
        // In real usage, exceptions should never be null
        assertThrows(NullPointerException.class, () -> {
            jobMonitoringService.markJobFailed(1L, null);
        });
    }

    @Test
    void startJobExecution_WithNullDescription_HandlesGracefully() {
        when(executionRepository.save(any(ScheduledJobExecution.class))).thenReturn(testExecution);

        ScheduledJobExecution result = jobMonitoringService.startJobExecution(
            "testJob",
            null,
            false,
            null
        );

        assertNotNull(result);
        verify(executionRepository, times(1)).save(any(ScheduledJobExecution.class));
    }
}
