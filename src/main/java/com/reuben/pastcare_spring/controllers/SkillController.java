package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.dtos.SkillRequest;
import com.reuben.pastcare_spring.enums.SkillCategory;
import com.reuben.pastcare_spring.models.Skill;
import com.reuben.pastcare_spring.services.SkillService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;
    private final RequestContextUtil requestContextUtil;

    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PostMapping
    public ResponseEntity<Skill> createSkill(
            @Valid @RequestBody SkillRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Skill skill = skillService.createSkill(churchId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(skill);
    }

    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PutMapping("/{id}")
    public ResponseEntity<Skill> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Skill skill = skillService.updateSkill(churchId, id, request);
        return ResponseEntity.ok(skill);
    }

    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping("/{id}")
    public ResponseEntity<Skill> getSkillById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Skill skill = skillService.getSkillById(churchId, id);
        return ResponseEntity.ok(skill);
    }

    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping
    public ResponseEntity<List<Skill>> getAllSkills(
            @RequestParam(required = false, defaultValue = "true") Boolean activeOnly,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<Skill> skills = activeOnly
            ? skillService.getAllActiveSkills(churchId)
            : skillService.getAllSkills(churchId);
        return ResponseEntity.ok(skills);
    }

    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Skill>> getSkillsByCategory(
            @PathVariable SkillCategory category,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<Skill> skills = skillService.getSkillsByCategory(churchId, category);
        return ResponseEntity.ok(skills);
    }

    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping("/search")
    public ResponseEntity<List<Skill>> searchSkills(
            @RequestParam String query,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<Skill> skills = skillService.searchSkills(churchId, query);
        return ResponseEntity.ok(skills);
    }

    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        skillService.deleteSkill(churchId, id);
        return ResponseEntity.noContent().build();
    }

    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Skill> deactivateSkill(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Skill skill = skillService.deactivateSkill(churchId, id);
        return ResponseEntity.ok(skill);
    }

    @RequirePermission(Permission.MEMBER_EDIT_ALL)
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Skill> activateSkill(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Skill skill = skillService.activateSkill(churchId, id);
        return ResponseEntity.ok(skill);
    }

    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping("/stats/count")
    public ResponseEntity<Long> getActiveSkillsCount(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long count = skillService.getActiveSkillsCount(churchId);
        return ResponseEntity.ok(count);
    }
}
