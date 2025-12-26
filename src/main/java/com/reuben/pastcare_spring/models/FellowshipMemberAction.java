package com.reuben.pastcare_spring.models;

/**
 * Types of fellowship membership actions for tracking retention
 */
public enum FellowshipMemberAction {
    JOINED,           // Member joined the fellowship
    LEFT,             // Member left the fellowship
    TRANSFERRED_IN,   // Member transferred from another fellowship
    TRANSFERRED_OUT,  // Member transferred to another fellowship
    INACTIVE,         // Member became inactive (still member but not attending)
    REACTIVATED       // Previously inactive member became active again
}
