package com.fiveguys.trip_planner.service;


import com.fiveguys.trip_planner.dto.ExpenseRequestDto;
import com.fiveguys.trip_planner.dto.ExpenseResponseDto;
import com.fiveguys.trip_planner.response.ExpenseSummaryResponse;
import com.fiveguys.trip_planner.entity.*;
import com.fiveguys.trip_planner.repository.*;
import lombok.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetRepository budgetRepository;
    private final TripPlanRepository tripPlanRepository;

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

    @Transactional
    public List<ExpenseResponseDto> getExpensesByTripId(Long tripId) {
        List<Expense> expenses = expenseRepository.findByTripPlanId(tripId);
        return expenses.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseResponseDto createExpense(Long tripId, ExpenseRequestDto requestDto) {
        TripPlan tripPlan = tripPlanRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다."));

        Expense expense = Expense.builder()
                .tripPlan(tripPlan)
                .amount(requestDto.getAmount())
                .category(requestDto.getCategory())
                .description(requestDto.getDescription())
                .build();

        Expense savedExpense = expenseRepository.save(expense);
        return convertToDto(savedExpense);
    }

    @Transactional
    public ExpenseResponseDto updateExpense(Long expenseId, ExpenseRequestDto requestDto) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 경비 내역을 찾을 수 없습니다."));

        expense.update(
                requestDto.getAmount(),
                requestDto.getCategory(),
                requestDto.getDescription()
        );

        return convertToDto(expense);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 경비 내역을 찾을 수 없습니다."));
        expenseRepository.delete(expense);
    }

    private ExpenseResponseDto convertToDto(Expense expense) {
        return new ExpenseResponseDto(expense);
    }
}