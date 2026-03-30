package com.fiveguys.trip_planner.repository;

import com.fiveguys.trip_planner.entity.TripDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripDayRepository extends JpaRepository<TripDay, Long> {}