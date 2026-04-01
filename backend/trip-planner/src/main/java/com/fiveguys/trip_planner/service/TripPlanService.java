package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.TripPlanRequestDto;
import com.fiveguys.trip_planner.dto.TripPlanResponseDto;
import com.fiveguys.trip_planner.entity.TripDay;
import com.fiveguys.trip_planner.entity.TripMember;
import com.fiveguys.trip_planner.entity.TripPlan;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.TripDayRepository;
import com.fiveguys.trip_planner.repository.TripMemberRepository;
import com.fiveguys.trip_planner.repository.TripPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TripPlanService {
    private final TripPlanRepository tripPlanRepository;
    private final TripDayRepository tripDayRepository;
    private final TripMemberRepository tripMemberRepository;

    @Transactional
    public TripPlanResponseDto createTripPlan(TripPlanRequestDto requestDto, User user) {
        TripPlan tripPlan = new TripPlan();
        tripPlan.setOwner(user);
        tripPlan.setTitle(requestDto.getTitle());
        tripPlan.setDestination(requestDto.getDestination());
        tripPlan.setStartDate(requestDto.getStartDate());
        tripPlan.setEndDate(requestDto.getEndDate());
        tripPlan.setStatus("PLANNING");

        TripPlan savePlan = tripPlanRepository.save(tripPlan);

        TripMember ownerMember = new TripMember();
        ownerMember.setTripPlan(savePlan);
        ownerMember.setUser(user);
        ownerMember.setRole("OWNER");
        tripMemberRepository.save(ownerMember);

        long days = ChronoUnit.DAYS.between(requestDto.getStartDate(), requestDto.getEndDate()) +1;

        List<TripDay> tripDays = new ArrayList<>();
        for(int i = 0; i < days; i++) {
            TripDay tripDay = new TripDay();
            tripDay.setTripPlan(savePlan);
            tripDay.setDayNumber(i+1);
            tripDay.setDate(requestDto.getStartDate().plusDays(i));
            tripDays.add(tripDay);
        }
        tripDayRepository.saveAll(tripDays);

        return new TripPlanResponseDto(savePlan);
    }
}
