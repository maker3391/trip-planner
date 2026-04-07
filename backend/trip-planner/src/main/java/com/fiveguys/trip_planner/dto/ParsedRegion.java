package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지역(시·도/시·군·구) 정보 파싱 객체")
public class ParsedRegion {

    @Schema(description = "시·도(예: 서울특별시, 경기도)", example = "경기도")
    private final String province;

    @Schema(description = "시·군·구(예: 수원시, 강남구)", example = "수원시")
    private final String city;

    @Schema(description = "세부 지역(동/읍/면 등)", example = "영통구")
    private final String district;
    private final String neighborhood;

    @Schema(description = "지역 범위(예: PROVINCE, CITY, DISTRICT)", example = "CITY")
    private final RegionScope scope;

    @Schema(description = "표시용 목적지 문자열", example = "경기도 수원시 영통구")
    private final String displayDestination;

    public ParsedRegion(String province,
                        String city,
                        String district,
                        String neighborhood,
                        RegionScope scope,
                        String displayDestination) {
        this.province = province;
        this.city = city;
        this.district = district;
        this.neighborhood = neighborhood;
        this.scope = scope;
        this.displayDestination = displayDestination;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public RegionScope getScope() {
        return scope;
    }

    public String getDisplayDestination() {
        return displayDestination;
    }
}