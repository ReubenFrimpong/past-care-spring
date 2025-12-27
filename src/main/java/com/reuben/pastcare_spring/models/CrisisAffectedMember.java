package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;

@Entity
@Table(name = "crisis_affected_member")
public class CrisisAffectedMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crisis_id", nullable = false)
    private Crisis crisis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 500)
    private String notes;

    @Column(name = "is_primary_contact")
    private Boolean isPrimaryContact = false;

    // Constructors
    public CrisisAffectedMember() {
    }

    public CrisisAffectedMember(Crisis crisis, Member member) {
        this.crisis = crisis;
        this.member = member;
    }

    // Getters and Setters
    public Crisis getCrisis() {
        return crisis;
    }

    public void setCrisis(Crisis crisis) {
        this.crisis = crisis;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
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
