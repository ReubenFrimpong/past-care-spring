package com.reuben.pastcare_spring.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.dtos.MemberRequest;
import com.reuben.pastcare_spring.mapper.MemberMapper;
import com.reuben.pastcare_spring.models.Church;
import com.reuben.pastcare_spring.models.Fellowship;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.repositories.ChurchRepository;
import com.reuben.pastcare_spring.repositories.FellowshipRepository;
import com.reuben.pastcare_spring.repositories.MemberRepository;

@Service
public class MemberService {

  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private FellowshipRepository fellowshipRepository;

  @Autowired
  private ChurchRepository churchRepository;
  

  public List<MemberResponse> getAllMembers(){
    var members = memberRepository.findAll().stream().map(MemberMapper::toMemberResponse).toList();
    return members;
  }

  public MemberResponse createMember(MemberRequest memberRequest){
    var member = new Member();
    member.setFirstName(memberRequest.firstName());
    member.setOtherName(memberRequest.otherName());
    member.setLastName(memberRequest.otherName());
    member.setTitle(memberRequest.title());
    member.setSex(memberRequest.sex());

    Church church = churchRepository.findById(memberRequest.churchId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID provided"));
    member.setChurch(church);

    List<Fellowship> fellowships = fellowshipRepository.findAllById(memberRequest.fellowshipIds());

    if (fellowships.isEmpty()) {
        throw new IllegalArgumentException("Invalid fellowship IDs provided");
    }
    member.setFellowships(fellowships);
    member.setDob(memberRequest.dob());
    member.setPhoneNumber(memberRequest.phoneNumber());
    member.setWhatsappNumber(memberRequest.whatsappNumber());
    member.setOtherPhoneNumber(memberRequest.otherPhoneNumber());
    member.setAreaOfResidence(memberRequest.areaOfResidence());
    member.setAddress(memberRequest.gpsAddress());

    member.setProfileImageUrl(memberRequest.profileImageUrl());
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
    member.setLastName(memberRequest.otherName());
    member.setTitle(memberRequest.title());
    member.setSex(memberRequest.sex());

    Church church = churchRepository.findById(memberRequest.churchId())
        .orElseThrow(() -> new IllegalArgumentException("Invalid church ID provided"));
    member.setChurch(church);

    List<Fellowship> fellowships = fellowshipRepository.findAllById(memberRequest.fellowshipIds());

    if (fellowships.isEmpty()) {
        throw new IllegalArgumentException("Invalid fellowship IDs provided");
    }

    member.setDob(memberRequest.dob());
    member.setPhoneNumber(memberRequest.phoneNumber());
    member.setWhatsappNumber(memberRequest.whatsappNumber());
    member.setOtherPhoneNumber(memberRequest.otherPhoneNumber());
    member.setAreaOfResidence(memberRequest.areaOfResidence());
    member.setAddress(memberRequest.gpsAddress());

    member.setProfileImageUrl(memberRequest.profileImageUrl());
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

  public void deleteMember(Long id) {
    var member = memberRepository.findById(id).orElseThrow( ()-> new IllegalArgumentException("Member not found"));
    memberRepository.delete(member);
  }
}
