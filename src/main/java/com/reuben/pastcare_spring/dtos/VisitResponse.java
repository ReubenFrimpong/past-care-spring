package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.Visit;
import com.reuben.pastcare_spring.models.VisitType;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class VisitResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long careNeedId;
    private String careNeedTitle;
    private VisitType type;
    private LocalDate visitDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Long locationId;
    private String locationDetails;
    private List<AttendeeInfo> attendees = new ArrayList<>();
    private String purpose;
    private String notes;
    private String outcomes;
    private Boolean followUpRequired;
    private LocalDate followUpDate;
    private Boolean isCompleted;
    private Boolean isConfidential;
    private Boolean isPast;
    private Boolean isToday;
    private Boolean isUpcoming;
    private Instant createdAt;
    private Instant updatedAt;

    public static class AttendeeInfo {
        private Long id;
        private String name;

        public AttendeeInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static VisitResponse fromEntity(Visit visit) {
        VisitResponse response = new VisitResponse();
        response.setId(visit.getId());
        response.setMemberId(visit.getMember().getId());
        response.setMemberName(visit.getMember().getFirstName() + " " + visit.getMember().getLastName());
        
        if (visit.getCareNeed() != null) {
            response.setCareNeedId(visit.getCareNeed().getId());
            response.setCareNeedTitle(visit.getCareNeed().getTitle());
        }
        
        response.setType(visit.getType());
        response.setVisitDate(visit.getVisitDate());
        response.setStartTime(visit.getStartTime());
        response.setEndTime(visit.getEndTime());
        
        if (visit.getLocation() != null) {
            response.setLocationId(visit.getLocation().getId());
        }
        
        response.setLocationDetails(visit.getLocationDetails());
        
        if (visit.getAttendees() != null) {
            response.setAttendees(visit.getAttendees().stream()
                .map(user -> new AttendeeInfo(user.getId(), user.getName()))
                .collect(Collectors.toList()));
        }
        
        response.setPurpose(visit.getPurpose());
        response.setNotes(visit.getNotes());
        response.setOutcomes(visit.getOutcomes());
        response.setFollowUpRequired(visit.getFollowUpRequired());
        response.setFollowUpDate(visit.getFollowUpDate());
        response.setIsCompleted(visit.getIsCompleted());
        response.setIsConfidential(visit.getIsConfidential());
        response.setIsPast(visit.isPast());
        response.setIsToday(visit.isToday());
        response.setIsUpcoming(visit.isUpcoming());
        response.setCreatedAt(visit.getCreatedAt());
        response.setUpdatedAt(visit.getUpdatedAt());
        
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }
    
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    
    public Long getCareNeedId() { return careNeedId; }
    public void setCareNeedId(Long careNeedId) { this.careNeedId = careNeedId; }
    
    public String getCareNeedTitle() { return careNeedTitle; }
    public void setCareNeedTitle(String careNeedTitle) { this.careNeedTitle = careNeedTitle; }
    
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
    
    public List<AttendeeInfo> getAttendees() { return attendees; }
    public void setAttendees(List<AttendeeInfo> attendees) { this.attendees = attendees; }
    
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
    
    public Boolean getIsPast() { return isPast; }
    public void setIsPast(Boolean isPast) { this.isPast = isPast; }
    
    public Boolean getIsToday() { return isToday; }
    public void setIsToday(Boolean isToday) { this.isToday = isToday; }
    
    public Boolean getIsUpcoming() { return isUpcoming; }
    public void setIsUpcoming(Boolean isUpcoming) { this.isUpcoming = isUpcoming; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
