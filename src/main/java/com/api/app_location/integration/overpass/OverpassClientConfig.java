package com.api.app_location.integration.overpass;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(OverpassProperties.class)
public class OverpassClientConfig {

    @Bean
    public RestClient overpassRestClient(OverpassProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.baseUrl().toString())
                .defaultHeader("User-Agent", "CoffeWork/1.0")
                .requestFactory(requestFactory(properties))
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw buildException(response);
                })
                .build();
    }

    @Bean
    public OverpassClient overpassClient(RestClient overpassRestClient) {
        RestClientAdapter adapter = RestClientAdapter.create(overpassRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(adapter)
                .build();

        return factory.createClient(OverpassClient.class);
    }

    private SimpleClientHttpRequestFactory requestFactory(OverpassProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());
        return requestFactory;
    }

    private OverpassApiException buildException(ClientHttpResponse response) throws IOException {
        String responseBody = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        return new OverpassApiException(response.getStatusCode().value(), responseBody);
    }
}
