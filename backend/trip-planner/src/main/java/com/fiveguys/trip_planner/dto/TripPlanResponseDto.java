package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.TripPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
//백 -> 프론트
@Schema(description = "여행 계획 응답 객체")
@Getter
public class TripPlanResponseDto {

    @Schema(description = "여행 계획 ID", example = "101")
    private final Long id;

    @Schema(description = "여행 계획 생성자 ID", example = "55")
    private final Long ownerId;

    @Schema(description = "여행 제목", example = "2025 제주도 봄 여행")
    private final String title;

    @Schema(description = "여행 지역", example = "제주도")
    private final String destination;

    @Schema(description = "여행 시작 날짜", example = "2025-04-15")
    private final LocalDate startDate;

    @Schema(description = "여행 종료 날짜", example = "2025-04-18")
    private final LocalDate endDate;

    @Schema(description = "여행 상태", example = "PLANNED")
    private final String status;

    @Schema(description = "여행 계획 생성 시각", example = "2025-01-20T14:32:10")
    private final LocalDateTime createdAt;

    @Schema(description = "여행 일정 목록")
    private List<TripScheduleResponseDto> schedules;

    @Schema(description = "예상 경비 목록")
    private List<ExpenseResponseDto> expenses;

    @Schema(description = "총 예산")
    private BigDecimal totalBudget;

    @Schema(description = "통화")
    private String currency;

    @Schema(description = "초대 코드", example = "a1b2c3d4")
    private final String inviteCode;

    public TripPlanResponseDto(TripPlan tripPlan) {
        this.id = tripPlan.getId();
        this.ownerId = tripPlan.getOwner().getId();
        this.title = tripPlan.getTitle();
        this.destination = tripPlan.getDestination();
        this.startDate = tripPlan.getStartDate();
        this.endDate = tripPlan.getEndDate();
        this.status = tripPlan.getStatus();
        this.createdAt = tripPlan.getCreatedAt();
        this.inviteCode = tripPlan.getInviteCode();

        if (tripPlan.getSchedules() != null) {
            this.schedules = tripPlan.getSchedules().stream()
                    .map(TripScheduleResponseDto::new)
                    .collect(Collectors.toList());
        }

        if (tripPlan.getExpenses() != null) {
            this.expenses = tripPlan.getExpenses().stream()
                    .map(ExpenseResponseDto::new)
                    .collect(Collectors.toList());
        }

        if (tripPlan.getBudget() != null) {
            this.totalBudget = tripPlan.getBudget().getTotalBudget();
            this.currency = tripPlan.getBudget().getCurrency();
        }
    }
}

