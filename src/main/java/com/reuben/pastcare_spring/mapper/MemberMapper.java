package com.reuben.pastcare_spring.mapper;

import java.util.stream.Collectors;

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
      member.getCountryCode(),
      member.getTimezone(),
      member.getPhoneNumber(),
      member.getWhatsappNumber(),
      member.getOtherPhoneNumber(),
      LocationMapper.toLocationResponse(member.getLocation()),
      member.getProfileImageUrl(),
      member.getMaritalStatus(),
      member.getSpouse() != null ? member.getSpouse().getId() : null,  // spouseId
      member.getOccupation(),
      member.getMemberSince(),
      member.getEmergencyContactName(),
      member.getEmergencyContactNumber(),
      member.getNotes(),
      member.getIsVerified(),
      member.getStatus(),
      member.getProfileCompleteness(),
      member.getTags(),
      // Map parents
      member.getParents() != null ? member.getParents().stream()
        .map(parent -> new MemberResponse.ParentInfo(
          parent.getId(),
          parent.getFirstName() + " " + parent.getLastName()
        ))
        .collect(Collectors.toList()) : null,
      // Map children
      member.getChildren() != null ? member.getChildren().stream()
        .map(child -> new MemberResponse.ChildInfo(
          child.getId(),
          child.getFirstName() + " " + child.getLastName()
        ))
        .collect(Collectors.toList()) : null
    );
  }

}
