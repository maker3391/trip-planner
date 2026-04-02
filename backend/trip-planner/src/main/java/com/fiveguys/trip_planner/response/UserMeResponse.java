package com.fiveguys.trip_planner.response;

public record UserMeResponse(
        Long id,
        String email,
        String name,
        String role,
        String status
) {
}
