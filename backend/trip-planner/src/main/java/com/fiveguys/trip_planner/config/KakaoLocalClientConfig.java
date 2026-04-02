package com.fiveguys.trip_planner.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(KakaoLocalProperties.class)
public class KakaoLocalClientConfig {

    @Bean
    public RestClient kakaoLocalRestClient(KakaoLocalProperties kakaoLocalProperties) {
        return RestClient.builder()
                .baseUrl(kakaoLocalProperties.getBaseUrl())
                .build();
    }
}