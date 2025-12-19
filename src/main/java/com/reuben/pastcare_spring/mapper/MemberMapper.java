package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.dtos.MemberResponse;
import com.reuben.pastcare_spring.models.Member;

public class MemberMapper {


  public static MemberResponse toMemberResponse(Member member){
    return new MemberResponse(
      member.getId(),
      member.getFirstName(),
      member.getOtherName(),
      member.getLastName(),
      member.getTitle(),
      member.getSex(),
      member.getChurch(),
      member.getFellowships(),
      member.getDob(),
      member.getPhoneNumber(),
      member.getWhatsappNumber(),
      member.getOtherPhoneNumber(),
      LocationMapper.toLocationResponse(member.getLocation()),
      member.getProfileImageUrl(),
      member.getMaritalStatus(),
      member.getSpouseName(),
      member.getOccupation(),
      member.getMemberSince(),
      member.getEmergencyContactName(),
      member.getEmergencyContactNumber(),
      member.getNotes(),
      member.getIsVerified()
    );
  }

}
