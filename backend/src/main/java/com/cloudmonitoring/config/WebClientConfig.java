package com.cloudmonitoring.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebClient configuration for non-blocking communication with Prometheus and OmniRoute AI.
 */
@Configuration
public class WebClientConfig {

    @Value("${prometheus.base-url:http://localhost:9090}")
    private String prometheusBaseUrl;

    @Value("${omniroute.base-url:https://api.omniroute.ai/v1}")
    private String omniRouteBaseUrl;

    @Value("${omniroute.api-key:}")
    private String omniRouteApiKey;

    @Bean("prometheusWebClient")
    public WebClient prometheusWebClient() {
        return WebClient.builder()
                .baseUrl(prometheusBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean("omniRouteWebClient")
    public WebClient omniRouteWebClient() {
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(omniRouteBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        if (omniRouteApiKey != null && !omniRouteApiKey.isBlank()) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + omniRouteApiKey);
        }

        return builder.build();
    }
}