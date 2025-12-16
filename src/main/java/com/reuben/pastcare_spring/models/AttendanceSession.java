package com.reuben.pastcare_spring.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
public class AttendanceSession extends TenantBaseEntity {

  @Column(nullable = false)
  private String sessionName;

  @Column(nullable = false)
  private LocalDate sessionDate;

  private LocalTime sessionTime;

  @ManyToOne
  @JoinColumn(name = "fellowship_id")
  private Fellowship fellowship;

  @Column(columnDefinition = "TEXT")
  private String notes;

  private Boolean isCompleted;

  @OneToMany(mappedBy = "attendanceSession", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attendance> attendances;
}
