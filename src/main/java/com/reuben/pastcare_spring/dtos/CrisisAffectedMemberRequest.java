package com.reuben.pastcare_spring.dtos;

import jakarta.validation.constraints.NotNull;

public class CrisisAffectedMemberRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    private String notes;

    private Boolean isPrimaryContact;

    // Constructors
    public CrisisAffectedMemberRequest() {
    }

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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
