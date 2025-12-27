package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.Crisis;
import com.reuben.pastcare_spring.models.CrisisSeverity;
import com.reuben.pastcare_spring.models.CrisisStatus;
import com.reuben.pastcare_spring.models.CrisisType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public class CrisisResponse {
    private Long id;
    private String title;
    private String description;
    private CrisisType crisisType;
    private CrisisSeverity severity;
    private CrisisStatus status;
    private Long reportedById;
    private String reportedByName;
    private LocalDateTime reportedDate;
    private LocalDateTime incidentDate;
    private String location;

    // Geographic fields for auto-detecting affected members
    private String affectedSuburb;
    private String affectedCity;
    private String affectedDistrict;
    private String affectedRegion;
    private String affectedCountryCode;

    private Integer affectedMembersCount;
    private String responseTeamNotes;
    private String resolutionNotes;
    private LocalDateTime resolvedDate;
    private Boolean followUpRequired;
    private LocalDateTime followUpDate;
    private String resourcesMobilized;
    private Boolean communicationSent;
    private Boolean emergencyContactNotified;
    private Boolean isActive;
    private Boolean isCritical;
    private Boolean isResolved;
    private List<CrisisAffectedMemberResponse> affectedMembers;
    private List<AffectedLocationResponse> affectedLocations;
    private Instant createdAt;
    private Instant updatedAt;

    public static CrisisResponse fromEntity(Crisis crisis) {
        CrisisResponse response = new CrisisResponse();
        response.setId(crisis.getId());
        response.setTitle(crisis.getTitle());
        response.setDescription(crisis.getDescription());
        response.setCrisisType(crisis.getCrisisType());
        response.setSeverity(crisis.getSeverity());
        response.setStatus(crisis.getStatus());

        if (crisis.getReportedBy() != null) {
            response.setReportedById(crisis.getReportedBy().getId());
            response.setReportedByName(crisis.getReportedBy().getName());
        }

        response.setReportedDate(crisis.getReportedDate());
        response.setIncidentDate(crisis.getIncidentDate());
        response.setLocation(crisis.getLocation());

        // Set geographic fields
        response.setAffectedSuburb(crisis.getAffectedSuburb());
        response.setAffectedCity(crisis.getAffectedCity());
        response.setAffectedDistrict(crisis.getAffectedDistrict());
        response.setAffectedRegion(crisis.getAffectedRegion());
        response.setAffectedCountryCode(crisis.getAffectedCountryCode());

        response.setAffectedMembersCount(crisis.getAffectedMembersCount());
        response.setResponseTeamNotes(crisis.getResponseTeamNotes());
        response.setResolutionNotes(crisis.getResolutionNotes());
        response.setResolvedDate(crisis.getResolvedDate());
        response.setFollowUpRequired(crisis.getFollowUpRequired());
        response.setFollowUpDate(crisis.getFollowUpDate());
        response.setResourcesMobilized(crisis.getResourcesMobilized());
        response.setCommunicationSent(crisis.getCommunicationSent());
        response.setEmergencyContactNotified(crisis.getEmergencyContactNotified());
        response.setIsActive(crisis.isActive());
        response.setIsCritical(crisis.isCritical());
        response.setIsResolved(crisis.isResolved());
        response.setCreatedAt(crisis.getCreatedAt());
        response.setUpdatedAt(crisis.getUpdatedAt());

        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public CrisisType getCrisisType() { return crisisType; }
    public void setCrisisType(CrisisType crisisType) { this.crisisType = crisisType; }

    public CrisisSeverity getSeverity() { return severity; }
    public void setSeverity(CrisisSeverity severity) { this.severity = severity; }

    public CrisisStatus getStatus() { return status; }
    public void setStatus(CrisisStatus status) { this.status = status; }

    public Long getReportedById() { return reportedById; }
    public void setReportedById(Long reportedById) { this.reportedById = reportedById; }

    public String getReportedByName() { return reportedByName; }
    public void setReportedByName(String reportedByName) { this.reportedByName = reportedByName; }

    public LocalDateTime getReportedDate() { return reportedDate; }
    public void setReportedDate(LocalDateTime reportedDate) { this.reportedDate = reportedDate; }

    public LocalDateTime getIncidentDate() { return incidentDate; }
    public void setIncidentDate(LocalDateTime incidentDate) { this.incidentDate = incidentDate; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Integer getAffectedMembersCount() { return affectedMembersCount; }
    public void setAffectedMembersCount(Integer affectedMembersCount) { this.affectedMembersCount = affectedMembersCount; }

    public String getResponseTeamNotes() { return responseTeamNotes; }
    public void setResponseTeamNotes(String responseTeamNotes) { this.responseTeamNotes = responseTeamNotes; }

    public String getResolutionNotes() { return resolutionNotes; }
    public void setResolutionNotes(String resolutionNotes) { this.resolutionNotes = resolutionNotes; }

    public LocalDateTime getResolvedDate() { return resolvedDate; }
    public void setResolvedDate(LocalDateTime resolvedDate) { this.resolvedDate = resolvedDate; }

    public Boolean getFollowUpRequired() { return followUpRequired; }
    public void setFollowUpRequired(Boolean followUpRequired) { this.followUpRequired = followUpRequired; }

    public LocalDateTime getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDateTime followUpDate) { this.followUpDate = followUpDate; }

    public String getResourcesMobilized() { return resourcesMobilized; }
    public void setResourcesMobilized(String resourcesMobilized) { this.resourcesMobilized = resourcesMobilized; }

    public Boolean getCommunicationSent() { return communicationSent; }
    public void setCommunicationSent(Boolean communicationSent) { this.communicationSent = communicationSent; }

    public Boolean getEmergencyContactNotified() { return emergencyContactNotified; }
    public void setEmergencyContactNotified(Boolean emergencyContactNotified) { this.emergencyContactNotified = emergencyContactNotified; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsCritical() { return isCritical; }
    public void setIsCritical(Boolean isCritical) { this.isCritical = isCritical; }

    public Boolean getIsResolved() { return isResolved; }
    public void setIsResolved(Boolean isResolved) { this.isResolved = isResolved; }

    public List<CrisisAffectedMemberResponse> getAffectedMembers() { return affectedMembers; }
    public void setAffectedMembers(List<CrisisAffectedMemberResponse> affectedMembers) { this.affectedMembers = affectedMembers; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getAffectedSuburb() { return affectedSuburb; }
    public void setAffectedSuburb(String affectedSuburb) { this.affectedSuburb = affectedSuburb; }

    public String getAffectedCity() { return affectedCity; }
    public void setAffectedCity(String affectedCity) { this.affectedCity = affectedCity; }

    public String getAffectedDistrict() { return affectedDistrict; }
    public void setAffectedDistrict(String affectedDistrict) { this.affectedDistrict = affectedDistrict; }

    public String getAffectedRegion() { return affectedRegion; }
    public void setAffectedRegion(String affectedRegion) { this.affectedRegion = affectedRegion; }

    public String getAffectedCountryCode() { return affectedCountryCode; }
    public void setAffectedCountryCode(String affectedCountryCode) { this.affectedCountryCode = affectedCountryCode; }

    public List<AffectedLocationResponse> getAffectedLocations() { return affectedLocations; }
    public void setAffectedLocations(List<AffectedLocationResponse> affectedLocations) { this.affectedLocations = affectedLocations; }
}
