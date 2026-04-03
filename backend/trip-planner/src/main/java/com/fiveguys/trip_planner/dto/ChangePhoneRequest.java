package com.fiveguys.trip_planner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePhoneRequest(

        @NotBlank
        @Pattern(
                regexp = "^01[0-9]-?\\d{3,4}-?\\d{4}$",
                message = "올바른 phone 형식이어야 합니다."
        )
        String phone
) {
}