package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 본인 정보 응답 객체 (마이페이지용)")
public record UserMeResponse(

        @Schema(description = "사용자 고유 식별자(ID)", example = "1")
        Long id,

        @Schema(description = "계정 이메일", example = "user@example.com")
        String email,

        @Schema(description = "사용자 실명", example = "홍길동")
        String name,

        @Schema(description = "사용자 닉네임", example = "길동이")
        String nickname,

        @Schema(description = "사용자 전화번호", example = "010-1234-5678")
        String phone,

        @Schema(description = "사용자 권한", example = "ROLE_USER")
        String role,

        @Schema(description = "계정 상태 (ACTIVE, SLEEP, DELETED 등)", example = "ACTIVE")
        String status
) {
}