package com.reuben.pastcare_spring.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents a geographic location affected by a crisis.
 * Allows a crisis to affect multiple locations (e.g., multiple cities, suburbs, etc.)
 */
@Entity
@Table(name = "crisis_affected_location")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrisisAffectedLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crisis_id", nullable = false)
    private Crisis crisis;

    @Column(name = "suburb", length = 100)
    private String suburb;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "district", length = 100)
    private String district;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "country_code", length = 2)
    private String countryCode;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public CrisisAffectedLocation(Crisis crisis, String suburb, String city, String district, String region, String countryCode) {
        this.crisis = crisis;
        this.suburb = suburb;
        this.city = city;
        this.district = district;
        this.region = region;
        this.countryCode = countryCode;
    }
}
