package com.reuben.pastcare_spring.models;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class Member extends BaseEntity {

  @Column(nullable = false)
  private String firstName;

  private String otherName;

  @Column(nullable = false)
  private String lastName;

  private String title;

  @Column(nullable = false)
  private String sex;

  @ManyToOne
  @JoinColumn(name = "chapel_id", nullable = false)
  Chapel chapel;

  private LocalDate dob;

  private String phoneNumber;

  private String whatsappNumber;

  private String otherPhoneNumber;

  private String areaOfResidence;

  private String gpsAddress;

  @ManyToOne
  @JoinColumn(name = "bacenta_id")
  private Bacenta bacenta;

  private String profileImageUrl;

  private String maritalStatus;

  private String spouseName;

  private String occupation;

  private LocalDate memberSince;

  private String emergencyContactName;

  private String emergencyContactNumber;

  @Column(columnDefinition = "TEXT")
  private String notes;

  private Boolean isVerified;

}
