package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ExpenseRequestDto;
import com.fiveguys.trip_planner.dto.TripPlanRequestDto;
import com.fiveguys.trip_planner.dto.TripPlanResponseDto;
import com.fiveguys.trip_planner.dto.TripScheduleRequestDto;
import com.fiveguys.trip_planner.entity.*;

import com.fiveguys.trip_planner.repository.CommunityRepository;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TripPlanService {
    private final TripPlanRepository tripPlanRepository;
    private final TripMemberRepository tripMemberRepository;
    private final PlaceRepository placeRepository;
    private final NotificationService notificationService;
    private final CommunityRepository communityRepository;

    @Transactional
    public TripPlanResponseDto createTripPlan(TripPlanRequestDto requestDto, User user) {
        TripPlan tripPlan = new TripPlan();
        tripPlan.setOwner(user);
        tripPlan.setTitle(requestDto.getTitle());
        tripPlan.setDestination(requestDto.getDestination());
        tripPlan.setStartDate(requestDto.getStartDate());
        tripPlan.setEndDate(requestDto.getEndDate());
        tripPlan.setMaxMembers(requestDto.getMaxMembers() != null ? requestDto.getMaxMembers() : 10);
        tripPlan.setStatus("PLANNING");
        tripPlan.setInviteCode(UUID.randomUUID().toString().substring(0, 8));

        if(requestDto.getMaxMembers() != null) {
            if(requestDto.getMaxMembers() < 1) {
                throw new IllegalArgumentException("최소 인원은 방장을 포함하여 1명 이상이어야 합니다.");
            }
            tripPlan.setMaxMembers(requestDto.getMaxMembers());
        }

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
                expense.setExpenseType(expenseDto.getExpenseType() != null ? expenseDto.getExpenseType() : "ACTUAL");
                expense.setCreatedAt(LocalDateTime.now());
                expense.setPaidByUser(user);

                // ✅ 하위 항목 처리 추가
                if (expenseDto.getSubExpenses() != null) {
                    for (ExpenseRequestDto subDto : expenseDto.getSubExpenses()) {
                        Expense subExpense = new Expense();
                        subExpense.setTripPlan(tripPlan);
                        subExpense.setAmount(subDto.getAmount());
                        subExpense.setCategory(subDto.getCategory() != null ? subDto.getCategory() : "ETC");
                        subExpense.setDescription(subDto.getDescription());
                        subExpense.setExpenseType(subDto.getExpenseType() != null ? subDto.getExpenseType() : "ACTUAL");
                        subExpense.setCreatedAt(LocalDateTime.now());
                        subExpense.setPaidByUser(user);
                        expense.addSubExpense(subExpense);
                    }
                }

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

    public List<TripPlanResponseDto> getMyTripPlans(User user) {
        List<TripMember> memberships = this.tripMemberRepository.findByUser(user);
        return memberships.stream()
                .filter(member -> "OWNER".equals(member.getRole())) // ✅ OWNER만 필터링
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

        if(requestDto.getMaxMembers() != null) {
            long currentMemberCount = tripPlan.getMembers().stream()
                    .filter(m -> "OWNER".equals(m.getRole()) || "MEMBER".equals(m.getRole()))
                    .count();

            if(requestDto.getMaxMembers() < currentMemberCount) {
                throw new IllegalArgumentException("현재 참여 중인 인원(" + currentMemberCount + "명)보다 적게 제한을 설정할 수 없습니다.");
            }
            tripPlan.setMaxMembers(requestDto.getMaxMembers());
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
                expense.setExpenseType(expenseDto.getExpenseType() != null ? expenseDto.getExpenseType() : "ACTUAL");
                expense.setCreatedAt(LocalDateTime.now());
                expense.setPaidByUser(user);

                // ✅ 하위 항목 처리 추가
                if (expenseDto.getSubExpenses() != null) {
                    for (ExpenseRequestDto subDto : expenseDto.getSubExpenses()) {
                        Expense subExpense = new Expense();
                        subExpense.setTripPlan(tripPlan);
                        subExpense.setAmount(subDto.getAmount());
                        subExpense.setCategory(subDto.getCategory() != null ? subDto.getCategory() : "ETC");
                        subExpense.setDescription(subDto.getDescription());
                        subExpense.setExpenseType(subDto.getExpenseType() != null ? subDto.getExpenseType() : "ACTUAL");
                        subExpense.setCreatedAt(LocalDateTime.now());
                        subExpense.setPaidByUser(user);
                        expense.addSubExpense(subExpense);
                    }
                }

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

    @Transactional
    public TripPlanResponseDto joinTripByInviteCode(String inviteCode, User user) {
        TripPlan tripPlan = tripPlanRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료 초대 코드입니다."));

        if(tripMemberRepository.existsByTripPlanAndUser(tripPlan, user)) {
            throw new IllegalStateException("이미 참여 중인 여행입니다.");
        }

        TripMember newMember = new TripMember();
        newMember.setTripPlan(tripPlan);
        newMember.setUser(user);
        newMember.setRole("MEMBER");
        tripMemberRepository.save(newMember);

        String message = user.getNickname() + "님이 [" + tripPlan.getTitle() + "] 여행에 참가 신청을 하였습니다.";
        String targetUrl = "/trip-list";

        Optional<Community> communityOpt = communityRepository.findFirstByTripPlan(tripPlan);
        if (communityOpt.isPresent()) {
            targetUrl = "/community/" + communityOpt.get().getId();
        }
        notificationService.send(tripPlan.getOwner(), message, "TRIP_JOIN", targetUrl);

        return new TripPlanResponseDto(tripPlan);
    }
}