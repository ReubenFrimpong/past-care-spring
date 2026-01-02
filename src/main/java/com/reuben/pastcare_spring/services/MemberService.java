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
import org.springframework.transaction.annotation.Transactional;
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
import com.reuben.pastcare_spring.models.Household;
import com.reuben.pastcare_spring.models.Location;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.models.MemberStatus;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.FellowshipRepository;
import com.reuben.pastcare_spring.repositories.HouseholdRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.specifications.MemberSpecification;
import com.reuben.pastcare_spring.exceptions.FileUploadException;
import org.springframework.data.jpa.domain.Specification;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class MemberService {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private TenantValidationService tenantValidationService;

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

  @Autowired
  private TierEnforcementService tierEnforcementService;

  @Autowired
  private GoalService goalService;

  @Autowired
  private HouseholdRepository householdRepository;


  @Transactional(readOnly = true)
  public List<MemberResponse> getAllMembers(){
    var members = memberRepository.findAll().stream().map(MemberMapper::toMemberResponse).toList();
    return members;
  }

  @Transactional(readOnly = true)
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

  @Transactional(readOnly = true)
  public MemberResponse getMemberById(Long id) {
    Member member = memberRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Member not found"));

    // CRITICAL SECURITY: Validate member belongs to current church
    tenantValidationService.validateMemberAccess(member);

    return MemberMapper.toMemberResponse(member);
  }

  public MemberResponse createMember(MemberRequest memberRequest){

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
    member.setEmail(memberRequest.email());
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

    // Handle household assignment/creation BEFORE saving member
    handleHouseholdAssignment(member, memberRequest, church);

    // Save member first (needed for family relation linkages)
    Member createdMember = memberRepository.save(member);

    // Handle family relations AFTER saving member (requires member ID)
    handleFamilyRelations(createdMember, memberRequest, church);

    // Recalculate MEMBERS type goals after adding a new member
    recalculateMemberGoals();

    return MemberMapper.toMemberResponse(createdMember);
  }

  public MemberResponse updateMember(Long id, MemberRequest memberRequest){

    var member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Member not found"));

    // CRITICAL SECURITY: Validate member belongs to current church
    tenantValidationService.validateMemberAccess(member);

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
    member.setEmail(memberRequest.email());
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

    // Handle household assignment/creation (only if explicitly requested)
    if (memberRequest.householdId() != null || Boolean.TRUE.equals(memberRequest.createNewHousehold())) {
      handleHouseholdAssignment(member, memberRequest, church);
    }

    // Save member
    memberRepository.save(member);

    // Handle family relations (only if explicitly provided in request)
    if (memberRequest.linkSpouseId() != null ||
        (memberRequest.linkParentIds() != null && !memberRequest.linkParentIds().isEmpty()) ||
        (memberRequest.linkChildIds() != null && !memberRequest.linkChildIds().isEmpty())) {
      handleFamilyRelations(member, memberRequest, church);
    }

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

    // CRITICAL SECURITY: Validate member belongs to current church
    tenantValidationService.validateMemberAccess(member);

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
    member.setEmail(request.email());
    member.setSex(request.sex());
    member.setChurch(church);

    // Set default values
    member.setCountryCode(request.countryCode() != null ? request.countryCode() : "GH");
    member.setTimezone("Africa/Accra"); // Default timezone
    member.setMaritalStatus("unknown"); // Default marital status for quick add
    member.setStatus(MemberStatus.MEMBER); // Default to MEMBER (visitors are managed in a separate module)
    member.setIsVerified(false); // Quick-added members need verification

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

    // Recalculate MEMBERS type goals after adding a new member
    recalculateMemberGoals();

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
   * <p><strong>SECURITY:</strong> Enforces tier member limits before processing any members.
   * Throws TierLimitExceededException if import would exceed tier limit.
   *
   * @param request Bulk import request with member data and options
   * @param churchId The church ID to associate imported members with
   * @return Import results with success/failure counts and error details
   * @throws com.reuben.pastcare_spring.exceptions.TierLimitExceededException if would exceed tier limit
   */
  public MemberBulkImportResponse bulkImportMembers(MemberBulkImportRequest request, Long churchId) {
    Church church = churchRepository.findById(churchId)
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID"));

    // ============================================================================
    // CRITICAL SECURITY: Enforce tier member limit BEFORE processing any members
    // ============================================================================
    // Calculate how many NEW members will be created (exclude updates/duplicates estimate)
    int totalRowsToProcess = request.members().size();

    // Count duplicates if updateExisting is false (they won't be new members)
    int estimatedDuplicates = 0;
    if (!request.updateExisting()) {
      for (Map<String, String> rowData : request.members()) {
        String phoneNumber = rowData.get("phoneNumber");
        if (phoneNumber != null && !phoneNumber.isEmpty()) {
          if (memberRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            estimatedDuplicates++;
          }
        }
      }
    }

    // Worst case: all rows are new members (if updateExisting=true or no duplicates)
    int maxNewMembers = totalRowsToProcess - estimatedDuplicates;

    // Enforce tier limit for maximum possible new members
    // This prevents tier bypass through bulk upload
    tierEnforcementService.enforceTierLimit(churchId, maxNewMembers);
    // If we reach here, tier limit check passed

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

    // Recalculate MEMBERS type goals after bulk import (if any members were added)
    if (successCount > 0) {
      recalculateMemberGoals();
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
    // Validate logical consistency before creating member
    validateDataLogicalConsistency(data);

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
    member.setIsVerified(true); // Bulk imported members are verified (existing church members)
    member.setProfileCompleteness(calculateProfileCompleteness(member));

    return member;
  }

  /**
   * Validate logical consistency of CSV import data.
   * Catches contradictions like single status with spouse info.
   */
  private void validateDataLogicalConsistency(Map<String, String> data) {
    List<String> warnings = new ArrayList<>();

    String maritalStatus = data.get("maritalStatus");
    String spouseName = data.get("spouseName");
    String spousePhone = data.get("spousePhone");
    String dob = data.get("dob");
    String sex = data.get("sex");

    // Check: Single/Never Married but has spouse information
    if (maritalStatus != null) {
      String status = maritalStatus.toLowerCase().trim();
      boolean isSingle = status.equals("single") || status.equals("never married") || status.equals("never_married");
      boolean hasSpouseInfo = (spouseName != null && !spouseName.trim().isEmpty()) ||
                              (spousePhone != null && !spousePhone.trim().isEmpty());

      if (isSingle && hasSpouseInfo) {
        throw new IllegalArgumentException(
          "Logical inconsistency: Marital status is '" + maritalStatus +
          "' but spouse information is provided. Please verify the data."
        );
      }

      // Check: Married/Engaged but no spouse info (warning, not error)
      boolean isMarried = status.equals("married") || status.equals("engaged");
      if (isMarried && !hasSpouseInfo) {
        // This is okay - spouse info is optional, just log for awareness
        log.debug("Member marked as '{}' but no spouse info provided", maritalStatus);
      }
    }

    // Check: Date of birth makes member unreasonably young or old
    if (dob != null && !dob.trim().isEmpty()) {
      try {
        LocalDate birthDate = LocalDate.parse(dob);
        LocalDate now = LocalDate.now();
        int age = now.getYear() - birthDate.getYear();

        if (age < 0) {
          throw new IllegalArgumentException(
            "Invalid date of birth: " + dob + " is in the future"
          );
        }

        if (age > 120) {
          throw new IllegalArgumentException(
            "Invalid date of birth: " + dob + " would make member over 120 years old"
          );
        }

        // Warning for very young members (but allow since could be children)
        if (age < 1) {
          log.debug("Member has date of birth less than 1 year ago: {}", dob);
        }
      } catch (java.time.format.DateTimeParseException e) {
        throw new IllegalArgumentException(
          "Invalid date format for dob: " + dob + ". Use YYYY-MM-DD format."
        );
      }
    }

    // Check: Sex field validation
    if (sex != null && !sex.trim().isEmpty()) {
      String normalizedSex = sex.toLowerCase().trim();
      if (!normalizedSex.equals("male") && !normalizedSex.equals("female") &&
          !normalizedSex.equals("m") && !normalizedSex.equals("f")) {
        throw new IllegalArgumentException(
          "Invalid sex value: '" + sex + "'. Use 'male', 'female', 'm', or 'f'."
        );
      }
    }

    // Check: Marital status validation
    if (maritalStatus != null && !maritalStatus.trim().isEmpty()) {
      String status = maritalStatus.toLowerCase().trim();
      List<String> validStatuses = List.of(
        "single", "married", "divorced", "widowed", "separated",
        "engaged", "never married", "never_married", "unknown"
      );
      if (!validStatuses.contains(status)) {
        throw new IllegalArgumentException(
          "Invalid marital status: '" + maritalStatus +
          "'. Valid values: single, married, divorced, widowed, separated, engaged, never married, unknown."
        );
      }
    }

    // Check: Title consistency with sex (optional validation)
    String title = data.get("title");
    if (title != null && sex != null) {
      String normalizedTitle = title.toLowerCase().trim();
      String normalizedSex = sex.toLowerCase().trim();

      // Mr. should be male, Mrs./Miss should be female
      if ((normalizedTitle.equals("mr") || normalizedTitle.equals("mr.")) &&
          (normalizedSex.equals("female") || normalizedSex.equals("f"))) {
        throw new IllegalArgumentException(
          "Logical inconsistency: Title 'Mr.' is typically used for males, but sex is 'female'."
        );
      }
      if ((normalizedTitle.equals("mrs") || normalizedTitle.equals("mrs.") ||
           normalizedTitle.equals("miss") || normalizedTitle.equals("ms") || normalizedTitle.equals("ms.")) &&
          (normalizedSex.equals("male") || normalizedSex.equals("m"))) {
        throw new IllegalArgumentException(
          "Logical inconsistency: Title '" + title + "' is typically used for females, but sex is 'male'."
        );
      }
    }
  }

  /**
   * Update an existing Member entity from mapped CSV data.
   */
  private Member updateMemberFromMap(Member member, Map<String, String> data, Church church) {
    // Validate logical consistency before updating
    // Merge existing member data with new data for validation
    Map<String, String> mergedData = new HashMap<>(data);
    if (!mergedData.containsKey("maritalStatus") && member.getMaritalStatus() != null) {
      mergedData.put("maritalStatus", member.getMaritalStatus());
    }
    if (!mergedData.containsKey("sex") && member.getSex() != null) {
      mergedData.put("sex", member.getSex());
    }
    if (!mergedData.containsKey("dob") && member.getDob() != null) {
      mergedData.put("dob", member.getDob().toString());
    }
    if (!mergedData.containsKey("title") && member.getTitle() != null) {
      mergedData.put("title", member.getTitle());
    }
    validateDataLogicalConsistency(mergedData);

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
   * This operation is optional - married members can just provide spouse name without linking.
   * If either member is already linked to a different spouse, the old link is removed.
   *
   * @param memberId the member to link
   * @param spouseId the spouse member to link
   * @param churchId the church ID for validation
   * @return the updated member response
   */
  @Transactional
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
      memberRepository.save(oldSpouse);
    }
    if (spouse.getSpouse() != null && !spouse.getSpouse().getId().equals(memberId)) {
      Member oldSpouse = spouse.getSpouse();
      oldSpouse.setSpouse(null);
      memberRepository.save(oldSpouse);
    }

    // Create bidirectional link
    member.setSpouse(spouse);
    member.setMaritalStatus("married");

    spouse.setSpouse(member);
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
  @Transactional
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
    spouse.setSpouse(null);

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
  @Transactional(readOnly = true)
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

  // ==================== Parent-Child Relationship Methods ====================

  /**
   * Adds a parent to a child member.
   *
   * @param childId the child member ID
   * @param parentId the parent member ID
   * @param churchId the church ID for validation
   * @return the updated child member response
   */
  @Transactional
  public MemberResponse addParent(Long childId, Long parentId, Long churchId) {
    // Validate not linking member to themselves
    if (childId.equals(parentId)) {
      throw new IllegalArgumentException("A member cannot be their own parent");
    }

    // Fetch both members
    Member child = memberRepository.findById(childId)
        .orElseThrow(() -> new IllegalArgumentException("Child member not found: " + childId));
    Member parent = memberRepository.findById(parentId)
        .orElseThrow(() -> new IllegalArgumentException("Parent member not found: " + parentId));

    // Validate both belong to the same church
    if (!child.getChurch().getId().equals(churchId)) {
      throw new IllegalArgumentException("Child member does not belong to your church");
    }
    if (!parent.getChurch().getId().equals(churchId)) {
      throw new IllegalArgumentException("Parent member does not belong to your church");
    }

    // Check if parent is already added
    if (child.getParents().contains(parent)) {
      throw new IllegalArgumentException("This parent is already linked to the child");
    }

    // Add parent to child
    child.getParents().add(parent);

    // Save child (bidirectional relationship will be managed by JPA)
    Member savedChild = memberRepository.save(child);

    return MemberMapper.toMemberResponse(savedChild);
  }

  /**
   * Removes a parent from a child member.
   *
   * @param childId the child member ID
   * @param parentId the parent member ID to remove
   * @param churchId the church ID for validation
   * @return the updated child member response
   */
  @Transactional
  public MemberResponse removeParent(Long childId, Long parentId, Long churchId) {
    Member child = memberRepository.findById(childId)
        .orElseThrow(() -> new IllegalArgumentException("Child member not found: " + childId));
    Member parent = memberRepository.findById(parentId)
        .orElseThrow(() -> new IllegalArgumentException("Parent member not found: " + parentId));

    // Validate church ownership
    if (!child.getChurch().getId().equals(churchId)) {
      throw new IllegalArgumentException("Child member does not belong to your church");
    }

    // Check if parent exists
    if (!child.getParents().contains(parent)) {
      throw new IllegalArgumentException("This parent is not linked to the child");
    }

    // Remove parent from child
    child.getParents().remove(parent);

    // Save child
    Member savedChild = memberRepository.save(child);

    return MemberMapper.toMemberResponse(savedChild);
  }

  /**
   * Gets all parents of a child member.
   *
   * @param childId the child member ID
   * @param churchId the church ID for validation
   * @return list of parent member responses
   */
  @Transactional(readOnly = true)
  public List<MemberResponse> getParents(Long childId, Long churchId) {
    Member child = memberRepository.findById(childId)
        .orElseThrow(() -> new IllegalArgumentException("Child member not found: " + childId));

    // Validate church ownership
    if (!child.getChurch().getId().equals(churchId)) {
      throw new IllegalArgumentException("Member does not belong to your church");
    }

    return child.getParents().stream()
        .map(MemberMapper::toMemberResponse)
        .toList();
  }

  /**
   * Gets all children of a parent member.
   *
   * @param parentId the parent member ID
   * @param churchId the church ID for validation
   * @return list of child member responses
   */
  @Transactional(readOnly = true)
  public List<MemberResponse> getChildren(Long parentId, Long churchId) {
    Member parent = memberRepository.findById(parentId)
        .orElseThrow(() -> new IllegalArgumentException("Parent member not found: " + parentId));

    // Validate church ownership
    if (!parent.getChurch().getId().equals(churchId)) {
      throw new IllegalArgumentException("Member does not belong to your church");
    }

    return parent.getChildren().stream()
        .map(MemberMapper::toMemberResponse)
        .toList();
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

  // ==================== Household and Family Relations Helpers ====================

  /**
   * Handles household assignment or creation for a member.
   * Priority order:
   * 1. Join existing household (if householdId provided)
   * 2. Create new household (if createNewHousehold is true)
   * 3. Inherit spouse's household (if linking spouse with household)
   */
  private void handleHouseholdAssignment(Member member, MemberRequest request, Church church) {
    // Priority 1: Join existing household
    if (request.householdId() != null) {
      Household household = householdRepository.findById(request.householdId())
          .orElseThrow(() -> new IllegalArgumentException("Household not found with id: " + request.householdId()));

      // Validate household belongs to same church
      if (!household.getChurch().getId().equals(church.getId())) {
        throw new IllegalArgumentException("Household does not belong to the same church");
      }

      member.setHousehold(household);
      log.info("Member assigned to existing household: {}", household.getHouseholdName());
      return;
    }

    // Priority 2: Create new household
    if (Boolean.TRUE.equals(request.createNewHousehold())) {
      Household newHousehold = createHouseholdForMember(member, request, church);
      member.setHousehold(newHousehold);
      log.info("Created new household for member: {}", newHousehold.getHouseholdName());
      return;
    }

    // Priority 3: Inherit spouse's household (if linking spouse)
    if (request.linkSpouseId() != null) {
      Member spouse = memberRepository.findById(request.linkSpouseId()).orElse(null);
      if (spouse != null && spouse.getHousehold() != null) {
        // Validate spouse's household belongs to same church
        if (spouse.getHousehold().getChurch().getId().equals(church.getId())) {
          member.setHousehold(spouse.getHousehold());
          log.info("Member inherited spouse's household: {}", spouse.getHousehold().getHouseholdName());
          return;
        }
      }
    }

    // No household assignment
    log.info("Member created without household assignment");
  }

  /**
   * Creates a new household for a member.
   */
  private Household createHouseholdForMember(Member member, MemberRequest request, Church church) {
    Household household = new Household();
    household.setChurch(church);

    // Generate household name if not provided
    String householdName = request.newHouseholdName();
    if (householdName == null || householdName.isBlank()) {
      householdName = member.getLastName() + " Household";
    }
    household.setHouseholdName(householdName);

    // Set household head if requested
    if (Boolean.TRUE.equals(request.makeHouseholdHead())) {
      household.setHouseholdHead(member);
    }

    // Inherit location from member
    if (member.getLocation() != null) {
      household.setSharedLocation(member.getLocation());
    }

    // Set default established date to today
    household.setEstablishedDate(LocalDate.now());

    return householdRepository.save(household);
  }

  /**
   * Handles family relation linkages (spouse, parents, children) atomically.
   * Must be called AFTER member is saved (requires member ID).
   */
  private void handleFamilyRelations(Member member, MemberRequest request, Church church) {
    // Link spouse (bidirectional)
    if (request.linkSpouseId() != null) {
      linkSpouseBidirectional(member, request.linkSpouseId(), church);
    }

    // Link parents (many-to-many)
    if (request.linkParentIds() != null && !request.linkParentIds().isEmpty()) {
      linkParents(member, request.linkParentIds(), church);
    }

    // Link children (many-to-many)
    if (request.linkChildIds() != null && !request.linkChildIds().isEmpty()) {
      linkChildren(member, request.linkChildIds(), church);
    }
  }

  /**
   * Links spouse bidirectionally (sets both member.spouse and spouse.spouse).
   */
  private void linkSpouseBidirectional(Member member, Long spouseId, Church church) {
    Member spouse = memberRepository.findById(spouseId)
        .orElseThrow(() -> new IllegalArgumentException("Spouse not found with id: " + spouseId));

    // Validate spouse belongs to same church
    if (!spouse.getChurch().getId().equals(church.getId())) {
      throw new IllegalArgumentException("Spouse does not belong to the same church");
    }

    // Prevent self-linking
    if (member.getId().equals(spouseId)) {
      throw new IllegalArgumentException("Cannot link member as their own spouse");
    }

    // Set bidirectional relationship
    member.setSpouse(spouse);
    spouse.setSpouse(member);

    memberRepository.save(spouse);
    log.info("Linked spouse bidirectionally: {} <-> {}", member.getId(), spouse.getId());
  }

  /**
   * Links parents to member (many-to-many).
   */
  private void linkParents(Member member, List<Long> parentIds, Church church) {
    for (Long parentId : parentIds) {
      Member parent = memberRepository.findById(parentId)
          .orElseThrow(() -> new IllegalArgumentException("Parent not found with id: " + parentId));

      // Validate parent belongs to same church
      if (!parent.getChurch().getId().equals(church.getId())) {
        throw new IllegalArgumentException("Parent does not belong to the same church");
      }

      // Prevent self-linking
      if (member.getId().equals(parentId)) {
        throw new IllegalArgumentException("Cannot link member as their own parent");
      }

      // Add parent relationship
      member.getParents().add(parent);
      log.info("Linked parent: {} -> {}", member.getId(), parent.getId());
    }

    memberRepository.save(member);
  }

  /**
   * Links children to member (many-to-many).
   */
  private void linkChildren(Member member, List<Long> childIds, Church church) {
    for (Long childId : childIds) {
      Member child = memberRepository.findById(childId)
          .orElseThrow(() -> new IllegalArgumentException("Child not found with id: " + childId));

      // Validate child belongs to same church
      if (!child.getChurch().getId().equals(church.getId())) {
        throw new IllegalArgumentException("Child does not belong to the same church");
      }

      // Prevent self-linking
      if (member.getId().equals(childId)) {
        throw new IllegalArgumentException("Cannot link member as their own child");
      }

      // Add child relationship (adds member as parent to child)
      child.getParents().add(member);
      memberRepository.save(child);
      log.info("Linked child: {} -> {}", member.getId(), child.getId());
    }
  }

  // ==================== Goal Recalculation Helper ====================

  /**
   * Recalculates all active MEMBERS type goals.
   * Called automatically when members are added, imported, or deleted.
   * This ensures goal progress stays up-to-date without manual intervention.
   */
  private void recalculateMemberGoals() {
    try {
      goalService.recalculateGoalsByType(com.reuben.pastcare_spring.enums.GoalType.MEMBERS);
    } catch (Exception e) {
      // Log but don't fail the member operation if goal recalculation fails
      log.warn("Failed to recalculate member goals: {}", e.getMessage());
    }
  }
}
