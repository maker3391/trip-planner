package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.Expense;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "경비 지출 내역 응답 객체")
@Getter
@NoArgsConstructor
public class ExpenseResponseDto {

    @Schema(description = "지출 내역 ID", example = "1")
    private Long id;

    @Schema(description = "지출 금액", example = "15000")
    private BigDecimal amount;

    @Schema(description = "카테고리", example = "FOOD")
    private String category;

    @Schema(description = "지출 상세 내용", example = "해운대 국밥")
    private String description;

    @Schema(description = "지출 타입", example = "ACTUAL")
    private String expenseType;

    // --- 핵심 추가 부분: 하위 지출 내역 응답 리스트 ---
    @Schema(description = "하위 상세 지출 내역 리스트")
    private List<ExpenseResponseDto> subExpenses;

    public ExpenseResponseDto(Expense expense) {
        this.id = expense.getId();
        this.amount = expense.getAmount();
        this.category = expense.getCategory();
        this.description = expense.getDescription();
        this.expenseType = expense.getExpenseType();

        // 엔티티에 저장된 subExpenses 리스트를 DTO 리스트로 변환
        if (expense.getSubExpenses() != null) {
            this.subExpenses = expense.getSubExpenses().stream()
                    .map(ExpenseResponseDto::new) // 재귀적으로 DTO 생성
                    .collect(Collectors.toList());
        }
    }
}