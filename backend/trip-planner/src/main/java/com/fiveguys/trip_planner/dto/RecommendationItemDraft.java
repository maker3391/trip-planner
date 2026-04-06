package com.fiveguys.trip_planner.dto;

public class RecommendationItemDraft {

    private String name;

    public RecommendationItemDraft() {
    }

    public RecommendationItemDraft(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}