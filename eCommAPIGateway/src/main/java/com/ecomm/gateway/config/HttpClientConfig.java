package com.ecomm.gateway.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;

/**
 * HTTP Client Configuration for the API Gateway reverse proxy.
 * Downstream HTTP 4xx/5xx responses are passed back directly to the client.
 */
@Configuration
public class HttpClientConfig {

    private final GatewayProperties gatewayProperties;

    public HttpClientConfig(GatewayProperties gatewayProperties) {
        this.gatewayProperties = gatewayProperties;
    }

    @Bean
    public RestTemplate gatewayRestTemplate(RestTemplateBuilder builder) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(gatewayProperties.getHttp().getConnectTimeoutMs());
        factory.setReadTimeout(gatewayProperties.getHttp().getReadTimeoutMs());

        return builder
                .requestFactory(() -> factory)
                .setConnectTimeout(Duration.ofMillis(gatewayProperties.getHttp().getConnectTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(gatewayProperties.getHttp().getReadTimeoutMs()))
                .errorHandler(new PassThroughResponseErrorHandler())
                .build();
    }

    private static class PassThroughResponseErrorHandler implements ResponseErrorHandler {
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return false; // Do not treat 4xx/5xx as Java exceptions; let proxy forward status & body
        }

        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // No-op
        }
    }
}
