package com.reuben.pastcare_spring.models;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
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
  @JoinColumn(name = "chapel_id", nullable = false)
  private Chapel chapel;

  @Column(nullable = false)
  private String password;

  private String primaryService;

  private String designation;

}
