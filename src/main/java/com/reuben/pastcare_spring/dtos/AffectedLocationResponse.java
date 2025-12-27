package com.reuben.pastcare_spring.dtos;

import com.reuben.pastcare_spring.models.CrisisAffectedLocation;

/**
 * Response DTO for affected location
 */
public class AffectedLocationResponse {
    private Long id;
    private String suburb;
    private String city;
    private String district;
    private String region;
    private String countryCode;

    public static AffectedLocationResponse fromEntity(CrisisAffectedLocation location) {
        AffectedLocationResponse response = new AffectedLocationResponse();
        response.setId(location.getId());
        response.setSuburb(location.getSuburb());
        response.setCity(location.getCity());
        response.setDistrict(location.getDistrict());
        response.setRegion(location.getRegion());
        response.setCountryCode(location.getCountryCode());
        return response;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
