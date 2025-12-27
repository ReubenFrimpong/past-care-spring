package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.PrayerCategory;
import com.reuben.pastcare_spring.models.PrayerPriority;
import com.reuben.pastcare_spring.models.PrayerRequest;
import com.reuben.pastcare_spring.models.PrayerRequestStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class PrayerRequestResponse {
    private Long id;
    private Long memberId;
    private String memberName;
    private Long submittedById;
    private String submittedByName;
    private String title;
    private String description;
    private PrayerCategory category;
    private PrayerPriority priority;
    private PrayerRequestStatus status;
    private Boolean isAnonymous;
    private Boolean isUrgent;
    private LocalDate expirationDate;
    private LocalDateTime answeredDate;
    private String testimony;
    private Integer prayerCount;
    private Boolean isPublic;
    private String tags;
    private Boolean isExpired;
    private Boolean isAnswered;
    private Instant createdAt;
    private Instant updatedAt;

    public static PrayerRequestResponse fromEntity(PrayerRequest prayerRequest) {
        PrayerRequestResponse response = new PrayerRequestResponse();
        response.setId(prayerRequest.getId());
        response.setMemberId(prayerRequest.getMember().getId());

        // Handle anonymous requests - hide member details if anonymous
        if (prayerRequest.getIsAnonymous() != null && prayerRequest.getIsAnonymous()) {
            response.setMemberName("Anonymous");
        } else {
            response.setMemberName(prayerRequest.getMember().getFirstName() + " " + prayerRequest.getMember().getLastName());
        }

        response.setSubmittedById(prayerRequest.getSubmittedBy().getId());
        response.setSubmittedByName(prayerRequest.getSubmittedBy().getName());
        response.setTitle(prayerRequest.getTitle());
        response.setDescription(prayerRequest.getDescription());
        response.setCategory(prayerRequest.getCategory());
        response.setPriority(prayerRequest.getPriority());
        response.setStatus(prayerRequest.getStatus());
        response.setIsAnonymous(prayerRequest.getIsAnonymous());
        response.setIsUrgent(prayerRequest.getIsUrgent());
        response.setExpirationDate(prayerRequest.getExpirationDate());
        response.setAnsweredDate(prayerRequest.getAnsweredDate());
        response.setTestimony(prayerRequest.getTestimony());
        response.setPrayerCount(prayerRequest.getPrayerCount());
        response.setIsPublic(prayerRequest.getIsPublic());
        response.setTags(prayerRequest.getTags());
        response.setIsExpired(prayerRequest.isExpired());
        response.setIsAnswered(prayerRequest.isAnswered());
        response.setCreatedAt(prayerRequest.getCreatedAt());
        response.setUpdatedAt(prayerRequest.getUpdatedAt());

        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMemberId() { return memberId; }
    public void setMemberId(Long memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public Long getSubmittedById() { return submittedById; }
    public void setSubmittedById(Long submittedById) { this.submittedById = submittedById; }

    public String getSubmittedByName() { return submittedByName; }
    public void setSubmittedByName(String submittedByName) { this.submittedByName = submittedByName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PrayerCategory getCategory() { return category; }
    public void setCategory(PrayerCategory category) { this.category = category; }

    public PrayerPriority getPriority() { return priority; }
    public void setPriority(PrayerPriority priority) { this.priority = priority; }

    public PrayerRequestStatus getStatus() { return status; }
    public void setStatus(PrayerRequestStatus status) { this.status = status; }

    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }

    public Boolean getIsUrgent() { return isUrgent; }
    public void setIsUrgent(Boolean isUrgent) { this.isUrgent = isUrgent; }

    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }

    public LocalDateTime getAnsweredDate() { return answeredDate; }
    public void setAnsweredDate(LocalDateTime answeredDate) { this.answeredDate = answeredDate; }

    public String getTestimony() { return testimony; }
    public void setTestimony(String testimony) { this.testimony = testimony; }

    public Integer getPrayerCount() { return prayerCount; }
    public void setPrayerCount(Integer prayerCount) { this.prayerCount = prayerCount; }

    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }

    public Boolean getIsAnswered() { return isAnswered; }
    public void setIsAnswered(Boolean isAnswered) { this.isAnswered = isAnswered; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
