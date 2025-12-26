package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.PledgePaymentRequest;
import com.reuben.pastcare_spring.dtos.PledgeRequest;
import com.reuben.pastcare_spring.dtos.PledgeResponse;
import com.reuben.pastcare_spring.dtos.PledgeStatsResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Giving Module Phase 3: Pledge & Campaign Management
 * Service for managing member pledges
 */
@Service
@RequiredArgsConstructor
public class PledgeService {

  private final PledgeRepository pledgeRepository;
  private final PledgePaymentRepository pledgePaymentRepository;
  private final MemberRepository memberRepository;
  private final CampaignRepository campaignRepository;
  private final ChurchRepository churchRepository;
  private final DonationRepository donationRepository;
  private final CampaignService campaignService;

  /**
   * Get all pledges for a church
   */
  public List<PledgeResponse> getAllPledges(Long churchId) {
    Church church = getChurch(churchId);
    return pledgeRepository.findByChurch(church).stream()
        .map(PledgeResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get pledge by ID
   */
  public PledgeResponse getPledgeById(Long churchId, Long id) {
    Church church = getChurch(churchId);
    Pledge pledge = pledgeRepository.findByIdAndChurch(id, church)
        .orElseThrow(() -> new IllegalArgumentException("Pledge not found with id: " + id));
    return PledgeResponse.fromEntity(pledge);
  }

  /**
   * Get pledges by member
   */
  public List<PledgeResponse> getPledgesByMember(Long churchId, Long memberId) {
    Church church = getChurch(churchId);
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

    return pledgeRepository.findByMemberAndChurch(member, church).stream()
        .map(PledgeResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get pledges by campaign
   */
  public List<PledgeResponse> getPledgesByCampaign(Long churchId, Long campaignId) {
    Church church = getChurch(churchId);
    Campaign campaign = campaignRepository.findByIdAndChurch(campaignId, church)
        .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id: " + campaignId));

    return pledgeRepository.findByCampaignAndChurch(campaign, church).stream()
        .map(PledgeResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Create a new pledge
   */
  @Transactional
  public PledgeResponse createPledge(Long churchId, PledgeRequest request) {
    Church church = getChurch(churchId);
    Member member = memberRepository.findById(request.getMemberId())
        .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.getMemberId()));

    Pledge pledge = new Pledge();
    pledge.setChurch(church);
    pledge.setMember(member);

    if (request.getCampaignId() != null) {
      Campaign campaign = campaignRepository.findByIdAndChurch(request.getCampaignId(), church)
          .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id: " + request.getCampaignId()));
      pledge.setCampaign(campaign);
    }

    updatePledgeFromRequest(pledge, request);

    // Calculate next payment date
    pledge.setNextPaymentDate(calculateNextPaymentDate(request.getStartDate(), request.getFrequency()));

    // Initialize amount remaining
    pledge.setAmountRemaining(pledge.getTotalAmount());

    Pledge saved = pledgeRepository.save(pledge);

    // Update campaign progress if pledged to a campaign
    if (saved.getCampaign() != null) {
      campaignService.updateCampaignProgress(saved.getCampaign().getId());
    }

    return PledgeResponse.fromEntity(saved);
  }

  /**
   * Update an existing pledge
   */
  @Transactional
  public PledgeResponse updatePledge(Long churchId, Long id, PledgeRequest request) {
    Church church = getChurch(churchId);
    Pledge pledge = pledgeRepository.findByIdAndChurch(id, church)
        .orElseThrow(() -> new IllegalArgumentException("Pledge not found with id: " + id));

    updatePledgeFromRequest(pledge, request);
    Pledge saved = pledgeRepository.save(pledge);

    // Update campaign progress
    if (saved.getCampaign() != null) {
      campaignService.updateCampaignProgress(saved.getCampaign().getId());
    }

    return PledgeResponse.fromEntity(saved);
  }

  /**
   * Delete a pledge
   */
  @Transactional
  public void deletePledge(Long churchId, Long id) {
    Church church = getChurch(churchId);
    Pledge pledge = pledgeRepository.findByIdAndChurch(id, church)
        .orElseThrow(() -> new IllegalArgumentException("Pledge not found with id: " + id));

    Long campaignId = pledge.getCampaign() != null ? pledge.getCampaign().getId() : null;

    pledgeRepository.delete(pledge);

    // Update campaign progress
    if (campaignId != null) {
      campaignService.updateCampaignProgress(campaignId);
    }
  }

  /**
   * Record a pledge payment
   */
  @Transactional
  public PledgeResponse recordPayment(Long churchId, PledgePaymentRequest request) {
    Church church = getChurch(churchId);
    Pledge pledge = pledgeRepository.findByIdAndChurch(request.getPledgeId(), church)
        .orElseThrow(() -> new IllegalArgumentException("Pledge not found with id: " + request.getPledgeId()));

    // Update pledge amounts
    pledge.setAmountPaid(pledge.getAmountPaid().add(request.getAmount()));
    pledge.setAmountRemaining(pledge.getTotalAmount().subtract(pledge.getAmountPaid()));
    pledge.setPaymentsMade(pledge.getPaymentsMade() + 1);
    pledge.setLastPaymentDate(request.getPaymentDate());

    // Calculate next payment date
    if (pledge.getFrequency() != PledgeFrequency.ONE_TIME) {
      pledge.setNextPaymentDate(
          calculateNextPaymentDate(request.getPaymentDate(), pledge.getFrequency())
      );
    }

    // Update status if fully paid
    if (pledge.isFullyPaid()) {
      pledge.setStatus(PledgeStatus.COMPLETED);
    }

    // Create pledge payment record
    PledgePayment payment = new PledgePayment();
    payment.setChurch(church);
    payment.setPledge(pledge);
    payment.setAmount(request.getAmount());
    payment.setPaymentDate(request.getPaymentDate());
    payment.setStatus(PledgePaymentStatus.PAID);
    payment.setNotes(request.getNotes());

    // Link to donation if provided
    if (request.getDonationId() != null) {
      Donation donation = donationRepository.findById(request.getDonationId())
          .orElseThrow(() -> new IllegalArgumentException("Donation not found"));
      payment.setDonation(donation);
    }

    pledgePaymentRepository.save(payment);

    Pledge saved = pledgeRepository.save(pledge);

    // Update campaign progress
    if (saved.getCampaign() != null) {
      campaignService.updateCampaignProgress(saved.getCampaign().getId());
    }

    return PledgeResponse.fromEntity(saved);
  }

  /**
   * Cancel a pledge
   */
  @Transactional
  public PledgeResponse cancelPledge(Long churchId, Long id) {
    Church church = getChurch(churchId);
    Pledge pledge = pledgeRepository.findByIdAndChurch(id, church)
        .orElseThrow(() -> new IllegalArgumentException("Pledge not found with id: " + id));

    pledge.setStatus(PledgeStatus.CANCELLED);
    Pledge saved = pledgeRepository.save(pledge);

    // Update campaign progress
    if (saved.getCampaign() != null) {
      campaignService.updateCampaignProgress(saved.getCampaign().getId());
    }

    return PledgeResponse.fromEntity(saved);
  }

  /**
   * Get active pledges
   */
  public List<PledgeResponse> getActivePledges(Long churchId) {
    Church church = getChurch(churchId);
    return pledgeRepository.findActivePledges(church).stream()
        .map(PledgeResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get overdue pledges
   */
  public List<PledgeResponse> getOverduePledges(Long churchId) {
    Church church = getChurch(churchId);
    LocalDate today = LocalDate.now();
    return pledgeRepository.findOverduePledges(church, today).stream()
        .map(PledgeResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get pledge statistics
   */
  public PledgeStatsResponse getPledgeStats(Long churchId) {
    Church church = getChurch(churchId);

    List<Pledge> allPledges = pledgeRepository.findByChurch(church);

    long totalPledges = allPledges.size();
    long activePledges = pledgeRepository.countActivePledges(church);
    long completedPledges = allPledges.stream()
        .filter(p -> p.getStatus() == PledgeStatus.COMPLETED)
        .count();
    long overduePledges = pledgeRepository.findOverduePledges(church, LocalDate.now()).size();

    BigDecimal totalPledgedAmount = allPledges.stream()
        .filter(p -> p.getStatus() != PledgeStatus.CANCELLED)
        .map(Pledge::getTotalAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalPaidAmount = allPledges.stream()
        .map(Pledge::getAmountPaid)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalRemainingAmount = allPledges.stream()
        .filter(p -> p.getStatus() == PledgeStatus.ACTIVE)
        .map(Pledge::getAmountRemaining)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    double averageCompletionRate = allPledges.stream()
        .filter(p -> p.getStatus() != PledgeStatus.CANCELLED)
        .mapToDouble(Pledge::getProgressPercentage)
        .average()
        .orElse(0.0);

    int totalPayments = allPledges.stream()
        .mapToInt(Pledge::getPaymentsMade)
        .sum();

    return new PledgeStatsResponse(
        totalPledges,
        activePledges,
        completedPledges,
        overduePledges,
        totalPledgedAmount,
        totalPaidAmount,
        totalRemainingAmount,
        averageCompletionRate,
        totalPayments
    );
  }

  // Helper methods

  private void updatePledgeFromRequest(Pledge pledge, PledgeRequest request) {
    pledge.setTotalAmount(request.getTotalAmount());
    pledge.setCurrency(request.getCurrency());
    pledge.setFrequency(request.getFrequency());
    pledge.setInstallments(request.getInstallments());
    pledge.setPledgeDate(request.getPledgeDate());
    pledge.setStartDate(request.getStartDate());
    pledge.setEndDate(request.getEndDate());
    pledge.setNotes(request.getNotes());
    pledge.setSendReminders(request.getSendReminders());
    pledge.setReminderDaysBefore(request.getReminderDaysBefore());
  }

  private LocalDate calculateNextPaymentDate(LocalDate currentDate, PledgeFrequency frequency) {
    return switch (frequency) {
      case ONE_TIME -> null;
      case WEEKLY -> currentDate.plusWeeks(1);
      case BIWEEKLY -> currentDate.plusWeeks(2);
      case MONTHLY -> currentDate.plusMonths(1);
      case QUARTERLY -> currentDate.plusMonths(3);
      case YEARLY -> currentDate.plusYears(1);
    };
  }

  private Church getChurch(Long churchId) {
    return churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
  }
}
