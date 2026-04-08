package com.fiveguys.trip_planner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao.local")
public class KakaoLocalProperties {

    private String baseUrl;
    private String restApiKey;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getRestApiKey() {
        return restApiKey;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setRestApiKey(String restApiKey) {
        this.restApiKey = restApiKey;
    }
}