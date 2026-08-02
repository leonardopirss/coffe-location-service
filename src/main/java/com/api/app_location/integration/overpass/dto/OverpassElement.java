package com.api.app_location.integration.overpass.dto;

import java.util.Map;

public record OverpassElement(
        String type,
        Long id,
        Double lat,
        Double lon,
        OverpassCenter center,
        Map<String, String> tags
) {

    public Double resolvedLatitude() {
        if ("node".equals(type)) {
            return lat;
        }

        return center != null ? center.lat() : null;
    }

    public Double resolvedLongitude() {
        if ("node".equals(type)) {
            return lon;
        }

        return center != null ? center.lon() : null;
    }
}
