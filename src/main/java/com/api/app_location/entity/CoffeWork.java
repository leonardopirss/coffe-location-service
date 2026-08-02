package com.api.app_location.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "ESTABLISHMENT",
        schema = "COFFEWORK",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_establishment_osm", columnNames = {"osm_type", "osm_id"})
        }
)
public class CoffeWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "adress")
    private String adress;

    @Column(name = "street")
    private String street;

    @Column(name = "address_number")
    private String addressNumber;

    @Column(name = "neighborhood")
    private String neighborhood;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "municipality")
    private String municipality;

    @Column(name = "uf")
    private String uf;

    @Column(name = "description")
    private String description;

    @Column(name = "assessment")
    private Integer assessment;

    @Column(name = "lng")
    private Double longitude;

    @Column(name = "lat")
    private Double latitude;

    @Column(name = "osm_type")
    private String osmType;

    @Column(name = "osm_id")
    private Long osmId;

    @Column(name = "internet_access")
    private String internetAccess;

    @Column(name = "opening_hours")
    private String openingHours;
}
