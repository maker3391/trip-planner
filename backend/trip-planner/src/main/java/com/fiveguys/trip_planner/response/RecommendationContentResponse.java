package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "AI 추천 콘텐츠 통합 응답 객체 (일정 + 추천 항목)")
public class RecommendationContentResponse {

    @Schema(description = "일차별로 구성된 여행 일정 리스트")
    private List<DayPlanResponse> dayPlans = new ArrayList<>();

    @Schema(description = "AI가 엄선한 개별 추천 장소/아이템 리스트")
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