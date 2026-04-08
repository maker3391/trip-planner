package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 개별 추천 장소 응답 객체")
public class RecommendationItemResponse {

    @Schema(description = "추천 장소 이름", example = "해운대 소문난 암소갈비집")
    private String name;

    @Schema(description = "장소 지번/도로명 주소", example = "부산광역시 해운대구 중동2로10번길 32-10")
    private String address;

    @Schema(description = "상세 정보를 확인할 수 있는 외부 URL (구글 맵 등)", example = "https://maps.google.com/?cid=...")
    private String placeUrl;

    @Schema(description = "장소 카테고리 (예: 맛집, 카페, 관광지)", example = "맛집")
    private String category;

    public RecommendationItemResponse() {
    }

    public RecommendationItemResponse(String name) {
        this.name = name;
    }

    public RecommendationItemResponse(String name, String address, String placeUrl, String category) {
        this.name = name;
        this.address = address;
        this.placeUrl = placeUrl;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPlaceUrl() {
        return placeUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPlaceUrl(String placeUrl) {
        this.placeUrl = placeUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}