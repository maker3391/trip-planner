package com.fiveguys.trip_planner.response;

import com.fiveguys.trip_planner.dto.KakaoPlaceDto;

import java.util.List;

public class DayCourseResponse {

    private int dayNumber;
    private List<KakaoPlaceDto> places;

    public DayCourseResponse() {
    }

    public DayCourseResponse(int dayNumber, List<KakaoPlaceDto> places) {
        this.dayNumber = dayNumber;
        this.places = places;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public List<KakaoPlaceDto> getPlaces() {
        return places;
    }

    public void setPlaces(List<KakaoPlaceDto> places) {
        this.places = places;
    }
}