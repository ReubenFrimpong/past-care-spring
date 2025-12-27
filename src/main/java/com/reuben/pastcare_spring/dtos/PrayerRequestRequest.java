package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.PrayerCategory;
import com.reuben.pastcare_spring.models.PrayerPriority;
import com.reuben.pastcare_spring.models.PrayerRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class PrayerRequestRequest {

    @NotNull(message = "Member ID is required")
    private Long memberId;

    @NotNull(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Category is required")
    private PrayerCategory category;

    private PrayerPriority priority;

    private PrayerRequestStatus status;

    private Boolean isAnonymous;

    private Boolean isUrgent;

    private LocalDate expirationDate;

    private Boolean isPublic;

    private String tags;

    // Constructors
    public PrayerRequestRequest() {
    }

    // Getters and Setters
    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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
}
