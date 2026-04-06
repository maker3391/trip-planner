package com.fiveguys.trip_planner.dto;

import java.util.ArrayList;
import java.util.List;

public class DayPlanDraft {

    private Integer day;
    private List<String> places = new ArrayList<>();

    public DayPlanDraft() {
    }

    public DayPlanDraft(Integer day, List<String> places) {
        this.day = day;
        this.places = places;
    }

    public Integer getDay() {
        return day;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public List<String> getPlaces() {
        return places;
    }

    public void setPlaces(List<String> places) {
        this.places = places;
    }
}