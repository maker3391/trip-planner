package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "비밀번호 변경 요청 객체")
public record ChangePasswordRequest(

        @Schema(description = "현재 사용 중인 비밀번호", example = "oldPassword123!")
        @NotBlank
        String currentPassword,

        @Schema(
                description = "새로 설정할 비밀번호 (8~20자, 영문/숫자/특수문자 포함 필수)",
                example = "newPassword2026!",
                minLength = 8,
                maxLength = 20
        )
        @NotBlank
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>/?\\\\|`~])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>/?\\\\|`~]{8,20}$",
                message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 하고 공백은 사용할 수 없습니다."
        )
        String newPassword
) {
}