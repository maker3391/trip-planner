package com.fiveguys.trip_planner.response;

import com.fiveguys.trip_planner.dto.RecommendedPlaceDto;

import java.util.List;

public class DayCourseResponse {

    private int dayNumber;
    private List<RecommendedPlaceDto> places;

    public DayCourseResponse() {
    }

    public DayCourseResponse(int dayNumber, List<RecommendedPlaceDto> places) {
        this.dayNumber = dayNumber;
        this.places = places;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public List<RecommendedPlaceDto> getPlaces() {
        return places;
    }

    public void setPlaces(List<RecommendedPlaceDto> places) {
        this.places = places;
    }
}