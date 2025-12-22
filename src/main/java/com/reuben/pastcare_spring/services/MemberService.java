package com.reuben.pastcare_spring.services;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.reuben.pastcare_spring.dtos.AdvancedSearchRequest;
import com.reuben.pastcare_spring.dtos.AdvancedSearchResponse;
import com.reuben.pastcare_spring.dtos.MemberBulkImportRequest;
import com.reuben.pastcare_spring.dtos.MemberBulkImportResponse;
import com.reuben.pastcare_spring.dtos.MemberBulkUpdateRequest;
import com.reuben.pastcare_spring.dtos.MemberBulkUpdateResponse;
import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.dtos.MemberRequest;
import com.reuben.pastcare_spring.dtos.MemberStatsResponse;
import com.reuben.pastcare_spring.dtos.ProfileImageUploadResponse;
import com.reuben.pastcare_spring.dtos.ProfileCompletenessResponse;
import com.reuben.pastcare_spring.dtos.CompletenessStatsResponse;
import com.reuben.pastcare_spring.mapper.MemberMapper;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.Location;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.MemberStatus;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.FellowshipRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.specifications.MemberSpecification;
import com.reuben.pastcare_spring.exceptions.FileUploadException;
import org.springframework.data.jpa.domain.Specification;


@Service
public class MemberService {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private FellowshipRepository fellowshipRepository;

  @Autowired
  private LocationService locationService;

  @Autowired
  private ChurchRepository churchRepository;

  @Autowired
  private ImageService imageService;

  @Autowired
  private ProfileCompletenessService profileCompletenessService;


  public List<MemberResponse> getAllMembers(){
    var members = memberRepository.findAll().stream().map(MemberMapper::toMemberResponse).toList();
    return members;
  }

  public Page<MemberResponse> getMembers(Long churchId, String search, String filter, Pageable pageable) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    Page<Member> members;

    if (search != null && !search.isEmpty()) {
      // Search with optional filter
      if ("verified".equals(filter)) {
        members = memberRepository.searchMembersWithVerifiedFilter(church, search, true, pageable);
      } else if ("unverified".equals(filter)) {
        members = memberRepository.searchMembersWithVerifiedFilter(church, search, false, pageable);
      } else if ("married".equals(filter)) {
        members = memberRepository.searchMembersWithMaritalFilter(church, search, "married", pageable);
      } else if ("single".equals(filter)) {
        members = memberRepository.searchMembersWithMaritalFilter(church, search, "single", pageable);
      } else {
        members = memberRepository.searchMembers(church, search, pageable);
      }
    } else {
      // Filter only, no search
      if ("verified".equals(filter)) {
        members = memberRepository.findByChurchAndIsVerified(church, true, pageable);
      } else if ("unverified".equals(filter)) {
        members = memberRepository.findByChurchAndIsVerified(church, false, pageable);
      } else if ("married".equals(filter)) {
        members = memberRepository.findByChurchAndMaritalStatus(church, "married", pageable);
      } else if ("single".equals(filter)) {
        members = memberRepository.findByChurchAndMaritalStatus(church, "single", pageable);
      } else {
        members = memberRepository.findByChurch(church, pageable);
      }
    }

    return members.map(MemberMapper::toMemberResponse);
  }

  public MemberStatsResponse getMemberStats(Long churchId) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    long totalMembers = memberRepository.countByChurch(church);
    long verified = memberRepository.countByChurchAndIsVerified(church, true);
    long unverified = memberRepository.countByChurchAndIsVerified(church, false);

    // Count members added in the current month
    YearMonth currentMonth = YearMonth.now();
    YearMonth lastMonth = currentMonth.minusMonths(1);
    long newThisMonth = memberRepository.countByChurchAndMemberSinceAfter(church, lastMonth);

    // Calculate active rate (verified members as percentage)
    int activeRate = totalMembers > 0 ? (int) ((verified * 100) / totalMembers) : 0;

    return new MemberStatsResponse(totalMembers, newThisMonth, verified, unverified, activeRate);
  }

  public MemberResponse getMemberById(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Member not found"));
    return MemberMapper.toMemberResponse(member);
  }

  public MemberResponse createMember(MemberRequest memberRequest){
    // Validate spouse name is required for married members
    validateSpouseRequirement(memberRequest.maritalStatus(), memberRequest.spouseName());

    var member = new Member();
    member.setFirstName(memberRequest.firstName());
    member.setOtherName(memberRequest.otherName());
    member.setLastName(memberRequest.lastName());
    member.setTitle(memberRequest.title());
    member.setSex(memberRequest.sex());

    Church church = churchRepository.findById(memberRequest.churchId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID provided"));
    member.setChurch(church);

    // Handle fellowships - only validate if IDs are provided
    if (memberRequest.fellowshipIds() != null && !memberRequest.fellowshipIds().isEmpty()) {
      List<Fellowship> fellowships = fellowshipRepository.findAllById(memberRequest.fellowshipIds());

      if (fellowships.size() != memberRequest.fellowshipIds().size()) {
        throw new IllegalArgumentException("One or more invalid fellowship IDs provided");
      }
      member.setFellowships(fellowships);
    } else {
      member.setFellowships(List.of());
    }
    member.setDob(memberRequest.dob());
    member.setCountryCode(memberRequest.countryCode() != null ? memberRequest.countryCode() : "GH"); // Default to Ghana
    member.setTimezone(memberRequest.timezone() != null ? memberRequest.timezone() : "Africa/Accra"); // Default to Ghana timezone
    member.setPhoneNumber(memberRequest.phoneNumber());
    member.setWhatsappNumber(memberRequest.whatsappNumber());
    member.setOtherPhoneNumber(memberRequest.otherPhoneNumber());

    // Handle location - use new Location entity if nominatimAddress is provided
    if (memberRequest.nominatimAddress() != null && memberRequest.coordinates() != null) {
      Location location = locationService.getOrCreateLocation(
        memberRequest.coordinates(),
        memberRequest.nominatimAddress()
      );
      member.setLocation(location);
    }

    member.setMaritalStatus(memberRequest.maritalStatus());
    member.setSpouseName(memberRequest.spouseName());
    member.setOccupation(memberRequest.occupation());
    member.setMemberSince(memberRequest.memberSince());
    member.setEmergencyContactName(memberRequest.emergencyContactName());
    member.setEmergencyContactNumber(memberRequest.emergencyContactNumber());
    member.setNotes(memberRequest.notes());
    member.setIsVerified(true);

    // Handle tags - normalize and set if provided
    if (memberRequest.tags() != null && !memberRequest.tags().isEmpty()) {
      member.setTags(normalizeTags(memberRequest.tags()));
    }

    // Calculate profile completeness
    int completeness = profileCompletenessService.calculateCompleteness(member);
    member.setProfileCompleteness(completeness);

    Member createdMember = memberRepository.save(member);
    return MemberMapper.toMemberResponse(createdMember);
  }

  /**
   * Validates that spouse name is provided when marital status is married.
   * Handles case-insensitive marital status check.
   *
   * @param maritalStatus the marital status to validate
   * @param spouseName the spouse name to check
   * @throws IllegalArgumentException if married but spouse name is missing
   */
  private void validateSpouseRequirement(String maritalStatus, String spouseName) {
    if (maritalStatus != null && maritalStatus.trim().equalsIgnoreCase("married")) {
      if (spouseName == null || spouseName.trim().isEmpty()) {
        throw new IllegalArgumentException("Spouse name is required for married members");
      }
    }
  }

  public MemberResponse updateMember(Long id, MemberRequest memberRequest){
    // Validate spouse name is required for married members
    validateSpouseRequirement(memberRequest.maritalStatus(), memberRequest.spouseName());

    var member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Member not found"));
    member.setFirstName(memberRequest.firstName());
    member.setOtherName(memberRequest.otherName());
    member.setLastName(memberRequest.lastName());
    member.setTitle(memberRequest.title());
    member.setSex(memberRequest.sex());

    Church church = churchRepository.findById(memberRequest.churchId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID provided"));
    member.setChurch(church);
    member.getFellowships().clear();

    // Handle fellowships - only validate if IDs are provided
    if (memberRequest.fellowshipIds() != null && !memberRequest.fellowshipIds().isEmpty()) {
      List<Fellowship> fellowships = fellowshipRepository.findAllById(memberRequest.fellowshipIds());

      if (fellowships.size() != memberRequest.fellowshipIds().size()) {
        throw new IllegalArgumentException("One or more invalid fellowship IDs provided");
      }
      member.getFellowships().addAll(fellowships);
    }
    member.setDob(memberRequest.dob());
    member.setCountryCode(memberRequest.countryCode() != null ? memberRequest.countryCode() : "GH"); // Default to Ghana
    member.setTimezone(memberRequest.timezone() != null ? memberRequest.timezone() : "Africa/Accra"); // Default to Ghana timezone
    member.setPhoneNumber(memberRequest.phoneNumber());
    member.setWhatsappNumber(memberRequest.whatsappNumber());
    member.setOtherPhoneNumber(memberRequest.otherPhoneNumber());

    // Handle location - use new Location entity if nominatimAddress is provided
    if (memberRequest.nominatimAddress() != null && memberRequest.coordinates() != null) {
      Location location = locationService.getOrCreateLocation(
        memberRequest.coordinates(),
        memberRequest.nominatimAddress()
      );
      member.setLocation(location);
    }

    member.setMaritalStatus(memberRequest.maritalStatus());
    member.setSpouseName(memberRequest.spouseName());
    member.setOccupation(memberRequest.occupation());
    member.setMemberSince(memberRequest.memberSince());
    member.setEmergencyContactName(memberRequest.emergencyContactName());
    member.setEmergencyContactNumber(memberRequest.emergencyContactNumber());
    member.setNotes(memberRequest.notes());
    member.setIsVerified(true);

    // Preserve existing profile image if not provided in update request
    if (memberRequest.profileImageUrl() != null) {
      member.setProfileImageUrl(memberRequest.profileImageUrl());
    }
    // If profileImageUrl is null in request, existing image is preserved (no change)

    // Handle tags - normalize and set if provided
    if (memberRequest.tags() != null) {
      member.getTags().clear();
      if (!memberRequest.tags().isEmpty()) {
        member.getTags().addAll(normalizeTags(memberRequest.tags()));
      }
    }

    // Calculate profile completeness
    int completeness = profileCompletenessService.calculateCompleteness(member);
    member.setProfileCompleteness(completeness);

    memberRepository.save(member);
    return MemberMapper.toMemberResponse(member);
  }

  public ProfileImageUploadResponse uploadProfileImage(Long id, MultipartFile image) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Member not found"));

    String oldImagePath = member.getProfileImageUrl();

    try {
      String imagePath = imageService.uploadProfileImage(image, oldImagePath);
      member.setProfileImageUrl(imagePath);
      memberRepository.save(member);
      return new ProfileImageUploadResponse(imagePath);
    } catch (IOException e) {
      throw new FileUploadException("Failed to upload image", e);
    }
  }

  public void deleteMember(Long id) {
    var member = memberRepository.findById(id).orElseThrow( ()-> new IllegalArgumentException("Member not found"));
    memberRepository.delete(member);
  }

  /**
   * Quick add a member with minimal required information.
   * Used for fast registration at church entrance, events, etc.
   * Sets default values and marks member for later profile completion.
   *
   * @param request Quick add request with minimal fields
   * @param churchId The church ID to associate the member with
   * @return Created member response
   */
  public MemberResponse quickAddMember(com.reuben.pastcare_spring.dtos.MemberQuickAddRequest request, Long churchId) {
    // Check for duplicate phone number
    if (memberRepository.findByPhoneNumber(request.phoneNumber()).isPresent()) {
      throw new IllegalArgumentException("Phone number already exists: " + request.phoneNumber());
    }

    // Validate church exists
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    // Create member with minimal fields
    var member = new Member();
    member.setFirstName(request.firstName());
    member.setLastName(request.lastName());
    member.setPhoneNumber(request.phoneNumber());
    member.setSex(request.sex());
    member.setChurch(church);

    // Set default values
    member.setCountryCode(request.countryCode() != null ? request.countryCode() : "GH");
    member.setTimezone("Africa/Accra"); // Default timezone
    member.setMaritalStatus("unknown"); // Default marital status for quick add
    member.setStatus(com.reuben.pastcare_spring.models.MemberStatus.VISITOR);
    member.setIsVerified(false);

    // Handle location if provided
    if (request.nominatimAddress() != null && request.coordinates() != null) {
      @SuppressWarnings("unchecked")
      Map<String, Object> addressMap = (Map<String, Object>) request.nominatimAddress();
      Location location = locationService.getOrCreateLocation(
        request.coordinates(),
        addressMap
      );
      member.setLocation(location);
    }

    // Add tags if provided
    if (request.tags() != null && !request.tags().isEmpty()) {
      member.setTags(new java.util.HashSet<>(request.tags()));
    }

    // Calculate profile completeness
    member.setProfileCompleteness(calculateProfileCompleteness(member));

    // Save member
    var savedMember = memberRepository.save(member);

    // TODO: Send welcome SMS if phone provided (future enhancement)

    return MemberMapper.toMemberResponse(savedMember);
  }

  /**
   * Calculate profile completeness percentage based on filled fields.
   *
   * @param member The member to calculate completeness for
   * @return Completeness percentage (0-100)
   */
  private int calculateProfileCompleteness(Member member) {
    int totalFields = 15; // Total number of profile fields
    int filledFields = 0;

    // Required fields
    if (member.getFirstName() != null && !member.getFirstName().isEmpty()) filledFields++;
    if (member.getLastName() != null && !member.getLastName().isEmpty()) filledFields++;
    if (member.getPhoneNumber() != null && !member.getPhoneNumber().isEmpty()) filledFields++;
    if (member.getSex() != null && !member.getSex().isEmpty()) filledFields++;
    if (member.getMaritalStatus() != null && !member.getMaritalStatus().isEmpty()) filledFields++;

    // Optional but important fields
    if (member.getOtherName() != null && !member.getOtherName().isEmpty()) filledFields++;
    if (member.getDob() != null) filledFields++;
    if (member.getLocation() != null) filledFields++;
    if (member.getProfileImageUrl() != null && !member.getProfileImageUrl().isEmpty()) filledFields++;
    if (member.getOccupation() != null && !member.getOccupation().isEmpty()) filledFields++;
    if (member.getWhatsappNumber() != null && !member.getWhatsappNumber().isEmpty()) filledFields++;
    if (member.getMemberSince() != null) filledFields++;
    if (member.getEmergencyContactName() != null && !member.getEmergencyContactName().isEmpty()) filledFields++;
    if (member.getEmergencyContactNumber() != null && !member.getEmergencyContactNumber().isEmpty()) filledFields++;
    if (member.getFellowships() != null && !member.getFellowships().isEmpty()) filledFields++;

    return (int) Math.round((filledFields * 100.0) / totalFields);
  }

  /**
   * Bulk import members from CSV/Excel data.
   * Supports column mapping, validation, duplicate detection, and partial imports.
   *
   * @param request Bulk import request with member data and options
   * @param churchId The church ID to associate imported members with
   * @return Import results with success/failure counts and error details
   */
  public MemberBulkImportResponse bulkImportMembers(MemberBulkImportRequest request, Long churchId) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    List<MemberResponse> importedMembers = new ArrayList<>();
    List<MemberBulkImportResponse.ImportError> errors = new ArrayList<>();
    int successCount = 0;
    int failureCount = 0;
    int duplicateCount = 0;
    int updatedCount = 0;

    // Process each row
    for (int i = 0; i < request.members().size(); i++) {
      Map<String, String> rowData = request.members().get(i);
      int rowNumber = i + 1; // 1-based for user readability

      try {
        // Apply column mapping if provided
        Map<String, String> mappedData = applyColumnMapping(rowData, request.columnMapping());

        // Check for duplicate phone number
        String phoneNumber = mappedData.get("phoneNumber");
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
          var existingMember = memberRepository.findByPhoneNumber(phoneNumber);

          if (existingMember.isPresent()) {
            if (request.updateExisting()) {
              // Update existing member
              Member updated = updateMemberFromMap(existingMember.get(), mappedData, church);
              importedMembers.add(MemberMapper.toMemberResponse(updated));
              updatedCount++;
              successCount++;
            } else {
              // Skip duplicate
              duplicateCount++;
              errors.add(new MemberBulkImportResponse.ImportError(
                rowNumber,
                "phoneNumber",
                "Phone number already exists: " + phoneNumber,
                rowData
              ));
            }
            continue;
          }
        }

        // Create new member from mapped data
        Member newMember = createMemberFromMap(mappedData, church);
        var savedMember = memberRepository.save(newMember);
        importedMembers.add(MemberMapper.toMemberResponse(savedMember));
        successCount++;

      } catch (IllegalArgumentException e) {
        failureCount++;
        errors.add(new MemberBulkImportResponse.ImportError(
          rowNumber,
          "validation",
          e.getMessage(),
          rowData
        ));

        // If not skipping invalid rows, fail entire import
        if (!request.skipInvalidRows()) {
          throw new IllegalArgumentException(
            "Import failed at row " + rowNumber + ": " + e.getMessage()
          );
        }
      }
    }

    return new MemberBulkImportResponse(
      request.members().size(),
      successCount,
      failureCount,
      duplicateCount,
      updatedCount,
      errors,
      importedMembers
    );
  }

  /**
   * Apply column mapping to raw row data.
   * Converts file column names to expected field names.
   */
  private Map<String, String> applyColumnMapping(Map<String, String> rowData, Map<String, String> mapping) {
    if (mapping == null || mapping.isEmpty()) {
      return rowData; // No mapping, return as-is
    }

    Map<String, String> mapped = new HashMap<>();
    for (Map.Entry<String, String> entry : rowData.entrySet()) {
      String fileColumn = entry.getKey();
      String value = entry.getValue();

      // Use mapping if exists, otherwise use original column name
      String targetField = mapping.getOrDefault(fileColumn, fileColumn);
      mapped.put(targetField, value);
    }
    return mapped;
  }

  /**
   * Create a new Member entity from mapped CSV data.
   */
  private Member createMemberFromMap(Map<String, String> data, Church church) {
    Member member = new Member();

    // Required fields
    member.setFirstName(getRequiredField(data, "firstName"));
    member.setLastName(getRequiredField(data, "lastName"));
    member.setPhoneNumber(getRequiredField(data, "phoneNumber"));
    member.setSex(getRequiredField(data, "sex"));
    member.setChurch(church);

    // Optional fields
    member.setOtherName(data.get("otherName"));
    member.setTitle(data.get("title"));
    member.setCountryCode(data.getOrDefault("countryCode", "GH"));
    member.setTimezone(data.getOrDefault("timezone", "Africa/Accra"));
    member.setWhatsappNumber(data.get("whatsappNumber"));
    member.setOtherPhoneNumber(data.get("otherPhoneNumber"));
    member.setMaritalStatus(data.getOrDefault("maritalStatus", "unknown"));
    member.setSpouseName(data.get("spouseName"));
    member.setOccupation(data.get("occupation"));
    member.setEmergencyContactName(data.get("emergencyContactName"));
    member.setEmergencyContactNumber(data.get("emergencyContactNumber"));
    member.setNotes(data.get("notes"));

    // Date fields
    if (data.get("dob") != null && !data.get("dob").isEmpty()) {
      try {
        member.setDob(LocalDate.parse(data.get("dob")));
      } catch (Exception e) {
        // Skip invalid date
      }
    }

    if (data.get("memberSince") != null && !data.get("memberSince").isEmpty()) {
      try {
        member.setMemberSince(YearMonth.parse(data.get("memberSince")));
      } catch (Exception e) {
        // Skip invalid date
      }
    }

    // Default values for bulk import
    member.setStatus(MemberStatus.MEMBER); // Default to MEMBER for bulk imports
    member.setIsVerified(false); // Needs verification
    member.setProfileCompleteness(calculateProfileCompleteness(member));

    return member;
  }

  /**
   * Update an existing Member entity from mapped CSV data.
   */
  private Member updateMemberFromMap(Member member, Map<String, String> data, Church church) {
    // Update only provided fields
    if (data.containsKey("firstName")) member.setFirstName(data.get("firstName"));
    if (data.containsKey("lastName")) member.setLastName(data.get("lastName"));
    if (data.containsKey("otherName")) member.setOtherName(data.get("otherName"));
    if (data.containsKey("title")) member.setTitle(data.get("title"));
    if (data.containsKey("sex")) member.setSex(data.get("sex"));
    if (data.containsKey("phoneNumber")) member.setPhoneNumber(data.get("phoneNumber"));
    if (data.containsKey("whatsappNumber")) member.setWhatsappNumber(data.get("whatsappNumber"));
    if (data.containsKey("otherPhoneNumber")) member.setOtherPhoneNumber(data.get("otherPhoneNumber"));
    if (data.containsKey("maritalStatus")) member.setMaritalStatus(data.get("maritalStatus"));
    if (data.containsKey("spouseName")) member.setSpouseName(data.get("spouseName"));
    if (data.containsKey("occupation")) member.setOccupation(data.get("occupation"));
    if (data.containsKey("emergencyContactName")) member.setEmergencyContactName(data.get("emergencyContactName"));
    if (data.containsKey("emergencyContactNumber")) member.setEmergencyContactNumber(data.get("emergencyContactNumber"));
    if (data.containsKey("notes")) member.setNotes(data.get("notes"));

    // Recalculate profile completeness
    member.setProfileCompleteness(calculateProfileCompleteness(member));

    return memberRepository.save(member);
  }

  /**
   * Get required field from data or throw exception.
   */
  private String getRequiredField(Map<String, String> data, String fieldName) {
    String value = data.get(fieldName);
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException("Required field missing: " + fieldName);
    }
    return value.trim();
  }

  /**
   * Bulk update multiple members with specified fields.
   * Supports updating fellowships, tags, status, and verification state.
   */
  public MemberBulkUpdateResponse bulkUpdateMembers(MemberBulkUpdateRequest request, Long churchId) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    List<MemberResponse> updatedMembers = new ArrayList<>();
    List<MemberBulkUpdateResponse.UpdateError> errors = new ArrayList<>();
    int successCount = 0;
    int failureCount = 0;

    for (Long memberId : request.memberIds()) {
      try {
        // Fetch member and verify it belongs to the church
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        if (!member.getChurch().getId().equals(churchId)) {
          throw new IllegalArgumentException("Member does not belong to your church");
        }

        // Apply updates
        boolean updated = false;

        // Update fellowships
        if (request.fellowshipIds() != null) {
          updated = true;
          updateMemberFellowships(member, request.fellowshipIds(), church);
        }

        // Update tags
        if (request.tags() != null) {
          updated = true;
          updateMemberTags(member, request.tags());
        }

        // Update status
        if (request.status() != null && !request.status().isBlank()) {
          updated = true;
          try {
            member.setStatus(MemberStatus.valueOf(request.status().toUpperCase()));
          } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + request.status());
          }
        }

        // Update verification status
        if (request.isVerified() != null) {
          updated = true;
          member.setIsVerified(request.isVerified());
        }

        // Update marital status
        if (request.maritalStatus() != null && !request.maritalStatus().isBlank()) {
          updated = true;
          member.setMaritalStatus(request.maritalStatus());
        }

        // Save if any updates were made
        if (updated) {
          // Recalculate profile completeness
          member.setProfileCompleteness(calculateProfileCompleteness(member));
          Member savedMember = memberRepository.save(member);
          updatedMembers.add(MemberMapper.toMemberResponse(savedMember));
          successCount++;
        } else {
          failureCount++;
          errors.add(new MemberBulkUpdateResponse.UpdateError(
              memberId,
              member.getFirstName() + " " + member.getLastName(),
              "No updates specified"
          ));
        }
      } catch (Exception e) {
        failureCount++;
        // Try to get member name for error message
        String memberName = "Unknown";
        try {
          var member = memberRepository.findById(memberId);
          if (member.isPresent()) {
            memberName = member.get().getFirstName() + " " + member.get().getLastName();
          }
        } catch (Exception ignored) {}

        errors.add(new MemberBulkUpdateResponse.UpdateError(
            memberId,
            memberName,
            e.getMessage()
        ));
      }
    }

    return new MemberBulkUpdateResponse(
        request.memberIds().size(),
        successCount,
        failureCount,
        errors,
        updatedMembers
    );
  }

  /**
   * Update member fellowships based on action type (ADD, REMOVE, REPLACE).
   */
  private void updateMemberFellowships(Member member, MemberBulkUpdateRequest.UpdateAction<List<Long>> action, Church church) {
    List<Fellowship> currentFellowships = member.getFellowships();

    switch (action.action()) {
      case ADD -> {
        // Add new fellowships to existing ones
        for (Long fellowshipId : action.values()) {
          Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
              .orElseThrow(() -> new IllegalArgumentException("Fellowship not found: " + fellowshipId));
          if (!fellowship.getChurch().getId().equals(church.getId())) {
            throw new IllegalArgumentException("Fellowship does not belong to your church");
          }
          currentFellowships.add(fellowship);
        }
      }
      case REMOVE -> {
        // Remove specified fellowships
        currentFellowships.removeIf(f -> action.values().contains(f.getId()));
      }
      case REPLACE -> {
        // Replace all fellowships
        currentFellowships.clear();
        for (Long fellowshipId : action.values()) {
          Fellowship fellowship = fellowshipRepository.findById(fellowshipId)
              .orElseThrow(() -> new IllegalArgumentException("Fellowship not found: " + fellowshipId));
          if (!fellowship.getChurch().getId().equals(church.getId())) {
            throw new IllegalArgumentException("Fellowship does not belong to your church");
          }
          currentFellowships.add(fellowship);
        }
      }
    }
  }

  /**
   * Update member tags based on action type (ADD, REMOVE, REPLACE).
   */
  private void updateMemberTags(Member member, MemberBulkUpdateRequest.UpdateAction<Set<String>> action) {
    Set<String> currentTags = member.getTags();

    switch (action.action()) {
      case ADD -> {
        // Add new tags to existing ones
        currentTags.addAll(action.values());
      }
      case REMOVE -> {
        // Remove specified tags
        currentTags.removeAll(action.values());
      }
      case REPLACE -> {
        // Replace all tags
        currentTags.clear();
        currentTags.addAll(action.values());
      }
    }
  }

  /**
   * Bulk delete members
   * Deletes multiple members at once with church validation
   *
   * @param memberIds List of member IDs to delete
   * @param churchId ID of the church making the request
   * @return Map with success/failure counts and error details
   */
  public Map<String, Object> bulkDeleteMembers(List<Long> memberIds, Long churchId) {
    // Validate church exists
    churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    int successCount = 0;
    int failureCount = 0;
    List<Map<String, Object>> errors = new ArrayList<>();

    for (Long memberId : memberIds) {
      try {
        // Fetch member and verify it belongs to the church
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        if (!member.getChurch().getId().equals(churchId)) {
          throw new IllegalArgumentException("Member does not belong to your church");
        }

        // Delete the member
        memberRepository.delete(member);
        successCount++;

      } catch (Exception e) {
        failureCount++;
        Map<String, Object> error = new HashMap<>();
        error.put("memberId", memberId);
        error.put("errorMessage", e.getMessage());
        errors.add(error);
      }
    }

    Map<String, Object> result = new HashMap<>();
    result.put("totalMembers", memberIds.size());
    result.put("successCount", successCount);
    result.put("failureCount", failureCount);
    result.put("errors", errors);

    return result;
  }

  /**
   * Advanced search with dynamic filter criteria.
   * Supports complex queries with multiple filters, logical operators (AND/OR),
   * and nested filter groups.
   *
   * @param request  Search request with filter criteria
   * @param churchId Church ID to filter by (ensures data isolation)
   * @param pageable Pagination parameters
   * @return Search response with filtered members and metadata
   */
  public AdvancedSearchResponse advancedSearch(AdvancedSearchRequest request, Long churchId, Pageable pageable) {
    long startTime = System.currentTimeMillis();

    // Validate church exists
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    // Build specification from search request
    Specification<Member> specification = MemberSpecification.fromAdvancedSearch(request, church);

    // Execute query
    Page<Member> members = memberRepository.findAll(specification, pageable);

    // Calculate execution time
    long executionTime = System.currentTimeMillis() - startTime;

    // Count total filters
    int totalFilters = request.filterGroups().stream()
        .mapToInt(group -> group.filters().size())
        .sum();

    // Build metadata
    AdvancedSearchResponse.SearchMetadata metadata = new AdvancedSearchResponse.SearchMetadata(
        totalFilters,
        executionTime,
        "Advanced search with " + totalFilters + " filter(s)"
    );

    // Map to response DTOs
    Page<MemberResponse> memberResponses = members.map(MemberMapper::toMemberResponse);

    return new AdvancedSearchResponse(memberResponses, metadata);
  }

  // ==================== Tag Management Methods ====================

  /**
   * Normalizes tags by converting to lowercase and trimming whitespace.
   * Validates tag format: 1-50 characters, alphanumeric, hyphens, and underscores only.
   *
   * @param tags the set of tags to normalize
   * @return normalized set of tags
   * @throws IllegalArgumentException if any tag is invalid
   */
  private Set<String> normalizeTags(Set<String> tags) {
    Set<String> normalized = new java.util.HashSet<>();

    for (String tag : tags) {
      if (tag == null || tag.trim().isEmpty()) {
        continue; // Skip null or empty tags
      }

      String normalizedTag = tag.trim().toLowerCase();

      // Validate tag format
      if (!normalizedTag.matches("^[a-z0-9_-]{1,50}$")) {
        throw new IllegalArgumentException(
          "Invalid tag format: '" + tag + "'. Tags must be 1-50 characters and contain only lowercase letters, numbers, hyphens, and underscores."
        );
      }

      normalized.add(normalizedTag);
    }

    return normalized;
  }

  /**
   * Adds tags to a member.
   *
   * @param memberId the member ID
   * @param tags the tags to add
   * @return updated member response
   */
  public MemberResponse addTags(Long memberId, Set<String> tags) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("Member not found"));

    Set<String> normalizedTags = normalizeTags(tags);
    member.getTags().addAll(normalizedTags);

    memberRepository.save(member);
    return MemberMapper.toMemberResponse(member);
  }

  /**
   * Removes tags from a member.
   *
   * @param memberId the member ID
   * @param tags the tags to remove
   * @return updated member response
   */
  public MemberResponse removeTags(Long memberId, Set<String> tags) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("Member not found"));

    Set<String> normalizedTags = normalizeTags(tags);
    member.getTags().removeAll(normalizedTags);

    memberRepository.save(member);
    return MemberMapper.toMemberResponse(member);
  }

  /**
   * Replaces all tags for a member.
   *
   * @param memberId the member ID
   * @param tags the new set of tags
   * @return updated member response
   */
  public MemberResponse setTags(Long memberId, Set<String> tags) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("Member not found"));

    member.getTags().clear();
    if (tags != null && !tags.isEmpty()) {
      Set<String> normalizedTags = normalizeTags(tags);
      member.getTags().addAll(normalizedTags);
    }

    memberRepository.save(member);
    return MemberMapper.toMemberResponse(member);
  }

  /**
   * Gets all unique tags used across all members in a church.
   *
   * @param churchId the church ID
   * @return set of all unique tags with usage count
   */
  public Map<String, Long> getAllTags(Long churchId) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    Page<Member> membersPage = memberRepository.findByChurch(church, org.springframework.data.domain.Pageable.unpaged());
    List<Member> members = membersPage.getContent();

    // Count tag occurrences
    Map<String, Long> tagCounts = new HashMap<>();
    for (Member member : members) {
      for (String tag : member.getTags()) {
        tagCounts.put(tag, tagCounts.getOrDefault(tag, 0L) + 1);
      }
    }

    return tagCounts;
  }

  /**
   * Finds all members with a specific tag.
   *
   * @param churchId the church ID
   * @param tag the tag to search for
   * @param pageable pagination information
   * @return page of members with the tag
   */
  public Page<MemberResponse> getMembersByTag(Long churchId, String tag, Pageable pageable) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    String normalizedTag = tag.trim().toLowerCase();
    Page<Member> members = memberRepository.findByChurchAndTagsContaining(church, normalizedTag, pageable);

    return members.map(MemberMapper::toMemberResponse);
  }

  // ==================== Spouse Linking Methods ====================

  /**
   * Links two members as spouses (bidirectional).
   * Both members must belong to the same church.
   * If either member is already linked to a different spouse, the old link is removed.
   *
   * @param memberId the member to link
   * @param spouseId the spouse member to link
   * @param churchId the church ID for validation
   * @return the updated member response
   */
  public MemberResponse linkSpouse(Long memberId, Long spouseId, Long churchId) {
    // Validate same member not linking to self
    if (memberId.equals(spouseId)) {
      throw new IllegalArgumentException("A member cannot be linked to themselves as spouse");
    }

    // Fetch both members
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
    Member spouse = memberRepository.findById(spouseId)
        .orElseThrow(() -> new IllegalArgumentException("Spouse member not found: " + spouseId));

    // Validate both belong to the same church
    if (!member.getChurch().getId().equals(churchId)) {
      throw new IllegalArgumentException("Member does not belong to your church");
    }
    if (!spouse.getChurch().getId().equals(churchId)) {
      throw new IllegalArgumentException("Spouse member does not belong to your church");
    }

    // Remove existing spouse links if any (clean up old relationships)
    if (member.getSpouse() != null && !member.getSpouse().getId().equals(spouseId)) {
      Member oldSpouse = member.getSpouse();
      oldSpouse.setSpouse(null);
      oldSpouse.setSpouseName(null);
      memberRepository.save(oldSpouse);
    }
    if (spouse.getSpouse() != null && !spouse.getSpouse().getId().equals(memberId)) {
      Member oldSpouse = spouse.getSpouse();
      oldSpouse.setSpouse(null);
      oldSpouse.setSpouseName(null);
      memberRepository.save(oldSpouse);
    }

    // Create bidirectional link
    member.setSpouse(spouse);
    member.setSpouseName(spouse.getFirstName() + " " + spouse.getLastName());
    member.setMaritalStatus("married");

    spouse.setSpouse(member);
    spouse.setSpouseName(member.getFirstName() + " " + member.getLastName());
    spouse.setMaritalStatus("married");

    // If both are in different households, optionally assign to same household
    // This is just a suggestion - the household assignment is left to the caller

    // Recalculate profile completeness for both
    member.setProfileCompleteness(profileCompletenessService.calculateCompleteness(member));
    spouse.setProfileCompleteness(profileCompletenessService.calculateCompleteness(spouse));

    // Save both
    memberRepository.save(spouse);
    Member savedMember = memberRepository.save(member);

    return MemberMapper.toMemberResponse(savedMember);
  }

  /**
   * Unlinks a member from their spouse (bidirectional).
   * Removes the spouse link from both members.
   *
   * @param memberId the member to unlink
   * @param churchId the church ID for validation
   * @return the updated member response
   */
  public MemberResponse unlinkSpouse(Long memberId, Long churchId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

    // Validate church ownership
    if (!member.getChurch().getId().equals(churchId)) {
      throw new IllegalArgumentException("Member does not belong to your church");
    }

    // Check if member has a spouse
    if (member.getSpouse() == null) {
      throw new IllegalArgumentException("Member is not linked to a spouse");
    }

    // Get the spouse and unlink both sides
    Member spouse = member.getSpouse();

    member.setSpouse(null);
    // Keep spouseName as historical reference but could be cleared if desired
    // member.setSpouseName(null);

    spouse.setSpouse(null);
    // spouse.setSpouseName(null);

    // Recalculate profile completeness for both
    member.setProfileCompleteness(profileCompletenessService.calculateCompleteness(member));
    spouse.setProfileCompleteness(profileCompletenessService.calculateCompleteness(spouse));

    // Save both
    memberRepository.save(spouse);
    Member savedMember = memberRepository.save(member);

    return MemberMapper.toMemberResponse(savedMember);
  }

  /**
   * Gets the spouse of a member.
   *
   * @param memberId the member ID
   * @param churchId the church ID for validation
   * @return the spouse member response, or null if not linked
   */
  public MemberResponse getSpouse(Long memberId, Long churchId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

    // Validate church ownership
    if (!member.getChurch().getId().equals(churchId)) {
      throw new IllegalArgumentException("Member does not belong to your church");
    }

    if (member.getSpouse() == null) {
      return null;
    }

    return MemberMapper.toMemberResponse(member.getSpouse());
  }

  // ==================== Profile Completeness Methods ====================

  /**
   * Gets profile completeness details for a member.
   *
   * @param memberId the member ID
   * @return completeness response with missing fields and suggestions
   */
  public ProfileCompletenessResponse getProfileCompleteness(Long memberId) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new IllegalArgumentException("Member not found"));

    int completeness = profileCompletenessService.calculateCompleteness(member);
    List<String> missingFields = profileCompletenessService.getMissingFields(member);
    List<String> suggestions = profileCompletenessService.getSuggestions(member);

    return new ProfileCompletenessResponse(completeness, missingFields, suggestions);
  }

  /**
   * Gets church-wide completeness statistics.
   *
   * @param churchId the church ID
   * @return completeness statistics
   */
  public CompletenessStatsResponse getCompletenessStats(Long churchId) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    Page<Member> membersPage = memberRepository.findByChurch(church, org.springframework.data.domain.Pageable.unpaged());
    List<Member> members = membersPage.getContent();

    if (members.isEmpty()) {
      return new CompletenessStatsResponse(
          0.0, 0, 0, 0, 0,
          new CompletenessStatsResponse.Distribution(0, 0, 0, 0)
      );
    }

    // Calculate statistics
    long totalMembers = members.size();
    double totalCompleteness = 0;
    long range0to25 = 0;
    long range26to50 = 0;
    long range51to75 = 0;
    long range76to100 = 0;

    for (Member member : members) {
      int completeness = member.getProfileCompleteness() != null ?
          member.getProfileCompleteness() : 0;

      totalCompleteness += completeness;

      if (completeness <= 25) {
        range0to25++;
      } else if (completeness <= 50) {
        range26to50++;
      } else if (completeness <= 75) {
        range51to75++;
      } else {
        range76to100++;
      }
    }

    double averageCompleteness = totalCompleteness / totalMembers;
    long completeProfiles = members.stream()
        .filter(m -> m.getProfileCompleteness() != null && m.getProfileCompleteness() == 100)
        .count();
    long nearlyComplete = members.stream()
        .filter(m -> m.getProfileCompleteness() != null &&
            m.getProfileCompleteness() >= 75 && m.getProfileCompleteness() < 100)
        .count();
    long incomplete = members.stream()
        .filter(m -> m.getProfileCompleteness() == null || m.getProfileCompleteness() < 75)
        .count();

    CompletenessStatsResponse.Distribution distribution =
        new CompletenessStatsResponse.Distribution(range0to25, range26to50, range51to75, range76to100);

    return new CompletenessStatsResponse(
        averageCompleteness,
        totalMembers,
        completeProfiles,
        nearlyComplete,
        incomplete,
        distribution
    );
  }
}
