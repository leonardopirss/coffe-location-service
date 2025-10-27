package com.api.app_location.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "ESTABLISHMENT", schema = "COFFEWORK")
public class CoffeWork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "adress")
    private String adress;

    @Column(name = "municipality")
    private String municipality;

    @Column(name = "uf")
    private String uf;

    @Column(name = "description")
    private String description;

    @Column(name = "assessment")
    private Integer assessment;

    @Column(name = "lng")
    private double longitude;

    @Column(name = "lat")
    private double latitude;
}
