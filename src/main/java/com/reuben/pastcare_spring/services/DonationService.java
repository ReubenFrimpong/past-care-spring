package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.DonationRequest;
import com.reuben.pastcare_spring.dtos.DonationResponse;
import com.reuben.pastcare_spring.dtos.DonationSummaryResponse;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Giving Module Phase 1: Donation Recording
 * Service for managing donations
 */
@Service
@RequiredArgsConstructor
public class DonationService {

  private final DonationRepository donationRepository;
  private final MemberRepository memberRepository;
  private final UserRepository userRepository;
  private final ChurchRepository churchRepository;

  /**
   * Get all donations for a church
   */
  public List<DonationResponse> getAllDonations(Long churchId) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

    return donationRepository.findByChurch(church).stream()
        .map(DonationResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get donation by ID
   */
  public DonationResponse getDonationById(Long id) {
    Donation donation = donationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + id));
    return DonationResponse.fromEntity(donation);
  }

  /**
   * Create a new donation
   */
  @Transactional
  public DonationResponse createDonation(Long churchId, Long userId, DonationRequest request) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

    Donation donation = new Donation();
    donation.setChurch(church);
    donation.setRecordedBy(user);

    updateDonationFromRequest(donation, request);

    Donation saved = donationRepository.save(donation);
    return DonationResponse.fromEntity(saved);
  }

  /**
   * Update an existing donation
   */
  @Transactional
  public DonationResponse updateDonation(Long id, DonationRequest request) {
    Donation donation = donationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + id));

    updateDonationFromRequest(donation, request);

    Donation updated = donationRepository.save(donation);
    return DonationResponse.fromEntity(updated);
  }

  /**
   * Delete a donation
   */
  @Transactional
  public void deleteDonation(Long id) {
    Donation donation = donationRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + id));
    donationRepository.delete(donation);
  }

  /**
   * Get donations by date range
   */
  public List<DonationResponse> getDonationsByDateRange(Long churchId, LocalDate startDate, LocalDate endDate) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

    return donationRepository.findByChurchAndDateRange(church, startDate, endDate).stream()
        .map(DonationResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get donations by member
   */
  public List<DonationResponse> getDonationsByMember(Long churchId, Long memberId) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + memberId));

    return donationRepository.findByMemberAndChurch(member, church).stream()
        .map(DonationResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get donations by type
   */
  public List<DonationResponse> getDonationsByType(Long churchId, DonationType donationType) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

    return donationRepository.findByChurchAndDonationType(church, donationType).stream()
        .map(DonationResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get donations by campaign
   */
  public List<DonationResponse> getDonationsByCampaign(Long churchId, String campaign) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

    return donationRepository.findByChurchAndCampaign(church, campaign).stream()
        .map(DonationResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get donation summary for a church
   */
  public DonationSummaryResponse getDonationSummary(Long churchId) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

    List<Donation> allDonations = donationRepository.findByChurch(church);

    if (allDonations.isEmpty()) {
      return new DonationSummaryResponse(
          BigDecimal.ZERO, 0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L, 0L
      );
    }

    BigDecimal totalAmount = allDonations.stream()
        .map(Donation::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    long donationCount = allDonations.size();

    BigDecimal averageDonation = totalAmount.divide(
        BigDecimal.valueOf(donationCount), 2, RoundingMode.HALF_UP
    );

    BigDecimal largestDonation = allDonations.stream()
        .map(Donation::getAmount)
        .max(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);

    BigDecimal smallestDonation = allDonations.stream()
        .map(Donation::getAmount)
        .min(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);

    long uniqueDonors = allDonations.stream()
        .filter(d -> d.getMember() != null)
        .map(Donation::getMember)
        .distinct()
        .count();

    long anonymousDonations = allDonations.stream()
        .filter(Donation::getIsAnonymous)
        .count();

    return new DonationSummaryResponse(
        totalAmount,
        donationCount,
        averageDonation,
        largestDonation,
        smallestDonation,
        uniqueDonors,
        anonymousDonations
    );
  }

  /**
   * Get donation summary by date range
   */
  public DonationSummaryResponse getDonationSummaryByDateRange(Long churchId, LocalDate startDate, LocalDate endDate) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));

    List<Donation> donations = donationRepository.findByChurchAndDateRange(church, startDate, endDate);

    if (donations.isEmpty()) {
      return new DonationSummaryResponse(
          BigDecimal.ZERO, 0L, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0L, 0L
      );
    }

    BigDecimal totalAmount = donations.stream()
        .map(Donation::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    long donationCount = donations.size();

    BigDecimal averageDonation = totalAmount.divide(
        BigDecimal.valueOf(donationCount), 2, RoundingMode.HALF_UP
    );

    BigDecimal largestDonation = donations.stream()
        .map(Donation::getAmount)
        .max(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);

    BigDecimal smallestDonation = donations.stream()
        .map(Donation::getAmount)
        .min(BigDecimal::compareTo)
        .orElse(BigDecimal.ZERO);

    long uniqueDonors = donations.stream()
        .filter(d -> d.getMember() != null)
        .map(Donation::getMember)
        .distinct()
        .count();

    long anonymousDonations = donations.stream()
        .filter(Donation::getIsAnonymous)
        .count();

    return new DonationSummaryResponse(
        totalAmount,
        donationCount,
        averageDonation,
        largestDonation,
        smallestDonation,
        uniqueDonors,
        anonymousDonations
    );
  }

  /**
   * Issue receipt for a donation
   */
  @Transactional
  public DonationResponse issueReceipt(Long donationId, String receiptNumber) {
    Donation donation = donationRepository.findById(donationId)
        .orElseThrow(() -> new IllegalArgumentException("Donation not found with id: " + donationId));

    donation.setReceiptIssued(true);
    donation.setReceiptNumber(receiptNumber);

    Donation updated = donationRepository.save(donation);
    return DonationResponse.fromEntity(updated);
  }

  /**
   * Helper method to update donation from request DTO
   */
  private void updateDonationFromRequest(Donation donation, DonationRequest request) {
    // Set member (nullable for anonymous donations)
    if (request.memberId() != null) {
      Member member = memberRepository.findById(request.memberId())
          .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.memberId()));
      donation.setMember(member);
    } else {
      donation.setMember(null);
    }

    donation.setAmount(request.amount());
    donation.setDonationDate(request.donationDate());
    donation.setDonationType(request.donationType());
    donation.setPaymentMethod(request.paymentMethod());
    donation.setIsAnonymous(request.isAnonymous());
    donation.setReferenceNumber(request.referenceNumber());
    donation.setNotes(request.notes());
    donation.setCampaign(request.campaign());
    donation.setCurrency(request.currency());
  }
}
