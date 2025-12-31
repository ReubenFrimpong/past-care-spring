package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.ChurchStorageSummaryResponse;
import com.reuben.pastcare_spring.dtos.PlatformStorageStatsResponse;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.StorageUsage;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.StorageUsageRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for platform-wide storage management and analytics.
 * SUPERADMIN-only functionality.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PlatformStorageService {

    private final ChurchRepository churchRepository;
    private final StorageUsageRepository storageUsageRepository;
    private final MemberRepository memberRepository;
    private final UserRepository userRepository;

    private static final double DEFAULT_STORAGE_LIMIT_MB = 2048.0; // 2 GB

    /**
     * Get platform-wide storage statistics.
     */
    public PlatformStorageStatsResponse getPlatformStorageStats() {
        log.info("Calculating platform-wide storage statistics");

        List<Church> allChurches = churchRepository.findAll();
        int totalChurches = allChurches.size();

        // Get latest storage usage for all churches
        List<StorageUsage> allStorageUsages = new ArrayList<>();
        for (Church church : allChurches) {
            StorageUsage latest = storageUsageRepository.findLatestByChurch(church).orElse(null);
            if (latest != null) {
                allStorageUsages.add(latest);
            }
        }

        int churchesWithStorage = allStorageUsages.size();

        // Calculate totals
        double totalStorageMb = allStorageUsages.stream()
                .mapToDouble(StorageUsage::getTotalStorageMb)
                .sum();

        double totalFileMb = allStorageUsages.stream()
                .mapToDouble(StorageUsage::getFileStorageMb)
                .sum();

        double totalDatabaseMb = allStorageUsages.stream()
                .mapToDouble(StorageUsage::getDatabaseStorageMb)
                .sum();

        double averageMb = churchesWithStorage > 0 ? totalStorageMb / churchesWithStorage : 0.0;

        // Find highest storage consumer
        StorageUsage highest = allStorageUsages.stream()
                .max(Comparator.comparingDouble(StorageUsage::getTotalStorageMb))
                .orElse(null);

        // Calculate 30-day growth
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        double storageThirtyDaysAgo = 0.0;

        for (Church church : allChurches) {
            StorageUsage oldestInRange = storageUsageRepository
                    .findFirstByChurchAndCalculatedAtAfterOrderByCalculatedAtAsc(church, thirtyDaysAgo)
                    .orElse(null);
            if (oldestInRange != null) {
                storageThirtyDaysAgo += oldestInRange.getTotalStorageMb();
            }
        }

        double growthMb = totalStorageMb - storageThirtyDaysAgo;
        double growthPercent = storageThirtyDaysAgo > 0 ? (growthMb / storageThirtyDaysAgo) * 100.0 : 0.0;

        return PlatformStorageStatsResponse.builder()
                .totalStorageUsedMb(totalStorageMb)
                .totalStorageUsedDisplay(formatStorage(totalStorageMb))
                .averageStoragePerChurchMb(averageMb)
                .averageStoragePerChurchDisplay(formatStorage(averageMb))
                .totalFileStorageMb(totalFileMb)
                .totalFileStorageDisplay(formatStorage(totalFileMb))
                .totalDatabaseStorageMb(totalDatabaseMb)
                .totalDatabaseStorageDisplay(formatStorage(totalDatabaseMb))
                .churchesWithStorage(churchesWithStorage)
                .totalChurches(totalChurches)
                .storageGrowth30dMb(growthMb)
                .storageGrowth30dPercent(growthPercent)
                .highestStorageChurchId(highest != null ? highest.getChurch().getId() : null)
                .highestStorageChurchName(highest != null ? highest.getChurch().getName() : null)
                .highestStorageAmountMb(highest != null ? highest.getTotalStorageMb() : null)
                .highestStorageAmountDisplay(highest != null ? formatStorage(highest.getTotalStorageMb()) : "0 MB")
                .build();
    }

    /**
     * Get top storage consumers.
     */
    public List<ChurchStorageSummaryResponse> getTopStorageConsumers(int limit) {
        log.info("Fetching top {} storage consumers", limit);

        List<Church> allChurches = churchRepository.findAll();
        List<ChurchStorageSummaryResponse> summaries = new ArrayList<>();

        for (Church church : allChurches) {
            StorageUsage latest = storageUsageRepository.findLatestByChurch(church).orElse(null);
            if (latest != null) {
                summaries.add(buildStorageSummary(church, latest));
            }
        }

        // Sort by total storage descending and limit
        return summaries.stream()
                .sorted(Comparator.comparingDouble(ChurchStorageSummaryResponse::getTotalStorageMb).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get all church storage summaries.
     */
    public List<ChurchStorageSummaryResponse> getAllChurchStorageSummaries() {
        log.info("Fetching all church storage summaries");

        List<Church> allChurches = churchRepository.findAll();
        List<ChurchStorageSummaryResponse> summaries = new ArrayList<>();

        for (Church church : allChurches) {
            StorageUsage latest = storageUsageRepository.findLatestByChurch(church).orElse(null);
            if (latest != null) {
                summaries.add(buildStorageSummary(church, latest));
            } else {
                // Church with no storage usage yet
                summaries.add(buildEmptyStorageSummary(church));
            }
        }

        // Sort by total storage descending
        return summaries.stream()
                .sorted(Comparator.comparingDouble(ChurchStorageSummaryResponse::getTotalStorageMb).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Build storage summary from church and storage usage.
     */
    private ChurchStorageSummaryResponse buildStorageSummary(Church church, StorageUsage usage) {
        double usagePercentage = (usage.getTotalStorageMb() / DEFAULT_STORAGE_LIMIT_MB) * 100.0;
        boolean isOverLimit = usage.getTotalStorageMb() > DEFAULT_STORAGE_LIMIT_MB;

        int memberCount = (int) memberRepository.countByChurch(church);
        int userCount = (int) userRepository.countByChurch(church);

        return ChurchStorageSummaryResponse.builder()
                .churchId(church.getId())
                .churchName(church.getName())
                .active(church.isActive())
                .totalStorageMb(usage.getTotalStorageMb())
                .totalStorageDisplay(formatStorage(usage.getTotalStorageMb()))
                .fileStorageMb(usage.getFileStorageMb())
                .fileStorageDisplay(formatStorage(usage.getFileStorageMb()))
                .databaseStorageMb(usage.getDatabaseStorageMb())
                .databaseStorageDisplay(formatStorage(usage.getDatabaseStorageMb()))
                .storageLimitMb(DEFAULT_STORAGE_LIMIT_MB)
                .storageLimitDisplay(formatStorage(DEFAULT_STORAGE_LIMIT_MB))
                .usagePercentage(usagePercentage)
                .isOverLimit(isOverLimit)
                .lastCalculated(usage.getCalculatedAt())
                .memberCount(memberCount)
                .userCount(userCount)
                .build();
    }

    /**
     * Build empty storage summary for churches with no usage data.
     */
    private ChurchStorageSummaryResponse buildEmptyStorageSummary(Church church) {
        int memberCount = (int) memberRepository.countByChurch(church);
        int userCount = (int) userRepository.countByChurch(church);

        return ChurchStorageSummaryResponse.builder()
                .churchId(church.getId())
                .churchName(church.getName())
                .active(church.isActive())
                .totalStorageMb(0.0)
                .totalStorageDisplay("0 MB")
                .fileStorageMb(0.0)
                .fileStorageDisplay("0 MB")
                .databaseStorageMb(0.0)
                .databaseStorageDisplay("0 MB")
                .storageLimitMb(DEFAULT_STORAGE_LIMIT_MB)
                .storageLimitDisplay(formatStorage(DEFAULT_STORAGE_LIMIT_MB))
                .usagePercentage(0.0)
                .isOverLimit(false)
                .lastCalculated(null)
                .memberCount(memberCount)
                .userCount(userCount)
                .build();
    }

    /**
     * Format storage size for display.
     * Converts MB to GB if >= 1024 MB.
     */
    private String formatStorage(double mb) {
        if (mb >= 1024.0) {
            double gb = mb / 1024.0;
            return String.format("%.2f GB", gb);
        } else {
            return String.format("%.2f MB", mb);
        }
    }
}
