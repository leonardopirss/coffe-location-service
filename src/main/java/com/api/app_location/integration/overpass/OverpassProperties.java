package com.api.app_location.integration.overpass;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "integration.overpass")
public record OverpassProperties(
        URI baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        Duration cacheTtl
) {

    public OverpassProperties {
        if (baseUrl == null) {
            baseUrl = URI.create("https://overpass-api.de");
        }

        if (connectTimeout == null) {
            connectTimeout = Duration.ofSeconds(5);
        }

        if (readTimeout == null) {
            readTimeout = Duration.ofSeconds(30);
        }

        if (cacheTtl == null) {
            cacheTtl = Duration.ofDays(7);
        }
    }
}
