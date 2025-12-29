package com.reuben.pastcare_spring.controllers;

import com.reuben.pastcare_spring.annotations.RequirePermission;
import com.reuben.pastcare_spring.enums.Permission;

import com.reuben.pastcare_spring.models.Location;
import com.reuben.pastcare_spring.services.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Autowired
    private LocationService locationService;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    /**
     * Proxy endpoint for OpenStreetMap Nominatim search
     * This avoids CORS issues when calling Nominatim from the frontend
     * Supports international locations - not limited to Ghana
     */
    @RequirePermission(Permission.MEMBER_VIEW_ALL)
    @GetMapping("/search")
    public ResponseEntity<?> searchLocation(@RequestParam String query) {
        try {
            // Build the Nominatim URL with required parameters
            // Allow international searches - users can specify country in query
            String url = String.format(
                "%s?format=json&q=%s&limit=10&addressdetails=1",
                NOMINATIM_URL,
                query
            );

            // Make the request with proper User-Agent header (required by Nominatim)
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "PastCare Church Management System");

            org.springframework.http.HttpEntity<?> entity = new org.springframework.http.HttpEntity<>(headers);

            ResponseEntity<Object[]> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                Object[].class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to search location: " + e.getMessage()));
        }
    }

    /**
     * Create or get existing location from Nominatim data
     * This endpoint ensures locations are deduplicated by coordinates
     */
    @RequirePermission(Permission.CHURCH_SETTINGS_EDIT)
    @PostMapping("/create-from-nominatim")
    public ResponseEntity<?> createLocationFromNominatim(@RequestBody NominatimLocationRequest request) {
        try {
            Location location = locationService.getOrCreateLocation(
                request.coordinates(),
                request.address()
            );
            return ResponseEntity.ok(new LocationResponse(
                location.getId(),
                location.getCoordinates(),
                location.getDisplayName(),
                location.getFullAddress()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new ErrorResponse("Failed to create location: " + e.getMessage()));
        }
    }

    private record NominatimLocationRequest(
        String coordinates,
        Map<String, Object> address
    ) {}

    private record LocationResponse(
        Long id,
        String coordinates,
        String displayName,
        String fullAddress
    ) {}

    private record ErrorResponse(String message) {}
}
