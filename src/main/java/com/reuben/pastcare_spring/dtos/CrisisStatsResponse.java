package com.reuben.pastcare_spring.dtos;

public class CrisisStatsResponse {
    private Long totalCrises;
    private Long activeCrises;
    private Long inResponseCrises;
    private Long resolvedCrises;
    private Long criticalCrises;
    private Long highSeverityCrises;
    private Long totalAffectedMembers;

    // Constructors
    public CrisisStatsResponse() {
    }

    public CrisisStatsResponse(Long totalCrises, Long activeCrises, Long inResponseCrises,
                               Long resolvedCrises, Long criticalCrises, Long highSeverityCrises,
                               Long totalAffectedMembers) {
        this.totalCrises = totalCrises;
        this.activeCrises = activeCrises;
        this.inResponseCrises = inResponseCrises;
        this.resolvedCrises = resolvedCrises;
        this.criticalCrises = criticalCrises;
        this.highSeverityCrises = highSeverityCrises;
        this.totalAffectedMembers = totalAffectedMembers;
    }

    // Getters and Setters
    public Long getTotalCrises() { return totalCrises; }
    public void setTotalCrises(Long totalCrises) { this.totalCrises = totalCrises; }

    public Long getActiveCrises() { return activeCrises; }
    public void setActiveCrises(Long activeCrises) { this.activeCrises = activeCrises; }

    public Long getInResponseCrises() { return inResponseCrises; }
    public void setInResponseCrises(Long inResponseCrises) { this.inResponseCrises = inResponseCrises; }

    public Long getResolvedCrises() { return resolvedCrises; }
    public void setResolvedCrises(Long resolvedCrises) { this.resolvedCrises = resolvedCrises; }

    public Long getCriticalCrises() { return criticalCrises; }
    public void setCriticalCrises(Long criticalCrises) { this.criticalCrises = criticalCrises; }

    public Long getHighSeverityCrises() { return highSeverityCrises; }
    public void setHighSeverityCrises(Long highSeverityCrises) { this.highSeverityCrises = highSeverityCrises; }

    public Long getTotalAffectedMembers() { return totalAffectedMembers; }
    public void setTotalAffectedMembers(Long totalAffectedMembers) { this.totalAffectedMembers = totalAffectedMembers; }
}
