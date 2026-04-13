package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.TripPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    @EntityGraph(attributePaths = {"budget", "schedules", "schedules.place", "expenses"})
    @Query("select t from TripPlan t where t.id = :id")
    Optional<TripPlan> findWithDetailsById(Long id);

    Optional<TripPlan> findByInviteCode(String inviteCode);
}