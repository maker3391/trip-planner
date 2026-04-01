package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.TripPlanRequestDto;
import com.fiveguys.trip_planner.dto.TripPlanResponseDto;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.UserRepository;
import com.fiveguys.trip_planner.service.TripPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripPlanController {
    private final TripPlanService tripPlanService;
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<TripPlanResponseDto> createTripPlan(
            @RequestBody TripPlanRequestDto requestDto,
            Principal principal // 로그인한 사용자 정보 가져오기
            ) {
        // 현재 로그인한 사용자의 email(또는 ID)로 User 엔티티 조회
        Long userId = Long.parseLong(principal.getName());
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        TripPlanResponseDto response = tripPlanService.createTripPlan(requestDto, user);

        return ResponseEntity.ok(response);
    }
}
