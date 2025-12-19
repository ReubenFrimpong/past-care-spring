package com.reuben.pastcare_spring.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a standardized geographic location in Ghana.
 * This entity ensures consistent location naming across the system
 * and enables efficient querying and grouping for visualization.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
  name = "locations",
  indexes = {
    @Index(name = "idx_coordinates", columnList = "coordinates"),
    @Index(name = "idx_suburb", columnList = "suburb"),
    @Index(name = "idx_city", columnList = "city"),
    @Index(name = "idx_region", columnList = "region")
  }
)
@Data
public class Location extends BaseEntity {

  /**
   * GPS coordinates in "latitude,longitude" format
   * Example: "5.6037,0.1870"
   */
  @Column(nullable = false, unique = true, length = 50)
  private String coordinates;

  /**
   * Region/State in Ghana
   * Example: "Greater Accra Region", "Ashanti Region"
   */
  @Column(length = 100)
  private String region;

  /**
   * District/County
   * Example: "Accra Metropolitan", "Kumasi Metropolitan"
   */
  @Column(length = 100)
  private String district;

  /**
   * City/Town
   * Example: "Accra", "Kumasi", "Tema"
   */
  @Column(nullable = false, length = 100)
  private String city;

  /**
   * Suburb/Neighborhood
   * Example: "Dansoman", "East Legon", "Osu"
   */
  @Column(length = 100)
  private String suburb;

  /**
   * Full display name from OpenStreetMap
   * Stored for reference but not used for querying
   */
  @Column(length = 500)
  private String fullAddress;

  /**
   * Returns a standardized display name for the location
   * Priority: Suburb > City
   */
  public String getDisplayName() {
    if (suburb != null && !suburb.isEmpty()) {
      if (city != null && !city.isEmpty()) {
        return suburb + ", " + city;
      }
      return suburb;
    }
    // City should never be null due to database constraints and service layer validation
    // But if it somehow is, return a sensible default
    return city != null && !city.isEmpty() ? city : "Unknown Location";
  }

  /**
   * Returns a short name (just the most specific location)
   */
  public String getShortName() {
    if (suburb != null && !suburb.isEmpty()) {
      return suburb;
    }
    return city != null && !city.isEmpty() ? city : "Unknown Location";
  }

  /**
   * Returns the full hierarchical name
   */
  public String getFullName() {
    StringBuilder name = new StringBuilder();
    if (suburb != null && !suburb.isEmpty()) {
      name.append(suburb).append(", ");
    }
    name.append(city);
    if (district != null && !district.isEmpty()) {
      name.append(", ").append(district);
    }
    if (region != null && !region.isEmpty()) {
      name.append(", ").append(region);
    }
    return name.toString();
  }
}
