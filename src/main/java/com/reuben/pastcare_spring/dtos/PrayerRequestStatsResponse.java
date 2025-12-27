package com.reuben.pastcare_spring.dtos;

public class PrayerRequestStatsResponse {
    private Long totalPrayerRequests;
    private Long pendingPrayerRequests;
    private Long activePrayerRequests;
    private Long answeredPrayerRequests;
    private Long urgentPrayerRequests;
    private Long publicPrayerRequests;

    // Constructors
    public PrayerRequestStatsResponse() {
    }

    public PrayerRequestStatsResponse(Long totalPrayerRequests, Long pendingPrayerRequests,
                                      Long activePrayerRequests, Long answeredPrayerRequests,
                                      Long urgentPrayerRequests, Long publicPrayerRequests) {
        this.totalPrayerRequests = totalPrayerRequests;
        this.pendingPrayerRequests = pendingPrayerRequests;
        this.activePrayerRequests = activePrayerRequests;
        this.answeredPrayerRequests = answeredPrayerRequests;
        this.urgentPrayerRequests = urgentPrayerRequests;
        this.publicPrayerRequests = publicPrayerRequests;
    }

    // Getters and Setters
    public Long getTotalPrayerRequests() { return totalPrayerRequests; }
    public void setTotalPrayerRequests(Long totalPrayerRequests) { this.totalPrayerRequests = totalPrayerRequests; }

    public Long getPendingPrayerRequests() { return pendingPrayerRequests; }
    public void setPendingPrayerRequests(Long pendingPrayerRequests) { this.pendingPrayerRequests = pendingPrayerRequests; }

    public Long getActivePrayerRequests() { return activePrayerRequests; }
    public void setActivePrayerRequests(Long activePrayerRequests) { this.activePrayerRequests = activePrayerRequests; }

    public Long getAnsweredPrayerRequests() { return answeredPrayerRequests; }
    public void setAnsweredPrayerRequests(Long answeredPrayerRequests) { this.answeredPrayerRequests = answeredPrayerRequests; }

    public Long getUrgentPrayerRequests() { return urgentPrayerRequests; }
    public void setUrgentPrayerRequests(Long urgentPrayerRequests) { this.urgentPrayerRequests = urgentPrayerRequests; }

    public Long getPublicPrayerRequests() { return publicPrayerRequests; }
    public void setPublicPrayerRequests(Long publicPrayerRequests) { this.publicPrayerRequests = publicPrayerRequests; }
}
