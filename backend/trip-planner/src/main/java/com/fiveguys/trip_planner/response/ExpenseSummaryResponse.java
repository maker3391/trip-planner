
package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Schema(description = "여행 경비 요약 응답 객체 (대시보드용)")
@Getter
@Builder
public class ExpenseSummaryResponse {

    @Schema(description = "설정된 총 예산", example = "1000000")
    private BigDecimal totalBudget;         // 설정된 총 예산

    @Schema(description = "계획 단계에서의 총 예상 지출액 (계획한 장소들의 합계)", example = "850000")
    private BigDecimal totalPlannedAmount;  // 예산 단계에서의 총 예상 지출액

    @Schema(description = "현재까지 실제로 지출한 총 금액", example = "450000")
    private BigDecimal totalActualAmount;   // 현재까지의 총 실제 지출액

    @Schema(description = "예산 대비 남은 금액 (TotalBudget - TotalActual)", example = "550000")
    private BigDecimal remainingBudget;     // 실제 예산 대비 남은 금액 (Budget - Actual)

    @Schema(description = "계획 대비 실제 지출 차이 (Planned - Actual)", example = "400000")
    private BigDecimal planVsActualGap;     // 예상 대비 실제 지출 차이 (Planned - Actual)

    @Schema(description = "총 예산 대비 실제 지출 비율 (%)", example = "45.0")
    private double budgetUsagePercentage;   // 총 예산 대비 실제 지출 비율 (%)
}