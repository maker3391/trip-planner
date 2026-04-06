package com.fiveguys.trip_planner.response;

import java.util.ArrayList;
import java.util.List;

public class RecommendationContentResponse {

    private List<DayPlanResponse> dayPlans = new ArrayList<>();
    private List<RecommendationItemResponse> items = new ArrayList<>();

    public RecommendationContentResponse() {
    }

    public RecommendationContentResponse(List<DayPlanResponse> dayPlans,
                                         List<RecommendationItemResponse> items) {
        this.dayPlans = dayPlans;
        this.items = items;
    }

    public List<DayPlanResponse> getDayPlans() {
        return dayPlans;
    }

    public void setDayPlans(List<DayPlanResponse> dayPlans) {
        this.dayPlans = dayPlans;
    }

    public List<RecommendationItemResponse> getItems() {
        return items;
    }

    public void setItems(List<RecommendationItemResponse> items) {
        this.items = items;
    }
}