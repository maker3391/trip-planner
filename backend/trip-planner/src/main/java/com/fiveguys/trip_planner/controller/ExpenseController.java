package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.ExpenseRequestDto;
import com.fiveguys.trip_planner.dto.ExpenseResponseDto;
import com.fiveguys.trip_planner.response.ExpenseSummaryResponse;
import com.fiveguys.trip_planner.response.MessageResponse;
import com.fiveguys.trip_planner.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Expense", description = "여행 경비(예산) 관리 API")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @Operation(summary = "경비 대시보드 요약 조회", description = "총 예산, 예상 지출, 실제 지출 및 비율을 계산하여 반환합니다.")
    @GetMapping("/trips/{tripId}/expenses/summary")
    public ResponseEntity<ExpenseSummaryResponse> getBudgetSummary(@PathVariable Long tripId) {
        ExpenseSummaryResponse summary = expenseService.getBudgetAnalysis(tripId);
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "여행 경비 전체 리스트 조회", description = "특정 여행의 모든 지출(실제/예상) 내역을 조회합니다.")
    @GetMapping("/trips/{tripId}/expenses")
    public ResponseEntity<List<ExpenseResponseDto>> getAllExpense(@PathVariable Long tripId) {
        List<ExpenseResponseDto> expense = expenseService.getExpensesByTripId(tripId);
        return ResponseEntity.ok(expense);
    }

    @Operation(summary = "여행 경비 내용 수정", description = "금액, 내용 혹은 ESTIMATED -> ACTUAL 상태 변경 등을 처리합니다.")
    @PutMapping("/expenses/{expenseId}")
    public ResponseEntity<ExpenseResponseDto> updateExpense(@PathVariable Long expenseId,
                                                            @Valid @RequestBody ExpenseRequestDto requestDto) {
        ExpenseResponseDto updated = expenseService.updateExpense(expenseId, requestDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "여행 경비 내용 삭제", description = "등록된 경비 내용을 삭제합니다.")
    @DeleteMapping("/expenses/{expenseId}")
    public ResponseEntity<MessageResponse> deleteExpense(@PathVariable Long expenseId) {
        expenseService.deleteExpense(expenseId);
        return ResponseEntity.ok(new MessageResponse("항목이 성공적으로 삭제되었습니다."));
    }
}
