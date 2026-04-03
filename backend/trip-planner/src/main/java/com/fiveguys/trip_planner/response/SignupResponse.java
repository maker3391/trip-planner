package com.fiveguys.trip_planner.response;

public record SignupResponse(
        Long userId,
        String email,
        String name,
        String nickname,
        String role,
        String message
) {
}