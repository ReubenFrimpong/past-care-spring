package com.reuben.pastcare_spring.services;

import java.io.IOException;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.dtos.MemberRequest;
import com.reuben.pastcare_spring.dtos.MemberStatsResponse;
import com.reuben.pastcare_spring.dtos.ProfileImageUploadResponse;
import com.reuben.pastcare_spring.mapper.MemberMapper;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.Location;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.FellowshipRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;
import com.reuben.pastcare_spring.exceptions.FileUploadException;


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

    Member createdMember = memberRepository.save(member);
    return MemberMapper.toMemberResponse(createdMember);
  }

  public MemberResponse updateMember(Long id, MemberRequest memberRequest){
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
}
