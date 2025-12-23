package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.MemberSkillRequest;
import com.reuben.pastcare_spring.enums.ProficiencyLevel;
import com.reuben.pastcare_spring.models.MemberSkill;
import com.reuben.pastcare_spring.services.MemberSkillService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/member-skills")
@RequiredArgsConstructor
public class MemberSkillController {

    private final MemberSkillService memberSkillService;
    private final RequestContextUtil requestContextUtil;

    @PostMapping
    public ResponseEntity<MemberSkill> assignSkillToMember(
            @Valid @RequestBody MemberSkillRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        MemberSkill memberSkill = memberSkillService.assignSkillToMember(churchId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberSkill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MemberSkill> updateMemberSkill(
            @PathVariable Long id,
            @Valid @RequestBody MemberSkillRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        MemberSkill memberSkill = memberSkillService.updateMemberSkill(churchId, id, request);
        return ResponseEntity.ok(memberSkill);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<MemberSkill>> getMemberSkills(
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<MemberSkill> skills = memberSkillService.getMemberSkills(churchId, memberId);
        return ResponseEntity.ok(skills);
    }

    @GetMapping("/skill/{skillId}")
    public ResponseEntity<List<MemberSkill>> getMembersWithSkill(
            @PathVariable Long skillId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<MemberSkill> members = memberSkillService.getMembersWithSkill(churchId, skillId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/skill/{skillId}/willing")
    public ResponseEntity<List<MemberSkill>> getMembersWillingToServe(
            @PathVariable Long skillId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<MemberSkill> members = memberSkillService.getMembersWillingToServe(churchId, skillId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/skill/{skillId}/proficiency")
    public ResponseEntity<List<MemberSkill>> getMembersBySkillAndProficiency(
            @PathVariable Long skillId,
            @RequestParam List<ProficiencyLevel> levels,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<MemberSkill> members = memberSkillService.getMembersBySkillAndProficiency(churchId, skillId, levels);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/member/{memberId}/serving")
    public ResponseEntity<List<MemberSkill>> getCurrentlyServingSkills(
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<MemberSkill> skills = memberSkillService.getCurrentlyServingSkills(churchId, memberId);
        return ResponseEntity.ok(skills);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeMemberSkill(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        memberSkillService.removeMemberSkill(churchId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/member/{memberId}/count")
    public ResponseEntity<Long> getMemberSkillCount(
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long count = memberSkillService.getMemberSkillCount(churchId, memberId);
        return ResponseEntity.ok(count);
    }
}
