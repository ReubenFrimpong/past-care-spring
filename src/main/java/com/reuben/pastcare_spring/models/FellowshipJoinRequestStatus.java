package com.reuben.pastcare_spring.models;

/**
 * Status of a fellowship join request.
 */
public enum FellowshipJoinRequestStatus {
  /**
   * Request is pending review
   */
  PENDING,

  /**
   * Request has been approved
   */
  APPROVED,

  /**
   * Request has been rejected
   */
  REJECTED
}
