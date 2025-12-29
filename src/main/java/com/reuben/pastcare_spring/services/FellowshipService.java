package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FellowshipService {

  private final FellowshipRepository fellowshipRepository;
  private final FellowshipJoinRequestRepository joinRequestRepository;
  private final FellowshipMemberHistoryRepository memberHistoryRepository;
  private final FellowshipMultiplicationRepository multiplicationRepository;
  private final UserRepository userRepository;
  private final MemberRepository memberRepository;
  private final LocationRepository locationRepository;
  private final ChurchRepository churchRepository;
  private final ImageService imageService;
  private final TenantValidationService tenantValidationService;

  /**
   * Get all fellowships
   */
  public List<FellowshipResponse> getAllFellowships() {
    return fellowshipRepository.findAll().stream()
      .map(FellowshipResponse::fromEntity)
      .collect(Collectors.toList());
  }

  /**
   * Get fellowship by ID
   */
  public FellowshipResponse getFellowshipById(Long id) {
    Fellowship fellowship = fellowshipRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + id));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    return FellowshipResponse.fromEntity(fellowship);
  }

  /**
   * Create a new fellowship
   */
  @Transactional
  public FellowshipResponse createFellowship(Long churchId, FellowshipRequest request) {
    Fellowship fellowship = new Fellowship();

    // Set church (tenant isolation)
    Church church = churchRepository.findById(churchId)
      .orElseThrow(() -> new IllegalArgumentException("Church not found with id: " + churchId));
    fellowship.setChurch(church);

    updateFellowshipFromRequest(fellowship, request);
    Fellowship saved = fellowshipRepository.save(fellowship);
    return FellowshipResponse.fromEntity(saved);
  }

  /**
   * Update an existing fellowship
   */
  @Transactional
  public FellowshipResponse updateFellowship(Long id, FellowshipRequest request) {
    Fellowship fellowship = fellowshipRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + id));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    updateFellowshipFromRequest(fellowship, request);
    Fellowship updated = fellowshipRepository.save(fellowship);
    return FellowshipResponse.fromEntity(updated);
  }

  /**
   * Delete a fellowship
   */
  @Transactional
  public void deleteFellowship(Long id) {
    Fellowship fellowship = fellowshipRepository.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + id));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    fellowshipRepository.delete(fellowship);
  }

  /**
   * Get all active fellowships
   */
  public List<FellowshipResponse> getActiveFellowships() {
    return fellowshipRepository.findAll().stream()
      .filter(Fellowship::getIsActive)
      .map(FellowshipResponse::fromEntity)
      .collect(Collectors.toList());
  }

  /**
   * Get fellowships accepting new members
   */
  public List<FellowshipResponse> getFellowshipsAcceptingMembers() {
    return fellowshipRepository.findAll().stream()
      .filter(f -> f.getIsActive() && f.getAcceptingMembers())
      .map(FellowshipResponse::fromEntity)
      .collect(Collectors.toList());
  }

  /**
   * Get fellowships by type
   */
  public List<FellowshipResponse> getFellowshipsByType(FellowshipType type) {
    return fellowshipRepository.findAll().stream()
      .filter(f -> f.getFellowshipType() == type)
      .map(FellowshipResponse::fromEntity)
      .collect(Collectors.toList());
  }

  /**
   * Assign leader to a fellowship
   */
  @Transactional
  public FellowshipResponse assignLeader(Long fellowshipId, Long userId) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

    fellowship.setLeader(user);
    Fellowship updated = fellowshipRepository.save(fellowship);
    return FellowshipResponse.fromEntity(updated);
  }

  /**
   * Add a co-leader to a fellowship
   */
  @Transactional
  public FellowshipResponse addColeader(Long fellowshipId, Long userId) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

    if (fellowship.getColeaders() == null) {
      fellowship.setColeaders(new ArrayList<>());
    }

    if (!fellowship.getColeaders().contains(user)) {
      fellowship.getColeaders().add(user);
    }

    Fellowship updated = fellowshipRepository.save(fellowship);
    return FellowshipResponse.fromEntity(updated);
  }

  /**
   * Remove a co-leader from a fellowship
   */
  @Transactional
  public FellowshipResponse removeColeader(Long fellowshipId, Long userId) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    if (fellowship.getColeaders() != null) {
      fellowship.getColeaders().removeIf(user -> user.getId().equals(userId));
    }

    Fellowship updated = fellowshipRepository.save(fellowship);
    return FellowshipResponse.fromEntity(updated);
  }

  /**
   * Create a join request for a fellowship
   */
  @Transactional
  public FellowshipJoinRequestResponse createJoinRequest(FellowshipJoinRequestRequest request) {
    // Check if there's already a pending request
    boolean hasPendingRequest = joinRequestRepository.existsByFellowshipIdAndMemberIdAndStatus(
      request.fellowshipId(),
      request.memberId(),
      FellowshipJoinRequestStatus.PENDING
    );

    if (hasPendingRequest) {
      throw new IllegalArgumentException("Member already has a pending join request for this fellowship");
    }

    Fellowship fellowship = fellowshipRepository.findById(request.fellowshipId())
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + request.fellowshipId()));

    Member member = memberRepository.findById(request.memberId())
      .orElseThrow(() -> new IllegalArgumentException("Member not found with id: " + request.memberId()));

    // Check if fellowship is accepting members
    if (!fellowship.getAcceptingMembers()) {
      throw new IllegalArgumentException("Fellowship is not currently accepting new members");
    }

    // Check if fellowship has reached capacity
    if (fellowship.getMaxCapacity() != null &&
        fellowship.getMembers() != null &&
        fellowship.getMembers().size() >= fellowship.getMaxCapacity()) {
      throw new IllegalArgumentException("Fellowship has reached maximum capacity");
    }

    FellowshipJoinRequest joinRequest = new FellowshipJoinRequest();
    joinRequest.setFellowship(fellowship);
    joinRequest.setMember(member);
    joinRequest.setRequestMessage(request.requestMessage());
    joinRequest.setStatus(FellowshipJoinRequestStatus.PENDING);
    joinRequest.setRequestedAt(LocalDateTime.now());

    FellowshipJoinRequest saved = joinRequestRepository.save(joinRequest);
    return FellowshipJoinRequestResponse.fromEntity(saved);
  }

  /**
   * Get all join requests for a fellowship
   */
  public List<FellowshipJoinRequestResponse> getJoinRequestsByFellowship(Long fellowshipId) {
    return joinRequestRepository.findByFellowshipId(fellowshipId).stream()
      .map(FellowshipJoinRequestResponse::fromEntity)
      .collect(Collectors.toList());
  }

  /**
   * Get all pending join requests for a fellowship
   */
  public List<FellowshipJoinRequestResponse> getPendingJoinRequests(Long fellowshipId) {
    return joinRequestRepository.findByFellowshipIdAndStatus(fellowshipId, FellowshipJoinRequestStatus.PENDING)
      .stream()
      .map(FellowshipJoinRequestResponse::fromEntity)
      .collect(Collectors.toList());
  }

  /**
   * Approve a join request
   */
  @Transactional
  public FellowshipJoinRequestResponse approveJoinRequest(Long requestId, Long reviewerId, String reviewNotes) {
    FellowshipJoinRequest request = joinRequestRepository.findById(requestId)
      .orElseThrow(() -> new IllegalArgumentException("Join request not found with id: " + requestId));

    if (request.getStatus() != FellowshipJoinRequestStatus.PENDING) {
      throw new IllegalArgumentException("Only pending requests can be approved");
    }

    User reviewer = userRepository.findById(reviewerId)
      .orElseThrow(() -> new IllegalArgumentException("Reviewer not found with id: " + reviewerId));

    // Add member to fellowship
    Fellowship fellowship = request.getFellowship();
    Member member = request.getMember();

    if (fellowship.getMembers() == null) {
      fellowship.setMembers(new ArrayList<>());
    }

    if (!fellowship.getMembers().contains(member)) {
      fellowship.getMembers().add(member);
      fellowshipRepository.save(fellowship);
    }

    // Update request status
    request.setStatus(FellowshipJoinRequestStatus.APPROVED);
    request.setReviewedAt(LocalDateTime.now());
    request.setReviewedBy(reviewer);
    request.setReviewNotes(reviewNotes);

    FellowshipJoinRequest updated = joinRequestRepository.save(request);
    return FellowshipJoinRequestResponse.fromEntity(updated);
  }

  /**
   * Reject a join request
   */
  @Transactional
  public FellowshipJoinRequestResponse rejectJoinRequest(Long requestId, Long reviewerId, String reviewNotes) {
    FellowshipJoinRequest request = joinRequestRepository.findById(requestId)
      .orElseThrow(() -> new IllegalArgumentException("Join request not found with id: " + requestId));

    if (request.getStatus() != FellowshipJoinRequestStatus.PENDING) {
      throw new IllegalArgumentException("Only pending requests can be rejected");
    }

    User reviewer = userRepository.findById(reviewerId)
      .orElseThrow(() -> new IllegalArgumentException("Reviewer not found with id: " + reviewerId));

    request.setStatus(FellowshipJoinRequestStatus.REJECTED);
    request.setReviewedAt(LocalDateTime.now());
    request.setReviewedBy(reviewer);
    request.setReviewNotes(reviewNotes);

    FellowshipJoinRequest updated = joinRequestRepository.save(request);
    return FellowshipJoinRequestResponse.fromEntity(updated);
  }

  /**
   * Helper method to update fellowship from request DTO
   */
  private void updateFellowshipFromRequest(Fellowship fellowship, FellowshipRequest request) {
    fellowship.setName(request.name());
    fellowship.setDescription(request.description());
    fellowship.setImageUrl(request.imageUrl());
    fellowship.setFellowshipType(request.fellowshipType());
    fellowship.setMeetingDay(request.meetingDay());
    fellowship.setMeetingTime(request.meetingTime());
    fellowship.setMaxCapacity(request.maxCapacity());
    fellowship.setIsActive(request.isActive() != null ? request.isActive() : true);
    fellowship.setAcceptingMembers(request.acceptingMembers() != null ? request.acceptingMembers() : true);

    // Set leader
    if (request.leaderId() != null) {
      User leader = userRepository.findById(request.leaderId())
        .orElseThrow(() -> new IllegalArgumentException("Leader not found with id: " + request.leaderId()));
      fellowship.setLeader(leader);
    } else {
      // Clear leader if null is explicitly sent
      fellowship.setLeader(null);
    }

    // Set coleaders (including clearing if empty array is sent)
    if (request.coleaderIds() != null) {
      // Clear existing coleaders first (important for JPA to track deletions in join table)
      fellowship.getColeaders().clear();

      if (!request.coleaderIds().isEmpty()) {
        // Add new coleaders
        List<User> coleaders = userRepository.findAllById(request.coleaderIds());
        fellowship.getColeaders().addAll(coleaders);
      }
      // If empty array, the collection is already cleared above
    }

    // Set meeting location
    if (request.meetingLocationId() != null) {
      Location location = locationRepository.findById(request.meetingLocationId())
        .orElseThrow(() -> new IllegalArgumentException("Location not found with id: " + request.meetingLocationId()));
      fellowship.setMeetingLocation(location);
    }
  }

  /**
   * Upload fellowship image
   */
  @Transactional
  public FellowshipResponse uploadFellowshipImage(Long fellowshipId, org.springframework.web.multipart.MultipartFile image) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    try {
      // Upload image using ImageService with fellowship-specific directory
      String imagePath = imageService.uploadFellowshipImage(image, fellowship.getImageUrl());
      fellowship.setImageUrl(imagePath);
      Fellowship updated = fellowshipRepository.save(fellowship);
      return FellowshipResponse.fromEntity(updated);
    } catch (Exception e) {
      throw new RuntimeException("Failed to upload fellowship image: " + e.getMessage(), e);
    }
  }

  /**
   * Add multiple members to fellowship at once
   */
  @Transactional
  public FellowshipResponse addMembersBulk(Long fellowshipId, List<Long> memberIds) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    if (memberIds == null || memberIds.isEmpty()) {
      throw new IllegalArgumentException("Member IDs list cannot be empty");
    }

    // Get members and add them to fellowship
    List<Member> membersToAdd = memberRepository.findAllById(memberIds);

    if (membersToAdd.isEmpty()) {
      throw new IllegalArgumentException("No valid members found with provided IDs");
    }

    // IMPORTANT: Member owns the relationship, so we must add fellowship to member.fellowships
    // NOT add member to fellowship.members (which is mappedBy and won't persist)
    for (Member member : membersToAdd) {
      if (member.getFellowships() == null) {
        member.setFellowships(new ArrayList<>());
      }
      if (!member.getFellowships().contains(fellowship)) {
        member.getFellowships().add(fellowship);
      }
    }

    // Save members (owning side) to persist the relationship
    memberRepository.saveAll(membersToAdd);

    // Refresh fellowship to get updated members list
    Fellowship updated = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));
    return FellowshipResponse.fromEntity(updated);
  }

  /**
   * Remove multiple members from fellowship at once
   */
  @Transactional
  public FellowshipResponse removeMembersBulk(Long fellowshipId, List<Long> memberIds) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    if (memberIds == null || memberIds.isEmpty()) {
      throw new IllegalArgumentException("Member IDs list cannot be empty");
    }

    // Get members and remove them from fellowship
    List<Member> membersToRemove = memberRepository.findAllById(memberIds);

    // IMPORTANT: Member owns the relationship, so we must remove fellowship from member.fellowships
    // NOT remove member from fellowship.members (which is mappedBy and won't persist)
    for (Member member : membersToRemove) {
      if (member.getFellowships() != null) {
        member.getFellowships().remove(fellowship);
      }
    }

    // Save members (owning side) to persist the relationship
    memberRepository.saveAll(membersToRemove);

    // Refresh fellowship to get updated members list
    Fellowship updated = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));
    return FellowshipResponse.fromEntity(updated);
  }

  /**
   * Get list of member IDs in a fellowship
   */
  public List<Long> getFellowshipMemberIds(Long fellowshipId) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));

    if (fellowship.getMembers() == null) {
      return List.of();
    }

    return fellowship.getMembers().stream()
      .map(Member::getId)
      .toList();
  }

  // Fellowship Phase 2: Analytics

  /**
   * Get analytics for a specific fellowship
   */
  public FellowshipAnalyticsResponse getFellowshipAnalytics(Long fellowshipId) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    int currentMembers = fellowship.getMembers() != null ? fellowship.getMembers().size() : 0;
    Integer maxCapacity = fellowship.getMaxCapacity() != null ? fellowship.getMaxCapacity() : 100;

    double occupancyRate = maxCapacity > 0 ? (currentMembers * 100.0 / maxCapacity) : 0.0;

    // Count growth in last 30 and 90 days
    // This counts both approved join requests AND members who were created recently
    // (as a proxy for when they joined the fellowship until we have proper join timestamps)
    LocalDateTime thirtyDaysAgoLDT = LocalDateTime.now().minusDays(30);
    LocalDateTime ninetyDaysAgoLDT = LocalDateTime.now().minusDays(90);
    Instant thirtyDaysAgo = Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
    Instant ninetyDaysAgo = Instant.now().minus(90, java.time.temporal.ChronoUnit.DAYS);

    // Count from join requests (uses LocalDateTime)
    List<FellowshipJoinRequest> allRequests = joinRequestRepository.findByFellowshipId(fellowshipId);
    int joinRequestGrowth30Days = (int) allRequests.stream()
      .filter(r -> r.getStatus() == FellowshipJoinRequestStatus.APPROVED)
      .filter(r -> r.getReviewedAt() != null && r.getReviewedAt().isAfter(thirtyDaysAgoLDT))
      .count();

    int joinRequestGrowth90Days = (int) allRequests.stream()
      .filter(r -> r.getStatus() == FellowshipJoinRequestStatus.APPROVED)
      .filter(r -> r.getReviewedAt() != null && r.getReviewedAt().isAfter(ninetyDaysAgoLDT))
      .count();

    // Count members who were created recently (as proxy for fellowship join date)
    // This helps track members added directly without join requests (uses Instant)
    int recentMembersLast30Days = 0;
    int recentMembersLast90Days = 0;
    if (fellowship.getMembers() != null) {
      recentMembersLast30Days = (int) fellowship.getMembers().stream()
        .filter(m -> m.getCreatedAt() != null && m.getCreatedAt().isAfter(thirtyDaysAgo))
        .count();

      recentMembersLast90Days = (int) fellowship.getMembers().stream()
        .filter(m -> m.getCreatedAt() != null && m.getCreatedAt().isAfter(ninetyDaysAgo))
        .count();
    }

    // Combine both sources (max of the two, since join requests might overlap with member creation)
    int memberGrowthLast30Days = Math.max(joinRequestGrowth30Days, recentMembersLast30Days);
    int memberGrowthLast90Days = Math.max(joinRequestGrowth90Days, recentMembersLast90Days);

    int pendingRequests = (int) allRequests.stream()
      .filter(r -> r.getStatus() == FellowshipJoinRequestStatus.PENDING)
      .count();

    double growthRate = currentMembers > 0 ? (memberGrowthLast90Days * 100.0 / currentMembers) : 0.0;

    String healthStatus = FellowshipAnalyticsResponse.calculateHealthStatus(
      occupancyRate, memberGrowthLast30Days, currentMembers);

    String growthTrend = FellowshipAnalyticsResponse.calculateGrowthTrend(
      memberGrowthLast30Days, memberGrowthLast90Days);

    boolean isHealthy = "EXCELLENT".equals(healthStatus) || "GOOD".equals(healthStatus);

    return new FellowshipAnalyticsResponse(
      fellowship.getId(),
      fellowship.getName(),
      currentMembers,
      maxCapacity,
      Math.round(occupancyRate * 100.0) / 100.0, // Round to 2 decimal places
      memberGrowthLast30Days,
      memberGrowthLast90Days,
      Math.round(growthRate * 100.0) / 100.0,
      pendingRequests,
      isHealthy,
      healthStatus,
      growthTrend
    );
  }

  /**
   * Get analytics for all fellowships in the church
   */
  public List<FellowshipAnalyticsResponse> getAllFellowshipAnalytics() {
    List<Fellowship> fellowships = fellowshipRepository.findAll();
    return fellowships.stream()
      .map(f -> getFellowshipAnalytics(f.getId()))
      .collect(Collectors.toList());
  }

  /**
   * Get fellowship comparison data for dashboard
   */
  public List<FellowshipComparisonResponse> getFellowshipComparison() {
    List<Fellowship> fellowships = fellowshipRepository.findAll();
    LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

    List<FellowshipComparisonResponse> comparisons = fellowships.stream()
      .map(fellowship -> {
        int memberCount = fellowship.getMembers() != null ? fellowship.getMembers().size() : 0;

        List<FellowshipJoinRequest> requests = joinRequestRepository.findByFellowshipId(fellowship.getId());

        int joinRequestsLast30Days = (int) requests.stream()
          .filter(r -> r.getRequestedAt().isAfter(thirtyDaysAgo))
          .count();

        int totalRequests = requests.size();
        int approvedRequests = (int) requests.stream()
          .filter(r -> r.getStatus() == FellowshipJoinRequestStatus.APPROVED)
          .count();

        int approvalRate = totalRequests > 0 ? (approvedRequests * 100 / totalRequests) : 0;

        FellowshipAnalyticsResponse analytics = getFellowshipAnalytics(fellowship.getId());

        return new FellowshipComparisonResponse(
          fellowship.getId(),
          fellowship.getName(),
          fellowship.getFellowshipType() != null ? fellowship.getFellowshipType().name() : "UNKNOWN",
          memberCount,
          0.0, // Average attendance rate - will be implemented when attendance tracking is added
          joinRequestsLast30Days,
          approvalRate,
          analytics.healthStatus(),
          0 // Rank will be assigned after sorting
        );
      })
      .collect(Collectors.toList());

    // Sort by health status and member count, then assign ranks
    comparisons.sort((a, b) -> {
      // Sort by health status first (EXCELLENT > GOOD > FAIR > AT_RISK)
      int healthCompare = compareHealthStatus(a.healthStatus(), b.healthStatus());
      if (healthCompare != 0) return healthCompare;

      // Then by member count descending
      return Integer.compare(b.memberCount(), a.memberCount());
    });

    // Assign ranks
    for (int i = 0; i < comparisons.size(); i++) {
      FellowshipComparisonResponse old = comparisons.get(i);
      comparisons.set(i, new FellowshipComparisonResponse(
        old.fellowshipId(),
        old.fellowshipName(),
        old.fellowshipType(),
        old.memberCount(),
        old.averageAttendanceRate(),
        old.joinRequestsLast30Days(),
        old.approvalRate(),
        old.healthStatus(),
        i + 1 // Rank (1-based)
      ));
    }

    return comparisons;
  }

  /**
   * Helper method to compare health status for sorting
   */
  private int compareHealthStatus(String a, String b) {
    int rankA = getHealthStatusRank(a);
    int rankB = getHealthStatusRank(b);
    return Integer.compare(rankA, rankB);
  }

  private int getHealthStatusRank(String healthStatus) {
    return switch (healthStatus) {
      case "EXCELLENT" -> 1;
      case "GOOD" -> 2;
      case "FAIR" -> 3;
      case "AT_RISK" -> 4;
      default -> 5;
    };
  }

  /**
   * Get fellowship retention metrics for a time period
   */
  public FellowshipRetentionResponse getFellowshipRetention(Long fellowshipId, java.time.LocalDate startDate, java.time.LocalDate endDate) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found with id: " + fellowshipId));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    // Get history for the period
    List<FellowshipMemberHistory> history = memberHistoryRepository
      .findByFellowshipAndEffectiveDateBetween(fellowship, startDate, endDate);

    // Count different actions
    long joined = history.stream().filter(h -> h.getAction() == FellowshipMemberAction.JOINED).count();
    long left = history.stream().filter(h -> h.getAction() == FellowshipMemberAction.LEFT).count();
    long transfersIn = history.stream().filter(h -> h.getAction() == FellowshipMemberAction.TRANSFERRED_IN).count();
    long transfersOut = history.stream().filter(h -> h.getAction() == FellowshipMemberAction.TRANSFERRED_OUT).count();
    long inactive = history.stream().filter(h -> h.getAction() == FellowshipMemberAction.INACTIVE).count();
    long reactivated = history.stream().filter(h -> h.getAction() == FellowshipMemberAction.REACTIVATED).count();

    // Calculate members at start and end
    int currentMembers = fellowship.getMembers() != null ? fellowship.getMembers().size() : 0;
    int netChange = (int) (joined + transfersIn + reactivated - left - transfersOut - inactive);
    int membersAtStart = currentMembers - netChange;

    // Calculate retention and churn rates
    double retentionRate = membersAtStart > 0 ?
      ((membersAtStart - left) * 100.0 / membersAtStart) : 100.0;
    double churnRate = membersAtStart > 0 ?
      (left * 100.0 / membersAtStart) : 0.0;

    String healthIndicator = calculateRetentionHealth(retentionRate);

    return new FellowshipRetentionResponse(
      fellowship.getId(),
      fellowship.getName(),
      startDate,
      endDate,
      membersAtStart,
      (int) joined,
      (int) left,
      currentMembers,
      Math.round(retentionRate * 100.0) / 100.0,
      Math.round(churnRate * 100.0) / 100.0,
      (int) transfersIn,
      (int) transfersOut,
      (int) inactive,
      (int) reactivated,
      healthIndicator
    );
  }

  /**
   * Record a fellowship membership action (for tracking retention)
   */
  @Transactional
  public void recordMembershipAction(Long fellowshipId, Long memberId, FellowshipMemberAction action,
                                     String notes, Long recordedByUserId) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found"));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    Member member = memberRepository.findById(memberId)
      .orElseThrow(() -> new IllegalArgumentException("Member not found"));
    User recordedBy = userRepository.findById(recordedByUserId)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    FellowshipMemberHistory history = new FellowshipMemberHistory();
    history.setFellowship(fellowship);
    history.setMember(member);
    history.setAction(action);
    history.setEffectiveDate(java.time.LocalDate.now());
    history.setNotes(notes);
    history.setRecordedBy(recordedBy);
    history.setChurch(fellowship.getChurch());

    memberHistoryRepository.save(history);
  }

  private String calculateRetentionHealth(double retentionRate) {
    if (retentionRate >= 90.0) return "EXCELLENT";
    if (retentionRate >= 75.0) return "GOOD";
    if (retentionRate >= 60.0) return "CONCERNING";
    return "CRITICAL";
  }

  // ========== Fellowship Multiplication Tracking ==========

  /**
   * Get all fellowship multiplications
   */
  public List<FellowshipMultiplicationResponse> getAllMultiplications() {
    return multiplicationRepository.findAllByOrderByMultiplicationDateDesc().stream()
      .map(this::toMultiplicationResponse)
      .collect(Collectors.toList());
  }

  /**
   * Get multiplications for a specific fellowship (as parent)
   */
  public List<FellowshipMultiplicationResponse> getFellowshipMultiplications(Long fellowshipId) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found"));
    return multiplicationRepository.findByParentFellowship(fellowship).stream()
      .map(this::toMultiplicationResponse)
      .collect(Collectors.toList());
  }

  /**
   * Record a fellowship multiplication event
   */
  @Transactional
  public FellowshipMultiplicationResponse recordMultiplication(Long parentId, RecordMultiplicationRequest request, Long recordedByUserId) {
    Fellowship parent = fellowshipRepository.findById(parentId)
      .orElseThrow(() -> new IllegalArgumentException("Parent fellowship not found"));
    Fellowship child = fellowshipRepository.findById(request.childFellowshipId())
      .orElseThrow(() -> new IllegalArgumentException("Child fellowship not found"));
    User recordedBy = userRepository.findById(recordedByUserId)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    FellowshipMultiplication multiplication = new FellowshipMultiplication();
    multiplication.setParentFellowship(parent);
    multiplication.setChildFellowship(child);
    multiplication.setMultiplicationDate(request.multiplicationDate());
    multiplication.setReason(request.reason());
    multiplication.setMembersTransferred(request.membersTransferred());
    multiplication.setNotes(request.notes());
    multiplication.setRecordedBy(recordedBy);
    multiplication.setChurch(parent.getChurch());

    FellowshipMultiplication saved = multiplicationRepository.save(multiplication);
    return toMultiplicationResponse(saved);
  }

  private FellowshipMultiplicationResponse toMultiplicationResponse(FellowshipMultiplication multiplication) {
    return new FellowshipMultiplicationResponse(
      multiplication.getId(),
      multiplication.getParentFellowship().getId(),
      multiplication.getParentFellowship().getName(),
      multiplication.getChildFellowship().getId(),
      multiplication.getChildFellowship().getName(),
      multiplication.getMultiplicationDate(),
      multiplication.getReason(),
      multiplication.getMembersTransferred(),
      multiplication.getNotes(),
      multiplication.getCreatedAt()
    );
  }

  // ========== Fellowship Balance Recommendations ==========

  /**
   * Get balance recommendations for all fellowships
   */
  public List<FellowshipBalanceRecommendationResponse> getBalanceRecommendations() {
    List<Fellowship> fellowships = fellowshipRepository.findAll();
    List<FellowshipBalanceRecommendationResponse> recommendations = new ArrayList<>();

    for (Fellowship fellowship : fellowships) {
      FellowshipBalanceRecommendationResponse recommendation = analyzeBalance(fellowship);
      if (recommendation != null) {
        recommendations.add(recommendation);
      }
    }

    return recommendations;
  }

  /**
   * Get balance recommendation for a specific fellowship
   */
  public FellowshipBalanceRecommendationResponse getFellowshipBalanceRecommendation(Long fellowshipId) {
    Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
      .orElseThrow(() -> new IllegalArgumentException("Fellowship not found"));

    // CRITICAL SECURITY: Validate fellowship belongs to current church
    tenantValidationService.validateFellowshipAccess(fellowship);

    return analyzeBalance(fellowship);
  }

  private FellowshipBalanceRecommendationResponse analyzeBalance(Fellowship fellowship) {
    int currentSize = fellowship.getMembers().size();
    Integer maxCapacity = fellowship.getMaxCapacity();

    // Optimal fellowship size: 12-25 members for effective community
    int optimalMin = 12;
    int optimalMax = 25;

    List<String> suggestedActions = new ArrayList<>();
    String recommendationType = null;
    String priority = "LOW";
    String reason = null;

    // Fellowship is too large
    if (maxCapacity != null && currentSize >= maxCapacity) {
      recommendationType = "SPLIT";
      priority = "HIGH";
      reason = "Fellowship has reached maximum capacity";
      suggestedActions.add("Identify potential leaders for new fellowship");
      suggestedActions.add("Plan multiplication date");
      suggestedActions.add("Divide members geographically or by life stage");
    } else if (currentSize > optimalMax) {
      recommendationType = "SPLIT";
      priority = maxCapacity != null && currentSize > maxCapacity * 0.8 ? "HIGH" : "MEDIUM";
      reason = "Fellowship size exceeds optimal range for community building";
      suggestedActions.add("Consider multiplying into two fellowships");
      suggestedActions.add("Maintain 60-70% of current members in each group");
    }
    // Fellowship is too small
    else if (currentSize < optimalMin && currentSize > 0) {
      recommendationType = "MERGE";
      priority = currentSize < 5 ? "HIGH" : "MEDIUM";
      reason = "Fellowship size below optimal range";
      suggestedActions.add("Consider merging with another small fellowship");
      suggestedActions.add("Increase recruitment efforts");
      suggestedActions.add("Partner with larger fellowship for events");
    }
    // Fellowship is approaching capacity
    else if (maxCapacity != null && currentSize > maxCapacity * 0.8) {
      recommendationType = "NEW_FELLOWSHIP";
      priority = "MEDIUM";
      reason = "Fellowship approaching maximum capacity";
      suggestedActions.add("Begin preparing for future multiplication");
      suggestedActions.add("Identify and train potential leaders");
      suggestedActions.add("Monitor growth trends");
    }
    // No recommendation needed
    else {
      return null;
    }

    return new FellowshipBalanceRecommendationResponse(
      fellowship.getId(),
      fellowship.getName(),
      recommendationType,
      priority,
      reason,
      currentSize,
      optimalMax,
      null, // demographicImbalance - could be enhanced later
      suggestedActions
    );
  }
}
