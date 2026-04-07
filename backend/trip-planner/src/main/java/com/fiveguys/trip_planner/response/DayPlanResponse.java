package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "일자별 추천 여행 계획 응답 객체")
public class DayPlanResponse {

    @Schema(description = "여행 일차 (n일차)", example = "1")
    private Integer day;

    @Schema(
            description = "해당 일차에 방문할 장소 명칭 리스트",
            example = "[\"해운대 해수욕장\", \"광안대교\", \"더베이101\"]"
    )
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