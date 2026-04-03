package com.fiveguys.trip_planner.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "tour.api")
public class TourApiProperties {

    private String hubBaseUrl;
    private String relatedBaseUrl;
    private String serviceKey;
    private String mobileOs;
    private String mobileApp;

    public String getHubBaseUrl() {
        return hubBaseUrl;
    }

    public void setHubBaseUrl(String hubBaseUrl) {
        this.hubBaseUrl = hubBaseUrl;
    }

    public String getRelatedBaseUrl() {
        return relatedBaseUrl;
    }

    public void setRelatedBaseUrl(String relatedBaseUrl) {
        this.relatedBaseUrl = relatedBaseUrl;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    public String getMobileOs() {
        return mobileOs;
    }

    public void setMobileOs(String mobileOs) {
        this.mobileOs = mobileOs;
    }

    public String getMobileApp() {
        return mobileApp;
    }

    public void setMobileApp(String mobileApp) {
        this.mobileApp = mobileApp;
    }
}