package com.fiveguys.trip_planner.service;

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
    public TripPlanResponseDto updateTripPlan(Long tripId, TripPlanRequestDto requestDto, User user) {
        // 1. 기존 계획 조회 및 권한 확인
        TripPlan tripPlan = tripPlanRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 계획을 찾을 수 없습니다."));

        if (!tripPlan.getOwner().getId().equals(user.getId())) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }

        // 2. 기본 정보 업데이트
        tripPlan.setTitle(requestDto.getTitle());
        tripPlan.setDestination(requestDto.getDestination());
        tripPlan.setStartDate(requestDto.getStartDate());
        tripPlan.setEndDate(requestDto.getEndDate());

        // 3. 기존 일정 비우기 (orphanRemoval = true 설정으로 인해 연결이 끊긴 자식은 삭제됨)
        tripPlan.getSchedules().clear();

        // 4. 새로운 일정 추가
        if (requestDto.getSchedules() != null) {
            for (TripScheduleRequestDto scheduleRequestDto : requestDto.getSchedules()) {
                TripSchedule schedule = new TripSchedule();

                // 양방향 연관관계 편의 설정
                schedule.setTripPlan(tripPlan);

                schedule.setDayNumber(scheduleRequestDto.getDayNumber());
                schedule.setTitle(scheduleRequestDto.getTitle());
                schedule.setVisitOrder(scheduleRequestDto.getVisitOrder());
                schedule.setStartTime(scheduleRequestDto.getStartTime());
                schedule.setEndTime(scheduleRequestDto.getEndTime());
                schedule.setMemo(scheduleRequestDto.getMemo());
                schedule.setEstimatedStayMinutes(scheduleRequestDto.getEstimatedStayMinutes());

                // 장소 처리 로직
                if (scheduleRequestDto.getGooglePlaceId() != null) {
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

        // 5. 명시적으로 변경 사항 반영 (핵심 포인트)
        // saveAndFlush를 사용하면 ResponseDto로 변환되기 전에 DB에 SQL이 즉시 날아갑니다.
        tripPlanRepository.saveAndFlush(tripPlan);

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
