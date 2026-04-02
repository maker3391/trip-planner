package com.fiveguys.trip_planner.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiClientConfig {

    @Bean
    public RestClient openAiRestClient(OpenAiProperties openAiProperties) {
        return RestClient.builder()
                .baseUrl(openAiProperties.getBaseUrl())
                .build();
    }
}