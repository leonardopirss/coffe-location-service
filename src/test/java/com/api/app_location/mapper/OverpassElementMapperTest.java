package com.api.app_location.mapper;

import com.api.app_location.dto.CoffeWorkDTO;
import com.api.app_location.entity.CoffeWork;
import com.api.app_location.integration.overpass.dto.OverpassCenter;
import com.api.app_location.integration.overpass.dto.OverpassElement;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OverpassElementMapperTest {

    private final OverpassElementMapper mapper = Mappers.getMapper(OverpassElementMapper.class);

    @Test
    void shouldMapOverpassElementToEntityWithAddressFields() {
        OverpassElement element = new OverpassElement(
                "node",
                123456789L,
                -30.0346,
                -51.2177,
                null,
                Map.of(
                        "name", "Cafe Exemplo",
                        "addr:street", "Rua Exemplo",
                        "addr:housenumber", "123",
                        "addr:suburb", "Centro",
                        "addr:postcode", "90000-000",
                        "addr:city", "Porto Alegre",
                        "addr:state", "RS",
                        "internet_access", "wlan",
                        "opening_hours", "Mo-Fr 08:00-20:00"
                )
        );

        CoffeWork result = mapper.toEntity(element);

        assertThat(result.getId()).isNull();
        assertThat(result.getName()).isEqualTo("Cafe Exemplo");
        assertThat(result.getAdress()).isEqualTo("Rua Exemplo, nº 123, Centro, CEP 90000-000");
        assertThat(result.getStreet()).isEqualTo("Rua Exemplo");
        assertThat(result.getAddressNumber()).isEqualTo("123");
        assertThat(result.getNeighborhood()).isEqualTo("Centro");
        assertThat(result.getPostalCode()).isEqualTo("90000-000");
        assertThat(result.getMunicipality()).isEqualTo("Porto Alegre");
        assertThat(result.getUf()).isEqualTo("RS");
        assertThat(result.getLatitude()).isEqualTo(-30.0346);
        assertThat(result.getLongitude()).isEqualTo(-51.2177);
        assertThat(result.getOsmType()).isEqualTo("node");
        assertThat(result.getOsmId()).isEqualTo(123456789L);
        assertThat(result.getInternetAccess()).isEqualTo("wlan");
        assertThat(result.getOpeningHours()).isEqualTo("Mo-Fr 08:00-20:00");
    }

    @Test
    void shouldMapWayCenterCoordinatesToDto() {
        OverpassElement element = new OverpassElement(
                "way",
                987654321L,
                null,
                null,
                new OverpassCenter(-30.035, -51.218),
                Map.of(
                        "name", "Coworking Exemplo",
                        "addr:place", "Shopping Exemplo",
                        "addr:neighbourhood", "Moinhos"
                )
        );

        CoffeWorkDTO result = mapper.toDTO(element);

        assertThat(result.getName()).isEqualTo("Coworking Exemplo");
        assertThat(result.getAdress()).isEqualTo("Shopping Exemplo, Moinhos");
        assertThat(result.getStreet()).isEqualTo("Shopping Exemplo");
        assertThat(result.getNeighborhood()).isEqualTo("Moinhos");
        assertThat(result.getLatitude()).isEqualTo(-30.035);
        assertThat(result.getLongitude()).isEqualTo(-51.218);
        assertThat(result.getOsmType()).isEqualTo("way");
        assertThat(result.getOsmId()).isEqualTo(987654321L);
    }
}
