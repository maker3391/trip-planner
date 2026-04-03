package com.fiveguys.trip_planner.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(TourApiProperties.class)
public class TourApiClientConfig {
}