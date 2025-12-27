package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "prayer_requests")
public class PrayerRequest extends TenantBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id", nullable = false)
    private User submittedBy;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PrayerCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrayerPriority priority = PrayerPriority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PrayerRequestStatus status = PrayerRequestStatus.PENDING;

    @Column(name = "is_anonymous")
    private Boolean isAnonymous = false;

    @Column(name = "is_urgent")
    private Boolean isUrgent = false;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Column(name = "answered_date")
    private LocalDateTime answeredDate;

    @Column(columnDefinition = "TEXT")
    private String testimony;

    @Column(name = "prayer_count")
    private Integer prayerCount = 0;

    @Column(name = "is_public")
    private Boolean isPublic = true;

    @Column(length = 500)
    private String tags;

    // Constructors
    public PrayerRequest() {
    }

    public PrayerRequest(Member member, User submittedBy, String title, PrayerCategory category) {
        this.member = member;
        this.submittedBy = submittedBy;
        this.title = title;
        this.category = category;
    }

    // Getters and Setters
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public User getSubmittedBy() {
        return submittedBy;
    }

    public void setSubmittedBy(User submittedBy) {
        this.submittedBy = submittedBy;
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

    public PrayerCategory getCategory() {
        return category;
    }

    public void setCategory(PrayerCategory category) {
        this.category = category;
    }

    public PrayerPriority getPriority() {
        return priority;
    }

    public void setPriority(PrayerPriority priority) {
        this.priority = priority;
    }

    public PrayerRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PrayerRequestStatus status) {
        this.status = status;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    public Boolean getIsUrgent() {
        return isUrgent;
    }

    public void setIsUrgent(Boolean isUrgent) {
        this.isUrgent = isUrgent;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public LocalDateTime getAnsweredDate() {
        return answeredDate;
    }

    public void setAnsweredDate(LocalDateTime answeredDate) {
        this.answeredDate = answeredDate;
    }

    public String getTestimony() {
        return testimony;
    }

    public void setTestimony(String testimony) {
        this.testimony = testimony;
    }

    public Integer getPrayerCount() {
        return prayerCount;
    }

    public void setPrayerCount(Integer prayerCount) {
        this.prayerCount = prayerCount;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // Helper methods
    public boolean isExpired() {
        return expirationDate != null &&
               LocalDate.now().isAfter(expirationDate) &&
               status != PrayerRequestStatus.ANSWERED;
    }

    public boolean isAnswered() {
        return status == PrayerRequestStatus.ANSWERED;
    }

    public void incrementPrayerCount() {
        if (this.prayerCount == null) {
            this.prayerCount = 0;
        }
        this.prayerCount++;
    }
}
