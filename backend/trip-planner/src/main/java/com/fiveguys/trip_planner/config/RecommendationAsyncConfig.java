package com.fiveguys.trip_planner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class RecommendationAsyncConfig {

    @Bean(name = "recommendationExecutor", destroyMethod = "shutdown")
    public ExecutorService recommendationExecutor() {
        return Executors.newFixedThreadPool(6);
    }
}