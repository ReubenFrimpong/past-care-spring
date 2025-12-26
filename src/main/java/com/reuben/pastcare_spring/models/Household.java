package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a household or family unit within the church.
 * A household groups members who live together or are closely related.
 */
@Entity
@Table(
    name = "households",
    indexes = {
        @Index(name = "idx_household_church_id", columnList = "church_id"),
        @Index(name = "idx_household_name", columnList = "household_name")
    }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class Household extends TenantBaseEntity {

    /**
     * Name of the household (e.g., "The Doe Family", "Smith Household")
     */
    @Column(nullable = false, length = 200)
    private String householdName;

    /**
     * Primary contact person for the household (usually the head of household)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_head_id")
    private Member householdHead;

    /**
     * Shared address/location for the household
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location sharedLocation;

    /**
     * All members belonging to this household
     * Mapped by the household field in Member entity
     * Note: We don't cascade deletes - members should remain even if household is deleted
     */
    @OneToMany(mappedBy = "household", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY, orphanRemoval = false)
    private List<Member> members = new ArrayList<>();

    /**
     * Additional notes about the household
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Date when the household was established/formed
     */
    private LocalDate establishedDate;

    /**
     * URL to household/family photo
     */
    @Column(length = 500)
    private String householdImageUrl;

    /**
     * Shared family email address (optional)
     */
    @Column(length = 200)
    private String householdEmail;

    /**
     * Shared family phone number (optional)
     */
    @Column(length = 50)
    private String householdPhone;

    /**
     * Gets the count of members in this household
     */
    @Transient
    public int getMemberCount() {
        return members != null ? members.size() : 0;
    }

    /**
     * Adds a member to this household
     */
    public void addMember(Member member) {
        if (members == null) {
            members = new ArrayList<>();
        }
        if (!members.contains(member)) {
            members.add(member);
            member.setHousehold(this);
        }
    }

    /**
     * Removes a member from this household
     */
    public void removeMember(Member member) {
        if (members != null && members.contains(member)) {
            members.remove(member);
            member.setHousehold(null);
        }
    }
}
