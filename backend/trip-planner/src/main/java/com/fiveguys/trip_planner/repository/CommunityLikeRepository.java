package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {

    // 🔥 핵심 (토글용)
    Optional<CommunityLike> findByUserAndCommunity(User user, Community community);

    boolean existsByUserAndCommunity(User user, Community community);

    // 🔥 카운트
    Long countByCommunity(Community community);
}