package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(description = "리프레시 토큰 갱신 요청 객체")
@Getter
@NoArgsConstructor
public class RefreshTokenRequest {

    @Schema(description = "리프레시 토큰 값", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;
}