package com.reuben.pastcare_spring.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    /**
     * Proxy endpoint for OpenStreetMap Nominatim search
     * This avoids CORS issues when calling Nominatim from the frontend
     * Supports international locations - not limited to Ghana
     */
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

    private record ErrorResponse(String message) {}
}
