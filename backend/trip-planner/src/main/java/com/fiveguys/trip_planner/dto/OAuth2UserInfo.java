package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "소셜(OAuth2) 인증 사용자 정보 객체")
public record OAuth2UserInfo(

        @Schema(description = "소셜 계정 이메일", example = "user@gmail.com")
        String email,

        @Schema(description = "소셜 계정 이름", example = "홍길동")
        String name,

        @Schema(description = "소셜 로그인 제공자 (google, kakao 등)", example = "google")
        String provider,

        @Schema(description = "소셜 제공자 측 고유 식별자(ID)", example = "1029384756")
        String providerId
) {
}
