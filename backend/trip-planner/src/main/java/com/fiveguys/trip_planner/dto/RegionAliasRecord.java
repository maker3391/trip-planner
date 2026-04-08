package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지역 별칭 및 검색 최적화 정보 객체")
public class RegionAliasRecord {

    @Schema(description = "사용자가 입력할 수 있는 지역 별칭", example = "강남")
    private final String alias;

    @Schema(description = "매핑되는 실제 도시명", example = "서울특별시")
    private final String city;

    @Schema(description = "대상 행정 레벨 (CITY, DISTRICT 등)", example = "DISTRICT")
    private final String targetLevel;

    @Schema(description = "최종 변환될 지역 이름", example = "강남구")
    private final String targetName;

    @Schema(description = "상위 지역(시, 도) 이름", example = "서울특별시")
    private final String targetParent;

    @Schema(description = "검색 정확도를 높이기 위한 쿼리 힌트", example = "서울 강남구")
    private final String queryHint;

    @Schema(description = "별칭 매핑 우선순위 (낮을수록 높음)", example = "1")
    private final int priority;

    public RegionAliasRecord(String alias,
                             String city,
                             String targetLevel,
                             String targetName,
                             String targetParent,
                             String queryHint,
                             int priority) {
        this.alias = alias;
        this.city = city;
        this.targetLevel = targetLevel;
        this.targetName = targetName;
        this.targetParent = targetParent;
        this.queryHint = queryHint;
        this.priority = priority;
    }

    public String getAlias() {
        return alias;
    }

    public String getCity() {
        return city;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public String getTargetName() {
        return targetName;
    }

    public String getTargetParent() {
        return targetParent;
    }

    public String getQueryHint() {
        return queryHint;
    }

    public int getPriority() {
        return priority;
    }
}