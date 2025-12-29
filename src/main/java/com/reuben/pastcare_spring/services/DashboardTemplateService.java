package com.reuben.pastcare_spring.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reuben.pastcare_spring.dtos.DashboardTemplateRequest;
import com.reuben.pastcare_spring.dtos.DashboardTemplateResponse;
import com.reuben.pastcare_spring.enums.Role;
import com.reuben.pastcare_spring.models.DashboardLayout;
import com.reuben.pastcare_spring.models.DashboardTemplate;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.DashboardLayoutRepository;
import com.reuben.pastcare_spring.repositories.DashboardTemplateRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing dashboard templates.
 * Dashboard Phase 2.2: Role-Based Templates
 *
 * Provides CRUD operations for templates and methods to apply templates to user layouts.
 */
@Service
@RequiredArgsConstructor
public class DashboardTemplateService {

    private final DashboardTemplateRepository templateRepository;
    private final DashboardLayoutRepository layoutRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get all templates available for a user's role.
     * Returns templates for the user's role, ordered by default first.
     */
    public List<DashboardTemplateResponse> getTemplatesForRole(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        return templateRepository.findTemplatesForRole(user.getRole()).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get templates for a specific role (admin only).
     * Used for template management by administrators.
     */
    public List<DashboardTemplateResponse> getTemplatesForSpecificRole(Role role) {
        return templateRepository.findByRoleOrderByTemplateNameAsc(role).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get all templates (admin only).
     * Returns all templates across all roles.
     */
    public List<DashboardTemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get a specific template by ID.
     */
    public DashboardTemplateResponse getTemplate(Long templateId) {
        DashboardTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));
        return mapToResponse(template);
    }

    /**
     * Get the default template for a specific role.
     */
    public DashboardTemplateResponse getDefaultTemplate(Role role) {
        DashboardTemplate template = templateRepository.findByRoleAndIsDefaultTrue(role)
            .orElseThrow(() -> new RuntimeException("No default template found for role: " + role));
        return mapToResponse(template);
    }

    /**
     * Create a new custom template (admin only).
     * Validates JSON configuration and checks for duplicate names.
     */
    @Transactional
    public DashboardTemplateResponse createTemplate(DashboardTemplateRequest request, Long createdByUserId) {
        // Validate JSON configuration
        validateLayoutConfig(request.getLayoutConfig());

        // Check for duplicate template name for this role
        if (templateRepository.existsByRoleAndTemplateNameIgnoreCase(request.getRole(), request.getTemplateName())) {
            throw new IllegalArgumentException("Template with name '" + request.getTemplateName() + "' already exists for role " + request.getRole());
        }

        User createdBy = userRepository.findById(createdByUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // If this is set as default, unset other defaults for this role
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            unsetDefaultTemplateForRole(request.getRole());
        }

        DashboardTemplate template = new DashboardTemplate();
        template.setTemplateName(request.getTemplateName());
        template.setDescription(request.getDescription());
        template.setRole(request.getRole());
        template.setLayoutConfig(request.getLayoutConfig());
        template.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        template.setPreviewImageUrl(request.getPreviewImageUrl());
        template.setCreatedBy(createdBy);

        DashboardTemplate saved = templateRepository.save(template);
        return mapToResponse(saved);
    }

    /**
     * Update an existing template (admin only).
     */
    @Transactional
    public DashboardTemplateResponse updateTemplate(Long templateId, DashboardTemplateRequest request) {
        DashboardTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));

        // Validate JSON configuration
        validateLayoutConfig(request.getLayoutConfig());

        // If changing to default, unset other defaults for this role
        if (Boolean.TRUE.equals(request.getIsDefault()) && !Boolean.TRUE.equals(template.getIsDefault())) {
            unsetDefaultTemplateForRole(request.getRole());
        }

        template.setTemplateName(request.getTemplateName());
        template.setDescription(request.getDescription());
        template.setRole(request.getRole());
        template.setLayoutConfig(request.getLayoutConfig());
        template.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
        template.setPreviewImageUrl(request.getPreviewImageUrl());

        DashboardTemplate updated = templateRepository.save(template);
        return mapToResponse(updated);
    }

    /**
     * Delete a template (admin only).
     * Prevents deletion of default templates.
     */
    @Transactional
    public void deleteTemplate(Long templateId) {
        DashboardTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));

        if (Boolean.TRUE.equals(template.getIsDefault())) {
            throw new IllegalStateException("Cannot delete default template. Please set another template as default first.");
        }

        templateRepository.delete(template);
    }

    /**
     * Apply a template to a user's dashboard.
     * Creates or updates the user's default layout with the template configuration.
     */
    @Transactional
    public void applyTemplate(Long userId, Long templateId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        DashboardTemplate template = templateRepository.findById(templateId)
            .orElseThrow(() -> new RuntimeException("Template not found"));

        // Verify template is for user's role or user is admin
        if (template.getRole() != user.getRole() &&
            user.getRole() != Role.ADMIN &&
            user.getRole() != Role.SUPERADMIN) {
            throw new IllegalArgumentException("Template is not available for your role");
        }

        // Get or create user's layout
        DashboardLayout layout = layoutRepository.findByUserAndIsDefaultTrue(user)
            .orElse(DashboardLayout.builder()
                .user(user)
                .church(user.getChurch())
                .isDefault(true)
                .build());

        // Apply template configuration
        layout.setLayoutName(template.getTemplateName());
        layout.setLayoutConfig(template.getLayoutConfig());

        layoutRepository.save(layout);
    }

    /**
     * Unset default flag for all templates of a specific role.
     * Used when setting a new default template.
     */
    private void unsetDefaultTemplateForRole(Role role) {
        templateRepository.findByRoleAndIsDefaultTrue(role)
            .ifPresent(template -> {
                template.setIsDefault(false);
                templateRepository.save(template);
            });
    }

    /**
     * Validate layout configuration JSON.
     */
    private void validateLayoutConfig(String layoutConfig) {
        try {
            objectMapper.readTree(layoutConfig);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid layout configuration JSON: " + e.getMessage());
        }
    }

    /**
     * Map DashboardTemplate entity to response DTO.
     */
    private DashboardTemplateResponse mapToResponse(DashboardTemplate template) {
        DashboardTemplateResponse response = new DashboardTemplateResponse();
        response.setId(template.getId());
        response.setTemplateName(template.getTemplateName());
        response.setDescription(template.getDescription());
        response.setRole(template.getRole());
        response.setRoleDisplayName(template.getRoleDisplayName());
        response.setLayoutConfig(template.getLayoutConfig());
        response.setIsDefault(template.getIsDefault());
        response.setPreviewImageUrl(template.getPreviewImageUrl());
        response.setCreatedBy(template.getCreatedBy() != null ? template.getCreatedBy().getId() : null);
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        return response;
    }
}
