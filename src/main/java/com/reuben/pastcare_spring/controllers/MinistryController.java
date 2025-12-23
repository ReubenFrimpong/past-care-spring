package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.MinistryRequest;
import com.reuben.pastcare_spring.enums.MinistryStatus;
import com.reuben.pastcare_spring.models.Ministry;
import com.reuben.pastcare_spring.services.MinistryService;
import com.reuben.pastcare_spring.util.RequestContextUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ministries")
@RequiredArgsConstructor
public class MinistryController {

    private final MinistryService ministryService;
    private final RequestContextUtil requestContextUtil;

    @PostMapping
    public ResponseEntity<Ministry> createMinistry(
            @Valid @RequestBody MinistryRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Ministry ministry = ministryService.createMinistry(churchId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ministry);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ministry> updateMinistry(
            @PathVariable Long id,
            @Valid @RequestBody MinistryRequest request,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Ministry ministry = ministryService.updateMinistry(churchId, id, request);
        return ResponseEntity.ok(ministry);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ministry> getMinistryById(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Ministry ministry = ministryService.getMinistryById(churchId, id);
        return ResponseEntity.ok(ministry);
    }

    @GetMapping
    public ResponseEntity<List<Ministry>> getAllMinistries(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<Ministry> ministries = ministryService.getAllMinistries(churchId);
        return ResponseEntity.ok(ministries);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Ministry>> getMinistriesByStatus(
            @PathVariable MinistryStatus status,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<Ministry> ministries = ministryService.getMinistriesByStatus(churchId, status);
        return ResponseEntity.ok(ministries);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Ministry>> searchMinistries(
            @RequestParam String query,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<Ministry> ministries = ministryService.searchMinistries(churchId, query);
        return ResponseEntity.ok(ministries);
    }

    @GetMapping("/leader/{leaderId}")
    public ResponseEntity<List<Ministry>> getMinistriesByLeader(
            @PathVariable Long leaderId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<Ministry> ministries = ministryService.getMinistriesByLeader(churchId, leaderId);
        return ResponseEntity.ok(ministries);
    }

    @GetMapping("/member/{memberId}")
    public ResponseEntity<List<Ministry>> getMinistriesByMember(
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        List<Ministry> ministries = ministryService.getMinistriesByMember(churchId, memberId);
        return ResponseEntity.ok(ministries);
    }

    @PostMapping("/{id}/members/{memberId}")
    public ResponseEntity<Ministry> addMemberToMinistry(
            @PathVariable Long id,
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Ministry ministry = ministryService.addMemberToMinistry(churchId, id, memberId);
        return ResponseEntity.ok(ministry);
    }

    @DeleteMapping("/{id}/members/{memberId}")
    public ResponseEntity<Ministry> removeMemberFromMinistry(
            @PathVariable Long id,
            @PathVariable Long memberId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Ministry ministry = ministryService.removeMemberFromMinistry(churchId, id, memberId);
        return ResponseEntity.ok(ministry);
    }

    @PostMapping("/{id}/skills/{skillId}")
    public ResponseEntity<Ministry> addRequiredSkill(
            @PathVariable Long id,
            @PathVariable Long skillId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Ministry ministry = ministryService.addRequiredSkill(churchId, id, skillId);
        return ResponseEntity.ok(ministry);
    }

    @DeleteMapping("/{id}/skills/{skillId}")
    public ResponseEntity<Ministry> removeRequiredSkill(
            @PathVariable Long id,
            @PathVariable Long skillId,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Ministry ministry = ministryService.removeRequiredSkill(churchId, id, skillId);
        return ResponseEntity.ok(ministry);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMinistry(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        ministryService.deleteMinistry(churchId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Long> getActiveMinistriesCount(HttpServletRequest httpRequest) {
        Long churchId = requestContextUtil.extractChurchId(httpRequest);
        Long count = ministryService.getActiveMinistriesCount(churchId);
        return ResponseEntity.ok(count);
    }
}
