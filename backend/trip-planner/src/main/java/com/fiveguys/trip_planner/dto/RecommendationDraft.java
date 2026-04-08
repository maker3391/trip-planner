package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "여행 추천 초안 정보 객체")
public class RecommendationDraft {

    @Schema(description = "여행 목적 또는 의도", example = "가족 여행")
    private String intent;

    @Schema(description = "주요 여행지(도시/지역명)", example = "제주도")
    private String destination;

    @Schema(description = "세부 지역 정보", example = "서귀포시")
    private String detailArea;

    @Schema(description = "총 여행 일수", example = "3")
    private Integer days;

    @Schema(description = "일자별 여행 계획 목록")
    private List<DayPlanDraft> dayPlans = new ArrayList<>();

    @Schema(description = "추천 여행 요소(명소/음식점/활동 등) 목록")
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

    public RecommendationDraft(String intent,
                               String destination,
                               String detailArea,
                               Integer days,
                               List<DayPlanDraft> dayPlans,
                               List<RecommendationItemDraft> items) {
        this.intent = intent;
        this.destination = destination;
        this.detailArea = detailArea;
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

    public String getDetailArea() {
        return detailArea;
    }

    public void setDetailArea(String detailArea) {
        this.detailArea = detailArea;
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