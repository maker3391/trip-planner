package com.fiveguys.trip_planner.dto;

public record LoginResopnse(
        Long userId,
        String email,
        String name,
        String role,
        String message
) {
}
