package com.reuben.pastcare_spring.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
public class Church extends BaseEntity {
  @Column(nullable = false)
  private String name;
}
