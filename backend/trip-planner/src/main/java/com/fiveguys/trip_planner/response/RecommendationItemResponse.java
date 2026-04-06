package com.fiveguys.trip_planner.response;

public class RecommendationItemResponse {

    private String name;


    public RecommendationItemResponse() {
    }

    public RecommendationItemResponse(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}