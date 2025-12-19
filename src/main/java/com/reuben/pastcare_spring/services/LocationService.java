package com.reuben.pastcare_spring.services;

import com.reuben.pastcare_spring.models.Location;
import com.reuben.pastcare_spring.repositories.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Service for managing standardized location data.
 * Ensures location deduplication and consistent naming across the system.
 */
@Service
public class LocationService {

  @Autowired
  private LocationRepository locationRepository;

  /**
   * Get or create a location from Nominatim search result.
   * This method ensures we don't create duplicate locations.
   *
   * @param coordinates GPS coordinates in "lat,lon" format
   * @param nominatimAddress Address components from Nominatim API
   * @return The existing or newly created Location entity
   */
  @Transactional
  public Location getOrCreateLocation(String coordinates, Map<String, Object> nominatimAddress) {
    // First, try to find by exact coordinates
    return locationRepository.findByCoordinates(coordinates)
      .orElseGet(() -> createLocationFromNominatim(coordinates, nominatimAddress));
  }

  /**
   * Creates a new standardized location from Nominatim data
   */
  private Location createLocationFromNominatim(String coordinates, Map<String, Object> nominatimAddress) {
    Location location = new Location();
    location.setCoordinates(coordinates);

    // Extract standardized location components
    // Nominatim returns different fields for different location types
    String region = extractField(nominatimAddress, "state", "region");
    String district = extractField(nominatimAddress, "county", "state_district");
    String city = extractField(nominatimAddress, "city", "town", "municipality");
    String suburb = extractField(nominatimAddress, "suburb", "neighbourhood", "quarter");

    location.setRegion(region);
    location.setDistrict(district);
    location.setCity(city != null ? city : "Ghana"); // Fallback to Ghana if no city
    location.setSuburb(suburb);

    // Store the full display name for reference
    if (nominatimAddress.containsKey("display_name")) {
      location.setFullAddress(nominatimAddress.get("display_name").toString());
    }

    return locationRepository.save(location);
  }

  /**
   * Helper method to extract the first non-null field from Nominatim address
   */
  private String extractField(Map<String, Object> address, String... fieldNames) {
    for (String fieldName : fieldNames) {
      Object value = address.get(fieldName);
      if (value != null && !value.toString().isEmpty()) {
        return value.toString();
      }
    }
    return null;
  }

  /**
   * Find location by coordinates
   */
  public Location findByCoordinates(String coordinates) {
    return locationRepository.findByCoordinates(coordinates).orElse(null);
  }

  /**
   * Get location by ID
   */
  public Location findById(Long id) {
    return locationRepository.findById(id).orElse(null);
  }
}
