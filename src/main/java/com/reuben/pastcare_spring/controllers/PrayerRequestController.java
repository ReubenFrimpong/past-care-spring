package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.PrayerRequestDto;
import com.reuben.pastcare_spring.models.PrayerRequest;
import com.reuben.pastcare_spring.models.PrayerRequestStatus;
import com.reuben.pastcare_spring.services.PrayerRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PrayerRequestController {

    private final PrayerRequestService prayerRequestService;

    /**
     * Member submits a new prayer request
     */
    @PostMapping("/portal/prayer-requests")
    public ResponseEntity<PrayerRequest> submitPrayerRequest(
            @RequestParam Long churchId,
            @RequestParam Long memberId,
            @Valid @RequestBody PrayerRequestDto dto) {
        PrayerRequest prayerRequest = prayerRequestService.submitPrayerRequest(churchId, memberId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(prayerRequest);
    }

    /**
     * Member gets their own prayer requests
     */
    @GetMapping("/portal/prayer-requests/my-requests")
    public ResponseEntity<List<PrayerRequest>> getMyPrayerRequests(@RequestParam Long memberId) {
        List<PrayerRequest> requests = prayerRequestService.getMemberPrayerRequests(memberId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Member adds testimony to their answered prayer
     */
    @PostMapping("/portal/prayer-requests/{id}/testimony")
    public ResponseEntity<PrayerRequest> addTestimony(
            @PathVariable Long id,
            @RequestParam Long churchId,
            @RequestParam Long memberId,
            @RequestBody Map<String, String> body) {
        String testimony = body.get("testimony");
        if (testimony == null || testimony.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        PrayerRequest updated = prayerRequestService.addTestimony(churchId, id, memberId, testimony);
        return ResponseEntity.ok(updated);
    }

    /**
     * Member deletes their own prayer request (soft delete by archiving)
     */
    @DeleteMapping("/portal/prayer-requests/{id}")
    public ResponseEntity<Void> deletePrayerRequest(
            @PathVariable Long id,
            @RequestParam Long churchId,
            @RequestParam Long memberId) {
        prayerRequestService.deletePrayerRequest(churchId, id, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Admin/Staff gets all prayer requests for the church
     */
    @GetMapping("/prayer-requests")
    public ResponseEntity<List<PrayerRequest>> getAllPrayerRequests(@RequestParam Long churchId) {
        List<PrayerRequest> requests = prayerRequestService.getAllPrayerRequests(churchId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get prayer requests by status
     */
    @GetMapping("/prayer-requests/status/{status}")
    public ResponseEntity<List<PrayerRequest>> getPrayerRequestsByStatus(
            @PathVariable PrayerRequestStatus status,
            @RequestParam Long churchId) {
        List<PrayerRequest> requests = prayerRequestService.getPrayerRequestsByStatus(churchId, status);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get public prayer requests (for prayer team/bulletin)
     */
    @GetMapping("/prayer-requests/public")
    public ResponseEntity<List<PrayerRequest>> getPublicPrayerRequests(@RequestParam Long churchId) {
        List<PrayerRequest> requests = prayerRequestService.getPublicPrayerRequests(churchId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get urgent prayer requests
     */
    @GetMapping("/prayer-requests/urgent")
    public ResponseEntity<List<PrayerRequest>> getUrgentPrayerRequests(@RequestParam Long churchId) {
        List<PrayerRequest> requests = prayerRequestService.getUrgentPrayerRequests(churchId);
        return ResponseEntity.ok(requests);
    }

    /**
     * Get prayer testimonies (answered prayers)
     */
    @GetMapping("/prayer-requests/testimonies")
    public ResponseEntity<List<PrayerRequest>> getPrayerTestimonies(@RequestParam Long churchId) {
        List<PrayerRequest> testimonies = prayerRequestService.getPrayerTestimonies(churchId);
        return ResponseEntity.ok(testimonies);
    }

    /**
     * Admin/Staff updates prayer request status
     */
    @PatchMapping("/prayer-requests/{id}/status")
    public ResponseEntity<PrayerRequest> updatePrayerRequestStatus(
            @PathVariable Long id,
            @RequestParam Long churchId,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            PrayerRequestStatus status = PrayerRequestStatus.valueOf(statusStr);
            PrayerRequest updated = prayerRequestService.updateStatus(churchId, id, status);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Archive expired prayer requests (can be called by scheduled task)
     */
    @PostMapping("/prayer-requests/archive-expired")
    public ResponseEntity<Map<String, Integer>> archiveExpiredPrayerRequests() {
        int archivedCount = prayerRequestService.archiveExpiredPrayerRequests();
        return ResponseEntity.ok(Map.of("archivedCount", archivedCount));
    }
}
