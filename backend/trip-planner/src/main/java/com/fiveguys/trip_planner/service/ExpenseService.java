package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ExpenseRequestDto;
import com.fiveguys.trip_planner.dto.ExpenseResponseDto;
import com.fiveguys.trip_planner.response.ExpenseSummaryResponse;
import com.fiveguys.trip_planner.entity.*;
import com.fiveguys.trip_planner.repository.*;
import lombok.RequiredArgsConstructor;
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

    /**
     * 통계 분석 로직: 하위 항목이 상위에 합산된 구조이므로,
     * 중복 합산을 방지하기 위해 parent가 null인(최상위) 항목들만 계산에 사용합니다.
     */
    public ExpenseSummaryResponse calculateAnalysis(List<Expense> expenses, Budget budget) {
        BigDecimal totalBudget = (budget != null) ? budget.getTotalBudget() : BigDecimal.ZERO;

        // 최상위 항목만 필터링 (하위 항목 금액은 이미 상위에 합산되어 있음)
        List<Expense> mainExpenses = expenses.stream()
                .filter(e -> e.getParent() == null)
                .collect(Collectors.toList());

        BigDecimal totalPlanned = mainExpenses.stream()
                .filter(e -> "ESTIMATED".equals(e.getExpenseType()))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalActual = mainExpenses.stream()
                .filter(e -> "ACTUAL".equals(e.getExpenseType()))
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingBudget = totalBudget.subtract(totalActual);
        BigDecimal planVsActualGap = totalPlanned.subtract(totalActual);

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

    @Transactional(readOnly = true)
    public List<ExpenseResponseDto> getExpensesByTripId(Long tripId) {
        // 모든 리스트를 가져온 뒤, 최상위 부모 항목만 DTO로 변환합니다.
        // DTO 생성자 내부에서 자식들을 재귀적으로 변환하여 트리 구조를 형성합니다.
        List<Expense> expenses = expenseRepository.findByTripPlanId(tripId);
        return expenses.stream()
                .filter(e -> e.getParent() == null)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public ExpenseResponseDto createExpense(Long tripId, ExpenseRequestDto requestDto) {
        TripPlan tripPlan = tripPlanRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다."));

        // 1. 상위 항목 생성
        Expense parentExpense = Expense.builder()
                .tripPlan(tripPlan)
                .amount(requestDto.getAmount())
                .category(requestDto.getCategory())
                .description(requestDto.getDescription())
                .expenseType(requestDto.getExpenseType() != null ? requestDto.getExpenseType() : "ACTUAL")
                .build();

        // 2. 하위 항목들이 있다면 부모에 연결 (CascadeType.ALL에 의해 함께 저장됨)
        if (requestDto.getSubExpenses() != null) {
            for (ExpenseRequestDto subDto : requestDto.getSubExpenses()) {
                Expense subExpense = Expense.builder()
                        .tripPlan(tripPlan)
                        .amount(subDto.getAmount())
                        .category(subDto.getCategory())
                        .description(subDto.getDescription())
                        .expenseType(subDto.getExpenseType() != null ? subDto.getExpenseType() : "ACTUAL")
                        .build();
                parentExpense.addSubExpense(subExpense);
            }
        }

        Expense savedExpense = expenseRepository.save(parentExpense);
        return convertToDto(savedExpense);
    }

    @Transactional
    public ExpenseResponseDto updateExpense(Long expenseId, ExpenseRequestDto requestDto) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 경비 내역을 찾을 수 없습니다."));

        // 1. 상위 정보 업데이트
        expense.update(
                requestDto.getAmount(),
                requestDto.getCategory(),
                requestDto.getDescription()
        );

        // 2. 기존 하위 항목들 교체 (orphanRemoval = true 설정으로 인해 삭제 처리됨)
        expense.getSubExpenses().clear();

        if (requestDto.getSubExpenses() != null) {
            for (ExpenseRequestDto subDto : requestDto.getSubExpenses()) {
                Expense subExpense = Expense.builder()
                        .tripPlan(expense.getTripPlan())
                        .amount(subDto.getAmount())
                        .category(subDto.getCategory())
                        .description(subDto.getDescription())
                        .expenseType(subDto.getExpenseType() != null ? subDto.getExpenseType() : "ACTUAL")
                        .build();
                expense.addSubExpense(subExpense);
            }
        }

        return convertToDto(expense);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new IllegalArgumentException("해당 경비 내역을 찾을 수 없습니다."));
        // CascadeType.ALL에 의해 하위 항목도 자동 삭제
        expenseRepository.delete(expense);
    }

    private ExpenseResponseDto convertToDto(Expense expense) {
        return new ExpenseResponseDto(expense);
    }
}