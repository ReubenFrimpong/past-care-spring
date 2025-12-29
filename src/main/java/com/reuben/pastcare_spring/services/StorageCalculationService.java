package com.reuben.pastcare_spring.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.StorageUsage;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Service to calculate and track storage usage for each church.
 *
 * Storage is calculated as:
 * 1. File Storage: Profile photos, event images, documents, attachments
 * 2. Database Storage: Estimated based on row counts and average row sizes
 *
 * Runs daily at 2 AM to update storage usage for all churches.
 */
@Service
@RequiredArgsConstructor
public class StorageCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(StorageCalculationService.class);

    private final ChurchRepository churchRepository;
    private final StorageUsageRepository storageUsageRepository;
    private final MemberRepository memberRepository;
    private final DonationRepository donationRepository;
    private final EventRepository eventRepository;
    private final VisitRepository visitRepository;
    private final HouseholdRepository householdRepository;
    private final AttendanceSessionRepository attendanceSessionRepository;
    private final CampaignRepository campaignRepository;
    private final FellowshipRepository fellowshipRepository;
    private final CareNeedRepository careNeedRepository;
    private final PrayerRequestRepository prayerRequestRepository;
    private final VisitorRepository visitorRepository;

    private final ObjectMapper objectMapper;

    /**
     * Estimated average row sizes in bytes for each entity type.
     * Based on typical field sizes (IDs, strings, dates, etc.)
     */
    private static final Map<String, Integer> ENTITY_SIZE_ESTIMATES = Map.ofEntries(
            Map.entry("members", 1024),              // 1 KB per member (name, email, phone, dates, etc.)
            Map.entry("donations", 512),             // 512 bytes per donation (amount, type, date, campaign)
            Map.entry("events", 2048),               // 2 KB per event (title, description, location, dates)
            Map.entry("visits", 1024),               // 1 KB per visit (member, notes, date, type)
            Map.entry("households", 768),            // 768 bytes per household (name, location, contacts)
            Map.entry("attendance_sessions", 512),   // 512 bytes per session (event, date, stats)
            Map.entry("attendance_records", 128),    // 128 bytes per attendance record (member, session, status)
            Map.entry("campaigns", 1024),            // 1 KB per campaign (name, goal, dates, description)
            Map.entry("pledges", 256),               // 256 bytes per pledge (amount, member, campaign, date)
            Map.entry("fellowships", 512),           // 512 bytes per fellowship (name, leader, description)
            Map.entry("care_needs", 768),            // 768 bytes per care need (member, type, notes, status)
            Map.entry("prayer_requests", 512),       // 512 bytes per prayer request (content, member, date)
            Map.entry("visitors", 512),              // 512 bytes per visitor (name, contact, visit date)
            Map.entry("users", 384),                 // 384 bytes per user (email, password hash, role)
            Map.entry("sms_messages", 256),          // 256 bytes per SMS (phone, message, status)
            Map.entry("event_images", 128)           // 128 bytes per image metadata (path stored, not image)
    );

    /**
     * Scheduled job to calculate storage for all churches.
     * Runs daily at 2 AM.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void calculateStorageForAllChurches() {
        logger.info("Starting scheduled storage calculation for all churches");

        List<Church> churches = churchRepository.findAll();
        int successCount = 0;
        int failureCount = 0;

        for (Church church : churches) {
            try {
                calculateAndSaveStorageUsage(church.getId());
                successCount++;
                logger.debug("Storage calculated for church {} ({})", church.getName(), church.getId());
            } catch (Exception e) {
                failureCount++;
                logger.error("Failed to calculate storage for church {} ({}): {}",
                        church.getName(), church.getId(), e.getMessage(), e);
            }
        }

        logger.info("Completed storage calculation: {} succeeded, {} failed", successCount, failureCount);

        // Clean up old storage records (keep only last 90 days)
        cleanupOldStorageRecords();
    }

    /**
     * Calculate and save storage usage for a specific church.
     * Can be called manually or by scheduled job.
     */
    @Transactional
    public StorageUsage calculateAndSaveStorageUsage(Long churchId) {
        logger.debug("Calculating storage for church {}", churchId);

        // Calculate file storage
        Map<String, Double> fileBreakdown = calculateFileStorage(churchId);
        double fileStorageMb = fileBreakdown.values().stream().mapToDouble(Double::doubleValue).sum();

        // Calculate database storage
        Map<String, Double> dbBreakdown = calculateDatabaseStorage(churchId);
        double dbStorageMb = dbBreakdown.values().stream().mapToDouble(Double::doubleValue).sum();

        // Create storage usage record
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found: " + churchId));

        StorageUsage storageUsage = StorageUsage.builder()
                .church(church)
                .fileStorageMb(fileStorageMb)
                .databaseStorageMb(dbStorageMb)
                .totalStorageMb(fileStorageMb + dbStorageMb)
                .fileStorageBreakdown(toJson(fileBreakdown))
                .databaseStorageBreakdown(toJson(dbBreakdown))
                .calculatedAt(LocalDateTime.now())
                .build();

        storageUsage = storageUsageRepository.save(storageUsage);

        logger.info("Storage usage for church {}: Files={} MB, DB={} MB, Total={} MB",
                churchId, String.format("%.2f", fileStorageMb),
                String.format("%.2f", dbStorageMb),
                String.format("%.2f", fileStorageMb + dbStorageMb));

        return storageUsage;
    }

    /**
     * Calculate file storage for a church.
     * Scans actual files in the uploads directory.
     */
    private Map<String, Double> calculateFileStorage(Long churchId) {
        Map<String, Double> breakdown = new HashMap<>();

        // Define storage paths for different file types
        String uploadBasePath = System.getProperty("user.home") + "/pastcare-uploads";

        Map<String, String> storagePaths = Map.of(
                "profilePhotos", uploadBasePath + "/churches/" + churchId + "/members/photos",
                "eventImages", uploadBasePath + "/churches/" + churchId + "/events/images",
                "documents", uploadBasePath + "/churches/" + churchId + "/documents",
                "attachments", uploadBasePath + "/churches/" + churchId + "/attachments"
        );

        for (Map.Entry<String, String> entry : storagePaths.entrySet()) {
            String category = entry.getKey();
            String path = entry.getValue();

            double sizeMb = calculateDirectorySize(path);
            breakdown.put(category, sizeMb);
        }

        return breakdown;
    }

    /**
     * Calculate the size of all files in a directory (recursive).
     * Returns size in megabytes (MB).
     */
    private double calculateDirectorySize(String directoryPath) {
        Path path = Paths.get(directoryPath);

        if (!Files.exists(path) || !Files.isDirectory(path)) {
            return 0.0;
        }

        try (Stream<Path> paths = Files.walk(path)) {
            long sizeBytes = paths
                    .filter(Files::isRegularFile)
                    .mapToLong(p -> {
                        try {
                            return Files.size(p);
                        } catch (IOException e) {
                            logger.warn("Failed to get size of file {}: {}", p, e.getMessage());
                            return 0L;
                        }
                    })
                    .sum();

            return sizeBytes / 1024.0 / 1024.0; // Convert bytes to MB
        } catch (IOException e) {
            logger.error("Failed to calculate directory size for {}: {}", directoryPath, e.getMessage());
            return 0.0;
        }
    }

    /**
     * Calculate database storage for a church.
     * Estimates based on row counts and average row sizes.
     */
    private Map<String, Double> calculateDatabaseStorage(Long churchId) {
        Map<String, Double> breakdown = new HashMap<>();

        // Get church entity (needed for some repositories)
        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found: " + churchId));

        // Count rows for each entity type
        // Note: Some repositories use Church entity, some use churchId
        Map<String, Long> rowCounts = new HashMap<>();
        rowCounts.put("members", memberRepository.countByChurch(church));
        rowCounts.put("donations", donationRepository.countByChurch(church));
        rowCounts.put("events", eventRepository.countByChurchId(churchId));
        rowCounts.put("visits", visitRepository.countByChurch(church));
        rowCounts.put("households", householdRepository.countByChurch(church));
        rowCounts.put("attendance_sessions", attendanceSessionRepository.countByChurch_Id(churchId));
        rowCounts.put("campaigns", campaignRepository.countByChurch_Id(churchId));
        rowCounts.put("fellowships", fellowshipRepository.countByChurch(church));
        rowCounts.put("care_needs", careNeedRepository.countByChurch(church));
        rowCounts.put("prayer_requests", prayerRequestRepository.countByChurch_Id(churchId));
        rowCounts.put("visitors", visitorRepository.countByChurch_Id(churchId));

        // Calculate estimated storage for each entity type
        for (Map.Entry<String, Long> entry : rowCounts.entrySet()) {
            String entityType = entry.getKey();
            Long count = entry.getValue();

            Integer avgSizeBytes = ENTITY_SIZE_ESTIMATES.get(entityType);
            if (avgSizeBytes != null && count > 0) {
                double sizeMb = (count * avgSizeBytes) / 1024.0 / 1024.0;
                breakdown.put(entityType, sizeMb);
            }
        }

        return breakdown;
    }

    /**
     * Get the latest storage usage for a church.
     */
    public StorageUsage getLatestStorageUsage(Long churchId) {
        return storageUsageRepository.findFirstByChurchIdOrderByCalculatedAtDesc(churchId)
                .orElseGet(() -> {
                    // No storage record exists yet, calculate it now
                    logger.info("No storage usage found for church {}, calculating now", churchId);
                    return calculateAndSaveStorageUsage(churchId);
                });
    }

    /**
     * Get storage usage history for a church.
     */
    public List<StorageUsage> getStorageUsageHistory(Long churchId, LocalDateTime startDate, LocalDateTime endDate) {
        return storageUsageRepository.findByChurchIdAndCalculatedAtBetweenOrderByCalculatedAtDesc(
                churchId, startDate, endDate);
    }

    /**
     * Check if a church is over their storage limit.
     */
    public boolean isOverStorageLimit(Long churchId, double limitMb) {
        StorageUsage latest = getLatestStorageUsage(churchId);
        return latest.getTotalStorageMb() > limitMb;
    }

    /**
     * Get storage usage percentage.
     */
    public double getStorageUsagePercentage(Long churchId, double limitMb) {
        StorageUsage latest = getLatestStorageUsage(churchId);
        return (latest.getTotalStorageMb() / limitMb) * 100.0;
    }

    /**
     * Clean up old storage records (keep only last 90 days).
     */
    @Transactional
    public void cleanupOldStorageRecords() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        try {
            storageUsageRepository.deleteByCalculatedAtBefore(cutoffDate);
            logger.info("Cleaned up storage records older than {}", cutoffDate);
        } catch (Exception e) {
            logger.error("Failed to clean up old storage records: {}", e.getMessage(), e);
        }
    }

    /**
     * Convert map to JSON string.
     */
    private String toJson(Map<String, Double> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            logger.error("Failed to convert map to JSON: {}", e.getMessage());
            return "{}";
        }
    }
}
