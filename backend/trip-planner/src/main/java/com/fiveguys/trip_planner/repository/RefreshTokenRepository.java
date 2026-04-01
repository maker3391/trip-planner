package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}