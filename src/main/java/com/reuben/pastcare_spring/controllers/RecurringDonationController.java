package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.dtos.PaymentInitializationRequest;
import com.reuben.pastcare_spring.dtos.PaymentInitializationResponse;
import com.reuben.pastcare_spring.dtos.RecurringDonationRequest;
import com.reuben.pastcare_spring.dtos.RecurringDonationResponse;
import com.reuben.pastcare_spring.enums.Permission;
import com.reuben.pastcare_spring.services.PaystackService;
import com.reuben.pastcare_spring.services.RecurringDonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for recurring donation operations
 */
@RestController
@RequestMapping("/api/recurring-donations")
@RequiredArgsConstructor
@Tag(name = "Recurring Donations", description = "Manage recurring donation subscriptions")
public class RecurringDonationController {

    private final RecurringDonationService recurringDonationService;
    private final PaystackService paystackService;

    @PostMapping
    @RequirePermission(Permission.DONATION_CREATE)
    @Operation(summary = "Create recurring donation")
    public ResponseEntity<RecurringDonationResponse> createRecurringDonation(
            @Valid @RequestBody RecurringDonationRequest request) {
        RecurringDonationResponse response = recurringDonationService.createRecurringDonation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @RequirePermission(Permission.DONATION_VIEW_ALL)
    @Operation(summary = "Get all recurring donations")
    public ResponseEntity<Page<RecurringDonationResponse>> getRecurringDonations(Pageable pageable) {
        Page<RecurringDonationResponse> response = recurringDonationService.getRecurringDonations(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}")
    @RequirePermission(Permission.DONATION_VIEW_ALL)
    @Operation(summary = "Get recurring donations by member")
    public ResponseEntity<Page<RecurringDonationResponse>> getRecurringDonationsByMember(
            @PathVariable Long memberId, Pageable pageable) {
        Page<RecurringDonationResponse> response = recurringDonationService.getRecurringDonationsByMember(memberId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @RequirePermission(Permission.DONATION_VIEW_ALL)
    @Operation(summary = "Get recurring donation by ID")
    public ResponseEntity<RecurringDonationResponse> getRecurringDonation(@PathVariable Long id) {
        RecurringDonationResponse response = recurringDonationService.getRecurringDonation(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @RequirePermission(Permission.DONATION_EDIT)
    @Operation(summary = "Update recurring donation")
    public ResponseEntity<RecurringDonationResponse> updateRecurringDonation(
            @PathVariable Long id,
            @Valid @RequestBody RecurringDonationRequest request) {
        RecurringDonationResponse response = recurringDonationService.updateRecurringDonation(id, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pause")
    @RequirePermission(Permission.DONATION_EDIT)
    @Operation(summary = "Pause recurring donation")
    public ResponseEntity<Void> pauseRecurringDonation(@PathVariable Long id) {
        recurringDonationService.pauseRecurringDonation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/resume")
    @RequirePermission(Permission.DONATION_EDIT)
    @Operation(summary = "Resume paused recurring donation")
    public ResponseEntity<Void> resumeRecurringDonation(@PathVariable Long id) {
        recurringDonationService.resumeRecurringDonation(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    @RequirePermission(Permission.DONATION_EDIT)
    @Operation(summary = "Cancel recurring donation")
    public ResponseEntity<Void> cancelRecurringDonation(@PathVariable Long id) {
        recurringDonationService.cancelRecurringDonation(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @RequirePermission(Permission.DONATION_DELETE)
    @Operation(summary = "Delete recurring donation")
    public ResponseEntity<Void> deleteRecurringDonation(@PathVariable Long id) {
        recurringDonationService.deleteRecurringDonation(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/initialize-payment")
    @RequirePermission(Permission.DONATION_CREATE)
    @Operation(summary = "Initialize one-time or recurring payment with Paystack")
    public ResponseEntity<PaymentInitializationResponse> initializePayment(
            @Valid @RequestBody PaymentInitializationRequest request) {
        PaymentInitializationResponse response = paystackService.initializePayment(request);
        return ResponseEntity.ok(response);
    }
}
