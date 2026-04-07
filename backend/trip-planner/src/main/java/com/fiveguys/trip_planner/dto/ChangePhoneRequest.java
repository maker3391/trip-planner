package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "전화번호 변경 요청 객체")
public record ChangePhoneRequest(

        @Schema(
                description = "새로운 전화번호 (하이픈 포함/미포함 모두 가능)",
                example = "010-1234-5678"
        )
        @NotBlank
        @Pattern(
                regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$",
                message = "올바른 phone 형식이어야 합니다."
        )
        String phone
) {
}