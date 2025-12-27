package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CareNeed;
import com.reuben.pastcare_spring.models.CareNeedPriority;
import com.reuben.pastcare_spring.models.CareNeedStatus;
import com.reuben.pastcare_spring.models.CareNeedType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class CareNeedResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private CareNeedType type;
    private CareNeedPriority priority;
    private CareNeedStatus status;
    private String title;
    private String description;
    private Long assignedToId;
    private String assignedToName;
    private LocalDate dueDate;
    private LocalDateTime resolvedDate;
    private String notes;
    private Boolean followUpRequired;
    private LocalDate followUpDate;
    private Boolean isConfidential;
    private Boolean isOverdue;
    private Boolean isResolved;
    private Instant createdAt;
    private Instant updatedAt;

    public static CareNeedResponse fromEntity(CareNeed careNeed) {
        CareNeedResponse response = new CareNeedResponse();
        response.setId(careNeed.getId());
        response.setMemberId(careNeed.getMember().getId());
        response.setMemberName(careNeed.getMember().getFirstName() + " " + careNeed.getMember().getLastName());
        response.setType(careNeed.getType());
        response.setPriority(careNeed.getPriority());
        response.setStatus(careNeed.getStatus());
        response.setTitle(careNeed.getTitle());
        response.setDescription(careNeed.getDescription());
        
        if (careNeed.getAssignedTo() != null) {
            response.setAssignedToId(careNeed.getAssignedTo().getId());
            response.setAssignedToName(careNeed.getAssignedTo().getName());
        }
        
        response.setDueDate(careNeed.getDueDate());
        response.setResolvedDate(careNeed.getResolvedDate());
        response.setNotes(careNeed.getNotes());
        response.setFollowUpRequired(careNeed.getFollowUpRequired());
        response.setFollowUpDate(careNeed.getFollowUpDate());
        response.setIsConfidential(careNeed.getIsConfidential());
        response.setIsOverdue(careNeed.isOverdue());
        response.setIsResolved(careNeed.isResolved());
        response.setCreatedAt(careNeed.getCreatedAt());
        response.setUpdatedAt(careNeed.getUpdatedAt());
        
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    
    public CareNeedType getType() { return type; }
    public void setType(CareNeedType type) { this.type = type; }
    
    public CareNeedPriority getPriority() { return priority; }
    public void setPriority(CareNeedPriority priority) { this.priority = priority; }
    
    public CareNeedStatus getStatus() { return status; }
    public void setStatus(CareNeedStatus status) { this.status = status; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public Long getAssignedToId() { return assignedToId; }
    public void setAssignedToId(Long assignedToId) { this.assignedToId = assignedToId; }
    
    public String getAssignedToName() { return assignedToName; }
    public void setAssignedToName(String assignedToName) { this.assignedToName = assignedToName; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public Boolean getFollowUpRequired() { return followUpRequired; }
    public void setFollowUpRequired(Boolean followUpRequired) { this.followUpRequired = followUpRequired; }
    
    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }
    
    public Boolean getIsConfidential() { return isConfidential; }
    public void setIsConfidential(Boolean isConfidential) { this.isConfidential = isConfidential; }
    
    public Boolean getIsOverdue() { return isOverdue; }
    public void setIsOverdue(Boolean isOverdue) { this.isOverdue = isOverdue; }
    
    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
