package com.fiveguys.trip_planner.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;

import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash("refresh_token")
public class RefreshToken {

    @Id
    private String id; // userId

    private String token;

    @TimeToLive
    private Long ttl; // seconds

    public static RefreshToken create(Long userId, String token, Long ttl) {
        return new RefreshToken(String.valueOf(userId), token, ttl);
    }
}