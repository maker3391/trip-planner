package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "경비 지출 내역 요청 객체")
@Getter
@Setter // 서비스 단에서 값을 처리하거나 테스트할 때 필요할 수 있으므로 추가
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

    @Schema(description = "지출 타입 (예: ESTIMATED, ACTUAL)", example = "ACTUAL")
    private String expenseType;

    // --- 핵심 추가 부분: 하위 지출 내역 리스트 ---
    @Schema(description = "하위 상세 지출 내역 리스트")
    private List<ExpenseRequestDto> subExpenses;

    @Schema(description = "지출 내역 ID (수정 시 사용)")
    private Long id;
}