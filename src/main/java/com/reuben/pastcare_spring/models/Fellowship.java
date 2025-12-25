package com.reuben.pastcare_spring.models;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
  name = "fellowship",
  indexes = {
    @Index(name = "idx_fellowship_type", columnList = "fellowshipType"),
    @Index(name = "idx_fellowship_leader", columnList = "leader_id"),
    @Index(name = "idx_fellowship_is_active", columnList = "isActive"),
    @Index(name = "idx_fellowship_accepting_members", columnList = "acceptingMembers")
  }
)
@Data
public class Fellowship extends TenantBaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private FellowshipType fellowshipType = FellowshipType.OTHER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id")
    private User leader;

    @ManyToMany
    @JoinTable(
      name = "fellowship_coleaders",
      joinColumns = @JoinColumn(name = "fellowship_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> coleaders = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DayOfWeek meetingDay;

    @Column
    private LocalTime meetingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_location_id")
    private Location meetingLocation;

    @Column
    private Integer maxCapacity;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean acceptingMembers = true;

    @ManyToMany(mappedBy = "fellowships")
    private List<Member> members = new ArrayList<>();

    @ManyToMany(mappedBy = "fellowships")
    private List<User> users = new ArrayList<>();
}
