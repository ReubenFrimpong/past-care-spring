package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.ChurchSummaryResponse;
import com.reuben.pastcare_spring.dtos.PlatformStatsResponse;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.StorageUsage;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PlatformStatsService {

    private final ChurchRepository churchRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final StorageUsageRepository storageUsageRepository;

    /**
     * Get platform-wide statistics (SUPERADMIN only).
     */
    public PlatformStatsResponse getPlatformStats() {
        log.info("Calculating platform-wide statistics");

        // Count churches
        long totalChurches = churchRepository.count();
        long activeChurches = churchRepository.findAll().stream()
                .filter(Church::isActive)
                .count();

        // Count users
        long totalUsers = userRepository.count();
        // For now, consider all users as active (enhance later with lastLogin check)
        long activeUsers = totalUsers;

        // Count members across all churches
        long totalMembers = memberRepository.count();

        // Calculate total storage
        List<StorageUsage> latestStorageUsages = storageUsageRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        su -> su.getChurch().getId(),
                        Collectors.maxBy((su1, su2) -> su1.getCreatedAt().compareTo(su2.getCreatedAt()))
                ))
                .values().stream()
                .filter(opt -> opt.isPresent())
                .map(opt -> opt.get())
                .collect(Collectors.toList());

        double totalStorageMB = latestStorageUsages.stream()
                .mapToDouble(su -> su.getTotalStorageMb())
                .sum();

        String totalStorageUsed = formatStorageSize(totalStorageMB);
        double averageStoragePerChurch = totalChurches > 0 ? totalStorageMB / totalChurches : 0.0;

        return PlatformStatsResponse.builder()
                .totalChurches(totalChurches)
                .activeChurches(activeChurches)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .totalMembers(totalMembers)
                .totalStorageUsed(totalStorageUsed)
                .averageStoragePerChurch(averageStoragePerChurch)
                .build();
    }

    /**
     * Get church summaries with pagination (SUPERADMIN only).
     */
    public Page<ChurchSummaryResponse> getChurchSummaries(Pageable pageable) {
        log.info("Fetching church summaries with pagination");

        Page<Church> churches = churchRepository.findAll(pageable);

        List<ChurchSummaryResponse> summaries = churches.stream()
                .map(this::convertToChurchSummary)
                .collect(Collectors.toList());

        return new PageImpl<>(summaries, pageable, churches.getTotalElements());
    }

    /**
     * Get all church summaries without pagination (SUPERADMIN only).
     */
    public List<ChurchSummaryResponse> getAllChurchSummaries() {
        log.info("Fetching all church summaries");

        List<Church> churches = churchRepository.findAll();

        return churches.stream()
                .map(this::convertToChurchSummary)
                .collect(Collectors.toList());
    }

    /**
     * Get church summary by ID (SUPERADMIN only).
     */
    public ChurchSummaryResponse getChurchSummary(Long churchId) {
        log.info("Fetching church summary for church ID: {}", churchId);

        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found with ID: " + churchId));

        return convertToChurchSummary(church);
    }

    /**
     * Activate a church (SUPERADMIN only).
     */
    public void activateChurch(Long churchId) {
        log.info("Activating church with ID: {}", churchId);

        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found with ID: " + churchId));

        church.setActive(true);
        churchRepository.save(church);

        log.info("Church activated successfully: {}", church.getName());
    }

    /**
     * Deactivate a church (SUPERADMIN only).
     */
    public void deactivateChurch(Long churchId) {
        log.info("Deactivating church with ID: {}", churchId);

        Church church = churchRepository.findById(churchId)
                .orElseThrow(() -> new IllegalArgumentException("Church not found with ID: " + churchId));

        church.setActive(false);
        churchRepository.save(church);

        log.info("Church deactivated successfully: {}", church.getName());
    }

    // ========== HELPER METHODS ==========

    private ChurchSummaryResponse convertToChurchSummary(Church church) {
        // Count users for this church
        int userCount = (int) userRepository.findAll().stream()
                .filter(user -> user.getChurch() != null && user.getChurch().getId().equals(church.getId()))
                .count();

        // Count members for this church
        int memberCount = (int) memberRepository.findAll().stream()
                .filter(member -> member.getChurch() != null && member.getChurch().getId().equals(church.getId()))
                .count();

        // Get latest storage usage for this church
        List<StorageUsage> storageUsages = storageUsageRepository.findAll().stream()
                .filter(su -> su.getChurch() != null && su.getChurch().getId().equals(church.getId()))
                .sorted((su1, su2) -> su2.getCreatedAt().compareTo(su1.getCreatedAt()))
                .collect(Collectors.toList());

        double storageMB = storageUsages.isEmpty() ? 0.0 : storageUsages.get(0).getTotalStorageMb();
        String storageUsed = formatStorageSize(storageMB);

        // Calculate storage percentage (assuming 2 GB = 2048 MB limit)
        double storageLimit = 2048.0; // 2 GB in MB
        int storagePercentage = (int) Math.min(100, (storageMB / storageLimit) * 100);

        return ChurchSummaryResponse.builder()
                .id(church.getId())
                .name(church.getName())
                .email(church.getEmail())
                .phoneNumber(church.getPhoneNumber())
                .address(church.getAddress())
                .active(church.isActive())
                .createdAt(church.getCreatedAt())
                .userCount(userCount)
                .memberCount(memberCount)
                .storageUsed(storageUsed)
                .storageUsedMB(storageMB)
                .storagePercentage(storagePercentage)
                .build();
    }

    private String formatStorageSize(double sizeMB) {
        if (sizeMB >= 1024) {
            return String.format("%.2f GB", sizeMB / 1024);
        } else {
            return String.format("%.2f MB", sizeMB);
        }
    }
}
