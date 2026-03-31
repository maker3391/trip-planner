package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.TripSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripScheduleRepository extends JpaRepository<TripSchedule, Long> {}