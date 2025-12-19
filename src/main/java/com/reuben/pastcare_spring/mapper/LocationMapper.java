package com.reuben.pastcare_spring.mapper;

import com.reuben.pastcare_spring.dtos.LocationResponse;
import com.reuben.pastcare_spring.models.Location;

public class LocationMapper {

  public static LocationResponse toLocationResponse(Location location) {
    if (location == null) {
      return null;
    }

    return new LocationResponse(
      location.getId(),
      location.getCoordinates(),
      location.getRegion(),
      location.getDistrict(),
      location.getCity(),
      location.getSuburb(),
      location.getDisplayName(),
      location.getShortName()
    );
  }
}
