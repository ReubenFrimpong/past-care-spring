package com.reuben.pastcare_spring.models;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
public class Chapel extends BaseEntity {

  @Column(nullable = false)
  private String name;

  @OneToMany(mappedBy = "chapel")
  private List<User> users;

}
