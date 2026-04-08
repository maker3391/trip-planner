package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
@Schema(description = "여행 일정 생성 요청 객체")
@Getter @Setter
@NoArgsConstructor
public class TripScheduleRequestDto {

    @Schema(description = "방문할 장소 ID (이미 등록된 장소일 경우)", example = "120")
    private Long placeId;

    @Schema(description = "일정 제목", example = "성산일출봉 방문")
    private String title;

    @Schema(description = "여행 몇 번째 날인지", example = "1")
    private Integer dayNumber;

    @Schema(description = "해당 날짜 내 방문 순서", example = "2")
    private Integer visitOrder;

    @Schema(description = "일정 시작 시간", example = "08:30")
    private LocalTime startTime;

    @Schema(description = "일정 종료 시간", example = "10:00")
    private LocalTime endTime;

    @Schema(description = "메모", example = "일출 시간에 맞춰 이동")
    private String memo;

    @Schema(description = "예상 체류 시간(분 단위)", example = "90")
    private Integer estimatedStayMinutes;

    @Schema(description = "방문 장소 이름(직접 입력 시)", example = "성산일출봉")
    private String placeName;

    @Schema(description = "방문 장소 주소", example = "제주특별자치도 서귀포시 성산읍 일출로 284-12")
    private String placeAddress;

    @Schema(description = "위도", example = "33.458675")
    private java.math.BigDecimal latitude;

    @Schema(description = "경도", example = "126.942168")
    private java.math.BigDecimal longitude;

    @Schema(description = "Google Place ID", example = "ChIJabcdEFGH1234")
    private String googlePlaceId;
}
