package com.reuben.pastcare_spring.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.reuben.pastcare_spring.config.PaystackConfig;
import com.reuben.pastcare_spring.dtos.RecurringDonationRequest;
import com.reuben.pastcare_spring.dtos.RecurringDonationResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.RecurringDonationRepository;
import com.reuben.pastcare_spring.repositories.PaymentTransactionRepository;
import com.reuben.pastcare_spring.repositories.DonationRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing recurring donations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringDonationService {

    private final RecurringDonationRepository recurringDonationRepository;
    private final MemberRepository memberRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final DonationRepository donationRepository;
    private final ChurchRepository churchRepository;
    private final UserRepository userRepository;
    private final PaystackService paystackService;
    private final PaystackConfig paystackConfig;

    /**
     * Create a new recurring donation
     */
    @Transactional
    public RecurringDonationResponse createRecurringDonation(RecurringDonationRequest request) {
        Long churchId = TenantContext.getCurrentChurchId();

        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new RuntimeException("Church not found"));
        Member member = memberRepository.findByIdAndChurch(request.getMemberId(), church)
            .orElseThrow(() -> new RuntimeException("Member not found"));

        // Create recurring donation entity
        RecurringDonation recurringDonation = new RecurringDonation();
        recurringDonation.setChurch(church);
        recurringDonation.setMember(member);
        recurringDonation.setAmount(request.getAmount());
        recurringDonation.setDonationType(request.getDonationType());
        recurringDonation.setFrequency(request.getFrequency());
        recurringDonation.setStartDate(request.getStartDate());
        recurringDonation.setEndDate(request.getEndDate());
        recurringDonation.setCurrency(request.getCurrency());
        recurringDonation.setCampaign(request.getCampaign());
        recurringDonation.setNotes(request.getNotes());
        recurringDonation.setStatus(RecurringDonationStatus.ACTIVE);

        // Set Paystack details
        recurringDonation.setPaystackAuthorizationCode(request.getPaystackAuthorizationCode());
        recurringDonation.setPaystackCustomerCode(request.getPaystackCustomerCode());
        recurringDonation.setPaystackPlanCode(request.getPaystackPlanCode());
        recurringDonation.setCardLast4(request.getCardLast4());
        recurringDonation.setCardBrand(request.getCardBrand());
        recurringDonation.setCardBin(request.getCardBin());

        // Calculate next charge date
        recurringDonation.setNextChargeDate(calculateNextChargeDate(request.getStartDate(), request.getFrequency()));

        RecurringDonation saved = recurringDonationRepository.save(recurringDonation);

        log.info("Recurring donation created: {} for member: {}", saved.getId(), member.getId());
        return RecurringDonationResponse.fromEntity(saved);
    }

    /**
     * Get all recurring donations for a church
     */
    public Page<RecurringDonationResponse> getRecurringDonations(Pageable pageable) {
        Long churchId = TenantContext.getCurrentChurchId();
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new RuntimeException("Church not found"));
        return recurringDonationRepository.findByChurch(church, pageable)
            .map(RecurringDonationResponse::fromEntity);
    }

    /**
     * Get recurring donations by member
     */
    public Page<RecurringDonationResponse> getRecurringDonationsByMember(Long memberId, Pageable pageable) {
        Long churchId = TenantContext.getCurrentChurchId();
        Church church = churchRepository.findById(churchId)
            .orElseThrow(() -> new RuntimeException("Church not found"));
        Member member = memberRepository.findByIdAndChurch(memberId, church)
            .orElseThrow(() -> new RuntimeException("Member not found"));
        return recurringDonationRepository.findByChurchAndMember(church, member, pageable)
            .map(RecurringDonationResponse::fromEntity);
    }

    /**
     * Get recurring donation by ID
     */
    public RecurringDonationResponse getRecurringDonation(Long id) {
        Long churchId = TenantContext.getCurrentChurchId();
        RecurringDonation recurringDonation = churchRepository.findById(churchId).flatMap(church -> recurringDonationRepository.findByIdAndChurch(id, church))
            .orElseThrow(() -> new RuntimeException("Recurring donation not found"));
        return RecurringDonationResponse.fromEntity(recurringDonation);
    }

    /**
     * Update recurring donation
     */
    @Transactional
    public RecurringDonationResponse updateRecurringDonation(Long id, RecurringDonationRequest request) {
        Long churchId = TenantContext.getCurrentChurchId();

        RecurringDonation recurringDonation = churchRepository.findById(churchId).flatMap(church -> recurringDonationRepository.findByIdAndChurch(id, church))
            .orElseThrow(() -> new RuntimeException("Recurring donation not found"));

        recurringDonation.setAmount(request.getAmount());
        recurringDonation.setDonationType(request.getDonationType());
        recurringDonation.setFrequency(request.getFrequency());
        recurringDonation.setEndDate(request.getEndDate());
        recurringDonation.setCampaign(request.getCampaign());
        recurringDonation.setNotes(request.getNotes());

        RecurringDonation updated = recurringDonationRepository.save(recurringDonation);
        log.info("Recurring donation updated: {}", id);

        return RecurringDonationResponse.fromEntity(updated);
    }

    /**
     * Pause a recurring donation
     */
    @Transactional
    public void pauseRecurringDonation(Long id) {
        Long churchId = TenantContext.getCurrentChurchId();
        RecurringDonation recurringDonation = churchRepository.findById(churchId).flatMap(church -> recurringDonationRepository.findByIdAndChurch(id, church))
            .orElseThrow(() -> new RuntimeException("Recurring donation not found"));

        recurringDonation.setStatus(RecurringDonationStatus.PAUSED);
        recurringDonationRepository.save(recurringDonation);

        log.info("Recurring donation paused: {}", id);
    }

    /**
     * Resume a paused recurring donation
     */
    @Transactional
    public void resumeRecurringDonation(Long id) {
        Long churchId = TenantContext.getCurrentChurchId();
        RecurringDonation recurringDonation = churchRepository.findById(churchId).flatMap(church -> recurringDonationRepository.findByIdAndChurch(id, church))
            .orElseThrow(() -> new RuntimeException("Recurring donation not found"));

        if (recurringDonation.getStatus() == RecurringDonationStatus.PAUSED) {
            recurringDonation.setStatus(RecurringDonationStatus.ACTIVE);
            recurringDonationRepository.save(recurringDonation);
            log.info("Recurring donation resumed: {}", id);
        }
    }

    /**
     * Cancel a recurring donation
     */
    @Transactional
    public void cancelRecurringDonation(Long id) {
        Long churchId = TenantContext.getCurrentChurchId();
        RecurringDonation recurringDonation = churchRepository.findById(churchId).flatMap(church -> recurringDonationRepository.findByIdAndChurch(id, church))
            .orElseThrow(() -> new RuntimeException("Recurring donation not found"));

        recurringDonation.setStatus(RecurringDonationStatus.CANCELLED);
        recurringDonationRepository.save(recurringDonation);

        log.info("Recurring donation cancelled: {}", id);
    }

    /**
     * Delete a recurring donation
     */
    @Transactional
    public void deleteRecurringDonation(Long id) {
        Long churchId = TenantContext.getCurrentChurchId();
        RecurringDonation recurringDonation = churchRepository.findById(churchId).flatMap(church -> recurringDonationRepository.findByIdAndChurch(id, church))
            .orElseThrow(() -> new RuntimeException("Recurring donation not found"));

        recurringDonationRepository.delete(recurringDonation);
        log.info("Recurring donation deleted: {}", id);
    }

    /**
     * Process recurring donations (scheduled task - runs daily at 2 AM)
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void processRecurringDonations() {
        log.info("Starting recurring donations processing...");

        // Get all churches and process each tenant's recurring donations
        // Note: This is a simplified version - in production, you'd iterate through all churches
        Long churchId = TenantContext.getCurrentChurchId();
        if (churchId == null) {
            log.warn("No church context set for recurring donations processing");
            return;
        }

        processRecurringDonationsForChurch(churchId);
    }

    /**
     * Process recurring donations for a specific church
     */
    private void processRecurringDonationsForChurch(Long churchId) {
        LocalDate today = LocalDate.now();

        // Find all recurring donations due for charging
        List<RecurringDonation> dueRecurringDonations = recurringDonationRepository.findDueForCharging(churchId, today);

        log.info("Found {} recurring donations due for processing in church {}", dueRecurringDonations.size(), churchId);

        for (RecurringDonation recurringDonation : dueRecurringDonations) {
            try {
                processRecurringDonation(recurringDonation);
            } catch (Exception e) {
                log.error("Error processing recurring donation: {}", recurringDonation.getId(), e);
            }
        }
    }

    /**
     * Process a single recurring donation
     */
    @Transactional
    public void processRecurringDonation(RecurringDonation recurringDonation) {
        log.info("Processing recurring donation: {}", recurringDonation.getId());

        // Check if end date reached
        if (recurringDonation.getEndDate() != null &&
            LocalDate.now().isAfter(recurringDonation.getEndDate())) {
            recurringDonation.setStatus(RecurringDonationStatus.COMPLETED);
            recurringDonationRepository.save(recurringDonation);
            log.info("Recurring donation completed (end date reached): {}", recurringDonation.getId());
            return;
        }

        // Generate unique reference
        String reference = "REC-" + recurringDonation.getId() + "-" + UUID.randomUUID().toString();

        // Create payment transaction record
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setChurch(recurringDonation.getChurch());
        transaction.setMember(recurringDonation.getMember());
        transaction.setRecurringDonation(recurringDonation);
        transaction.setAmount(recurringDonation.getAmount());
        transaction.setCurrency(recurringDonation.getCurrency());
        transaction.setDonationType(recurringDonation.getDonationType());
        transaction.setPaymentMethod(PaymentMethod.ONLINE);
        transaction.setStatus(PaymentTransactionStatus.PROCESSING);
        transaction.setPaymentReference(reference);
        transaction.setCustomerEmail(recurringDonation.getMember().getEmail());
        transaction.setCustomerPhone(recurringDonation.getMember().getPhoneNumber());
        transaction.setCampaign(recurringDonation.getCampaign());

        PaymentTransaction savedTransaction = paymentTransactionRepository.save(transaction);

        try {
            // Charge the authorization
            JsonNode response = paystackService.chargeAuthorization(
                recurringDonation.getPaystackAuthorizationCode(),
                recurringDonation.getAmount(),
                recurringDonation.getMember().getEmail(),
                reference
            );

            // Check if charge was successful
            boolean success = response.get("status").asBoolean();
            JsonNode data = response.has("data") ? response.get("data") : null;

            if (success && data != null && "success".equals(data.get("status").asText())) {
                handleSuccessfulPayment(recurringDonation, savedTransaction, data);
            } else {
                handleFailedPayment(recurringDonation, savedTransaction, response.get("message").asText());
            }

        } catch (Exception e) {
            handleFailedPayment(recurringDonation, savedTransaction, e.getMessage());
        }
    }

    /**
     * Handle successful payment
     */
    private void handleSuccessfulPayment(RecurringDonation recurringDonation,
                                        PaymentTransaction transaction, JsonNode data) {
        // Update transaction
        transaction.setStatus(PaymentTransactionStatus.SUCCESS);
        transaction.setPaidAt(LocalDateTime.now());
        transaction.setPaystackReference(data.get("reference").asText());
        if (data.has("id")) {
            transaction.setPaystackTransactionId(data.get("id").asText());
        }
        transaction.setGatewayResponse(data.toString());
        paymentTransactionRepository.save(transaction);

        // Create donation record
        Donation donation = new Donation();
        donation.setChurch(recurringDonation.getChurch());
        donation.setMember(recurringDonation.getMember());
        donation.setAmount(recurringDonation.getAmount());
        donation.setDonationType(recurringDonation.getDonationType());
        donation.setPaymentMethod(PaymentMethod.ONLINE);
        donation.setCurrency(recurringDonation.getCurrency());
        donation.setCampaign(recurringDonation.getCampaign());
        donation.setDonationDate(LocalDate.now());
        donation.setNotes("Auto-generated from recurring donation #" + recurringDonation.getId());
        donationRepository.save(donation);

        // Link transaction to donation
        transaction.setDonation(donation);
        paymentTransactionRepository.save(transaction);

        // Update recurring donation statistics
        recurringDonation.setTotalPayments(recurringDonation.getTotalPayments() + 1);
        recurringDonation.setTotalAmountPaid(
            recurringDonation.getTotalAmountPaid().add(recurringDonation.getAmount())
        );
        recurringDonation.setLastChargeDate(LocalDateTime.now());
        recurringDonation.setConsecutiveFailures(0); // Reset failures
        recurringDonation.setNextChargeDate(
            calculateNextChargeDate(recurringDonation.getNextChargeDate(), recurringDonation.getFrequency())
        );

        recurringDonationRepository.save(recurringDonation);

        log.info("Recurring donation processed successfully: {}", recurringDonation.getId());
    }

    /**
     * Handle failed payment
     */
    private void handleFailedPayment(RecurringDonation recurringDonation,
                                     PaymentTransaction transaction, String errorMessage) {
        // Update transaction
        transaction.setStatus(PaymentTransactionStatus.FAILED);
        transaction.setFailedAt(LocalDateTime.now());
        transaction.setFailureReason(errorMessage);
        transaction.setGatewayMessage(errorMessage);

        // Calculate next retry
        int retryCount = transaction.getRetryCount() + 1;
        transaction.setRetryCount(retryCount);

        if (retryCount < paystackConfig.getMaxRetryAttempts()) {
            // Schedule retry with exponential backoff
            long delayMinutes = paystackConfig.getInitialRetryDelayMinutes() * (long)Math.pow(2, retryCount - 1);
            long maxDelayMinutes = paystackConfig.getMaxRetryDelayHours() * 60;
            delayMinutes = Math.min(delayMinutes, maxDelayMinutes);

            transaction.setNextRetryAt(LocalDateTime.now().plusMinutes(delayMinutes));
            log.info("Payment failed, scheduling retry {} in {} minutes", retryCount, delayMinutes);
        } else {
            log.warn("Payment failed after {} retries, no more retries", retryCount);
        }

        paymentTransactionRepository.save(transaction);

        // Update recurring donation failure tracking
        recurringDonation.setConsecutiveFailures(recurringDonation.getConsecutiveFailures() + 1);
        recurringDonation.setLastFailureDate(LocalDateTime.now());
        recurringDonation.setLastFailureReason(errorMessage);

        // If too many consecutive failures, mark as failed
        if (recurringDonation.getConsecutiveFailures() >= 5) {
            recurringDonation.setStatus(RecurringDonationStatus.FAILED);
            log.error("Recurring donation marked as FAILED after {} consecutive failures: {}",
                     recurringDonation.getConsecutiveFailures(), recurringDonation.getId());
        }

        recurringDonationRepository.save(recurringDonation);
    }

    /**
     * Retry failed payment transactions (scheduled task - runs hourly)
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void retryFailedPayments() {
        log.info("Starting failed payment retry process...");

        Long churchId = TenantContext.getCurrentChurchId();
        if (churchId == null) {
            return;
        }

        List<PaymentTransaction> failedTransactions = paymentTransactionRepository.findReadyForRetry(
            churchId,
            LocalDateTime.now(),
            paystackConfig.getMaxRetryAttempts()
        );

        log.info("Found {} failed transactions ready for retry", failedTransactions.size());

        for (PaymentTransaction transaction : failedTransactions) {
            try {
                if (transaction.getRecurringDonation() != null) {
                    processRecurringDonation(transaction.getRecurringDonation());
                }
            } catch (Exception e) {
                log.error("Error retrying payment transaction: {}", transaction.getId(), e);
            }
        }
    }

    /**
     * Calculate next charge date based on frequency
     */
    private LocalDate calculateNextChargeDate(LocalDate currentDate, RecurringFrequency frequency) {
        return switch (frequency) {
            case WEEKLY -> currentDate.plusWeeks(1);
            case BIWEEKLY -> currentDate.plusWeeks(2);
            case MONTHLY -> currentDate.plusMonths(1);
            case QUARTERLY -> currentDate.plusMonths(3);
            case YEARLY -> currentDate.plusYears(1);
        };
    }
}
