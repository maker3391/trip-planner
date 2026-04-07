package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.aspectj.weaver.ast.Not;

@Schema(description = "로그인 요청 객체")
public record LoginRequest(

        @Schema(description = "로그인 이메일 (계정 아이디)", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "비밀번호", example = "password123!")
        @NotBlank
        String password
) {
}
