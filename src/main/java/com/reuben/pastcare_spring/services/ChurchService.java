package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service for managing church profile and settings
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChurchService {

    private final ChurchRepository churchRepository;
    private final ImageService imageService;

    /**
     * Get church by ID
     */
    public Church getChurchById(Long id) {
        return churchRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + id));
    }

    /**
     * Update church profile
     */
    @Transactional
    public Church updateChurch(Long id, Church churchRequest) {
        Church church = getChurchById(id);

        // Update basic fields
        if (churchRequest.getName() != null) {
            church.setName(churchRequest.getName());
        }
        if (churchRequest.getAddress() != null) {
            church.setAddress(churchRequest.getAddress());
        }
        if (churchRequest.getPhoneNumber() != null) {
            church.setPhoneNumber(churchRequest.getPhoneNumber());
        }
        if (churchRequest.getEmail() != null) {
            church.setEmail(churchRequest.getEmail());
        }
        if (churchRequest.getWebsite() != null) {
            church.setWebsite(churchRequest.getWebsite());
        }
        if (churchRequest.getPastor() != null) {
            church.setPastor(churchRequest.getPastor());
        }
        if (churchRequest.getDenomination() != null) {
            church.setDenomination(churchRequest.getDenomination());
        }
        if (churchRequest.getFoundedYear() != null) {
            church.setFoundedYear(churchRequest.getFoundedYear());
        }
        if (churchRequest.getNumberOfMembers() != null) {
            church.setNumberOfMembers(churchRequest.getNumberOfMembers());
        }

        Church savedChurch = churchRepository.save(church);
        log.info("Updated church profile for church ID: {}", id);
        return savedChurch;
    }

    /**
     * Upload church logo
     */
    @Transactional
    public String uploadLogo(Long id, MultipartFile file) {
        Church church = getChurchById(id);

        try {
            // Upload and compress logo (allow 500KB for church logo)
            String logoUrl = uploadChurchLogo(file, church.getLogoUrl());

            // Update church with new logo URL
            church.setLogoUrl(logoUrl);
            churchRepository.save(church);

            log.info("Uploaded logo for church ID: {}", id);
            return logoUrl;
        } catch (IOException e) {
            log.error("Failed to upload logo for church ID: {}", id, e);
            throw new RuntimeException("Failed to upload logo: " + e.getMessage());
        }
    }

    /**
     * Delete church logo
     */
    @Transactional
    public void deleteLogo(Long id) {
        Church church = getChurchById(id);

        if (church.getLogoUrl() != null && !church.getLogoUrl().isEmpty()) {
            // Delete the logo file
            imageService.deleteImage(church.getLogoUrl());

            // Remove logo URL from church
            church.setLogoUrl(null);
            churchRepository.save(church);

            log.info("Deleted logo for church ID: {}", id);
        }
    }

    /**
     * Get the logo URL of the first active church (for public endpoints)
     * This is used for landing page and favicon
     */
    public String getFirstActiveChurchLogoUrl() {
        return churchRepository.findAll().stream()
                .filter(Church::isActive)
                .findFirst()
                .map(Church::getLogoUrl)
                .orElse(null);
    }

    /**
     * Upload and compress a church logo
     * Similar to ImageService methods but specific for church logos
     */
    private String uploadChurchLogo(MultipartFile file, String oldLogoPath) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Use ImageService for fellowship images (500KB max, similar to logo needs)
        // This reuses the existing image compression logic
        return imageService.uploadFellowshipImage(file, oldLogoPath);
    }
}
