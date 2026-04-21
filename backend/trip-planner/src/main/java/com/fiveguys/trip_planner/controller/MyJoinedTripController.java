package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.TripPlanResponseDto;
import com.fiveguys.trip_planner.service.TripMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "🧳 내 참가 여행 API", description = "내가 참가 신청하거나 승인된 여행 목록 조회")
@RestController
@RequestMapping("/api/trip-members")
@RequiredArgsConstructor
public class MyJoinedTripController {

    private final TripMemberService tripMemberService;

    @Operation(summary = "내가 참가한 여행 목록 조회")
    @GetMapping("/joined")
    public ResponseEntity<List<TripPlanResponseDto>> getJoinedTrips() {
        return ResponseEntity.ok(tripMemberService.getJoinedTrips());
    }
}