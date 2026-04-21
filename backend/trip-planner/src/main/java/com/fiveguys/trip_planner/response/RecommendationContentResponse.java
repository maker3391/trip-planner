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

    @Schema(description = "프론트 표시용 추천 타입", example = "국밥")
    private String displayType;

    @Schema(description = "프론트 표시용 추천 제목", example = "부산 해운대구에서 가볼 만한 국밥집을 모아봤어요")
    private String displayTitle;

    public RecommendationContentResponse() {
    }

    public RecommendationContentResponse(List<DayPlanResponse> dayPlans,
                                         List<RecommendationItemResponse> items) {
        this.dayPlans = dayPlans;
        this.items = items;
    }

    public RecommendationContentResponse(List<DayPlanResponse> dayPlans,
                                         List<RecommendationItemResponse> items,
                                         String displayType,
                                         String displayTitle) {
        this.dayPlans = dayPlans;
        this.items = items;
        this.displayType = displayType;
        this.displayTitle = displayTitle;
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

    public String getDisplayType() {
        return displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }
}