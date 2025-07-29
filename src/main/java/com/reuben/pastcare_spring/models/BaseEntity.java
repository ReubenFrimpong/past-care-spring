package com.reuben.pastcare_spring.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;

@Data
@MappedSuperclass
public class BaseEntity {

  @Id
  @GeneratedValue
  private Integer id;

  @Column(updatable = false)
  private LocalDateTime createdAt;

  @Column(insertable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
      createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
      updatedAt = LocalDateTime.now();
  }
}
