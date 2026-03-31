package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * 특정 여행 계획(TripPlan)의 ID로 모든 지출 내역을 조회합니다.
     */
    List<Expense> findByTripPlanId(Long tripPlanId);
}