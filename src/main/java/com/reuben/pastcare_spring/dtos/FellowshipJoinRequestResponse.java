package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.FellowshipJoinRequest;
import com.reuben.pastcare_spring.models.FellowshipJoinRequestStatus;

import java.time.LocalDateTime;

/**
 * DTO for fellowship join request responses.
 */
public record FellowshipJoinRequestResponse(
  Long id,
  Long fellowshipId,
  String fellowshipName,
  Long memberId,
  String memberName,
  String requestMessage,
  FellowshipJoinRequestStatus status,
  LocalDateTime requestedAt,
  LocalDateTime reviewedAt,
  Long reviewedById,
  String reviewedByName,
  String reviewNotes
) {

  /**
   * Convert FellowshipJoinRequest entity to FellowshipJoinRequestResponse DTO
   */
  public static FellowshipJoinRequestResponse fromEntity(FellowshipJoinRequest request) {
    String memberName = request.getMember() != null
      ? request.getMember().getFirstName() + " " + request.getMember().getLastName()
      : null;

    String fellowshipName = request.getFellowship() != null
      ? request.getFellowship().getName()
      : null;

    String reviewedByName = null;
    Long reviewedById = null;
    if (request.getReviewedBy() != null) {
      reviewedById = request.getReviewedBy().getId();
      reviewedByName = request.getReviewedBy().getName();
    }

    return new FellowshipJoinRequestResponse(
      request.getId(),
      request.getFellowship() != null ? request.getFellowship().getId() : null,
      fellowshipName,
      request.getMember() != null ? request.getMember().getId() : null,
      memberName,
      request.getRequestMessage(),
      request.getStatus(),
      request.getRequestedAt(),
      request.getReviewedAt(),
      reviewedById,
      reviewedByName,
      request.getReviewNotes()
    );
  }
}
