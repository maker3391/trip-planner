package com.fiveguys.trip_planner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeNicknameRequest(

        @NotBlank
        @Size(min = 2, max = 30)
        String nickname
) {
}