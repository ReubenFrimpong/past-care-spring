package com.reuben.pastcare_spring.dtos;

/**
 * Request DTO for specifying an affected location
 */
public class AffectedLocationRequest {
    private String suburb;
    private String city;
    private String district;
    private String region;
    private String countryCode;

    public AffectedLocationRequest() {
    }

    public AffectedLocationRequest(String suburb, String city, String district, String region, String countryCode) {
        this.suburb = suburb;
        this.city = city;
        this.district = district;
        this.region = region;
        this.countryCode = countryCode;
    }

    // Getters and Setters
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
