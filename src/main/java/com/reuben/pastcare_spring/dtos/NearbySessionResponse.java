package com.reuben.pastcare_spring.dtos;

import java.time.LocalDate;
import java.time.LocalTime;

import com.reuben.pastcare_spring.enums.ServiceType;

/**
 * Response DTO for nearby sessions found via geofence.
 * Phase 1: Enhanced Attendance Tracking
 *
 * @param sessionId The attendance session ID
 * @param sessionName The session name
 * @param sessionDate The session date
 * @param sessionTime The session time
 * @param serviceType The type of service
 * @param fellowshipId The fellowship ID (null if church-wide)
 * @param fellowshipName The fellowship name (null if church-wide)
 * @param distanceMeters Distance from user's location in meters
 * @param geofenceRadiusMeters The geofence radius
 * @param isWithinGeofence Whether user is within the geofence
 * @param isCompleted Whether the session is completed
 */
public record NearbySessionResponse(
    Long sessionId,
    String sessionName,
    LocalDate sessionDate,
    LocalTime sessionTime,
    ServiceType serviceType,
    Long fellowshipId,
    String fellowshipName,
    Double distanceMeters,
    Integer geofenceRadiusMeters,
    Boolean isWithinGeofence,
    Boolean isCompleted
) {
}
