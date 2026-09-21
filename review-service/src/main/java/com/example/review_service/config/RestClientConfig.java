package com.example.review_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient movieRestClient(
            @Value("${movie-service.base-url}") String movieServiceBaseUrl) {

        return RestClient.builder()
                .baseUrl(movieServiceBaseUrl)
                .build();
    }
}