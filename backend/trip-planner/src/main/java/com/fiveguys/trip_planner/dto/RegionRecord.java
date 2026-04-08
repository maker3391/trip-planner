package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "행정 구역 상세 정보 객체")
public class RegionRecord {

    @Schema(description = "지역 행정 코드", example = "1168010100")
    private final String code;

    @Schema(description = "지역 명칭 (단축)", example = "역삼동")
    private final String name;

    @Schema(description = "상위 행정 구역 명칭", example = "강남구")
    private final String parent;

    @Schema(description = "소속 도시/광역단체 명칭", example = "서울특별시")
    private final String city;

    @Schema(description = "행정 레벨 (PROVINCE, CITY, DISTRICT, DONG 등)", example = "DONG")
    private final String level;

    @Schema(description = "전체 지역 명칭", example = "서울특별시 강남구 역삼동")
    private final String fullName;

    @Schema(description = "데이터 출처 (예: 법정동, 행정동 등)", example = "BCODE")
    private final String source;

    public RegionRecord(String code, String name, String parent, String city,
                        String level, String fullName, String source) {
        this.code = code;
        this.name = name;
        this.parent = parent;
        this.city = city;
        this.level = level;
        this.fullName = fullName;
        this.source = source;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public String getCity() {
        return city;
    }

    public String getLevel() {
        return level;
    }

    public String getFullName() {
        return fullName;
    }

    public String getSource() {
        return source;
    }
}