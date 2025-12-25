package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Represents a member's request to join a fellowship.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
  name = "fellowship_join_requests",
  indexes = {
    @Index(name = "idx_join_request_fellowship", columnList = "fellowship_id"),
    @Index(name = "idx_join_request_member", columnList = "member_id"),
    @Index(name = "idx_join_request_status", columnList = "status"),
    @Index(name = "idx_join_request_requested_at", columnList = "requestedAt")
  },
  uniqueConstraints = {
    @UniqueConstraint(
      name = "idx_unique_pending_join_request",
      columnNames = {"fellowship_id", "member_id", "status"}
    )
  }
)
@Data
public class FellowshipJoinRequest extends TenantBaseEntity {

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fellowship_id", nullable = false)
  private Fellowship fellowship;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id", nullable = false)
  private Member member;

  @Column(columnDefinition = "TEXT")
  private String requestMessage;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private FellowshipJoinRequestStatus status = FellowshipJoinRequestStatus.PENDING;

  @Column(nullable = false)
  private LocalDateTime requestedAt = LocalDateTime.now();

  @Column
  private LocalDateTime reviewedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reviewed_by")
  private User reviewedBy;

  @Column(columnDefinition = "TEXT")
  private String reviewNotes;
}
