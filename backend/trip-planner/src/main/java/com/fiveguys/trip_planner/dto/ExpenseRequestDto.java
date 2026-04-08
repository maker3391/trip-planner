package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "경비 지출 내역 요청 객체")
@Getter
@NoArgsConstructor
public class ExpenseRequestDto {

    @Schema(description = "지출 금액", example = "150000")
    @NotNull(message = "금액은 필수입니다.")
    @Min(value = 0, message = "금액은 0원 이상이어야 합니다.")
    private BigDecimal amount;

    @Schema(description = "지출 카테고리(예 : 식비, 교통, 숙박, 기타)", example = "FOOD")
    @NotNull(message = "카테고리는 필수입니다.")
    private String category;

    @Schema(description = "지출 상세 내용", example = "해운대 국밥")
    private String description;
}
