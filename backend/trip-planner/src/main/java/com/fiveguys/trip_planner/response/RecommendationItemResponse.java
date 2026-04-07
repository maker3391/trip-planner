package com.fiveguys.trip_planner.response;

public class RecommendationItemResponse {

    private String name;
    private String address;
    private String placeUrl;
    private String category;

    public RecommendationItemResponse() {
    }

    public RecommendationItemResponse(String name) {
        this.name = name;
    }

    public RecommendationItemResponse(String name, String address, String placeUrl, String category) {
        this.name = name;
        this.address = address;
        this.placeUrl = placeUrl;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPlaceUrl() {
        return placeUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPlaceUrl(String placeUrl) {
        this.placeUrl = placeUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}