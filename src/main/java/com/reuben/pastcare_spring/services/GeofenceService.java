package com.reuben.pastcare_spring.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.reuben.pastcare_spring.models.AttendanceSession;
import com.reuben.pastcare_spring.repositories.AttendanceSessionRepository;

/**
 * Service for handling geofence-based attendance check-in.
 * Phase 1: Enhanced Attendance Tracking
 *
 * Uses the Haversine formula to calculate distances between coordinates
 * and determine if a member is within the geofence radius of an attendance session.
 *
 * @author Claude Sonnet 4.5
 * @version 1.0
 * @since 2025-12-24
 */
@Service
public class GeofenceService {

  private static final double EARTH_RADIUS_METERS = 6371000; // Earth's radius in meters

  private final AttendanceSessionRepository attendanceSessionRepository;

  public GeofenceService(AttendanceSessionRepository attendanceSessionRepository) {
    this.attendanceSessionRepository = attendanceSessionRepository;
  }

  /**
   * Calculate the distance between two GPS coordinates using the Haversine formula.
   *
   * The Haversine formula calculates the great-circle distance between two points
   * on a sphere given their longitudes and latitudes.
   *
   * @param lat1 Latitude of first point (in degrees)
   * @param lon1 Longitude of first point (in degrees)
   * @param lat2 Latitude of second point (in degrees)
   * @param lon2 Longitude of second point (in degrees)
   * @return Distance between the two points in meters
   */
  public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
    // Convert latitude and longitude from degrees to radians
    double lat1Rad = Math.toRadians(lat1);
    double lon1Rad = Math.toRadians(lon1);
    double lat2Rad = Math.toRadians(lat2);
    double lon2Rad = Math.toRadians(lon2);

    // Differences
    double deltaLat = lat2Rad - lat1Rad;
    double deltaLon = lon2Rad - lon1Rad;

    // Haversine formula
    double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
        Math.cos(lat1Rad) * Math.cos(lat2Rad) *
            Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    // Distance in meters
    return EARTH_RADIUS_METERS * c;
  }

  /**
   * Check if a coordinate is within the geofence radius of a session.
   *
   * @param sessionId The attendance session ID
   * @param userLat User's current latitude
   * @param userLon User's current longitude
   * @return true if user is within geofence, false otherwise
   * @throws IllegalArgumentException if session not found or geofence not configured
   */
  public boolean isWithinGeofence(Long sessionId, double userLat, double userLon) {
    AttendanceSession session = attendanceSessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("Attendance session not found with id: " + sessionId));

    if (session.getGeofenceLatitude() == null || session.getGeofenceLongitude() == null) {
      throw new IllegalArgumentException("Geofence not configured for session: " + sessionId);
    }

    double distance = calculateDistance(
        session.getGeofenceLatitude(),
        session.getGeofenceLongitude(),
        userLat,
        userLon);

    return distance <= session.getGeofenceRadiusMeters();
  }

  /**
   * Find all active sessions within geofence range of a user's location.
   *
   * @param userLat User's current latitude
   * @param userLon User's current longitude
   * @param maxDistanceMeters Maximum distance to search (optional, defaults to 5000m)
   * @return List of sessions within range, ordered by distance (closest first)
   */
  public List<AttendanceSession> findNearbySessions(double userLat, double userLon, Integer maxDistanceMeters) {
    int searchRadius = (maxDistanceMeters != null) ? maxDistanceMeters : 5000; // Default 5km

    // Get all active sessions with geofence configured
    List<AttendanceSession> allSessions = attendanceSessionRepository.findAll()
        .stream()
        .filter(s -> s.getIsCompleted() == null || !s.getIsCompleted())
        .filter(s -> s.getGeofenceLatitude() != null && s.getGeofenceLongitude() != null)
        .collect(Collectors.toList());

    // Calculate distances and filter by search radius
    return allSessions.stream()
        .map(session -> {
          double distance = calculateDistance(
              session.getGeofenceLatitude(),
              session.getGeofenceLongitude(),
              userLat,
              userLon);
          return new SessionWithDistance(session, distance);
        })
        .filter(swd -> swd.distance <= searchRadius)
        .sorted((a, b) -> Double.compare(a.distance, b.distance)) // Sort by distance
        .map(swd -> swd.session)
        .collect(Collectors.toList());
  }

  /**
   * Get the distance from a user's location to a specific session's geofence center.
   *
   * @param sessionId The attendance session ID
   * @param userLat User's current latitude
   * @param userLon User's current longitude
   * @return Distance in meters
   * @throws IllegalArgumentException if session not found or geofence not configured
   */
  public double getDistanceToSession(Long sessionId, double userLat, double userLon) {
    AttendanceSession session = attendanceSessionRepository.findById(sessionId)
        .orElseThrow(() -> new IllegalArgumentException("Attendance session not found with id: " + sessionId));

    if (session.getGeofenceLatitude() == null || session.getGeofenceLongitude() == null) {
      throw new IllegalArgumentException("Geofence not configured for session: " + sessionId);
    }

    return calculateDistance(
        session.getGeofenceLatitude(),
        session.getGeofenceLongitude(),
        userLat,
        userLon);
  }

  /**
   * Helper class to hold session with calculated distance.
   */
  private static class SessionWithDistance {
    final AttendanceSession session;
    final double distance;

    SessionWithDistance(AttendanceSession session, double distance) {
      this.session = session;
      this.distance = distance;
    }
  }
}
