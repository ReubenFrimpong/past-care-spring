package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.dtos.MemberDto;
import com.reuben.pastcare_spring.models.Member;

public class MemberMapper {


  public static MemberDto toDto(Member member){
    return new MemberDto(
      member.getId(),
      member.getFirstName(),
      member.getOtherName(),
      member.getLastName(),
      member.getTitle(),
      member.getSex(),
      member.getChapel() != null ? member.getChapel().getId() : null,
      member.getDob(),
      member.getPhoneNumber(),
      member.getWhatsappNumber(),
      member.getOtherPhoneNumber(),
      member.getAreaOfResidence(),
      member.getGpsAddress(),
      member.getBacenta() != null ? member.getBacenta().getId() : null,
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
