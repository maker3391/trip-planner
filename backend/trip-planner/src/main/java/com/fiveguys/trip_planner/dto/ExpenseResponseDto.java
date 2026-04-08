package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Schema(description = "경비 지출 내역 응답 객체")
@Getter
@Builder
public class ExpenseResponseDto {

    @Schema(description = "지출 내역 ID", example = "1")
    private Long id;

    @Schema(description = "지출 금액", example = "15000")
    private BigDecimal amount;

    @Schema(description = "카테고리", example = "FOOD")
    private String category;

    @Schema(description = "지출 상세 내용", example = "해운대 국밥")
    private String description;
}
