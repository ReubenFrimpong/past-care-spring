package com.reuben.pastcare_spring.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.reuben.pastcare_spring.dtos.ConvertVisitorRequest;
import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.dtos.VisitorRequest;
import com.reuben.pastcare_spring.dtos.VisitorResponse;
import com.reuben.pastcare_spring.mapper.VisitorMapper;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.User;
import com.reuben.pastcare_spring.models.Visitor;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.repositories.UserRepository;
import com.reuben.pastcare_spring.repositories.VisitorRepository;

/**
 * Service for managing visitors.
 * Phase 1: Enhanced Attendance Tracking - Visitor Management
 *
 * Handles:
 * - CRUD operations for visitors
 * - Visit tracking and counting
 * - Follow-up assignment
 * - Visitor to member conversion
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
@Service
public class VisitorService {

  private final VisitorRepository visitorRepository;
  private final MemberRepository memberRepository;
  private final UserRepository userRepository;
  private final MemberService memberService;

  public VisitorService(
      VisitorRepository visitorRepository,
      MemberRepository memberRepository,
      UserRepository userRepository,
      MemberService memberService) {
    this.visitorRepository = visitorRepository;
    this.memberRepository = memberRepository;
    this.userRepository = userRepository;
    this.memberService = memberService;
  }

  /**
   * Create a new visitor record.
   *
   * @param request Visitor creation data
   * @return Created visitor
   */
  @Transactional
  public VisitorResponse createVisitor(VisitorRequest request) {
    // Check for duplicate phone number
    if (visitorRepository.existsByPhoneNumber(request.phoneNumber())) {
      throw new IllegalArgumentException("A visitor with this phone number already exists");
    }

    // Check for duplicate email if provided
    if (request.email() != null && !request.email().isBlank() &&
        visitorRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("A visitor with this email already exists");
    }

    Visitor visitor = new Visitor();
    visitor.setFirstName(request.firstName());
    visitor.setLastName(request.lastName());
    visitor.setPhoneNumber(request.phoneNumber());
    visitor.setEmail(request.email());
    visitor.setAgeGroup(request.ageGroup());
    visitor.setHowHeardAboutUs(request.howHeardAboutUs());
    visitor.setFollowUpStatus(request.followUpStatus());
    visitor.setNotes(request.notes());

    // Set invited by member if provided
    if (request.invitedByMemberId() != null) {
      Member invitedBy = memberRepository.findById(request.invitedByMemberId())
          .orElseThrow(() -> new IllegalArgumentException("Invited by member not found"));
      visitor.setInvitedByMember(invitedBy);
    }

    // Set assigned user if provided
    if (request.assignedToUserId() != null) {
      User assignedUser = userRepository.findById(request.assignedToUserId())
          .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));
      visitor.setAssignedToUser(assignedUser);
    }

    // Initialize visit tracking
    visitor.setIsFirstTime(true);
    visitor.setVisitCount(0);
    visitor.setConvertedToMember(false);

    Visitor savedVisitor = visitorRepository.save(visitor);
    return VisitorMapper.toVisitorResponse(savedVisitor);
  }

  /**
   * Get all visitors.
   *
   * @return List of all visitors
   */
  public List<VisitorResponse> getAllVisitors() {
    return visitorRepository.findAll()
        .stream()
        .map(VisitorMapper::toVisitorResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get visitor by ID.
   *
   * @param id Visitor ID
   * @return Visitor details
   */
  public VisitorResponse getVisitorById(Long id) {
    Visitor visitor = visitorRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Visitor not found with id: " + id));
    return VisitorMapper.toVisitorResponse(visitor);
  }

  /**
   * Update visitor information.
   *
   * @param id Visitor ID
   * @param request Updated visitor data
   * @return Updated visitor
   */
  @Transactional
  public VisitorResponse updateVisitor(Long id, VisitorRequest request) {
    Visitor visitor = visitorRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Visitor not found with id: " + id));

    // Check for duplicate phone number (excluding current visitor)
    if (!visitor.getPhoneNumber().equals(request.phoneNumber()) &&
        visitorRepository.existsByPhoneNumber(request.phoneNumber())) {
      throw new IllegalArgumentException("A visitor with this phone number already exists");
    }

    // Check for duplicate email (excluding current visitor)
    if (request.email() != null && !request.email().isBlank() &&
        !request.email().equals(visitor.getEmail()) &&
        visitorRepository.existsByEmail(request.email())) {
      throw new IllegalArgumentException("A visitor with this email already exists");
    }

    visitor.setFirstName(request.firstName());
    visitor.setLastName(request.lastName());
    visitor.setPhoneNumber(request.phoneNumber());
    visitor.setEmail(request.email());
    visitor.setAgeGroup(request.ageGroup());
    visitor.setHowHeardAboutUs(request.howHeardAboutUs());
    visitor.setFollowUpStatus(request.followUpStatus());
    visitor.setNotes(request.notes());

    // Update invited by member
    if (request.invitedByMemberId() != null) {
      Member invitedBy = memberRepository.findById(request.invitedByMemberId())
          .orElseThrow(() -> new IllegalArgumentException("Invited by member not found"));
      visitor.setInvitedByMember(invitedBy);
    } else {
      visitor.setInvitedByMember(null);
    }

    // Update assigned user
    if (request.assignedToUserId() != null) {
      User assignedUser = userRepository.findById(request.assignedToUserId())
          .orElseThrow(() -> new IllegalArgumentException("Assigned user not found"));
      visitor.setAssignedToUser(assignedUser);
    } else {
      visitor.setAssignedToUser(null);
    }

    Visitor updatedVisitor = visitorRepository.save(visitor);
    return VisitorMapper.toVisitorResponse(updatedVisitor);
  }

  /**
   * Delete a visitor.
   *
   * @param id Visitor ID
   */
  @Transactional
  public void deleteVisitor(Long id) {
    if (!visitorRepository.existsById(id)) {
      throw new IllegalArgumentException("Visitor not found with id: " + id);
    }
    visitorRepository.deleteById(id);
  }

  /**
   * Record a visit for a visitor (increments visit count).
   *
   * @param id Visitor ID
   * @return Updated visitor
   */
  @Transactional
  public VisitorResponse recordVisit(Long id) {
    Visitor visitor = visitorRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Visitor not found with id: " + id));

    visitor.setVisitCount(visitor.getVisitCount() + 1);
    visitor.setLastVisitDate(LocalDate.now());

    if (visitor.getVisitCount() > 1) {
      visitor.setIsFirstTime(false);
    }

    Visitor updatedVisitor = visitorRepository.save(visitor);
    return VisitorMapper.toVisitorResponse(updatedVisitor);
  }

  /**
   * Get all first-time visitors.
   *
   * @return List of first-time visitors
   */
  public List<VisitorResponse> getFirstTimeVisitors() {
    return visitorRepository.findByIsFirstTime(true)
        .stream()
        .map(VisitorMapper::toVisitorResponse)
        .collect(Collectors.toList());
  }

  /**
   * Get all visitors who have not been converted to members.
   *
   * @return List of non-converted visitors
   */
  public List<VisitorResponse> getNonConvertedVisitors() {
    return visitorRepository.findByConvertedToMember(false)
        .stream()
        .map(VisitorMapper::toVisitorResponse)
        .collect(Collectors.toList());
  }

  /**
   * Convert a visitor to a member.
   *
   * @param request Conversion request with visitor ID and member data
   * @return Created member response
   */
  @Transactional
  public MemberResponse convertVisitorToMember(ConvertVisitorRequest request) {
    Visitor visitor = visitorRepository.findById(request.visitorId())
        .orElseThrow(() -> new IllegalArgumentException("Visitor not found with id: " + request.visitorId()));

    if (visitor.getConvertedToMember() != null && visitor.getConvertedToMember()) {
      throw new IllegalArgumentException("Visitor has already been converted to a member");
    }

    // Create new member using the member service
    MemberResponse newMember = memberService.createMember(request.memberRequest());

    // Update visitor record
    visitor.setConvertedToMember(true);
    visitor.setConversionDate(request.conversionDate());

    // Link to the created member
    Member member = memberRepository.findById(newMember.id())
        .orElseThrow(() -> new IllegalStateException("Member was created but not found"));
    visitor.setConvertedMember(member);

    visitorRepository.save(visitor);

    return newMember;
  }

  /**
   * Get visitor by phone number.
   *
   * @param phoneNumber Phone number to search
   * @return Visitor if found
   */
  public VisitorResponse getVisitorByPhoneNumber(String phoneNumber) {
    Visitor visitor = visitorRepository.findByPhoneNumber(phoneNumber)
        .orElseThrow(() -> new IllegalArgumentException("Visitor not found with phone number: " + phoneNumber));
    return VisitorMapper.toVisitorResponse(visitor);
  }

  /**
   * Get visitor by email.
   *
   * @param email Email to search
   * @return Visitor if found
   */
  public VisitorResponse getVisitorByEmail(String email) {
    Visitor visitor = visitorRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Visitor not found with email: " + email));
    return VisitorMapper.toVisitorResponse(visitor);
  }
}
