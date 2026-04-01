
package com.fiveguys.trip_planner.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExpenseSummaryResponse {
    private BigDecimal totalBudget;         // 설정된 총 예산

    private BigDecimal totalPlannedAmount;  // 예산 단계에서의 총 예상 지출액
    private BigDecimal totalActualAmount;   // 현재까지의 총 실제 지출액

    private BigDecimal remainingBudget;     // 실제 예산 대비 남은 금액 (Budget - Actual)
    private BigDecimal planVsActualGap;     // 예상 대비 실제 지출 차이 (Planned - Actual)

    private double budgetUsagePercentage;   // 총 예산 대비 실제 지출 비율 (%)
}