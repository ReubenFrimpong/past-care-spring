package com.reuben.pastcare_spring.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Location entity with international address support.
 * Tests address formatting for different countries.
 */
@DisplayName("Location International Address Tests")
class LocationInternationalTest {

    // ========== Ghana Address Tests ==========

    @Test
    @DisplayName("Ghana address - full format with suburb, city, district, region")
    void testGhanaAddressFullFormat() {
        Location location = new Location();
        location.setCoordinates("5.6037,-0.1870");
        location.setCountryCode("GH");
        location.setCountryName("Ghana");
        location.setSuburb("East Legon");
        location.setCity("Accra");
        location.setDistrict("Accra Metropolitan");
        location.setRegion("Greater Accra Region");

        assertEquals("East Legon, Accra", location.getDisplayName());
        assertEquals("East Legon", location.getShortName());
        assertEquals("East Legon, Accra, Accra Metropolitan, Greater Accra Region", location.getFullName());
    }

    @Test
    @DisplayName("Ghana address - city only")
    void testGhanaAddressCityOnly() {
        Location location = new Location();
        location.setCoordinates("6.6885,-1.6244");
        location.setCountryCode("GH");
        location.setCountryName("Ghana");
        location.setCity("Kumasi");
        location.setRegion("Ashanti Region");

        assertEquals("Kumasi", location.getDisplayName());
        assertEquals("Kumasi", location.getShortName());
        assertEquals("Kumasi, Ashanti Region", location.getFullName());
    }

    // ========== USA Address Tests ==========

    @Test
    @DisplayName("USA address - full format with address lines, city, state, ZIP")
    void testUSAAddressFullFormat() {
        Location location = new Location();
        location.setCoordinates("40.7128,-74.0060");
        location.setCountryCode("US");
        location.setCountryName("United States");
        location.setAddressLine1("123 Main Street");
        location.setAddressLine2("Apt 4B");
        location.setCity("New York");
        location.setState("NY");
        location.setPostalCode("10001");

        assertEquals("123 Main Street, New York", location.getDisplayName());
        assertEquals("123 Main Street", location.getShortName());
        assertEquals("123 Main Street, Apt 4B, New York, NY 10001", location.getFullName());
    }

    @Test
    @DisplayName("USA address - without apartment number")
    void testUSAAddressWithoutApt() {
        Location location = new Location();
        location.setCoordinates("34.0522,-118.2437");
        location.setCountryCode("US");
        location.setCountryName("United States");
        location.setAddressLine1("456 Sunset Boulevard");
        location.setCity("Los Angeles");
        location.setState("CA");
        location.setPostalCode("90028");

        assertEquals("456 Sunset Boulevard, Los Angeles", location.getDisplayName());
        assertEquals("456 Sunset Boulevard", location.getShortName());
        assertEquals("456 Sunset Boulevard, Los Angeles, CA 90028", location.getFullName());
    }

    // ========== UK Address Tests ==========

    @Test
    @DisplayName("UK address - full format with address lines, town, county, postcode")
    void testUKAddressFullFormat() {
        Location location = new Location();
        location.setCoordinates("51.5074,-0.1278");
        location.setCountryCode("GB");
        location.setCountryName("United Kingdom");
        location.setAddressLine1("10 Downing Street");
        location.setCity("London");
        location.setDistrict("Westminster");
        location.setPostalCode("SW1A 2AA");

        assertEquals("10 Downing Street, London", location.getDisplayName());
        assertEquals("10 Downing Street", location.getShortName());
        assertEquals("10 Downing Street, London, Westminster, SW1A 2AA", location.getFullName());
    }

    @Test
    @DisplayName("UK address - with address line 2")
    void testUKAddressWithLine2() {
        Location location = new Location();
        location.setCoordinates("51.5074,-0.1278");
        location.setCountryCode("GB");
        location.setCountryName("United Kingdom");
        location.setAddressLine1("221B Baker Street");
        location.setAddressLine2("Flat B");
        location.setCity("London");
        location.setPostalCode("NW1 6XE");

        assertEquals("221B Baker Street, London", location.getDisplayName());
        assertEquals("221B Baker Street", location.getShortName());
        assertEquals("221B Baker Street, Flat B, London, NW1 6XE", location.getFullName());
    }

    // ========== Canada Address Tests ==========

    @Test
    @DisplayName("Canada address - full format with address, city, province, postal code")
    void testCanadaAddressFullFormat() {
        Location location = new Location();
        location.setCoordinates("43.6532,-79.3832");
        location.setCountryCode("CA");
        location.setCountryName("Canada");
        location.setAddressLine1("123 Queen Street West");
        location.setCity("Toronto");
        location.setProvince("Ontario");
        location.setPostalCode("M5H 2M9");

        assertEquals("123 Queen Street West, Toronto", location.getDisplayName());
        assertEquals("123 Queen Street West", location.getShortName());
        assertEquals("123 Queen Street West, Toronto, Ontario M5H 2M9", location.getFullName());
    }

    @Test
    @DisplayName("Canada address - with suite number")
    void testCanadaAddressWithSuite() {
        Location location = new Location();
        location.setCoordinates("45.5017,-73.5673");
        location.setCountryCode("CA");
        location.setCountryName("Canada");
        location.setAddressLine1("789 Rue Saint-Jacques");
        location.setAddressLine2("Suite 200");
        location.setCity("Montreal");
        location.setProvince("Quebec");
        location.setPostalCode("H2Y 1L6");

        assertEquals("789 Rue Saint-Jacques, Montreal", location.getDisplayName());
        assertEquals("789 Rue Saint-Jacques", location.getShortName());
        assertEquals("789 Rue Saint-Jacques, Suite 200, Montreal, Quebec H2Y 1L6", location.getFullName());
    }

    // ========== Generic International Address Tests ==========

    @Test
    @DisplayName("Generic international address - Nigeria")
    void testNigeriaAddress() {
        Location location = new Location();
        location.setCoordinates("6.5244,3.3792");
        location.setCountryCode("NG");
        location.setCountryName("Nigeria");
        location.setAddressLine1("15 Marina Street");
        location.setCity("Lagos");
        location.setState("Lagos State");

        assertEquals("15 Marina Street, Lagos", location.getDisplayName());
        assertEquals("15 Marina Street", location.getShortName());
        assertEquals("15 Marina Street, Lagos, Lagos State, Nigeria", location.getFullName());
    }

    @Test
    @DisplayName("Generic international address - France")
    void testFranceAddress() {
        Location location = new Location();
        location.setCoordinates("48.8566,2.3522");
        location.setCountryCode("FR");
        location.setCountryName("France");
        location.setAddressLine1("5 Avenue Anatole");
        location.setCity("Paris");
        location.setPostalCode("75007");

        assertEquals("5 Avenue Anatole, Paris", location.getDisplayName());
        assertEquals("5 Avenue Anatole", location.getShortName());
        assertEquals("5 Avenue Anatole, Paris, France", location.getFullName());
    }

    // ========== Edge Cases ==========

    @Test
    @DisplayName("Default values - new Location should have Ghana defaults")
    void testDefaultValues() {
        Location location = new Location();
        location.setCoordinates("0.0,0.0");
        location.setCity("Test City");

        assertEquals("GH", location.getCountryCode());
        assertEquals("Ghana", location.getCountryName());
    }

    @Test
    @DisplayName("City only - international address without street")
    void testCityOnlyInternational() {
        Location location = new Location();
        location.setCoordinates("35.6762,139.6503");
        location.setCountryCode("JP");
        location.setCountryName("Japan");
        location.setCity("Tokyo");

        assertEquals("Tokyo", location.getDisplayName());
        assertEquals("Tokyo", location.getShortName());
        assertEquals("Tokyo, Japan", location.getFullName());
    }

    @Test
    @DisplayName("Address with all fields null except required")
    void testMinimalAddress() {
        Location location = new Location();
        location.setCoordinates("0.0,0.0");
        location.setCity("Minimal City");
        location.setCountryCode("XX");
        location.setCountryName("Test Country");

        assertEquals("Minimal City", location.getDisplayName());
        assertEquals("Minimal City", location.getShortName());
        assertEquals("Minimal City, Test Country", location.getFullName());
    }
}
