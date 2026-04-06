package com.fiveguys.trip_planner.response;

public class RecommendationItemResponse {

    private String name;
    private String area;
    private String reason;

    public RecommendationItemResponse() {
    }

    public RecommendationItemResponse(String name, String area, String reason) {
        this.name = name;
        this.area = area;
        this.reason = reason;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}