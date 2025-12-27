package com.reuben.pastcare_spring.dtos;

public class CounselingSessionStatsResponse {
    private Long totalSessions;
    private Long scheduledSessions;
    private Long completedSessions;
    private Long cancelledSessions;
    private Long sessionsRequiringFollowUp;
    private Long referralsMade;
    private Long individualSessions;
    private Long groupSessions;
    private Long crisisSessions;

    // Constructors
    public CounselingSessionStatsResponse() {
    }

    public CounselingSessionStatsResponse(Long totalSessions, Long scheduledSessions, Long completedSessions,
                                          Long cancelledSessions, Long sessionsRequiringFollowUp,
                                          Long referralsMade, Long individualSessions, Long groupSessions,
                                          Long crisisSessions) {
        this.totalSessions = totalSessions;
        this.scheduledSessions = scheduledSessions;
        this.completedSessions = completedSessions;
        this.cancelledSessions = cancelledSessions;
        this.sessionsRequiringFollowUp = sessionsRequiringFollowUp;
        this.referralsMade = referralsMade;
        this.individualSessions = individualSessions;
        this.groupSessions = groupSessions;
        this.crisisSessions = crisisSessions;
    }

    // Getters and Setters
    public Long getTotalSessions() { return totalSessions; }
    public void setTotalSessions(Long totalSessions) { this.totalSessions = totalSessions; }

    public Long getScheduledSessions() { return scheduledSessions; }
    public void setScheduledSessions(Long scheduledSessions) { this.scheduledSessions = scheduledSessions; }

    public Long getCompletedSessions() { return completedSessions; }
    public void setCompletedSessions(Long completedSessions) { this.completedSessions = completedSessions; }

    public Long getCancelledSessions() { return cancelledSessions; }
    public void setCancelledSessions(Long cancelledSessions) { this.cancelledSessions = cancelledSessions; }

    public Long getSessionsRequiringFollowUp() { return sessionsRequiringFollowUp; }
    public void setSessionsRequiringFollowUp(Long sessionsRequiringFollowUp) { this.sessionsRequiringFollowUp = sessionsRequiringFollowUp; }

    public Long getReferralsMade() { return referralsMade; }
    public void setReferralsMade(Long referralsMade) { this.referralsMade = referralsMade; }

    public Long getIndividualSessions() { return individualSessions; }
    public void setIndividualSessions(Long individualSessions) { this.individualSessions = individualSessions; }

    public Long getGroupSessions() { return groupSessions; }
    public void setGroupSessions(Long groupSessions) { this.groupSessions = groupSessions; }

    public Long getCrisisSessions() { return crisisSessions; }
    public void setCrisisSessions(Long crisisSessions) { this.crisisSessions = crisisSessions; }
}
