package com.api.app_location.integration.overpass;

import com.api.app_location.integration.overpass.dto.OverpassResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface OverpassClient {

    @PostExchange(
            url = "/api/interpreter",
            contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    OverpassResponse execute(@RequestParam("data") String query);
}
