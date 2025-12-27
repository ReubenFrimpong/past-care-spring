package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CounselingStatus;
import com.reuben.pastcare_spring.models.CounselingType;
import com.reuben.pastcare_spring.models.SessionOutcome;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CounselingSessionRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Counselor ID is required")
    private Long counselorId;

    private Long careNeedId;

    @NotNull(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    private String sessionNotes;

    @NotNull(message = "Type is required")
    private CounselingType type;

    @NotNull(message = "Session date is required")
    private LocalDateTime sessionDate;

    private Integer durationMinutes;

    @Size(max = 200, message = "Location must not exceed 200 characters")
    private String location;

    private CounselingStatus status;

    // Referral fields
    private Boolean isReferralNeeded;

    @Size(max = 200, message = "Referred to must not exceed 200 characters")
    private String referredTo;

    @Size(max = 100, message = "Referral organization must not exceed 100 characters")
    private String referralOrganization;

    @Size(max = 20, message = "Referral phone must not exceed 20 characters")
    private String referralPhone;

    private String referralNotes;

    private LocalDateTime referralDate;

    // Follow-up
    private Boolean followUpRequired;

    private LocalDateTime followUpDate;

    private String followUpNotes;

    // Confidentiality
    private Boolean isConfidential;

    // Outcome
    private String outcome;

    private SessionOutcome sessionOutcome;

    // Constructors
    public CounselingSessionRequest() {
    }

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getCounselorId() {
        return counselorId;
    }

    public void setCounselorId(Long counselorId) {
        this.counselorId = counselorId;
    }

    public Long getCareNeedId() {
        return careNeedId;
    }

    public void setCareNeedId(Long careNeedId) {
        this.careNeedId = careNeedId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSessionNotes() {
        return sessionNotes;
    }

    public void setSessionNotes(String sessionNotes) {
        this.sessionNotes = sessionNotes;
    }

    public CounselingType getType() {
        return type;
    }

    public void setType(CounselingType type) {
        this.type = type;
    }

    public LocalDateTime getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDateTime sessionDate) {
        this.sessionDate = sessionDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public CounselingStatus getStatus() {
        return status;
    }

    public void setStatus(CounselingStatus status) {
        this.status = status;
    }

    public Boolean getIsReferralNeeded() {
        return isReferralNeeded;
    }

    public void setIsReferralNeeded(Boolean isReferralNeeded) {
        this.isReferralNeeded = isReferralNeeded;
    }

    public String getReferredTo() {
        return referredTo;
    }

    public void setReferredTo(String referredTo) {
        this.referredTo = referredTo;
    }

    public String getReferralOrganization() {
        return referralOrganization;
    }

    public void setReferralOrganization(String referralOrganization) {
        this.referralOrganization = referralOrganization;
    }

    public String getReferralPhone() {
        return referralPhone;
    }

    public void setReferralPhone(String referralPhone) {
        this.referralPhone = referralPhone;
    }

    public String getReferralNotes() {
        return referralNotes;
    }

    public void setReferralNotes(String referralNotes) {
        this.referralNotes = referralNotes;
    }

    public LocalDateTime getReferralDate() {
        return referralDate;
    }

    public void setReferralDate(LocalDateTime referralDate) {
        this.referralDate = referralDate;
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

    public String getFollowUpNotes() {
        return followUpNotes;
    }

    public void setFollowUpNotes(String followUpNotes) {
        this.followUpNotes = followUpNotes;
    }

    public Boolean getIsConfidential() {
        return isConfidential;
    }

    public void setIsConfidential(Boolean isConfidential) {
        this.isConfidential = isConfidential;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public SessionOutcome getSessionOutcome() {
        return sessionOutcome;
    }

    public void setSessionOutcome(SessionOutcome sessionOutcome) {
        this.sessionOutcome = sessionOutcome;
    }
}
