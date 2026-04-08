package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "JWT 인증 토큰 응답 객체")
public class TokenResponse {

    @Schema(
            description = "액세스 토큰 (API 요청 시 Authorization 헤더에 사용)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    )
    private String accessToken;

    @Schema(
            description = "리프레시 토큰 (액세스 토큰 만료 시 재발급용)",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh..."
    )
    private String refreshToken;
}