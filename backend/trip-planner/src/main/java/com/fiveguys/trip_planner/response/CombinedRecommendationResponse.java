package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "복합 추천 응답")
public class CombinedRecommendationResponse {

    @Schema(description = "여행 일정 추천 결과")
    private RecommendationContentResponse itinerary;

    @Schema(description = "맛집 추천 결과")
    private List<RecommendationItemResponse> restaurants = new ArrayList<>();

    @Schema(description = "숙소 추천 결과")
    private List<RecommendationItemResponse> stays = new ArrayList<>();

    public CombinedRecommendationResponse() {
    }

    public CombinedRecommendationResponse(RecommendationContentResponse itinerary,
                                          List<RecommendationItemResponse> restaurants,
                                          List<RecommendationItemResponse> stays) {
        this.itinerary = itinerary;
        this.restaurants = restaurants;
        this.stays = stays;
    }

    public RecommendationContentResponse getItinerary() {
        return itinerary;
    }

    public void setItinerary(RecommendationContentResponse itinerary) {
        this.itinerary = itinerary;
    }

    public List<RecommendationItemResponse> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(List<RecommendationItemResponse> restaurants) {
        this.restaurants = restaurants;
    }

    public List<RecommendationItemResponse> getStays() {
        return stays;
    }

    public void setStays(List<RecommendationItemResponse> stays) {
        this.stays = stays;
    }
}