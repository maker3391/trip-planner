package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지역 범위 구분(enum)")
public enum RegionScope {

    @Schema(description = "시·도 단위")
    PROVINCE,

    @Schema(description = "시·군·구 단위")
    CITY,

    @Schema(description = "동·읍·면 등 세부 지역 단위")
    DISTRICT,

    @Schema(description = "지역 범위를 확인할 수 없는 경우")
    UNKNOWN
}