package com.reuben.pastcare_spring.models;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
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

  @Column(unique = true, nullable = false)
  private String phoneNumber;

  private String whatsappNumber;

  private String otherPhoneNumber;

  @ManyToOne
  @JoinColumn(name = "location_id")
  private Location location;

  private String profileImageUrl;

  private String maritalStatus;

  private String spouseName;

  private String occupation;

  private YearMonth memberSince;

  private String emergencyContactName;

  private String emergencyContactNumber;

  @Column(columnDefinition = "TEXT")
  private String notes;

  private Boolean isVerified;

}
