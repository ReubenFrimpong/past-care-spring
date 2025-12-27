package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.FellowshipRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.SmsMessageRepository;
import com.reuben.pastcare_spring.repositories.SmsTemplateRepository;
import com.reuben.pastcare_spring.repositories.VisitorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class SmsService {

    private final SmsMessageRepository smsMessageRepository;
    private final SmsTemplateRepository smsTemplateRepository;
    private final SmsCreditService smsCreditService;
    private final SmsGatewayRouter smsGatewayRouter;
    private final PhoneNumberService phoneNumberService;
    private final MemberRepository memberRepository;
    private final VisitorRepository visitorRepository;
    private final FellowshipRepository fellowshipRepository;

    public SmsService(
        SmsMessageRepository smsMessageRepository,
        SmsTemplateRepository smsTemplateRepository,
        SmsCreditService smsCreditService,
        SmsGatewayRouter smsGatewayRouter,
        PhoneNumberService phoneNumberService,
        MemberRepository memberRepository,
        VisitorRepository visitorRepository,
        FellowshipRepository fellowshipRepository
    ) {
        this.smsMessageRepository = smsMessageRepository;
        this.smsTemplateRepository = smsTemplateRepository;
        this.smsCreditService = smsCreditService;
        this.smsGatewayRouter = smsGatewayRouter;
        this.phoneNumberService = phoneNumberService;
        this.memberRepository = memberRepository;
        this.visitorRepository = visitorRepository;
        this.fellowshipRepository = fellowshipRepository;
    }

    /**
     * Send single SMS
     */
    @Transactional
    public SmsMessage sendSms(
        User sender,
        Church church,
        String recipientPhone,
        String recipientName,
        String message,
        Member member,
        LocalDateTime scheduledTime
    ) {
        // Validate phone number
        if (!phoneNumberService.isValidPhoneNumber(recipientPhone)) {
            throw new IllegalArgumentException("Invalid phone number: " + recipientPhone);
        }

        String normalizedPhone = phoneNumberService.normalizePhoneNumber(recipientPhone);

        // Calculate cost
        BigDecimal cost = smsCreditService.calculateSmsCost(normalizedPhone, message);
        int messageCount = phoneNumberService.calculateMessageCount(message);

        // Check credits
        if (!smsCreditService.hasSufficientCredits(sender.getId(), church.getId(), cost)) {
            throw new IllegalStateException("Insufficient SMS credits. Required: " + cost +
                ", Current balance: " + smsCreditService.getBalance(sender.getId(), church.getId()));
        }

        // Create SMS message record
        SmsMessage smsMessage = new SmsMessage();
        smsMessage.setSender(sender);
        smsMessage.setChurch(church);
        smsMessage.setRecipient(member);
        smsMessage.setRecipientPhone(normalizedPhone);
        smsMessage.setRecipientName(recipientName);
        smsMessage.setMessage(message);
        smsMessage.setMessageCount(messageCount);
        smsMessage.setCost(cost);
        smsMessage.setScheduledTime(scheduledTime);
        smsMessage.setStatus(scheduledTime != null ? SmsStatus.SCHEDULED : SmsStatus.PENDING);

        smsMessage = smsMessageRepository.save(smsMessage);

        // If not scheduled, send immediately
        if (scheduledTime == null) {
            sendImmediately(smsMessage, sender, church);
        }

        return smsMessage;
    }

    /**
     * Send SMS immediately
     */
    @Async
    @Transactional
    public void sendImmediately(SmsMessage smsMessage, User sender, Church church) {
        try {
            smsMessage.setStatus(SmsStatus.SENDING);
            smsMessageRepository.save(smsMessage);

            // Deduct credits
            String referenceId = "SMS-" + smsMessage.getId();
            smsCreditService.deductCredits(
                sender,
                church,
                smsMessage.getCost(),
                "SMS to " + smsMessage.getRecipientPhone(),
                referenceId
            );

            // Send through gateway
            SmsGatewayService.SmsGatewayResponse response = smsGatewayRouter.sendSms(
                smsMessage.getRecipientPhone(),
                smsMessage.getMessage(),
                Map.of("sms_id", smsMessage.getId().toString())
            );

            // Update message status
            if (response.isSuccess()) {
                smsMessage.setStatus(SmsStatus.SENT);
                smsMessage.setGatewayMessageId(response.getMessageId());
                smsMessage.setSentAt(LocalDateTime.now());
            } else {
                smsMessage.setStatus(SmsStatus.FAILED);
                // Refund credits on failure
                smsCreditService.refundCredits(
                    sender,
                    church,
                    smsMessage.getCost(),
                    "Refund for failed SMS",
                    referenceId
                );
            }

            smsMessage.setGatewayResponse(response.getErrorMessage());
            smsMessageRepository.save(smsMessage);

            log.info("SMS sent: {} - Status: {}", smsMessage.getId(), smsMessage.getStatus());

        } catch (Exception e) {
            log.error("Error sending SMS {}: {}", smsMessage.getId(), e.getMessage(), e);
            smsMessage.setStatus(SmsStatus.FAILED);
            smsMessage.setGatewayResponse(e.getMessage());
            smsMessageRepository.save(smsMessage);

            // Refund credits on exception
            try {
                smsCreditService.refundCredits(
                    sender,
                    church,
                    smsMessage.getCost(),
                    "Refund for failed SMS",
                    "SMS-" + smsMessage.getId()
                );
            } catch (Exception refundError) {
                log.error("Error refunding credits: {}", refundError.getMessage());
            }
        }
    }

    /**
     * Send bulk SMS to multiple recipients
     */
    @Transactional
    public List<SmsMessage> sendBulkSms(
        User sender,
        Church church,
        List<String> recipientPhones,
        String message,
        LocalDateTime scheduledTime
    ) {
        // Calculate total cost
        BigDecimal totalCost = recipientPhones.stream()
            .filter(phoneNumberService::isValidPhoneNumber)
            .map(phone -> smsCreditService.calculateSmsCost(phone, message))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Check credits
        if (!smsCreditService.hasSufficientCredits(sender.getId(), church.getId(), totalCost)) {
            throw new IllegalStateException("Insufficient SMS credits. Required: " + totalCost +
                ", Current balance: " + smsCreditService.getBalance(sender.getId(), church.getId()));
        }

        // Send to each recipient
        return recipientPhones.stream()
            .filter(phoneNumberService::isValidPhoneNumber)
            .map(phone -> {
                try {
                    return sendSms(sender, church, phone, null, message, null, scheduledTime);
                } catch (Exception e) {
                    log.error("Error sending bulk SMS to {}: {}", phone, e.getMessage());
                    return null;
                }
            })
            .filter(sms -> sms != null)
            .toList();
    }

    /**
     * Send SMS to members by filter
     */
    @Transactional
    public List<SmsMessage> sendToMembers(
        User sender,
        Church church,
        List<Long> memberIds,
        String message,
        LocalDateTime scheduledTime
    ) {
        List<Member> members = memberRepository.findAllById(memberIds);

        return members.stream()
            .filter(member -> member.getPhoneNumber() != null && !member.getPhoneNumber().isEmpty())
            .map(member -> {
                try {
                    String fullName = member.getFirstName() + " " + member.getLastName();
                    return sendSms(sender, church, member.getPhoneNumber(), fullName, message, member, scheduledTime);
                } catch (Exception e) {
                    log.error("Error sending SMS to member {}: {}", member.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(sms -> sms != null)
            .toList();
    }

    /**
     * Send SMS to visitors
     */
    @Transactional
    public List<SmsMessage> sendToVisitors(
        User sender,
        Church church,
        List<Long> visitorIds,
        String message,
        LocalDateTime scheduledTime
    ) {
        List<Visitor> visitors = visitorRepository.findAllById(visitorIds);

        return visitors.stream()
            .filter(visitor -> visitor.getPhoneNumber() != null && !visitor.getPhoneNumber().isEmpty())
            .map(visitor -> {
                try {
                    String fullName = visitor.getFirstName() + " " + visitor.getLastName();
                    return sendSms(sender, church, visitor.getPhoneNumber(), fullName, message, null, scheduledTime);
                } catch (Exception e) {
                    log.error("Error sending SMS to visitor {}: {}", visitor.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(sms -> sms != null)
            .toList();
    }

    /**
     * Send SMS to all members in a fellowship
     */
    @Transactional
    public List<SmsMessage> sendToFellowship(
        User sender,
        Church church,
        Long fellowshipId,
        String message,
        LocalDateTime scheduledTime
    ) {
        Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
            .orElseThrow(() -> new IllegalArgumentException("Fellowship not found"));

        List<Member> members = fellowship.getMembers();

        return members.stream()
            .filter(member -> member.getPhoneNumber() != null && !member.getPhoneNumber().isEmpty())
            .map(member -> {
                try {
                    String fullName = member.getFirstName() + " " + member.getLastName();
                    return sendSms(sender, church, member.getPhoneNumber(), fullName, message, member, scheduledTime);
                } catch (Exception e) {
                    log.error("Error sending SMS to member {} in fellowship {}: {}", member.getId(), fellowshipId, e.getMessage());
                    return null;
                }
            })
            .filter(sms -> sms != null)
            .toList();
    }

    /**
     * Send SMS to all members in the church
     */
    @Transactional
    public List<SmsMessage> sendToAllMembers(
        User sender,
        Church church,
        String message,
        LocalDateTime scheduledTime
    ) {
        List<Member> members = memberRepository.findByChurchId(church.getId());

        return members.stream()
            .filter(member -> member.getPhoneNumber() != null && !member.getPhoneNumber().isEmpty())
            .map(member -> {
                try {
                    String fullName = member.getFirstName() + " " + member.getLastName();
                    return sendSms(sender, church, member.getPhoneNumber(), fullName, message, member, scheduledTime);
                } catch (Exception e) {
                    log.error("Error sending SMS to member {}: {}", member.getId(), e.getMessage());
                    return null;
                }
            })
            .filter(sms -> sms != null)
            .toList();
    }

    /**
     * Process scheduled SMS
     */
    @Transactional
    public void processScheduledMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<SmsMessage> scheduledMessages = smsMessageRepository
            .findByStatusAndScheduledTimeBefore(SmsStatus.SCHEDULED, now);

        for (SmsMessage smsMessage : scheduledMessages) {
            try {
                sendImmediately(smsMessage, smsMessage.getSender(), smsMessage.getChurch());
            } catch (Exception e) {
                log.error("Error processing scheduled SMS {}: {}", smsMessage.getId(), e.getMessage());
            }
        }
    }

    /**
     * Get SMS history for user
     */
    public Page<SmsMessage> getSmsHistory(Long senderId, Long churchId, Pageable pageable) {
        return smsMessageRepository.findBySenderIdAndChurchId(senderId, churchId, pageable);
    }

    /**
     * Get SMS by ID
     */
    public SmsMessage getSmsById(Long id) {
        return smsMessageRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("SMS not found"));
    }

    /**
     * Cancel scheduled SMS
     */
    @Transactional
    public void cancelScheduledSms(Long smsId, User user, Church church) {
        SmsMessage smsMessage = getSmsById(smsId);

        if (!smsMessage.getSender().getId().equals(user.getId())) {
            throw new IllegalStateException("You can only cancel your own messages");
        }

        if (!smsMessage.getStatus().equals(SmsStatus.SCHEDULED)) {
            throw new IllegalStateException("Only scheduled messages can be cancelled");
        }

        smsMessage.setStatus(SmsStatus.CANCELLED);
        smsMessageRepository.save(smsMessage);
    }

    /**
     * Get SMS statistics
     */
    public Map<String, Object> getSmsStats(Long senderId, Long churchId) {
        long total = smsMessageRepository.countBySenderIdAndChurchId(senderId, churchId);
        long sent = smsMessageRepository.countBySenderIdAndChurchIdAndStatus(senderId, churchId, SmsStatus.SENT);
        long delivered = smsMessageRepository.countBySenderIdAndChurchIdAndStatus(senderId, churchId, SmsStatus.DELIVERED);
        long failed = smsMessageRepository.countBySenderIdAndChurchIdAndStatus(senderId, churchId, SmsStatus.FAILED);

        Double totalCost = smsMessageRepository.sumCostBySenderIdAndChurchIdAndStatusIn(
            senderId, churchId, List.of(SmsStatus.SENT, SmsStatus.DELIVERED));

        return Map.of(
            "total", total,
            "sent", sent,
            "delivered", delivered,
            "failed", failed,
            "totalCost", totalCost != null ? totalCost : 0.0,
            "currentBalance", smsCreditService.getBalance(senderId, churchId)
        );
    }

    // Template methods

    public SmsTemplate createTemplate(SmsTemplate template) {
        return smsTemplateRepository.save(template);
    }

    public SmsTemplate updateTemplate(Long id, SmsTemplate template) {
        SmsTemplate existing = smsTemplateRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));

        existing.setName(template.getName());
        existing.setDescription(template.getDescription());
        existing.setTemplate(template.getTemplate());
        existing.setCategory(template.getCategory());
        existing.setIsActive(template.getIsActive());

        return smsTemplateRepository.save(existing);
    }

    public void deleteTemplate(Long id) {
        smsTemplateRepository.deleteById(id);
    }

    public Page<SmsTemplate> getTemplates(Long churchId, Boolean isActive, Pageable pageable) {
        return smsTemplateRepository.findByChurchIdAndIsActive(churchId, isActive, pageable);
    }

    public SmsTemplate getTemplateById(Long id, Long churchId) {
        return smsTemplateRepository.findByChurchIdAndId(churchId, id)
            .orElseThrow(() -> new IllegalArgumentException("Template not found"));
    }
}
