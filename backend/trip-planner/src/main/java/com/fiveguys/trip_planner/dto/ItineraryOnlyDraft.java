package com.fiveguys.trip_planner.dto;

import java.util.ArrayList;
import java.util.List;

public class ItineraryOnlyDraft {

    private List<DayPlanDraft> dayPlans = new ArrayList<>();

    public ItineraryOnlyDraft() {
    }

    public List<DayPlanDraft> getDayPlans() {
        return dayPlans;
    }

    public void setDayPlans(List<DayPlanDraft> dayPlans) {
        this.dayPlans = dayPlans;
    }
}