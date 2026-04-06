package com.fiveguys.trip_planner.dto;

import java.util.ArrayList;
import java.util.List;

public class RecommendationDraft {

    private String intent;
    private String destination;
    private Integer days;
    private List<DayPlanDraft> dayPlans = new ArrayList<>();
    private List<RecommendationItemDraft> items = new ArrayList<>();

    public RecommendationDraft() {
    }

    public RecommendationDraft(String intent,
                               String destination,
                               Integer days,
                               List<DayPlanDraft> dayPlans,
                               List<RecommendationItemDraft> items) {
        this.intent = intent;
        this.destination = destination;
        this.days = days;
        this.dayPlans = dayPlans;
        this.items = items;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Integer getDays() {
        return days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }

    public List<DayPlanDraft> getDayPlans() {
        return dayPlans;
    }

    public void setDayPlans(List<DayPlanDraft> dayPlans) {
        this.dayPlans = dayPlans;
    }

    public List<RecommendationItemDraft> getItems() {
        return items;
    }

    public void setItems(List<RecommendationItemDraft> items) {
        this.items = items;
    }
}