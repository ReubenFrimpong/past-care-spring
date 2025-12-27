package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@Slf4j
public class SmsController {

    private final SmsService smsService;
    private final UserRepository userRepository;
    private final ChurchRepository churchRepository;
    private final MemberRepository memberRepository;

    public SmsController(
        SmsService smsService,
        UserRepository userRepository,
        ChurchRepository churchRepository,
        MemberRepository memberRepository
    ) {
        this.smsService = smsService;
        this.userRepository = userRepository;
        this.churchRepository = churchRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Send single SMS
     */
    @PostMapping("/send")
    public ResponseEntity<SmsMessageResponse> sendSms(
        @Valid @RequestBody SendSmsRequest request,
        Authentication authentication
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            Member member = null;
            if (request.getMemberId() != null) {
                member = memberRepository.findById(request.getMemberId()).orElse(null);
            }

            SmsMessage smsMessage = smsService.sendSms(
                user,
                church,
                request.getRecipientPhone(),
                request.getRecipientName(),
                request.getMessage(),
                member,
                request.getScheduledTime()
            );

            return ResponseEntity.ok(mapToResponse(smsMessage));

        } catch (IllegalStateException e) {
            log.error("Error sending SMS: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(null);
        } catch (Exception e) {
            log.error("Error sending SMS: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Send bulk SMS
     */
    @PostMapping("/send-bulk")
    public ResponseEntity<List<SmsMessageResponse>> sendBulkSms(
        @Valid @RequestBody SendBulkSmsRequest request,
        Authentication authentication
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            List<SmsMessage> messages = smsService.sendBulkSms(
                user,
                church,
                request.getRecipientPhones(),
                request.getMessage(),
                request.getScheduledTime()
            );

            List<SmsMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .toList();

            return ResponseEntity.ok(responses);

        } catch (IllegalStateException e) {
            log.error("Error sending bulk SMS: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        } catch (Exception e) {
            log.error("Error sending bulk SMS: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Send SMS to members
     */
    @PostMapping("/send-to-members")
    public ResponseEntity<List<SmsMessageResponse>> sendToMembers(
        @Valid @RequestBody SendToMembersRequest request,
        Authentication authentication
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            List<SmsMessage> messages = smsService.sendToMembers(
                user,
                church,
                request.getMemberIds(),
                request.getMessage(),
                request.getScheduledTime()
            );

            List<SmsMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .toList();

            return ResponseEntity.ok(responses);

        } catch (IllegalStateException e) {
            log.error("Error sending SMS to members: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        } catch (Exception e) {
            log.error("Error sending SMS to members: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Send SMS to visitors
     */
    @PostMapping("/send-to-visitors")
    public ResponseEntity<List<SmsMessageResponse>> sendToVisitors(
        @Valid @RequestBody SendToVisitorsRequest request,
        Authentication authentication
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            List<SmsMessage> messages = smsService.sendToVisitors(
                user,
                church,
                request.getVisitorIds(),
                request.getMessage(),
                request.getScheduledTime()
            );

            List<SmsMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .toList();

            return ResponseEntity.ok(responses);

        } catch (IllegalStateException e) {
            log.error("Error sending SMS to visitors: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        } catch (Exception e) {
            log.error("Error sending SMS to visitors: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Send SMS to fellowship
     */
    @PostMapping("/send-to-fellowship")
    public ResponseEntity<List<SmsMessageResponse>> sendToFellowship(
        @Valid @RequestBody SendToFellowshipRequest request,
        Authentication authentication
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            List<SmsMessage> messages = smsService.sendToFellowship(
                user,
                church,
                request.getFellowshipId(),
                request.getMessage(),
                request.getScheduledTime()
            );

            List<SmsMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .toList();

            return ResponseEntity.ok(responses);

        } catch (IllegalStateException e) {
            log.error("Error sending SMS to fellowship: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        } catch (IllegalArgumentException e) {
            log.error("Fellowship not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Error sending SMS to fellowship: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Send SMS to all members in the church
     */
    @PostMapping("/send-to-all-members")
    public ResponseEntity<List<SmsMessageResponse>> sendToAllMembers(
        @Valid @RequestBody SendToAllMembersRequest request,
        Authentication authentication
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            List<SmsMessage> messages = smsService.sendToAllMembers(
                user,
                church,
                request.getMessage(),
                request.getScheduledTime()
            );

            List<SmsMessageResponse> responses = messages.stream()
                .map(this::mapToResponse)
                .toList();

            return ResponseEntity.ok(responses);

        } catch (IllegalStateException e) {
            log.error("Error sending SMS to all members: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).build();
        } catch (Exception e) {
            log.error("Error sending SMS to all members: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get SMS history
     */
    @GetMapping("/history")
    public ResponseEntity<Page<SmsMessageResponse>> getSmsHistory(
        Authentication authentication,
        Pageable pageable
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Long churchId = TenantContext.getCurrentChurchId();

            Page<SmsMessage> messages = smsService.getSmsHistory(user.getId(), churchId, pageable);
            Page<SmsMessageResponse> responses = messages.map(this::mapToResponse);

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error fetching SMS history: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get SMS by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SmsMessageResponse> getSmsById(@PathVariable Long id) {
        try {
            SmsMessage smsMessage = smsService.getSmsById(id);
            return ResponseEntity.ok(mapToResponse(smsMessage));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching SMS: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Cancel scheduled SMS
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelScheduledSms(
        @PathVariable Long id,
        Authentication authentication
    ) {
        try {
            User user = getUserFromAuth(authentication);
            Church church = getChurch();

            smsService.cancelScheduledSms(id, user, church);
            return ResponseEntity.ok().build();

        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error cancelling SMS: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get SMS statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSmsStats(Authentication authentication) {
        try {
            User user = getUserFromAuth(authentication);
            Long churchId = TenantContext.getCurrentChurchId();

            Map<String, Object> stats = smsService.getSmsStats(user.getId(), churchId);
            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("Error fetching SMS stats: {}", e.getMessage(), e);
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

    private SmsMessageResponse mapToResponse(SmsMessage smsMessage) {
        SmsMessageResponse response = new SmsMessageResponse();
        response.setId(smsMessage.getId());
        response.setSenderId(smsMessage.getSender().getId());
        response.setSenderName(smsMessage.getSender().getName());

        if (smsMessage.getRecipient() != null) {
            response.setMemberId(smsMessage.getRecipient().getId());
        }

        response.setRecipientPhone(smsMessage.getRecipientPhone());
        response.setRecipientName(smsMessage.getRecipientName());
        response.setMessage(smsMessage.getMessage());
        response.setMessageCount(smsMessage.getMessageCount());
        response.setCost(smsMessage.getCost());
        response.setStatus(smsMessage.getStatus());
        response.setGatewayMessageId(smsMessage.getGatewayMessageId());
        response.setDeliveryStatus(smsMessage.getDeliveryStatus());
        response.setDeliveryTime(smsMessage.getDeliveryTime());
        response.setScheduledTime(smsMessage.getScheduledTime());
        response.setSentAt(smsMessage.getSentAt());
        response.setCreatedAt(smsMessage.getCreatedAt());

        return response;
    }
}
