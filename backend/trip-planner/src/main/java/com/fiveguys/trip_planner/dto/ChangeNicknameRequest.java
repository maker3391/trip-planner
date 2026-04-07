package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "닉네임 변경 요청 객체")
public record ChangeNicknameRequest(

        @Schema(description = "새로운 닉네임 (2~30자)", example = "여행마스터", minLength = 2, maxLength = 30)
        @NotBlank
        @Size(min = 2, max = 30)
        String nickname
) {
}