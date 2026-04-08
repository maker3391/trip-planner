package com.fiveguys.trip_planner.dto;

public class ItineraryRequestContext {

    private String destination;
    private String detailArea;
    private Integer days;

    public ItineraryRequestContext() {
    }

    public ItineraryRequestContext(String destination, String detailArea, Integer days) {
        this.destination = destination;
        this.detailArea = detailArea;
        this.days = days;
    }

    public String getDestination() {
        return destination;
    }

    public String getDetailArea() {
        return detailArea;
    }

    public Integer getDays() {
        return days;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public void setDetailArea(String detailArea) {
        this.detailArea = detailArea;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
}