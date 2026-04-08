package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청 객체")
public record SignupRequest(

        @Schema(description = "이메일 주소", example = "user@example.com")
        @NotBlank
        @Email
        String email,

        @Schema(description = "비밀번호 (영문+숫자+특수문자 포함 8~20자)", example = "Passw0rd!")
        @NotBlank
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>/?\\\\|`~])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>/?\\\\|`~]{8,20}$",
                message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 하고 공백은 사용할 수 없습니다."
        )
        String password,

        @Schema(description = "실명", example = "홍길동")
        @NotBlank
        String name,

        @Schema(description = "닉네임", example = "길동이")
        @NotBlank
        @Size(min = 2, max = 30)
        String nickname,

        @Schema(description = "전화번호", example = "010-1234-5678")
        @Pattern(
                regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$",
                message = "올바른 phone 형식이어야 합니다."
        )
        String phone
) {
}