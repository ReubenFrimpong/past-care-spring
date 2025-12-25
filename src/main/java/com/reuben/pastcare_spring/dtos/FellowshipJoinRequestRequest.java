package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating a fellowship join request.
 */
public record FellowshipJoinRequestRequest(
  @NotNull(message = "Fellowship ID is required")
  Long fellowshipId,

  @NotNull(message = "Member ID is required")
  Long memberId,

  String requestMessage
) {
}
