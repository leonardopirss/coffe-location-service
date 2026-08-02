package com.api.app_location.integration.overpass.dto;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OverpassElementTest {

    @Test
    void shouldResolveCoordinatesFromNodeLatitudeAndLongitude() {
        OverpassElement element = new OverpassElement(
                "node",
                123L,
                -30.0346,
                -51.2177,
                null,
                Map.of()
        );

        assertThat(element.resolvedLatitude()).isEqualTo(-30.0346);
        assertThat(element.resolvedLongitude()).isEqualTo(-51.2177);
    }

    @Test
    void shouldResolveCoordinatesFromCenterForWay() {
        OverpassElement element = new OverpassElement(
                "way",
                456L,
                null,
                null,
                new OverpassCenter(-30.035, -51.218),
                Map.of()
        );

        assertThat(element.resolvedLatitude()).isEqualTo(-30.035);
        assertThat(element.resolvedLongitude()).isEqualTo(-51.218);
    }

    @Test
    void shouldReturnNullWhenCoordinatesAreUnavailable() {
        OverpassElement element = new OverpassElement(
                "relation",
                789L,
                null,
                null,
                null,
                Map.of()
        );

        assertThat(element.resolvedLatitude()).isNull();
        assertThat(element.resolvedLongitude()).isNull();
    }
}
