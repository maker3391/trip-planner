package com.fiveguys.trip_planner.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GooglePlaceConfig {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
