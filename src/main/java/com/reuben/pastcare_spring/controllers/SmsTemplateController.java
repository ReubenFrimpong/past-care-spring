package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.SmsTemplateRequest;
import com.reuben.pastcare_spring.dtos.SmsTemplateResponse;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.SmsTemplate;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.security.TenantContext;
import com.reuben.pastcare_spring.services.SmsService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sms/templates")
@Slf4j
public class SmsTemplateController {

    private final SmsService smsService;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;

    public SmsTemplateController(
        SmsService smsService,
        UserRepository userRepository,
        ChurchRepository churchRepository
    ) {
        this.smsService = smsService;
        this.userRepository = userRepository;
        this.churchRepository = churchRepository;
    }

    /**
     * Create template
     */
    @PostMapping
    public ResponseEntity<SmsTemplateResponse> createTemplate(
        @Valid @RequestBody SmsTemplateRequest request,
        Authentication authentication
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            SmsTemplate template = new SmsTemplate();
            template.setChurch(church);
            template.setCreatedBy(user);
            template.setName(request.getName());
            template.setDescription(request.getDescription());
            template.setTemplate(request.getTemplate());
            template.setCategory(request.getCategory());
            template.setIsActive(request.getIsActive());
            template.setIsDefault(request.getIsDefault());

            SmsTemplate saved = smsService.createTemplate(template);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(saved));

        } catch (Exception e) {
            log.error("Error creating template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update template
     */
    @PutMapping("/{id}")
    public ResponseEntity<SmsTemplateResponse> updateTemplate(
        @PathVariable Long id,
        @Valid @RequestBody SmsTemplateRequest request
    ) {
        try {
            SmsTemplate template = new SmsTemplate();
            template.setName(request.getName());
            template.setDescription(request.getDescription());
            template.setTemplate(request.getTemplate());
            template.setCategory(request.getCategory());
            template.setIsActive(request.getIsActive());

            SmsTemplate updated = smsService.updateTemplate(id, template);
            return ResponseEntity.ok(mapToResponse(updated));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete template
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTemplate(@PathVariable Long id) {
        try {
            smsService.deleteTemplate(id);
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("Error deleting template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all templates
     */
    @GetMapping
    public ResponseEntity<Page<SmsTemplateResponse>> getTemplates(
        @RequestParam(required = false, defaultValue = "true") Boolean isActive,
        Pageable pageable
    ) {
        try {
            Long churchId = TenantContext.getCurrentChurchId();
            Page<SmsTemplate> templates = smsService.getTemplates(churchId, isActive, pageable);
            Page<SmsTemplateResponse> responses = templates.map(this::mapToResponse);

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error fetching templates: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get template by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SmsTemplateResponse> getTemplateById(@PathVariable Long id) {
        try {
            Long churchId = TenantContext.getCurrentChurchId();
            SmsTemplate template = smsService.getTemplateById(id, churchId);
            return ResponseEntity.ok(mapToResponse(template));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching template: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Helper methods

    private User getUserFromAuth(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByEmail(username)
            .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private Church getChurch() {
        Long churchId = TenantContext.getCurrentChurchId();
        return churchRepository.findById(churchId)
            .orElseThrow(() -> new IllegalStateException("Church not found"));
    }

    private SmsTemplateResponse mapToResponse(SmsTemplate template) {
        SmsTemplateResponse response = new SmsTemplateResponse();
        response.setId(template.getId());
        response.setName(template.getName());
        response.setDescription(template.getDescription());
        response.setTemplate(template.getTemplate());
        response.setCategory(template.getCategory());
        response.setIsActive(template.getIsActive());
        response.setIsDefault(template.getIsDefault());
        response.setUsageCount(template.getUsageCount());

        if (template.getCreatedBy() != null) {
            response.setCreatedById(template.getCreatedBy().getId());
            response.setCreatedByName(template.getCreatedBy().getName());
        }

        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());

        return response;
    }
}
