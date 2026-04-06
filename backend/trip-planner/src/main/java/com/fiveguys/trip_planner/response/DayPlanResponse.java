package com.fiveguys.trip_planner.response;

import java.util.ArrayList;
import java.util.List;

public class DayPlanResponse {

    private Integer day;
    private List<String> places = new ArrayList<>();

    public DayPlanResponse() {
    }

    public DayPlanResponse(Integer day, String summary, List<String> places) {
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