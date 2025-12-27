package com.reuben.pastcare_spring.dtos;

public class CareNeedStatsResponse {
    private Long totalCareNeeds;
    private Long openCareNeeds;
    private Long inProgressCareNeeds;
    private Long resolvedCareNeeds;
    private Long overdueCareNeeds;
    private Long urgentCareNeeds;
    private Long unassignedCareNeeds;

    // Constructors
    public CareNeedStatsResponse() {
    }

    public CareNeedStatsResponse(Long totalCareNeeds, Long openCareNeeds, Long inProgressCareNeeds,
                                  Long resolvedCareNeeds, Long urgentCareNeeds, Long overdueCareNeeds) {
        this.totalCareNeeds = totalCareNeeds;
        this.openCareNeeds = openCareNeeds;
        this.inProgressCareNeeds = inProgressCareNeeds;
        this.resolvedCareNeeds = resolvedCareNeeds;
        this.urgentCareNeeds = urgentCareNeeds;
        this.overdueCareNeeds = overdueCareNeeds;
    }

    // Getters and Setters
    public Long getTotalCareNeeds() { return totalCareNeeds; }
    public void setTotalCareNeeds(Long totalCareNeeds) { this.totalCareNeeds = totalCareNeeds; }
    
    public Long getOpenCareNeeds() { return openCareNeeds; }
    public void setOpenCareNeeds(Long openCareNeeds) { this.openCareNeeds = openCareNeeds; }
    
    public Long getInProgressCareNeeds() { return inProgressCareNeeds; }
    public void setInProgressCareNeeds(Long inProgressCareNeeds) { this.inProgressCareNeeds = inProgressCareNeeds; }
    
    public Long getResolvedCareNeeds() { return resolvedCareNeeds; }
    public void setResolvedCareNeeds(Long resolvedCareNeeds) { this.resolvedCareNeeds = resolvedCareNeeds; }
    
    public Long getOverdueCareNeeds() { return overdueCareNeeds; }
    public void setOverdueCareNeeds(Long overdueCareNeeds) { this.overdueCareNeeds = overdueCareNeeds; }
    
    public Long getUrgentCareNeeds() { return urgentCareNeeds; }
    public void setUrgentCareNeeds(Long urgentCareNeeds) { this.urgentCareNeeds = urgentCareNeeds; }
    
    public Long getUnassignedCareNeeds() { return unassignedCareNeeds; }
    public void setUnassignedCareNeeds(Long unassignedCareNeeds) { this.unassignedCareNeeds = unassignedCareNeeds; }
}
