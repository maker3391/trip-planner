package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "비밀번호 재설정 확정 요청 데이터")
public class PasswordResetConfirmRequestDto {

    @Schema(description = "이메일 링크를 통해 전달받은 1회용 재설정 토큰", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank
    private String token;

    @Schema(description = "새롭게 설정할 비밀번호 (8~16자 영문 대/소문자, 숫자, 특수문자 포함)", example = "Fiveguys123!")
    @Pattern(regexp = "(?=.*[0-9])(?=.*[a-zA-z])(?=.*\\W)(?=\\S+$).{8,16}", message = "비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 사용하세요.")
    private String newPassword;
}
