package com.api.app_location.dto;

import lombok.*;

@Getter
@Setter
public class CoffeWorkDTO {

    private Integer id;

    private String name;

    private String adress;

    private String municipality;

    private String uf;

    private String description;

    private int assessment;

    private double latitude;

    private double longitude;
}
