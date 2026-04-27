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

    @Schema(description = "명소/관광지 추천 결과")
    private List<RecommendationItemResponse> attractions = new ArrayList<>();

    @Schema(description = "복합 추천에서 맛집 카드 제목")
    private String restaurantDisplayTitle;

    @Schema(description = "복합 추천에서 숙소 카드 제목")
    private String stayDisplayTitle;

    @Schema(description = "복합 추천에서 명소/관광지 카드 제목")
    private String attractionDisplayTitle;

    public CombinedRecommendationResponse() {
    }

    public CombinedRecommendationResponse(RecommendationContentResponse itinerary,
                                          List<RecommendationItemResponse> restaurants,
                                          List<RecommendationItemResponse> stays,
                                          List<RecommendationItemResponse> attractions,
                                          String restaurantDisplayTitle,
                                          String stayDisplayTitle,
                                          String attractionDisplayTitle) {
        this.itinerary = itinerary;
        this.restaurants = restaurants;
        this.stays = stays;
        this.attractions = attractions;
        this.restaurantDisplayTitle = restaurantDisplayTitle;
        this.stayDisplayTitle = stayDisplayTitle;
        this.attractionDisplayTitle = attractionDisplayTitle;
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

    public List<RecommendationItemResponse> getAttractions() {
        return attractions;
    }

    public void setAttractions(List<RecommendationItemResponse> attractions) {
        this.attractions = attractions;
    }

    public String getRestaurantDisplayTitle() {
        return restaurantDisplayTitle;
    }

    public void setRestaurantDisplayTitle(String restaurantDisplayTitle) {
        this.restaurantDisplayTitle = restaurantDisplayTitle;
    }

    public String getStayDisplayTitle() {
        return stayDisplayTitle;
    }

    public void setStayDisplayTitle(String stayDisplayTitle) {
        this.stayDisplayTitle = stayDisplayTitle;
    }

    public String getAttractionDisplayTitle() {
        return attractionDisplayTitle;
    }

    public void setAttractionDisplayTitle(String attractionDisplayTitle) {
        this.attractionDisplayTitle = attractionDisplayTitle;
    }
}