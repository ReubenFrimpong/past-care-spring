package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.VisitType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class VisitRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    private Long careNeedId;

    @NotNull(message = "Visit type is required")
    private VisitType type;

    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;

    private LocalTime startTime;

    private LocalTime endTime;

    private Long locationId;

    private String locationDetails;

    private List<Long> attendeeIds;

    private String purpose;

    private String notes;

    private String outcomes;

    private Boolean followUpRequired;

    private LocalDate followUpDate;

    private Boolean isCompleted;

    private Boolean isConfidential;

    // Getters and Setters
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    
    public Long getCareNeedId() { return careNeedId; }
    public void setCareNeedId(Long careNeedId) { this.careNeedId = careNeedId; }
    
    public VisitType getType() { return type; }
    public void setType(VisitType type) { this.type = type; }
    
    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
    
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    
    public String getLocationDetails() { return locationDetails; }
    public void setLocationDetails(String locationDetails) { this.locationDetails = locationDetails; }
    
    public List<Long> getAttendeeIds() { return attendeeIds; }
    public void setAttendeeIds(List<Long> attendeeIds) { this.attendeeIds = attendeeIds; }
    
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public String getOutcomes() { return outcomes; }
    public void setOutcomes(String outcomes) { this.outcomes = outcomes; }
    
    public Boolean getFollowUpRequired() { return followUpRequired; }
    public void setFollowUpRequired(Boolean followUpRequired) { this.followUpRequired = followUpRequired; }
    
    public LocalDate getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDate followUpDate) { this.followUpDate = followUpDate; }
    
    public Boolean getIsCompleted() { return isCompleted; }
    public void setIsCompleted(Boolean isCompleted) { this.isCompleted = isCompleted; }
    
    public Boolean getIsConfidential() { return isConfidential; }
    public void setIsConfidential(Boolean isConfidential) { this.isConfidential = isConfidential; }
}
