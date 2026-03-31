package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    /**
     * 특정 여행 계획(TripPlan)의 ID로 설정된 예산을 조회합니다.
     */
    Optional<Budget> findByTripPlanId(Long tripPlanId);
}