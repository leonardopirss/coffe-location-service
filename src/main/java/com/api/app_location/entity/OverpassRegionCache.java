package com.api.app_location.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "OVERPASS_REGION_CACHE",
        schema = "COFFEWORK",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_overpass_region_cache",
                        columnNames = {"latitude_bucket", "longitude_bucket", "radius_meters"}
                )
        }
)
public class OverpassRegionCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "latitude_bucket", nullable = false)
    private Double latitudeBucket;

    @Column(name = "longitude_bucket", nullable = false)
    private Double longitudeBucket;

    @Column(name = "radius_meters", nullable = false)
    private Integer radiusMeters;

    @Column(name = "last_fetched_at", nullable = false)
    private LocalDateTime lastFetchedAt;
}
