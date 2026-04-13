package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ExpenseRequestDto;
import com.fiveguys.trip_planner.dto.TripPlanRequestDto;
import com.fiveguys.trip_planner.dto.TripPlanResponseDto;
import com.fiveguys.trip_planner.dto.TripScheduleRequestDto;
import com.fiveguys.trip_planner.entity.*;

import com.fiveguys.trip_planner.repository.PlaceRepository;
import com.fiveguys.trip_planner.repository.TripMemberRepository;
import com.fiveguys.trip_planner.repository.TripPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripPlanService {
    private final TripPlanRepository tripPlanRepository;
    private final TripMemberRepository tripMemberRepository;
    private final PlaceRepository placeRepository;

    @Transactional
    public TripPlanResponseDto createTripPlan(TripPlanRequestDto requestDto, User user) {
        TripPlan tripPlan = new TripPlan();
        tripPlan.setOwner(user);
        tripPlan.setTitle(requestDto.getTitle());
        tripPlan.setDestination(requestDto.getDestination());
        tripPlan.setStartDate(requestDto.getStartDate());
        tripPlan.setEndDate(requestDto.getEndDate());
        tripPlan.setStatus("PLANNING");

        if (requestDto.getSchedules() != null) {
            for (TripScheduleRequestDto scheduleRequestDto : requestDto.getSchedules()) {
                TripSchedule schedule = new TripSchedule();
                schedule.setTripPlan(tripPlan);
                schedule.setDayNumber(scheduleRequestDto.getDayNumber());
                schedule.setTitle(scheduleRequestDto.getTitle());
                schedule.setVisitOrder(scheduleRequestDto.getVisitOrder());
                schedule.setStartTime(scheduleRequestDto.getStartTime());
                schedule.setEndTime(scheduleRequestDto.getEndTime());
                schedule.setMemo(scheduleRequestDto.getMemo());
                schedule.setEstimatedStayMinutes(scheduleRequestDto.getEstimatedStayMinutes());
                schedule.setPinColor(scheduleRequestDto.getPinColor());
                schedule.setSelectedPinColor(scheduleRequestDto.getSelectedPinColor());
                schedule.setLineColor(scheduleRequestDto.getLineColor());

                if(scheduleRequestDto.getGooglePlaceId() != null) {
                    Place place = placeRepository.findByExternalPlaceId(scheduleRequestDto.getGooglePlaceId())
                            .orElseGet(() -> {
                                Place newPlace = new Place();
                                newPlace.setName(scheduleRequestDto.getPlaceName());
                                newPlace.setAddress(scheduleRequestDto.getPlaceAddress());
                                newPlace.setLatitude(scheduleRequestDto.getLatitude());
                                newPlace.setLongitude(scheduleRequestDto.getLongitude());
                                newPlace.setExternalPlaceId(scheduleRequestDto.getGooglePlaceId());
                                return placeRepository.save(newPlace);
                            });
                    schedule.setPlace(place);
                }
                tripPlan.getSchedules().add(schedule);
            }
        }
        if (requestDto.getExpenses() != null) {
            for (ExpenseRequestDto expenseDto : requestDto.getExpenses()) {
                Expense expense = new Expense();
                expense.setTripPlan(tripPlan);
                expense.setAmount(expenseDto.getAmount());
                expense.setCategory(expenseDto.getCategory() != null ? expenseDto.getCategory() : "ETC");
                expense.setDescription(expenseDto.getDescription());
                expense.setExpenseType("ESTIMATED");
                expense.setCreatedAt(LocalDateTime.now());
                expense.setPaidByUser(user);

                tripPlan.getExpenses().add(expense);
            }
        }

        if (requestDto.getTotalBudget() != null) {
            BigDecimal safeBudget = requestDto.getTotalBudget().setScale(2, RoundingMode.HALF_UP);

            if (tripPlan.getBudget() != null) {
                tripPlan.getBudget().setTotalBudget(safeBudget);
            } else {
                Budget budget = new Budget();
                budget.setTripPlan(tripPlan);
                budget.setTotalBudget(safeBudget);
                budget.setCurrency(requestDto.getCurrency() != null ? requestDto.getCurrency() : "KRW");
                budget.setCreatedAt(LocalDateTime.now());
                tripPlan.setBudget(budget);
            }
        }

        TripPlan savePlan = tripPlanRepository.save(tripPlan);

        TripMember ownerMember = new TripMember();
        ownerMember.setTripPlan(savePlan);
        ownerMember.setUser(user);
        ownerMember.setRole("OWNER");
        tripMemberRepository.save(ownerMember);


        return new TripPlanResponseDto(savePlan);
    }

    @Transactional(readOnly = true)
    public TripPlanResponseDto getTripPlan(Long tripId) {
        TripPlan tripPlan = tripPlanRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다."));
        return new TripPlanResponseDto(tripPlan);
    }

    @Transactional(readOnly = true)
    public List<TripPlanResponseDto> getMyTripPlans(User user) {
        List<TripMember> memberships = tripMemberRepository.findByUser(user);

        return memberships.stream()
                .map(member -> new TripPlanResponseDto(member.getTripPlan()))
                .collect(Collectors.toList());
    }

    @Transactional
    public TripPlanResponseDto updateTripPlan (Long tripId, TripPlanRequestDto requestDto, User user) {
        TripPlan tripPlan = tripPlanRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다."));

        if(!tripPlan.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        if (requestDto.getTitle() != null) tripPlan.setTitle(requestDto.getTitle());
        if (requestDto.getDestination() != null) tripPlan.setDestination(requestDto.getDestination());
        if (requestDto.getStartDate() != null) tripPlan.setStartDate(requestDto.getStartDate());
        if (requestDto.getEndDate() != null) tripPlan.setEndDate(requestDto.getEndDate());
        if (requestDto.getStatus() != null) tripPlan.setStatus(requestDto.getStatus());


        if(requestDto.getSchedules() != null) {
            tripPlan.getSchedules().clear();

            for(TripScheduleRequestDto scheduleRequestDto : requestDto.getSchedules()) {
                TripSchedule schedule = new TripSchedule();
                schedule.setTripPlan(tripPlan);
                schedule.setDayNumber(scheduleRequestDto.getDayNumber());
                schedule.setTitle(scheduleRequestDto.getTitle());
                schedule.setVisitOrder(scheduleRequestDto.getVisitOrder());
                schedule.setStartTime(scheduleRequestDto.getStartTime());
                schedule.setEndTime(scheduleRequestDto.getEndTime());
                schedule.setMemo(scheduleRequestDto.getMemo());
                schedule.setEstimatedStayMinutes(scheduleRequestDto.getEstimatedStayMinutes());
                schedule.setPinColor(scheduleRequestDto.getPinColor());
                schedule.setSelectedPinColor(scheduleRequestDto.getSelectedPinColor());
                schedule.setLineColor(scheduleRequestDto.getLineColor());

                if(scheduleRequestDto.getGooglePlaceId() != null) {
                    Place place = placeRepository.findByExternalPlaceId(scheduleRequestDto.getGooglePlaceId())
                            .orElseGet(() -> {
                                Place newPlace = new Place();
                                newPlace.setName(scheduleRequestDto.getPlaceName());
                                newPlace.setAddress(scheduleRequestDto.getPlaceAddress());
                                newPlace.setLatitude(scheduleRequestDto.getLatitude());
                                newPlace.setLongitude(scheduleRequestDto.getLongitude());
                                newPlace.setExternalPlaceId(scheduleRequestDto.getGooglePlaceId());
                                return placeRepository.save(newPlace);
                            });
                    schedule.setPlace(place);
                }
                tripPlan.getSchedules().add(schedule);
            }
        }

        if (requestDto.getExpenses() != null) {
            tripPlan.getExpenses().clear();
            for (ExpenseRequestDto expenseDto : requestDto.getExpenses()) {
                Expense expense = new Expense();
                expense.setTripPlan(tripPlan);
                expense.setAmount(expenseDto.getAmount());
                expense.setCategory(expenseDto.getCategory() != null ? expenseDto.getCategory() : "ETC");
                expense.setDescription(expenseDto.getDescription());
                expense.setExpenseType("ESTIMATED");
                expense.setCreatedAt(LocalDateTime.now());
                expense.setPaidByUser(user);

                tripPlan.getExpenses().add(expense);
            }
        }

        if (requestDto.getTotalBudget() != null) {
            if (tripPlan.getBudget() != null) {
                tripPlan.getBudget().setTotalBudget(requestDto.getTotalBudget());
                tripPlan.getBudget().setUpdatedAt(LocalDateTime.now());
            } else {
                Budget budget = new Budget();
                budget.setTripPlan(tripPlan);
                budget.setTotalBudget(requestDto.getTotalBudget());
                budget.setCurrency(requestDto.getCurrency() != null ? requestDto.getCurrency() : "KRW");
                budget.setCreatedAt(LocalDateTime.now());

                tripPlan.setBudget(budget);
            }
        }
        return new TripPlanResponseDto(tripPlan);
    }

    @Transactional
    public void deleteTripPlan(Long tripId, User user) {
        TripPlan tripPlan = tripPlanRepository.findById(tripId)
                .orElseThrow(() -> new IllegalStateException("해당 여행 계획을 찾을 수 없습니다."));

        if(!tripPlan.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("삭제 권한이 없습니다.");
        }
        tripPlanRepository.delete(tripPlan);
    }
}