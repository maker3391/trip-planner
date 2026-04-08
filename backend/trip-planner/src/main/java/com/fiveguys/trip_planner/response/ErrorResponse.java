package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "공통 에러 응답 객체")
public record ErrorResponse(

        @Schema(description = "에러 발생 시각", example = "2026-04-07T15:20:10")
        LocalDateTime timestamp,

        @Schema(description = "HTTP 상태 코드", example = "400")
        int status,

        @Schema(description = "에러 유형", example = "Bad Request")
        String error,

        @Schema(description = "상세 에러 메시지", example = "이메일 형식이 올바르지 않습니다.")
        String message,

        @Schema(description = "에러가 발생한 API 경로", example = "/api/auth/signup")
        String path
) {
}
