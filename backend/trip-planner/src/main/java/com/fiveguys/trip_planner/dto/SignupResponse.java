package com.fiveguys.trip_planner.dto;

public record SignupResponse(
        Long userId,
        String email,
        String name,
        String role,
        String message
) {
}
