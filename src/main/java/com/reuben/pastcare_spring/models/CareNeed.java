package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "care_needs")
public class CareNeed extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private CareNeedType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CareNeedPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CareNeedStatus status = CareNeedStatus.OPEN;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "follow_up_required")
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_date")
    private LocalDate followUpDate;

    @Column(name = "is_confidential")
    private Boolean isConfidential = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    // Constructors
    public CareNeed() {
    }

    public CareNeed(Member member, CareNeedType type, CareNeedPriority priority, String title) {
        this.member = member;
        this.type = type;
        this.priority = priority;
        this.title = title;
    }

    // Getters and Setters
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public CareNeedType getType() {
        return type;
    }

    public void setType(CareNeedType type) {
        this.type = type;
    }

    public CareNeedPriority getPriority() {
        return priority;
    }

    public void setPriority(CareNeedPriority priority) {
        this.priority = priority;
    }

    public CareNeedStatus getStatus() {
        return status;
    }

    public void setStatus(CareNeedStatus status) {
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public User getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(User assignedTo) {
        this.assignedTo = assignedTo;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getFollowUpRequired() {
        return followUpRequired;
    }

    public void setFollowUpRequired(Boolean followUpRequired) {
        this.followUpRequired = followUpRequired;
    }

    public LocalDate getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDate followUpDate) {
        this.followUpDate = followUpDate;
    }

    public Boolean getIsConfidential() {
        return isConfidential;
    }

    public void setIsConfidential(Boolean isConfidential) {
        this.isConfidential = isConfidential;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    // Helper methods
    public boolean isOverdue() {
        return dueDate != null && 
               LocalDate.now().isAfter(dueDate) && 
               (status == CareNeedStatus.OPEN || status == CareNeedStatus.IN_PROGRESS);
    }

    public boolean isResolved() {
        return status == CareNeedStatus.RESOLVED || status == CareNeedStatus.CLOSED;
    }
}
