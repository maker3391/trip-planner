package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.TripSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;


import java.math.BigDecimal;
import java.time.LocalTime;

@Schema(description = "여행 일정 응답 객체")
@Getter
@Builder
public class TripScheduleResponseDto {

    @Schema(description = "일정 ID", example = "3001")
    private final Long id;

    @Schema(description = "여행 내 몇 번째 날인지", example = "1")
    private final Integer dayNumber;

    @Schema(description = "일정 제목", example = "성산일출봉 등반")
    private final String title;

    @Schema(description = "해당 날짜 내 방문 순서", example = "2")
    private final Integer visitOrder;

    @Schema(description = "일정 시작 시간", example = "08:30")
    private final LocalTime startTime;

    @Schema(description = "일정 종료 시간", example = "10:00")
    private final LocalTime endTime;

    @Schema(description = "일정 메모", example = "일출 보기 좋은 시간대")
    private final String memo;

    @Schema(description = "예상 체류 시간(분 단위)", example = "90")
    private final Integer estimatedStayMinutes;

    @Schema(description = "장소 고유 ID", example = "10")
    private Long placeId;

    @Schema(description = "장소 이름", example = "성산일출봉")
    private String placeName;

    @Schema(description = "장소 주소", example = "제주특별자치도 서귀포시 성산읍 일출로 284-12")
    private String placeAddress;

    @Schema(description = "위도", example = "33.458675")
    private BigDecimal latitude;

    @Schema(description = "경도", example = "126.942168")
    private BigDecimal longitude;

    @Schema(description = "Google Place ID", example = "ChIJabcdEFGH1234")
    private String googlePlaceId;

    @Schema(description = "지도 핀 색상", example = "#FF5733")
    private final String pinColor;

    @Schema(description = "선택된 지도 핀 색상", example = "#FF0000")
    private final String selectedPinColor;

    @Schema(description = "지도 선 색상", example = "#33C1FF")
    private final String lineColor;

    public TripScheduleResponseDto(TripSchedule schedule) {
        this.id = schedule.getId();
        this.dayNumber = schedule.getDayNumber();
        this.title = schedule.getTitle();
        this.visitOrder = schedule.getVisitOrder();
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        this.memo = schedule.getMemo();
        this.estimatedStayMinutes = schedule.getEstimatedStayMinutes();
        this.pinColor = schedule.getPinColor();
        this.selectedPinColor = schedule.getSelectedPinColor();
        this.lineColor = schedule.getLineColor();

        if(schedule.getPlace() != null) {
            this.placeName = schedule.getPlace().getName();
            this.placeAddress = schedule.getPlace().getAddress();
            this.latitude = schedule.getPlace().getLatitude();
            this.longitude = schedule.getPlace().getLongitude();
            this.googlePlaceId = schedule.getPlace().getExternalPlaceId();
        }
    }

    public static TripScheduleResponseDto from(TripSchedule schedule) {
        return TripScheduleResponseDto.builder()
                .id(schedule.getId())
                .dayNumber(schedule.getDayNumber())
                .title(schedule.getTitle())
                .visitOrder(schedule.getVisitOrder())
                .pinColor(schedule.getPinColor())
                .selectedPinColor(schedule.getSelectedPinColor())
                .lineColor(schedule.getLineColor())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .memo(schedule.getMemo())
                .estimatedStayMinutes(schedule.getEstimatedStayMinutes())

                .placeId(schedule.getPlace() != null ? schedule.getPlace().getId() : null)
                .placeName(schedule.getPlace() != null ? schedule.getPlace().getName() : null)
                .placeAddress(schedule.getPlace() != null ? schedule.getPlace().getAddress() : null)
                .build();
    }
}
