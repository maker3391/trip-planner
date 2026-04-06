package com.fiveguys.trip_planner.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TourApiProperties.class)
public class TourApiConfig {

    @Bean(name = "tourApiRestClient")
    public RestClient tourApiRestClient(TourApiProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.getBaseUrl() + properties.getServicePath())
                .build();
    }
}