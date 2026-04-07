package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "여행 추천 항목(명소/음식점/활동 등) 초안 객체")
public class RecommendationItemDraft {

    @Schema(description = "추천 항목 이름", example = "한라산 국립공원")
    private String name;

    public RecommendationItemDraft() {
    }

    public RecommendationItemDraft(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}