package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "crisis")
public class Crisis extends TenantBaseEntity {

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "crisis_type", nullable = false, length = 50)
    private CrisisType crisisType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CrisisSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CrisisStatus status = CrisisStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by_user_id", nullable = false)
    private User reportedBy;

    @Column(name = "reported_date", nullable = false)
    private LocalDateTime reportedDate;

    @Column(name = "incident_date")
    private LocalDateTime incidentDate;

    @Column(length = 200)
    private String location;

    // Geographic search fields for auto-detecting affected members
    @Column(name = "affected_suburb", length = 100)
    private String affectedSuburb;

    @Column(name = "affected_city", length = 100)
    private String affectedCity;

    @Column(name = "affected_district", length = 100)
    private String affectedDistrict;

    @Column(name = "affected_region", length = 100)
    private String affectedRegion;

    @Column(name = "affected_country_code", length = 2)
    private String affectedCountryCode;

    @Column(name = "affected_members_count")
    private Integer affectedMembersCount;

    @Column(name = "response_team_notes", columnDefinition = "TEXT")
    private String responseTeamNotes;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "resolved_date")
    private LocalDateTime resolvedDate;

    @Column(name = "follow_up_required")
    private Boolean followUpRequired = false;

    @Column(name = "follow_up_date")
    private LocalDateTime followUpDate;

    @Column(name = "resources_mobilized", length = 500)
    private String resourcesMobilized;

    @Column(name = "communication_sent")
    private Boolean communicationSent = false;

    @Column(name = "emergency_contact_notified")
    private Boolean emergencyContactNotified = false;

    // Relationship to affected locations (one crisis can affect multiple locations)
    @OneToMany(mappedBy = "crisis", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CrisisAffectedLocation> affectedLocationsList = new ArrayList<>();

    // Constructors
    public Crisis() {
    }

    public Crisis(String title, CrisisType crisisType, CrisisSeverity severity, User reportedBy) {
        this.title = title;
        this.crisisType = crisisType;
        this.severity = severity;
        this.reportedBy = reportedBy;
        this.reportedDate = LocalDateTime.now();
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

    public User getReportedBy() {
        return reportedBy;
    }

    public void setReportedBy(User reportedBy) {
        this.reportedBy = reportedBy;
    }

    public LocalDateTime getReportedDate() {
        return reportedDate;
    }

    public void setReportedDate(LocalDateTime reportedDate) {
        this.reportedDate = reportedDate;
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

    public LocalDateTime getResolvedDate() {
        return resolvedDate;
    }

    public void setResolvedDate(LocalDateTime resolvedDate) {
        this.resolvedDate = resolvedDate;
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

    public List<CrisisAffectedLocation> getAffectedLocationsList() {
        return affectedLocationsList;
    }

    public void setAffectedLocationsList(List<CrisisAffectedLocation> affectedLocationsList) {
        this.affectedLocationsList = affectedLocationsList;
    }

    // Helper methods for managing affected locations
    public void addAffectedLocation(CrisisAffectedLocation location) {
        affectedLocationsList.add(location);
        location.setCrisis(this);
    }

    public void removeAffectedLocation(CrisisAffectedLocation location) {
        affectedLocationsList.remove(location);
        location.setCrisis(null);
    }

    public void clearAffectedLocations() {
        affectedLocationsList.forEach(location -> location.setCrisis(null));
        affectedLocationsList.clear();
    }

    // Helper methods
    public boolean isActive() {
        return status == CrisisStatus.ACTIVE || status == CrisisStatus.IN_RESPONSE;
    }

    public boolean isCritical() {
        return severity == CrisisSeverity.CRITICAL;
    }

    public boolean isResolved() {
        return status == CrisisStatus.RESOLVED || status == CrisisStatus.CLOSED;
    }
}
