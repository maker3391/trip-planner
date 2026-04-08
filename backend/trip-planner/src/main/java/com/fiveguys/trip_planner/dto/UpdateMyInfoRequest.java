package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "내 정보 수정 요청 객체")
public record UpdateMyInfoRequest(

        @Schema(description = "이름", example = "홍길동", minLength = 2, maxLength = 50)
        @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
        String name,

        @Schema(description = "닉네임", example = "여행마스터", minLength = 2, maxLength = 30)
        @Size(min = 2, max = 30, message = "닉네임은 2자 이상 30자 이하여야 합니다.")
        String nickname,

        @Schema(description = "전화번호", example = "010-1234-5678")
        @Pattern(
                regexp = "^$|^01[0-9]-?\\d{3,4}-?\\d{4}$",
                message = "올바른 phone 형식이어야 합니다."
        )
        String phone,

        @Schema(description = "현재 비밀번호", example = "oldPassword123!")
        String currentPassword,

        @Schema(
                description = "새 비밀번호 (8~20자, 영문/숫자/특수문자 포함)",
                example = "newPassword2026!"
        )
        @Pattern(
                regexp = "^$|^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>/?\\\\|`~])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>/?\\\\|`~]{8,20}$",
                message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 하고 공백은 사용할 수 없습니다."
        )
        String newPassword
) {
}