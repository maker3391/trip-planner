package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ExpenseSummaryResponse;
import com.fiveguys.trip_planner.entity.*;
import com.fiveguys.trip_planner.repository.*;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;

    @Transactional(readOnly = true)
    public ExpenseSummaryResponse getBudgetAnalysis(Long tripPlanId) {
        List<Expense> expenses = expenseRepository.findByTripPlanId(tripPlanId);
        Budget budget = budgetRepository.findByTripPlanId(tripPlanId).orElse(null);

        return calculateAnalysis(expenses, budget);
    }

    public ExpenseSummaryResponse calculateAnalysis(List<Expense> expenses, Budget budget) {
        BigDecimal totalBudget = (budget != null) ? budget.getTotalBudget() : BigDecimal.ZERO;

        // 1. 타입별 합산 (Stream 활용)
        // expenseType이 "ESTIMATED"인 것들의 합
        BigDecimal totalPlanned = expenses.stream()
                .filter(e -> "ESTIMATED".equals(e.getExpenseType()))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // expenseType이 "ACTUAL"인 것들의 합
        BigDecimal totalActual = expenses.stream()
                .filter(e -> "ACTUAL".equals(e.getExpenseType()))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. 결과 계산
        BigDecimal remainingBudget = totalBudget.subtract(totalActual);
        BigDecimal planVsActualGap = totalPlanned.subtract(totalActual);

        // 3. 비율 계산
        double usagePercentage = 0;
        if (totalBudget.compareTo(BigDecimal.ZERO) > 0) {
            usagePercentage = totalActual.divide(totalBudget, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .doubleValue();
        }

        return ExpenseSummaryResponse.builder()
                .totalBudget(totalBudget)
                .totalPlannedAmount(totalPlanned)
                .totalActualAmount(totalActual)
                .remainingBudget(remainingBudget)
                .planVsActualGap(planVsActualGap)
                .budgetUsagePercentage(usagePercentage)
                .build();
    }
}