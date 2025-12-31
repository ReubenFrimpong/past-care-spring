package com.reuben.pastcare_spring.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for reading and filtering application logs
 * Used by SUPERADMIN for troubleshooting and monitoring
 */
@Service
@Slf4j
public class LogService {

    private static final String LOG_FILE_PATH = "logs/application.log";
    private static final DateTimeFormatter LOG_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Get recent log entries with optional filtering
     *
     * @param limit Maximum number of log entries to return
     * @param level Filter by log level (ERROR, WARN, INFO, DEBUG, null for all)
     * @param searchKeyword Filter by keyword in log message
     * @return List of log entries (most recent first)
     */
    public List<LogEntry> getRecentLogs(int limit, String level, String searchKeyword) {
        log.debug("Fetching recent logs: limit={}, level={}, keyword={}", limit, level, searchKeyword);

        try {
            Path logPath = Paths.get(LOG_FILE_PATH);

            // If log file doesn't exist, return empty list
            if (!Files.exists(logPath)) {
                log.warn("Log file not found at: {}", LOG_FILE_PATH);
                return Collections.emptyList();
            }

            // Read all lines from log file
            List<String> allLines = Files.readAllLines(logPath);

            // Parse and filter log entries
            List<LogEntry> logEntries = new ArrayList<>();
            for (String line : allLines) {
                LogEntry entry = parseLogLine(line);
                if (entry != null) {
                    // Apply level filter
                    if (level != null && !level.equalsIgnoreCase("ALL") && !entry.getLevel().equalsIgnoreCase(level)) {
                        continue;
                    }

                    // Apply keyword filter
                    if (searchKeyword != null && !searchKeyword.isEmpty()) {
                        if (!entry.getMessage().toLowerCase().contains(searchKeyword.toLowerCase()) &&
                            !entry.getLogger().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            continue;
                        }
                    }

                    logEntries.add(entry);
                }
            }

            // Sort by timestamp descending (most recent first)
            logEntries.sort(Comparator.comparing(LogEntry::getTimestamp).reversed());

            // Apply limit
            if (limit > 0 && logEntries.size() > limit) {
                logEntries = logEntries.subList(0, limit);
            }

            log.debug("Returning {} log entries", logEntries.size());
            return logEntries;

        } catch (IOException e) {
            log.error("Error reading log file: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Parse a single log line into a LogEntry object
     * Expected format: "2025-12-30 20:15:32 [ERROR] LoggerName - Message"
     */
    private LogEntry parseLogLine(String line) {
        try {
            // Example log format: "2025-12-30 20:15:32 [ERROR] com.reuben.pastcare_spring.services.ChurchService - Failed to update church"

            if (line == null || line.trim().isEmpty()) {
                return null;
            }

            // Simple regex pattern for standard log format
            // Pattern: "YYYY-MM-DD HH:MM:SS [LEVEL] Logger - Message"
            String[] parts = line.split(" - ", 2);
            if (parts.length < 2) {
                // If doesn't match expected format, return as-is with minimal parsing
                return LogEntry.builder()
                        .timestamp(LocalDateTime.now())
                        .level("INFO")
                        .logger("Unknown")
                        .message(line)
                        .build();
            }

            String messageContent = parts[1];
            String headerPart = parts[0];

            // Extract timestamp (first 19 characters: "YYYY-MM-DD HH:MM:SS")
            LocalDateTime timestamp;
            try {
                String timestampStr = headerPart.substring(0, 19);
                timestamp = LocalDateTime.parse(timestampStr, LOG_DATE_FORMATTER);
            } catch (Exception e) {
                timestamp = LocalDateTime.now();
            }

            // Extract level from [LEVEL]
            String level = "INFO";
            int levelStart = headerPart.indexOf('[');
            int levelEnd = headerPart.indexOf(']');
            if (levelStart >= 0 && levelEnd > levelStart) {
                level = headerPart.substring(levelStart + 1, levelEnd).trim();
            }

            // Extract logger name (after "] ")
            String logger = "Unknown";
            if (levelEnd >= 0 && headerPart.length() > levelEnd + 2) {
                logger = headerPart.substring(levelEnd + 2).trim();
            }

            return LogEntry.builder()
                    .timestamp(timestamp)
                    .level(level)
                    .logger(logger)
                    .message(messageContent)
                    .build();

        } catch (Exception e) {
            log.warn("Failed to parse log line: {}", line, e);
            return null;
        }
    }

    /**
     * Get log statistics
     */
    public LogStats getLogStats() {
        try {
            Path logPath = Paths.get(LOG_FILE_PATH);

            if (!Files.exists(logPath)) {
                return LogStats.builder()
                        .totalLines(0)
                        .errorCount(0)
                        .warnCount(0)
                        .infoCount(0)
                        .debugCount(0)
                        .fileSizeKB(0L)
                        .build();
            }

            List<String> allLines = Files.readAllLines(logPath);
            long fileSize = Files.size(logPath) / 1024; // KB

            int errorCount = 0;
            int warnCount = 0;
            int infoCount = 0;
            int debugCount = 0;

            for (String line : allLines) {
                if (line.contains("[ERROR]")) errorCount++;
                else if (line.contains("[WARN]")) warnCount++;
                else if (line.contains("[INFO]")) infoCount++;
                else if (line.contains("[DEBUG]")) debugCount++;
            }

            return LogStats.builder()
                    .totalLines(allLines.size())
                    .errorCount(errorCount)
                    .warnCount(warnCount)
                    .infoCount(infoCount)
                    .debugCount(debugCount)
                    .fileSizeKB(fileSize)
                    .build();

        } catch (IOException e) {
            log.error("Error reading log stats: {}", e.getMessage(), e);
            return LogStats.builder()
                    .totalLines(0)
                    .errorCount(0)
                    .warnCount(0)
                    .infoCount(0)
                    .debugCount(0)
                    .fileSizeKB(0L)
                    .build();
        }
    }

    /**
     * LogEntry DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class LogEntry {
        private LocalDateTime timestamp;
        private String level;
        private String logger;
        private String message;
    }

    /**
     * LogStats DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class LogStats {
        private int totalLines;
        private int errorCount;
        private int warnCount;
        private int infoCount;
        private int debugCount;
        private long fileSizeKB;
    }
}
