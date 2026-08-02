package com.api.app_location.integration.overpass.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OverpassMetadata(
        @JsonProperty("timestamp_osm_base")
        String timestampOsmBase,
        String copyright
) {
}
