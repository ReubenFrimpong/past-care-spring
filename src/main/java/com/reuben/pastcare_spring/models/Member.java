package com.reuben.pastcare_spring.models;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true, exclude = {"spouse", "parents", "children", "household", "fellowships"})
@ToString(exclude = {"spouse", "parents", "children", "household", "fellowships"})
@Entity
@Data
public class Member extends TenantBaseEntity {

  @Column(nullable = false)
  private String firstName;

  private String otherName;

  @Column(nullable = false)
  private String lastName;

  private String title;

  @Column(nullable = false)
  private String sex;

  @ManyToMany
  @JoinTable(
    name = "member_fellowships",
    joinColumns = @JoinColumn(name = "member_id"),
    inverseJoinColumns = @JoinColumn(name = "fellowship_id")
  )
  List<Fellowship> fellowships;

  private LocalDate dob;

  @Column(length = 10)
  private String countryCode; // ISO country code (e.g., "GH", "US", "NG")

  @Column(length = 50)
  private String timezone; // IANA timezone (e.g., "Africa/Accra", "America/New_York")

  @Column(unique = true, nullable = false)
  private String phoneNumber;

  @Column(length = 100)
  private String email;

  private String whatsappNumber;

  private String otherPhoneNumber;

  @ManyToOne
  @JoinColumn(name = "location_id")
  private Location location;

  private String profileImageUrl;

  private String maritalStatus;

  private String occupation;

  private YearMonth memberSince;

  private String emergencyContactName;

  private String emergencyContactNumber;

  @Column(columnDefinition = "TEXT")
  private String notes;

  private Boolean isVerified;

  // Phase 2 fields: Quick Operations & Bulk Management

  /**
   * Member status in their journey with the church.
   * VISITOR -> FIRST_TIMER -> REGULAR -> MEMBER -> LEADER
   */
  @Enumerated(EnumType.STRING)
  @Column(length = 30)
  private MemberStatus status = MemberStatus.MEMBER;

  /**
   * Profile completeness percentage (0-100).
   * Calculated based on filled vs total fields.
   * Quick add members start at ~25%.
   */
  @Column(name = "profile_completeness")
  private Integer profileCompleteness = 0;

  /**
   * Custom tags for categorizing members.
   * Examples: "visitor", "first-timer", "youth", "choir", "event:concert-2024"
   */
  @ElementCollection
  @CollectionTable(name = "member_tags", joinColumns = @JoinColumn(name = "member_id"))
  @Column(name = "tag")
  private Set<String> tags = new HashSet<>();

  // Phase 3 fields: Family & Household Management

  /**
   * The household/family this member belongs to.
   * Multiple members can belong to the same household.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "household_id")
  private Household household;

  /**
   * The spouse of this member (bidirectional relationship).
   * When spouse A links to spouse B, B automatically links back to A.
   * Both members should be in the same household.
   */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "spouse_id")
  private Member spouse;

  // Phase 3.3: Parent-Child Relationships

  /**
   * The parent members of this member (for children).
   * A child can have multiple parents (mother, father, guardian).
   * Uses a join table to support multiple parents per child.
   */
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
    name = "member_parents",
    joinColumns = @JoinColumn(name = "child_id"),
    inverseJoinColumns = @JoinColumn(name = "parent_id")
  )
  private Set<Member> parents = new HashSet<>();

  /**
   * The children of this member (for parents).
   * Inverse side of the parents relationship.
   * Automatically managed when parents are set.
   */
  @ManyToMany(mappedBy = "parents", fetch = FetchType.LAZY)
  private Set<Member> children = new HashSet<>();

}
