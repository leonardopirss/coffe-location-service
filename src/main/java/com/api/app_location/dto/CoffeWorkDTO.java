package com.api.app_location.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class CoffeWorkDTO {

    private Integer id;

    private String name;

    private String adress;

    private String street;

    private String addressNumber;

    private String neighborhood;

    private String postalCode;

    private String municipality;

    private String uf;

    private String description;

    private int assessment;

    private Double latitude;

    private Double longitude;

    private String osmType;

    private Long osmId;

    private String internetAccess;

    private String openingHours;
}
