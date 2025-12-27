package com.reuben.pastcare_spring.models;

/**
 * Enum representing the location type of an event.
 * Supports physical, virtual, and hybrid event formats.
 */
public enum EventLocationType {
    /**
     * Physical in-person event at a specific location
     */
    PHYSICAL("Physical", "In-person event at a physical location"),

    /**
     * Virtual online event (via Zoom, Teams, YouTube Live, etc.)
     */
    VIRTUAL("Virtual", "Online event via video conferencing"),

    /**
     * Hybrid event with both physical and virtual attendance options
     */
    HYBRID("Hybrid", "Both in-person and online attendance available");

    private final String displayName;
    private final String description;

    EventLocationType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Get EventLocationType from string value (case-insensitive)
     */
    public static EventLocationType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (EventLocationType type : EventLocationType.values()) {
            if (type.name().equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid EventLocationType: " + value);
    }

    /**
     * Check if this location type requires a physical address
     */
    public boolean requiresPhysicalAddress() {
        return this == PHYSICAL || this == HYBRID;
    }

    /**
     * Check if this location type requires a virtual link
     */
    public boolean requiresVirtualLink() {
        return this == VIRTUAL || this == HYBRID;
    }
}
