package com.reuben.pastcare_spring.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Represents a standardized geographic location with international address support.
 * This entity ensures consistent location naming across the system
 * and enables efficient querying and grouping for visualization.
 *
 * Supports both Ghana-specific fields (for backward compatibility)
 * and international address formats.
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(
  name = "locations",
  indexes = {
    @Index(name = "idx_coordinates", columnList = "coordinates"),
    @Index(name = "idx_suburb", columnList = "suburb"),
    @Index(name = "idx_city", columnList = "city"),
    @Index(name = "idx_region", columnList = "region"),
    @Index(name = "idx_country_code", columnList = "countryCode"),
    @Index(name = "idx_postal_code", columnList = "postalCode")
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

  // ========== International Fields ==========

  /**
   * ISO 3166-1 alpha-2 country code
   * Example: "GH", "US", "GB", "NG", "CA"
   */
  @Column(nullable = false, length = 2)
  private String countryCode = "GH";

  /**
   * Full country name
   * Example: "Ghana", "United States", "United Kingdom"
   */
  @Column(nullable = false, length = 100)
  private String countryName = "Ghana";

  /**
   * State (USA, Australia, etc.) or Region (Ghana)
   * Example: "California", "Greater Accra Region", "New South Wales"
   */
  @Column(length = 100)
  private String state;

  /**
   * Province (Canada, etc.)
   * Example: "Ontario", "British Columbia"
   */
  @Column(length = 100)
  private String province;

  /**
   * Postal/ZIP code
   * Example: "90210" (USA), "SW1A 1AA" (UK), "K1A 0B1" (Canada)
   */
  @Column(length = 20)
  private String postalCode;

  /**
   * Primary address line (street number and name)
   * Example: "123 Main Street", "45 Oxford Road"
   */
  @Column(length = 200)
  private String addressLine1;

  /**
   * Secondary address line (apartment, suite, etc.)
   * Example: "Apt 4B", "Suite 200"
   */
  @Column(length = 200)
  private String addressLine2;

  // ========== Ghana-Specific Fields (Backward Compatibility) ==========

  /**
   * Region in Ghana (legacy field, now mapped to state for Ghana addresses)
   * Example: "Greater Accra Region", "Ashanti Region"
   */
  @Column(length = 100)
  private String region;

  /**
   * District/County (Ghana-specific)
   * Example: "Accra Metropolitan", "Kumasi Metropolitan"
   */
  @Column(length = 100)
  private String district;

  /**
   * City/Town
   * Example: "Accra", "Kumasi", "Tema", "New York", "London"
   */
  @Column(nullable = false, length = 100)
  private String city;

  /**
   * Suburb/Neighborhood (Ghana-specific)
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
   * Handles both international and Ghana-specific addresses
   */
  public String getDisplayName() {
    // For Ghana addresses (backward compatibility)
    if ("GH".equals(countryCode)) {
      if (suburb != null && !suburb.isEmpty()) {
        if (city != null && !city.isEmpty()) {
          return suburb + ", " + city;
        }
        return suburb;
      }
      return city != null && !city.isEmpty() ? city : "Unknown Location";
    }

    // For international addresses
    if (addressLine1 != null && !addressLine1.isEmpty()) {
      return addressLine1 + ", " + city;
    }
    return city != null && !city.isEmpty() ? city : "Unknown Location";
  }

  /**
   * Returns a short name (just the most specific location)
   */
  public String getShortName() {
    // For Ghana
    if ("GH".equals(countryCode)) {
      if (suburb != null && !suburb.isEmpty()) {
        return suburb;
      }
    }

    // For international
    if (addressLine1 != null && !addressLine1.isEmpty()) {
      return addressLine1;
    }

    return city != null && !city.isEmpty() ? city : "Unknown Location";
  }

  /**
   * Returns the full hierarchical name formatted by country
   */
  public String getFullName() {
    StringBuilder name = new StringBuilder();

    if ("GH".equals(countryCode)) {
      // Ghana format: Suburb, City, District, Region
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
    } else if ("US".equals(countryCode)) {
      // USA format: Address Line 1, Address Line 2, City, State ZIP
      if (addressLine1 != null && !addressLine1.isEmpty()) {
        name.append(addressLine1);
      }
      if (addressLine2 != null && !addressLine2.isEmpty()) {
        if (name.length() > 0) name.append(", ");
        name.append(addressLine2);
      }
      if (name.length() > 0) name.append(", ");
      name.append(city);
      if (state != null && !state.isEmpty()) {
        name.append(", ").append(state);
      }
      if (postalCode != null && !postalCode.isEmpty()) {
        name.append(" ").append(postalCode);
      }
    } else if ("GB".equals(countryCode)) {
      // UK format: Address Line 1, Address Line 2, City, County, Postcode
      if (addressLine1 != null && !addressLine1.isEmpty()) {
        name.append(addressLine1);
      }
      if (addressLine2 != null && !addressLine2.isEmpty()) {
        if (name.length() > 0) name.append(", ");
        name.append(addressLine2);
      }
      if (name.length() > 0) name.append(", ");
      name.append(city);
      if (district != null && !district.isEmpty()) {
        name.append(", ").append(district);
      }
      if (postalCode != null && !postalCode.isEmpty()) {
        name.append(", ").append(postalCode);
      }
    } else if ("CA".equals(countryCode)) {
      // Canada format: Address Line 1, Address Line 2, City, Province Postal Code
      if (addressLine1 != null && !addressLine1.isEmpty()) {
        name.append(addressLine1);
      }
      if (addressLine2 != null && !addressLine2.isEmpty()) {
        if (name.length() > 0) name.append(", ");
        name.append(addressLine2);
      }
      if (name.length() > 0) name.append(", ");
      name.append(city);
      if (province != null && !province.isEmpty()) {
        name.append(", ").append(province);
      }
      if (postalCode != null && !postalCode.isEmpty()) {
        name.append(" ").append(postalCode);
      }
    } else {
      // Generic international format
      if (addressLine1 != null && !addressLine1.isEmpty()) {
        name.append(addressLine1);
      }
      if (addressLine2 != null && !addressLine2.isEmpty()) {
        if (name.length() > 0) name.append(", ");
        name.append(addressLine2);
      }
      if (name.length() > 0) name.append(", ");
      name.append(city);
      if (state != null && !state.isEmpty()) {
        name.append(", ").append(state);
      }
      if (countryName != null && !countryName.isEmpty()) {
        name.append(", ").append(countryName);
      }
    }

    return name.toString();
  }
}
