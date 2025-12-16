package com.reuben.pastcare_spring.models;


import java.time.LocalDateTime;
import java.util.List;

import com.reuben.pastcare_spring.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity{

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  private String phoneNumber;

  private String title;

  @ManyToOne
  @JoinColumn(name = "church_id", nullable = false)
  private Church church;

  @ManyToMany
  @JoinTable(
    name = "user_fellowships",
    joinColumns = @JoinColumn(name = "user_id"),
    inverseJoinColumns = @JoinColumn(name = "fellowship_id")
  )
  List<Fellowship> fellowships;

  @Column(nullable = false)
  private String password;

  private String primaryService;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private Role role = Role.ADMIN;

  @Column(nullable = false)
  private int failedLoginAttempts = 0;

  private LocalDateTime accountLockedUntil;

  @Column(nullable = false)
  private boolean accountLocked = false;

}
