package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지역 코드 정보 객체")
public class RegionCodeInfo {

    @Schema(description = "지역명(도시/행정구역)", example = "서울특별시")
    private String destination;

    @Schema(description = "관광 API 지역 코드(areaCode)", example = "1")
    private String areaCode;

    @Schema(description = "관광 API 시군구 코드(sigunguCode)", example = "4")
    private String sigunguCode;

    public RegionCodeInfo() {
    }

    public RegionCodeInfo(String destination, String areaCode, String sigunguCode) {
        this.destination = destination;
        this.areaCode = areaCode;
        this.sigunguCode = sigunguCode;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getSigunguCode() {
        return sigunguCode;
    }

    public void setSigunguCode(String sigunguCode) {
        this.sigunguCode = sigunguCode;
    }
}