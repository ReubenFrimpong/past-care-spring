package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.dtos.*;
import com.reuben.pastcare_spring.models.*;
import com.reuben.pastcare_spring.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FellowshipService {

  private final FellowshipRepository fellowshipRepository;
  private final FellowshipJoinRequestRepository joinRequestRepository;
  private final UserRepository userRepository;
  private final MemberRepository memberRepository;
  private final LocationRepository locationRepository;
  private final ChurchRepository churchRepository;
  private final ImageService imageService;

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
}
