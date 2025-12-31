package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.services.LogService;
import com.reuben.pastcare_spring.services.LogService.LogEntry;
import com.reuben.pastcare_spring.services.LogService.LogStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for log streaming and viewing
 * SUPERADMIN only - for platform monitoring and troubleshooting
 */
@RestController
@RequestMapping("/api/platform/logs")
@RequiredArgsConstructor
@Slf4j
public class LogStreamingController {

    private final LogService logService;

    /**
     * Get recent log entries with optional filtering
     *
     * @param limit Maximum number of logs to return (default 1000)
     * @param level Filter by log level (ERROR, WARN, INFO, DEBUG, ALL)
     * @param search Search keyword in log messages
     * @return List of log entries (most recent first)
     */
    @GetMapping("/recent")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<List<LogEntry>> getRecentLogs(
            @RequestParam(defaultValue = "1000") int limit,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String search
    ) {
        log.info("Fetching recent logs: limit={}, level={}, search={}", limit, level, search);

        // Enforce maximum limit
        if (limit > 10000) {
            limit = 10000; // Prevent excessive memory usage
        }

        List<LogEntry> logs = logService.getRecentLogs(limit, level, search);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get log statistics
     *
     * @return Log statistics (total lines, counts by level, file size)
     */
    @GetMapping("/stats")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<LogStats> getLogStats() {
        log.info("Fetching log statistics");
        LogStats stats = logService.getLogStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Search logs by keyword
     *
     * @param keyword Search keyword
     * @param limit Maximum results (default 100)
     * @return Matching log entries
     */
    @GetMapping("/search")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<List<LogEntry>> searchLogs(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "100") int limit
    ) {
        log.info("Searching logs for keyword: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<LogEntry> logs = logService.getRecentLogs(limit, null, keyword);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get error logs only (last 500)
     *
     * @return Error log entries
     */
    @GetMapping("/errors")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<List<LogEntry>> getErrorLogs() {
        log.info("Fetching error logs");
        List<LogEntry> logs = logService.getRecentLogs(500, "ERROR", null);
        return ResponseEntity.ok(logs);
    }

    /**
     * Get warning logs only (last 500)
     *
     * @return Warning log entries
     */
    @GetMapping("/warnings")
    @RequirePermission(Permission.PLATFORM_VIEW_ALL_CHURCHES)
    public ResponseEntity<List<LogEntry>> getWarningLogs() {
        log.info("Fetching warning logs");
        List<LogEntry> logs = logService.getRecentLogs(500, "WARN", null);
        return ResponseEntity.ok(logs);
    }
}
