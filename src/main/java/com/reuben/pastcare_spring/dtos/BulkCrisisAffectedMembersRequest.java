package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * DTO for bulk adding affected members to a crisis
 * Use case: Church-wide crises like COVID-19, natural disasters, etc.
 */
public class BulkCrisisAffectedMembersRequest {

    @NotEmpty(message = "At least one member ID is required")
    private List<Long> memberIds;

    private String notes;

    private Boolean isPrimaryContact;

    // Constructors
    public BulkCrisisAffectedMembersRequest() {
    }

    public BulkCrisisAffectedMembersRequest(List<Long> memberIds, String notes, Boolean isPrimaryContact) {
        this.memberIds = memberIds;
        this.notes = notes;
        this.isPrimaryContact = isPrimaryContact;
    }

    // Getters and Setters
    public List<Long> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<Long> memberIds) {
        this.memberIds = memberIds;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsPrimaryContact() {
        return isPrimaryContact;
    }

    public void setIsPrimaryContact(Boolean isPrimaryContact) {
        this.isPrimaryContact = isPrimaryContact;
    }
}
