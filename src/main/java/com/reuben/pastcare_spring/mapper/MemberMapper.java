package com.reuben.pastcare_spring.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;

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
      member.getChurch() != null ? member.getChurch().getId() : null,
      member.getChurch() != null ? member.getChurch().getName() : null,
      member.getFellowships() != null ? member.getFellowships().stream()
        .map(fellowship -> new MemberResponse.FellowshipSummary(
          fellowship.getId(),
          fellowship.getName()
        ))
        .collect(Collectors.toList()) : null,
      member.getDob(),
      member.getCountryCode(),
      member.getTimezone(),
      member.getPhoneNumber(),
      member.getEmail(),
      member.getWhatsappNumber(),
      member.getOtherPhoneNumber(),
      LocationMapper.toLocationResponse(member.getLocation()),
      member.getProfileImageUrl(),
      member.getMaritalStatus(),
      getSpouseIdSafely(member),
      member.getOccupation(),
      member.getMemberSince(),
      member.getEmergencyContactName(),
      member.getEmergencyContactNumber(),
      member.getNotes(),
      member.getIsVerified(),
      member.getStatus(),
      member.getProfileCompleteness(),
      member.getTags(),
      getParentsSafely(member),
      getChildrenSafely(member)
    );
  }

  /**
   * Safely get spouse ID, handling lazy loading.
   * Returns null if spouse is not initialized or null.
   */
  private static Long getSpouseIdSafely(Member member) {
    try {
      Member spouse = member.getSpouse();
      if (spouse != null && Hibernate.isInitialized(spouse)) {
        return spouse.getId();
      }
      // If not initialized, try to access - will work if within transaction
      if (spouse != null) {
        return spouse.getId();
      }
    } catch (Exception e) {
      // LazyInitializationException - return null
    }
    return null;
  }

  /**
   * Safely get parents list, handling lazy loading.
   * Returns empty list if parents collection is not initialized.
   */
  private static List<MemberResponse.ParentInfo> getParentsSafely(Member member) {
    try {
      if (member.getParents() != null && Hibernate.isInitialized(member.getParents())) {
        return member.getParents().stream()
          .map(parent -> new MemberResponse.ParentInfo(
            parent.getId(),
            parent.getFirstName() + " " + parent.getLastName()
          ))
          .collect(Collectors.toList());
      }
    } catch (Exception e) {
      // LazyInitializationException - return empty list
    }
    return Collections.emptyList();
  }

  /**
   * Safely get children list, handling lazy loading.
   * Returns empty list if children collection is not initialized.
   */
  private static List<MemberResponse.ChildInfo> getChildrenSafely(Member member) {
    try {
      if (member.getChildren() != null && Hibernate.isInitialized(member.getChildren())) {
        return member.getChildren().stream()
          .map(child -> new MemberResponse.ChildInfo(
            child.getId(),
            child.getFirstName() + " " + child.getLastName()
          ))
          .collect(Collectors.toList());
      }
    } catch (Exception e) {
      // LazyInitializationException - return empty list
    }
    return Collections.emptyList();
  }

}
