package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.TripPlanRequestDto;
import com.fiveguys.trip_planner.dto.TripPlanResponseDto;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.service.TripPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "여행 일정 API", description = "여행 계획의 생성(C), 조회(R), 수정(U), 삭제(D)를 담당하는 API 모음입니다.")
@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripPlanController {
    private final TripPlanService tripPlanService;
    @Operation(summary = "여행 계획 생성", description = "지도에서 선택한 장소들과 제목, 여행지, 날짜 정보를 받아 새로운 여행 계획을 DB에 저장합니다.")
    @PostMapping
    public ResponseEntity<TripPlanResponseDto> createTripPlan(@RequestBody TripPlanRequestDto requestDto,
                                                              @AuthenticationPrincipal User user) {
        TripPlanResponseDto responseDto = tripPlanService.createTripPlan(requestDto, user);

        return ResponseEntity.ok(responseDto);
    }
    @Operation(summary = "여행 계획 단건 조회", description = "여행의 고유 ID(id)를 경로에 넣어 요청하면, 해당 여행의 모든 상세 일정과 좌표 데이터를 1건 반환합니다. (지도 복원/상세보기용)")
    @GetMapping("/{id}")
    public ResponseEntity<TripPlanResponseDto> getTripPlan(@PathVariable Long id) {
        TripPlanResponseDto responseDto = tripPlanService.getTripPlan(id);
        return ResponseEntity.ok(responseDto);
    }
    @Operation(summary = "여행 계획 다건 조회", description = "현재 로그인한 사용자의 토큰을 기반으로, 그동안 작성한 모든 여행 계획 리스트를 최신순으로 불러옵니다. (여행 목록용)")
    @GetMapping
    public ResponseEntity<List<TripPlanResponseDto>> getMyTripPlans (@AuthenticationPrincipal User user) {
        List<TripPlanResponseDto> responseDto = tripPlanService.getMyTripPlans(user);
        return ResponseEntity.ok(responseDto);
    }
    @Operation(summary = "여행 계획 수정", description = "이미 저장된 여행의 제목, 날짜 또는 장소 리스트(schedules)중 변경사항이 있는 항목을 업데이트합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<TripPlanResponseDto> updateTripPlan(@PathVariable Long id,
                                                              @RequestBody TripPlanRequestDto requestDto,
                                                              @AuthenticationPrincipal User user) {
        TripPlanResponseDto responseDto = tripPlanService.updateTripPlan(id, requestDto, user);
        return ResponseEntity.ok(responseDto);
    }
    @Operation(summary = "여행 계획 삭제", description = "여행의 고유 ID(id)를 받아 해당 계획을 영구 삭제합니다. 본인의 글만 삭제할 수 있습니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTripPlan(@PathVariable Long id, @AuthenticationPrincipal User user) {
        tripPlanService.deleteTripPlan(id, user);
        return ResponseEntity.noContent().build();
    }
}
