package com.reuben.pastcare_spring.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.reuben.pastcare_spring.dtos.MemberDto;
import com.reuben.pastcare_spring.mapper.MemberMapper;
import com.reuben.pastcare_spring.models.Member;
import com.reuben.pastcare_spring.requests.MemberRequest;
import com.reuben.pastcare_spring.respositories.BacentaRepository;
import com.reuben.pastcare_spring.respositories.ChapelRepository;
import com.reuben.pastcare_spring.respositories.MemberRepository;

@Service
public class MemberService {

  private MemberRepository memberRepository;
  private ChapelRepository chapelRepository;
  private BacentaRepository bacentaRepository;
  
  public MemberService(MemberRepository memberRepository, ChapelRepository chapelRepository, BacentaRepository bacentaRepository) {
    this.memberRepository = memberRepository;
    this.chapelRepository = chapelRepository;
    this.bacentaRepository = bacentaRepository;
  }

  public List<MemberDto> getAllMembers(){
    var members = memberRepository.findAll().stream().map(MemberMapper::toDto).toList();
    return members;
  }

  public MemberDto createMember(MemberRequest memberRequest){
    var member = new Member();
    member.setFirstName(memberRequest.firstName());
    member.setOtherName(memberRequest.otherName());
    member.setLastName(memberRequest.otherName());
    member.setTitle(memberRequest.title());
    member.setSex(memberRequest.sex());

    var chapel = chapelRepository.findById(memberRequest.chapelId()).orElseThrow(() -> new IllegalArgumentException("Invalid Chapel"));
    member.setChapel(chapel);

    member.setDob(memberRequest.dob());
    member.setPhoneNumber(memberRequest.phoneNumber());
    member.setWhatsappNumber(memberRequest.whatsappNumber());
    member.setOtherPhoneNumber(memberRequest.otherPhoneNumber());
    member.setAreaOfResidence(memberRequest.areaOfResidence());
    member.setGpsAddress(memberRequest.gpsAddress());

    var bacenta = bacentaRepository.findById(memberRequest.bacentaId()).orElseThrow(() -> new IllegalArgumentException("Invalid Bacenta"));
    member.setBacenta(bacenta);

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
    //create bacenta
    return MemberMapper.toDto(createdMember);
  }

  public MemberDto updateMember(Integer id, MemberRequest memberRequest){
    var member = memberRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Member not found"));
    member.setFirstName(memberRequest.firstName());
    member.setOtherName(memberRequest.otherName());
    member.setLastName(memberRequest.otherName());
    member.setTitle(memberRequest.title());
    member.setSex(memberRequest.sex());

    var chapel = chapelRepository.findById(memberRequest.chapelId()).orElseThrow(() -> new IllegalArgumentException("Invalid Chapel"));
    member.setChapel(chapel);

    member.setDob(memberRequest.dob());
    member.setPhoneNumber(memberRequest.phoneNumber());
    member.setWhatsappNumber(memberRequest.whatsappNumber());
    member.setOtherPhoneNumber(memberRequest.otherPhoneNumber());
    member.setAreaOfResidence(memberRequest.areaOfResidence());
    member.setGpsAddress(memberRequest.gpsAddress());

    var bacenta = bacentaRepository.findById(memberRequest.bacentaId()).orElseThrow(() -> new IllegalArgumentException("Invalid Bacenta"));
    member.setBacenta(bacenta);

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
    return MemberMapper.toDto(member);
  }

  public void deleteMember(Integer id) {
    var member = memberRepository.findById(id).orElseThrow( ()-> new IllegalArgumentException("Member not found"));
    memberRepository.delete(member);
  }
}
