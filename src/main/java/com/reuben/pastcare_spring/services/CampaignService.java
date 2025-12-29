package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.CampaignRequest;
import com.reuben.pastcare_spring.dtos.CampaignResponse;
import com.reuben.pastcare_spring.dtos.CampaignStatsResponse;
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
 * Service for managing fundraising campaigns
 */
@Service
@RequiredArgsConstructor
public class CampaignService {

  private final CampaignRepository campaignRepository;
  private final ChurchRepository churchRepository;
  private final UserRepository userRepository;
  private final PledgeRepository pledgeRepository;
  private final DonationRepository donationRepository;
  private final TenantValidationService tenantValidationService;

  /**
   * Get all campaigns for a church
   */
  public List<CampaignResponse> getAllCampaigns(Long churchId) {
    Church church = getChurch(churchId);
    return campaignRepository.findByChurch(church).stream()
        .map(CampaignResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get campaign by ID
   */
  public CampaignResponse getCampaignById(Long churchId, Long id) {
    Church church = getChurch(churchId);
    Campaign campaign = campaignRepository.findByIdAndChurch(id, church)
        .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id: " + id));

    // CRITICAL SECURITY: Validate campaign belongs to current church
    tenantValidationService.validateCampaignAccess(campaign);

    return CampaignResponse.fromEntity(campaign);
  }

  /**
   * Create a new campaign
   */
  @Transactional
  public CampaignResponse createCampaign(Long churchId, Long userId, CampaignRequest request) {
    Church church = getChurch(churchId);
    User user = getUser(userId);

    Campaign campaign = new Campaign();
    campaign.setChurch(church);
    campaign.setCreatedBy(user);
    updateCampaignFromRequest(campaign, request);

    Campaign saved = campaignRepository.save(campaign);
    return CampaignResponse.fromEntity(saved);
  }

  /**
   * Update an existing campaign
   */
  @Transactional
  public CampaignResponse updateCampaign(Long churchId, Long id, CampaignRequest request) {
    Church church = getChurch(churchId);
    Campaign campaign = campaignRepository.findByIdAndChurch(id, church)
        .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id: " + id));

    // CRITICAL SECURITY: Validate campaign belongs to current church
    tenantValidationService.validateCampaignAccess(campaign);

    updateCampaignFromRequest(campaign, request);
    Campaign saved = campaignRepository.save(campaign);
    return CampaignResponse.fromEntity(saved);
  }

  /**
   * Delete a campaign
   */
  @Transactional
  public void deleteCampaign(Long churchId, Long id) {
    Church church = getChurch(churchId);
    Campaign campaign = campaignRepository.findByIdAndChurch(id, church)
        .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id: " + id));

    // CRITICAL SECURITY: Validate campaign belongs to current church
    tenantValidationService.validateCampaignAccess(campaign);

    // Check if campaign has associated pledges or donations
    long pledgeCount = pledgeRepository.countByCampaignAndChurch(campaign, church);
    if (pledgeCount > 0) {
      throw new IllegalStateException("Cannot delete campaign with existing pledges. Cancel pledges first.");
    }

    campaignRepository.delete(campaign);
  }

  /**
   * Get campaigns by status
   */
  public List<CampaignResponse> getCampaignsByStatus(Long churchId, CampaignStatus status) {
    Church church = getChurch(churchId);
    return campaignRepository.findByChurchAndStatus(church, status).stream()
        .map(CampaignResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get active campaigns
   */
  public List<CampaignResponse> getActiveCampaigns(Long churchId) {
    Church church = getChurch(churchId);
    return campaignRepository.findActiveCampaigns(church).stream()
        .map(CampaignResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get featured campaigns
   */
  public List<CampaignResponse> getFeaturedCampaigns(Long churchId) {
    Church church = getChurch(churchId);
    return campaignRepository.findFeaturedCampaigns(church).stream()
        .map(CampaignResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get public campaigns (for member portal)
   */
  public List<CampaignResponse> getPublicCampaigns(Long churchId) {
    Church church = getChurch(churchId);
    return campaignRepository.findPublicCampaigns(church).stream()
        .map(CampaignResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Get ongoing campaigns
   */
  public List<CampaignResponse> getOngoingCampaigns(Long churchId) {
    Church church = getChurch(churchId);
    LocalDate today = LocalDate.now();
    return campaignRepository.findOngoingCampaigns(church, today).stream()
        .map(CampaignResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Search campaigns by name
   */
  public List<CampaignResponse> searchCampaignsByName(Long churchId, String name) {
    Church church = getChurch(churchId);
    return campaignRepository.findByChurchAndNameContaining(church, name).stream()
        .map(CampaignResponse::fromEntity)
        .collect(Collectors.toList());
  }

  /**
   * Update campaign progress (called when donations/pledges are made)
   */
  @Transactional
  public void updateCampaignProgress(Long campaignId) {
    Campaign campaign = campaignRepository.findById(campaignId)
        .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id: " + campaignId));

    // CRITICAL SECURITY: Validate campaign belongs to current church
    tenantValidationService.validateCampaignAccess(campaign);

    Church church = campaign.getChurch();

    // Calculate total donations for this campaign
    BigDecimal totalDonations = donationRepository.getTotalDonationsByCampaign(campaign, church);

    // Calculate total pledges for this campaign
    BigDecimal totalPledges = pledgeRepository.calculateTotalPledgeAmount(campaign, church);
    BigDecimal totalPledgesPaid = pledgeRepository.calculateTotalPaidAmount(campaign, church);

    // Count donations and pledges
    int donationCount = (int) donationRepository.countByCampaignEntityAndChurch(campaign, church);
    int pledgeCount = (int) pledgeRepository.countByCampaignAndChurch(campaign, church);

    // Update campaign
    campaign.setCurrentAmount(totalDonations.add(totalPledgesPaid));
    campaign.setTotalPledges(totalPledges);
    campaign.setTotalDonations(donationCount);
    campaign.setTotalPledgesCount(pledgeCount);

    // Auto-complete if goal reached
    if (campaign.isGoalReached() && campaign.getStatus() == CampaignStatus.ACTIVE) {
      campaign.setStatus(CampaignStatus.COMPLETED);
    }

    campaignRepository.save(campaign);
  }

  /**
   * Pause a campaign
   */
  @Transactional
  public CampaignResponse pauseCampaign(Long churchId, Long id) {
    return updateCampaignStatus(churchId, id, CampaignStatus.PAUSED);
  }

  /**
   * Resume a paused campaign
   */
  @Transactional
  public CampaignResponse resumeCampaign(Long churchId, Long id) {
    return updateCampaignStatus(churchId, id, CampaignStatus.ACTIVE);
  }

  /**
   * Complete a campaign
   */
  @Transactional
  public CampaignResponse completeCampaign(Long churchId, Long id) {
    return updateCampaignStatus(churchId, id, CampaignStatus.COMPLETED);
  }

  /**
   * Cancel a campaign
   */
  @Transactional
  public CampaignResponse cancelCampaign(Long churchId, Long id) {
    return updateCampaignStatus(churchId, id, CampaignStatus.CANCELLED);
  }

  /**
   * Get campaign statistics
   */
  public CampaignStatsResponse getCampaignStats(Long churchId) {
    Church church = getChurch(churchId);

    List<Campaign> allCampaigns = campaignRepository.findByChurch(church);

    long totalCampaigns = allCampaigns.size();
    long activeCampaigns = campaignRepository.countActiveCampaigns(church);
    long completedCampaigns = campaignRepository.countByChurchAndStatus(church, CampaignStatus.COMPLETED);

    BigDecimal totalGoalAmount = allCampaigns.stream()
        .map(Campaign::getGoalAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalRaised = allCampaigns.stream()
        .map(Campaign::getCurrentAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalPledged = allCampaigns.stream()
        .map(Campaign::getTotalPledges)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    int totalDonations = allCampaigns.stream()
        .mapToInt(Campaign::getTotalDonations)
        .sum();

    int totalPledges = allCampaigns.stream()
        .mapToInt(Campaign::getTotalPledgesCount)
        .sum();

    double averageProgress = allCampaigns.stream()
        .mapToDouble(Campaign::getProgressPercentage)
        .average()
        .orElse(0.0);

    return new CampaignStatsResponse(
        totalCampaigns,
        activeCampaigns,
        completedCampaigns,
        totalGoalAmount,
        totalRaised,
        totalPledged,
        totalDonations,
        totalPledges,
        averageProgress
    );
  }

  // Helper methods

  private void updateCampaignFromRequest(Campaign campaign, CampaignRequest request) {
    campaign.setName(request.getName());
    campaign.setDescription(request.getDescription());
    campaign.setGoalAmount(request.getGoalAmount());
    campaign.setCurrency(request.getCurrency());
    campaign.setStartDate(request.getStartDate());
    campaign.setEndDate(request.getEndDate());
    campaign.setStatus(request.getStatus());
    campaign.setIsPublic(request.getIsPublic());
    campaign.setShowThermometer(request.getShowThermometer());
    campaign.setShowDonorList(request.getShowDonorList());
    campaign.setFeatured(request.getFeatured());
  }

  private CampaignResponse updateCampaignStatus(Long churchId, Long id, CampaignStatus status) {
    Church church = getChurch(churchId);
    Campaign campaign = campaignRepository.findByIdAndChurch(id, church)
        .orElseThrow(() -> new IllegalArgumentException("Campaign not found with id: " + id));

    // CRITICAL SECURITY: Validate campaign belongs to current church
    tenantValidationService.validateCampaignAccess(campaign);

    campaign.setStatus(status);
    Campaign saved = campaignRepository.save(campaign);
    return CampaignResponse.fromEntity(saved);
  }

  private Church getChurch(Long churchId) {
    return churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
  }

  private User getUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
  }
}
