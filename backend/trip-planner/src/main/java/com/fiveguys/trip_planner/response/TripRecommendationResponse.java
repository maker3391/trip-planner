package com.fiveguys.trip_planner.response;

import java.util.List;

public class TripRecommendationResponse {

    private String destination;
    private int days;
    private int totalPlaces;
    private List<DayCourseResponse> dayCourses;

    public TripRecommendationResponse() {
    }

    public TripRecommendationResponse(String destination, int days, int totalPlaces, List<DayCourseResponse> dayCourses) {
        this.destination = destination;
        this.days = days;
        this.totalPlaces = totalPlaces;
        this.dayCourses = dayCourses;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public int getTotalPlaces() {
        return totalPlaces;
    }

    public void setTotalPlaces(int totalPlaces) {
        this.totalPlaces = totalPlaces;
    }

    public List<DayCourseResponse> getDayCourses() {
        return dayCourses;
    }

    public void setDayCourses(List<DayCourseResponse> dayCourses) {
        this.dayCourses = dayCourses;
    }
}