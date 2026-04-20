package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.TripMember;
import com.fiveguys.trip_planner.entity.TripPlan;
import com.fiveguys.trip_planner.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, Long> {

    @EntityGraph(attributePaths = {"tripPlan", "tripPlan.budget"})
    List<TripMember> findByUser(User user);

    boolean existsByTripPlanAndUser(TripPlan tripPlan, User user);

    List<TripMember> findAllByTripPlan(TripPlan tripPlan);

    Optional<TripMember> findByIdAndTripPlan(Long id, TripPlan tripPlan);

    Optional<TripMember> findByTripPlanAndUser(TripPlan tripPlan, User user);
}