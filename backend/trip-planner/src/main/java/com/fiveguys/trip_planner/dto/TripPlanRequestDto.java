package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
//프론트 -> 백
@Schema(description = "여행 계획 생성 요청 객체")
@Getter @Setter
@NoArgsConstructor
public class TripPlanRequestDto {

    @Schema(description = "여행 제목", example = "2025 제주도 봄 여행")
    private String title;

    @Schema(description = "여행 지역", example = "제주도")
    private String destination;

    @Schema(description = "여행 시작 날짜", example = "2025-04-15")
    private LocalDate startDate;

    @Schema(description = "여행 종료 날짜", example = "2025-04-18")
    private LocalDate endDate;

    @Schema(description = "여행 상태 (예: PLANNED, IN_PROGRESS, COMPLETED)", example = "PLANNED")
    private String status;

    @Schema(description = "일정 목록")
    private List<TripScheduleRequestDto> schedules;

    @Schema(description = "예상 경비 목록")
    private List<ExpenseRequestDto> expenses;

    @Schema(description = "총 예산", example = "500000")
    private BigDecimal totalBudget;

    @Schema(description = "통화", example = "원")
    private String currency = "원";

    @Schema(description = "최대 참여 인원", example = "5")
    private Integer maxMembers;
}
