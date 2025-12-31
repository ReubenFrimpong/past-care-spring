package com.reuben.pastcare_spring.models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.reuben.pastcare_spring.enums.ServiceType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

  @ManyToOne
  @JoinColumn(name = "event_id")
  private Event event;

  @Column(columnDefinition = "TEXT")
  private String notes;

  private Boolean isCompleted;

  // Phase 1: Enhanced Attendance Tracking fields

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ServiceType serviceType = ServiceType.SUNDAY_MAIN_SERVICE;

  @Column(unique = true, length = 500)
  private String qrCodeData;

  @Column(columnDefinition = "TEXT")
  private String qrCodeUrl;

  private LocalDateTime qrCodeExpiresAt;

  private Double geofenceLatitude;

  private Double geofenceLongitude;

  private Integer geofenceRadiusMeters = 100;

  private Boolean allowLateCheckin = true;

  private Integer lateCutoffMinutes = 30;

  private Boolean isRecurring = false;

  @Column(length = 100)
  private String recurrencePattern;

  private Integer maxCapacity;

  private LocalDateTime checkInOpensAt;

  private LocalDateTime checkInClosesAt;

  @OneToMany(mappedBy = "attendanceSession", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attendance> attendances;
}
