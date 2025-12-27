package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CrisisAffectedMember;

import java.time.Instant;

public class CrisisAffectedMemberResponse {
    private Long id;
    private Long crisisId;
    private Long memberId;
    private String memberName;
    private String notes;
    private Boolean isPrimaryContact;
    private Instant createdAt;
    private Instant updatedAt;

    public static CrisisAffectedMemberResponse fromEntity(CrisisAffectedMember affectedMember) {
        CrisisAffectedMemberResponse response = new CrisisAffectedMemberResponse();
        response.setId(affectedMember.getId());
        response.setCrisisId(affectedMember.getCrisis().getId());
        response.setMemberId(affectedMember.getMember().getId());
        response.setMemberName(affectedMember.getMember().getFirstName() + " " + affectedMember.getMember().getLastName());
        response.setNotes(affectedMember.getNotes());
        response.setIsPrimaryContact(affectedMember.getIsPrimaryContact());
        response.setCreatedAt(affectedMember.getCreatedAt());
        response.setUpdatedAt(affectedMember.getUpdatedAt());
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCrisisId() { return crisisId; }
    public void setCrisisId(Long crisisId) { this.crisisId = crisisId; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public Boolean getIsPrimaryContact() { return isPrimaryContact; }
    public void setIsPrimaryContact(Boolean isPrimaryContact) { this.isPrimaryContact = isPrimaryContact; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
