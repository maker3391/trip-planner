package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.TripMember;
import com.fiveguys.trip_planner.entity.TripPlan;
import com.fiveguys.trip_planner.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripMemberRepository extends JpaRepository<TripMember, Long> {

    @EntityGraph(attributePaths = {"tripPlan", "tripPlan.budget"})
    List<TripMember> findByUser(User user);

    boolean existsByTripPlanAndUser(TripPlan tripPlan, User user);
}