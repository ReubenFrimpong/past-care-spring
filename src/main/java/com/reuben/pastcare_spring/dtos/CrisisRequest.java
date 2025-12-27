package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CrisisSeverity;
import com.reuben.pastcare_spring.models.CrisisStatus;
import com.reuben.pastcare_spring.models.CrisisType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class CrisisRequest {

    @NotNull(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Crisis type is required")
    private CrisisType crisisType;

    @NotNull(message = "Severity is required")
    private CrisisSeverity severity;

    private CrisisStatus status;

    private LocalDateTime incidentDate;

    private String location;

    // Geographic search fields for auto-detecting affected members (legacy single location)
    private String affectedSuburb;
    private String affectedCity;
    private String affectedDistrict;
    private String affectedRegion;
    private String affectedCountryCode;

    // Multiple affected locations support
    private List<AffectedLocationRequest> affectedLocations;

    private Integer affectedMembersCount;

    private String responseTeamNotes;

    private String resolutionNotes;

    private Boolean followUpRequired;

    private LocalDateTime followUpDate;

    private String resourcesMobilized;

    private Boolean communicationSent;

    private Boolean emergencyContactNotified;

    private List<Long> affectedMemberIds;

    // Constructors
    public CrisisRequest() {
    }

    // Getters and Setters
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

    public CrisisType getCrisisType() {
        return crisisType;
    }

    public void setCrisisType(CrisisType crisisType) {
        this.crisisType = crisisType;
    }

    public CrisisSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(CrisisSeverity severity) {
        this.severity = severity;
    }

    public CrisisStatus getStatus() {
        return status;
    }

    public void setStatus(CrisisStatus status) {
        this.status = status;
    }

    public LocalDateTime getIncidentDate() {
        return incidentDate;
    }

    public void setIncidentDate(LocalDateTime incidentDate) {
        this.incidentDate = incidentDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getAffectedMembersCount() {
        return affectedMembersCount;
    }

    public void setAffectedMembersCount(Integer affectedMembersCount) {
        this.affectedMembersCount = affectedMembersCount;
    }

    public String getResponseTeamNotes() {
        return responseTeamNotes;
    }

    public void setResponseTeamNotes(String responseTeamNotes) {
        this.responseTeamNotes = responseTeamNotes;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public Boolean getFollowUpRequired() {
        return followUpRequired;
    }

    public void setFollowUpRequired(Boolean followUpRequired) {
        this.followUpRequired = followUpRequired;
    }

    public LocalDateTime getFollowUpDate() {
        return followUpDate;
    }

    public void setFollowUpDate(LocalDateTime followUpDate) {
        this.followUpDate = followUpDate;
    }

    public String getResourcesMobilized() {
        return resourcesMobilized;
    }

    public void setResourcesMobilized(String resourcesMobilized) {
        this.resourcesMobilized = resourcesMobilized;
    }

    public Boolean getCommunicationSent() {
        return communicationSent;
    }

    public void setCommunicationSent(Boolean communicationSent) {
        this.communicationSent = communicationSent;
    }

    public Boolean getEmergencyContactNotified() {
        return emergencyContactNotified;
    }

    public void setEmergencyContactNotified(Boolean emergencyContactNotified) {
        this.emergencyContactNotified = emergencyContactNotified;
    }

    public List<Long> getAffectedMemberIds() {
        return affectedMemberIds;
    }

    public void setAffectedMemberIds(List<Long> affectedMemberIds) {
        this.affectedMemberIds = affectedMemberIds;
    }

    public String getAffectedSuburb() {
        return affectedSuburb;
    }

    public void setAffectedSuburb(String affectedSuburb) {
        this.affectedSuburb = affectedSuburb;
    }

    public String getAffectedCity() {
        return affectedCity;
    }

    public void setAffectedCity(String affectedCity) {
        this.affectedCity = affectedCity;
    }

    public String getAffectedDistrict() {
        return affectedDistrict;
    }

    public void setAffectedDistrict(String affectedDistrict) {
        this.affectedDistrict = affectedDistrict;
    }

    public String getAffectedRegion() {
        return affectedRegion;
    }

    public void setAffectedRegion(String affectedRegion) {
        this.affectedRegion = affectedRegion;
    }

    public String getAffectedCountryCode() {
        return affectedCountryCode;
    }

    public void setAffectedCountryCode(String affectedCountryCode) {
        this.affectedCountryCode = affectedCountryCode;
    }

    public List<AffectedLocationRequest> getAffectedLocations() {
        return affectedLocations;
    }

    public void setAffectedLocations(List<AffectedLocationRequest> affectedLocations) {
        this.affectedLocations = affectedLocations;
    }
}
