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
public class Bacenta extends BaseEntity {

  @Column(nullable = false)
  String name;

  @OneToMany(mappedBy = "bacenta")
  List <Member> members;
}
