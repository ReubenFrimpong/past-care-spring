package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.dtos.VisitorResponse;
import com.reuben.pastcare_spring.models.Visitor;

/**
 * Mapper for converting between Visitor entity and DTOs.
 * Phase 1: Enhanced Attendance Tracking - Visitor Management
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
public class VisitorMapper {

  /**
   * Convert Visitor entity to VisitorResponse DTO.
   *
   * @param visitor The visitor entity
   * @return VisitorResponse DTO
   */
  public static VisitorResponse toVisitorResponse(Visitor visitor) {
    if (visitor == null) {
      return null;
    }

    return new VisitorResponse(
        visitor.getId(),
        visitor.getFirstName(),
        visitor.getLastName(),
        visitor.getPhoneNumber(),
        visitor.getEmail(),
        visitor.getAgeGroup(),
        visitor.getHowHeardAboutUs(),
        visitor.getInvitedByMember() != null ? visitor.getInvitedByMember().getId() : null,
        visitor.getInvitedByMember() != null
            ? visitor.getInvitedByMember().getFirstName() + " " + visitor.getInvitedByMember().getLastName()
            : null,
        visitor.getIsFirstTime(),
        visitor.getVisitCount(),
        visitor.getLastVisitDate(),
        visitor.getAssignedToUser() != null ? visitor.getAssignedToUser().getId() : null,
        visitor.getAssignedToUser() != null ? visitor.getAssignedToUser().getName() : null,
        visitor.getFollowUpStatus(),
        visitor.getConvertedToMember(),
        visitor.getConvertedMember() != null ? visitor.getConvertedMember().getId() : null,
        visitor.getConversionDate(),
        visitor.getNotes(),
        visitor.getCreatedAt(),
        visitor.getUpdatedAt()
    );
  }
}
