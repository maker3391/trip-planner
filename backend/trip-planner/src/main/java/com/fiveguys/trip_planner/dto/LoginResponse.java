package com.fiveguys.trip_planner.dto;

public record LoginResponse(
        Long userId,
        String email,
        String name,
        String role,
        String message
) {
}
