package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CareNeedPriority;
import com.reuben.pastcare_spring.models.CareNeedStatus;
import com.reuben.pastcare_spring.models.CareNeedType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CareNeedRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Type is required")
    private CareNeedType type;

    @NotNull(message = "Priority is required")
    private CareNeedPriority priority;

    private CareNeedStatus status;

    @NotNull(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    private String title;

    private String description;

    private Long assignedToId;

    private LocalDate dueDate;

    private String notes;

    private Boolean followUpRequired;

    private LocalDate followUpDate;

    private Boolean isConfidential;

    // Constructors
    public CareNeedRequest() {
    }

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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

    public Long getAssignedToId() {
        return assignedToId;
    }

    public void setAssignedToId(Long assignedToId) {
        this.assignedToId = assignedToId;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
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
}
