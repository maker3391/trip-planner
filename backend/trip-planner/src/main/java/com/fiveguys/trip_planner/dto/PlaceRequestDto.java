package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Schema(description = "장소 생성 요청 객체")
@Getter @Setter
@NoArgsConstructor
public class PlaceRequestDto {

    @Schema(description = "장소 이름", example = "에버랜드")
    private String name;

    @Schema(description = "장소 주소", example = "경기도 용인시 처인구 포곡읍 에버랜드로 199")
    private String address;

    @Schema(description = "위도", example = "37.295047")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "127.202887")
    private BigDecimal longitude;

    @Schema(description = "장소 카테고리", example = "theme_park")
    private String category;

    @Schema(description = "외부 제공업체의 장소 ID", example = "ChIJ1234abcdXYZ")
    private String externalPlaceId;

    @Schema(description = "장소 상세 URL", example = "https://maps.google.com/?q=에버랜드")
    private String placeUrl;
}
