package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.Place;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;


import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "장소 정보 응답 객체")
@Getter
@NoArgsConstructor
public class PlaceResponseDto {

    @Schema(description = "장소 고유 ID", example = "101")
    private Long id;

    @Schema(description = "장소 이름", example = "에버랜드")
    private String name;

    @Schema(description = "장소 주소", example = "경기도 용인시 처인구 포곡읍 에버랜드로 199")
    private String address;

    @Schema(description = "위도", example = "37.295047")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "127.202887")
    private BigDecimal longitude;

    @Schema(description = "카테고리", example = "theme_park")
    private String category;

    @Schema(description = "외부 제공업체 장소 ID", example = "ChIJabcdEFGH1234")
    private String externalPlaceId;

    @Schema(description = "장소 상세 페이지 URL", example = "https://maps.google.com/?q=에버랜드")
    private String placeUrl;

    @Schema(description = "생성 일시", example = "2025-02-02T15:30:45")
    private LocalDateTime createdAt;

    public PlaceResponseDto(Place place) {
        this.id = place.getId();
        this.name = place.getName();
        this.address = place.getAddress();
        this.latitude = place.getLatitude();
        this.longitude = place.getLongitude();
        this.category = place.getCategory();
        this.externalPlaceId = place.getExternalPlaceId();
        this.placeUrl = place.getPlaceUrl();
        this.createdAt = place.getCreatedAt();
    }
}
