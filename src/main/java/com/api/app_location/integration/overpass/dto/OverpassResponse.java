package com.api.app_location.integration.overpass.dto;

import java.util.List;

public record OverpassResponse(
        Double version,
        String generator,
        OverpassMetadata osm3s,
        List<OverpassElement> elements
) {
}
