package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원가입 완료 응답 객체")
public record SignupResponse(

        @Schema(description = "생성된 사용자 고유 식별자(ID)", example = "1")
        Long userId,

        @Schema(description = "가입된 이메일 주소", example = "user@example.com")
        String email,

        @Schema(description = "사용자 실명", example = "홍길동")
        String name,

        @Schema(description = "사용자 닉네임", example = "길동이")
        String nickname,

        @Schema(description = "부여된 사용자 권한", example = "ROLE_USER")
        String role,

        @Schema(description = "가입 완료 안내 메시지", example = "회원가입이 성공적으로 완료되었습니다.")
        String message
) {
}