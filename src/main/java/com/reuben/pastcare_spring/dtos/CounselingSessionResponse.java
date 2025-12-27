package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CounselingSession;
import com.reuben.pastcare_spring.models.CounselingStatus;
import com.reuben.pastcare_spring.models.CounselingType;
import com.reuben.pastcare_spring.models.SessionOutcome;

import java.time.Instant;
import java.time.LocalDateTime;

public class CounselingSessionResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long counselorId;
    private String counselorName;
    private Long careNeedId;
    private String careNeedTitle;
    private String title;
    private String sessionNotes;
    private CounselingType type;
    private LocalDateTime sessionDate;
    private Integer durationMinutes;
    private String location;
    private CounselingStatus status;

    // Referral fields
    private Boolean isReferralNeeded;
    private String referredTo;
    private String referralOrganization;
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

    private Instant createdAt;
    private Instant updatedAt;

    public static CounselingSessionResponse fromEntity(CounselingSession session) {
        CounselingSessionResponse response = new CounselingSessionResponse();
        response.setId(session.getId());
        response.setMemberId(session.getMember().getId());
        response.setMemberName(session.getMember().getFirstName() + " " + session.getMember().getLastName());
        response.setCounselorId(session.getCounselor().getId());
        response.setCounselorName(session.getCounselor().getName());

        if (session.getCareNeed() != null) {
            response.setCareNeedId(session.getCareNeed().getId());
            response.setCareNeedTitle(session.getCareNeed().getTitle());
        }

        response.setTitle(session.getTitle());
        response.setSessionNotes(session.getSessionNotes());
        response.setType(session.getType());
        response.setSessionDate(session.getSessionDate());
        response.setDurationMinutes(session.getDurationMinutes());
        response.setLocation(session.getLocation());
        response.setStatus(session.getStatus());

        // Referral fields
        response.setIsReferralNeeded(session.getIsReferralNeeded());
        response.setReferredTo(session.getReferredTo());
        response.setReferralOrganization(session.getReferralOrganization());
        response.setReferralPhone(session.getReferralPhone());
        response.setReferralNotes(session.getReferralNotes());
        response.setReferralDate(session.getReferralDate());

        // Follow-up
        response.setFollowUpRequired(session.getFollowUpRequired());
        response.setFollowUpDate(session.getFollowUpDate());
        response.setFollowUpNotes(session.getFollowUpNotes());

        // Confidentiality
        response.setIsConfidential(session.getIsConfidential());

        // Outcome
        response.setOutcome(session.getOutcome());
        response.setSessionOutcome(session.getSessionOutcome());

        response.setCreatedAt(session.getCreatedAt());
        response.setUpdatedAt(session.getUpdatedAt());

        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public Long getCounselorId() { return counselorId; }
    public void setCounselorId(Long counselorId) { this.counselorId = counselorId; }

    public String getCounselorName() { return counselorName; }
    public void setCounselorName(String counselorName) { this.counselorName = counselorName; }

    public Long getCareNeedId() { return careNeedId; }
    public void setCareNeedId(Long careNeedId) { this.careNeedId = careNeedId; }

    public String getCareNeedTitle() { return careNeedTitle; }
    public void setCareNeedTitle(String careNeedTitle) { this.careNeedTitle = careNeedTitle; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSessionNotes() { return sessionNotes; }
    public void setSessionNotes(String sessionNotes) { this.sessionNotes = sessionNotes; }

    public CounselingType getType() { return type; }
    public void setType(CounselingType type) { this.type = type; }

    public LocalDateTime getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDateTime sessionDate) { this.sessionDate = sessionDate; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public CounselingStatus getStatus() { return status; }
    public void setStatus(CounselingStatus status) { this.status = status; }

    public Boolean getIsReferralNeeded() { return isReferralNeeded; }
    public void setIsReferralNeeded(Boolean isReferralNeeded) { this.isReferralNeeded = isReferralNeeded; }

    public String getReferredTo() { return referredTo; }
    public void setReferredTo(String referredTo) { this.referredTo = referredTo; }

    public String getReferralOrganization() { return referralOrganization; }
    public void setReferralOrganization(String referralOrganization) { this.referralOrganization = referralOrganization; }

    public String getReferralPhone() { return referralPhone; }
    public void setReferralPhone(String referralPhone) { this.referralPhone = referralPhone; }

    public String getReferralNotes() { return referralNotes; }
    public void setReferralNotes(String referralNotes) { this.referralNotes = referralNotes; }

    public LocalDateTime getReferralDate() { return referralDate; }
    public void setReferralDate(LocalDateTime referralDate) { this.referralDate = referralDate; }

    public Boolean getFollowUpRequired() { return followUpRequired; }
    public void setFollowUpRequired(Boolean followUpRequired) { this.followUpRequired = followUpRequired; }

    public LocalDateTime getFollowUpDate() { return followUpDate; }
    public void setFollowUpDate(LocalDateTime followUpDate) { this.followUpDate = followUpDate; }

    public String getFollowUpNotes() { return followUpNotes; }
    public void setFollowUpNotes(String followUpNotes) { this.followUpNotes = followUpNotes; }

    public Boolean getIsConfidential() { return isConfidential; }
    public void setIsConfidential(Boolean isConfidential) { this.isConfidential = isConfidential; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public SessionOutcome getSessionOutcome() { return sessionOutcome; }
    public void setSessionOutcome(SessionOutcome sessionOutcome) { this.sessionOutcome = sessionOutcome; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
