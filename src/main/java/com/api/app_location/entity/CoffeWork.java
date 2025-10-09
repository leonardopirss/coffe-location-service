package com.api.app_location.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "ESTABLISHMENT", schema = "COFFEWORK")
public class CoffeWork {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "establishment_seq")
    @SequenceGenerator(name = "establishment_seq", sequenceName = "coffework.idCoffe", allocationSize = 1)
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

}
