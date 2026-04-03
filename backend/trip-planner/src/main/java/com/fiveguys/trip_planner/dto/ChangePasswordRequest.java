package com.fiveguys.trip_planner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank
        String currentPassword,

        @NotBlank
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>/?\\\\|`~])[A-Za-z\\d!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>/?\\\\|`~]{8,20}$",
                message = "비밀번호는 8~20자이며 영문, 숫자, 특수문자를 모두 포함해야 하고 공백은 사용할 수 없습니다."
        )
        String newPassword
) {
}