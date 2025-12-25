package com.reuben.pastcare_spring.repositories;

import com.reuben.pastcare_spring.models.FellowshipJoinRequest;
import com.reuben.pastcare_spring.models.FellowshipJoinRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FellowshipJoinRequestRepository extends JpaRepository<FellowshipJoinRequest, Long> {

  /**
   * Find all join requests for a specific fellowship
   */
  List<FellowshipJoinRequest> findByFellowshipId(Long fellowshipId);

  /**
   * Find all join requests by a specific member
   */
  List<FellowshipJoinRequest> findByMemberId(Long memberId);

  /**
   * Find all join requests with a specific status
   */
  List<FellowshipJoinRequest> findByStatus(FellowshipJoinRequestStatus status);

  /**
   * Find all pending join requests for a fellowship
   */
  List<FellowshipJoinRequest> findByFellowshipIdAndStatus(Long fellowshipId, FellowshipJoinRequestStatus status);

  /**
   * Find a specific pending request for a member and fellowship
   */
  Optional<FellowshipJoinRequest> findByFellowshipIdAndMemberIdAndStatus(
    Long fellowshipId,
    Long memberId,
    FellowshipJoinRequestStatus status
  );

  /**
   * Check if a member has a pending request for a fellowship
   */
  boolean existsByFellowshipIdAndMemberIdAndStatus(
    Long fellowshipId,
    Long memberId,
    FellowshipJoinRequestStatus status
  );
}
