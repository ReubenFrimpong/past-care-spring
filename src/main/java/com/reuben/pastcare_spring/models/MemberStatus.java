package com.reuben.pastcare_spring.models;

/**
 * Represents the status of a member in their journey with the church.
 * Used to track member engagement and lifecycle stages.
 */
public enum MemberStatus {
    /**
     * First-time visitor to the church.
     * Just attended once or being registered at entrance.
     */
    VISITOR,

    /**
     * Has attended 2-3 times.
     * Showing interest but not yet regular.
     */
    FIRST_TIMER,

    /**
     * Attends regularly but not yet an official member.
     * Participates in services but hasn't completed membership process.
     */
    REGULAR,

    /**
     * Official church member.
     * Completed membership class/process, committed to the church.
     */
    MEMBER,

    /**
     * Church leader or ministry head.
     * Serves in leadership capacity (pastor, elder, deacon, ministry leader).
     */
    LEADER,

    /**
     * Former member who is no longer active.
     * Moved away, transferred to another church, or inactive.
     */
    INACTIVE
}
