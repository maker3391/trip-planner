package com.fiveguys.trip_planner.dto;

public record OAuth2UserInfo(
        String email,
        String name,
        String provider,
        String providerId
) {
}
