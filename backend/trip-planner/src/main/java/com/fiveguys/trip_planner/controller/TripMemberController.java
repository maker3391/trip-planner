package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.TripMemberResponse;
import com.fiveguys.trip_planner.service.TripMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "👨‍👩‍👧‍👦 여행 멤버 관리 API", description = "여행 참가 신청, 수락, 거절 및 강퇴")
@RestController
@RequestMapping("/api/trips/{tripId}/members")
@RequiredArgsConstructor
public class TripMemberController {

    private final TripMemberService tripMemberService;

    @Operation(summary = "참가 신청", description = "로그인한 유저가 해당 여행에 참가를 신청합니다.")
    @PostMapping("/join")
    public ResponseEntity<?> requestJoin(@PathVariable Long tripId) {
        try {
            tripMemberService.requestJoin(tripId);
            return ResponseEntity.ok(Map.of("success", true, "message", "참가 신청이 완료되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "멤버 목록 조회", description = "해당 여행의 대기중 및 확정된 멤버 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<TripMemberResponse>> getMembers(@PathVariable Long tripId) {
        return ResponseEntity.ok(tripMemberService.getMembers(tripId));
    }

    @Operation(summary = "참가 수락 (방장 전용)", description = "대기중인 유저의 참가를 수락합니다.")
    @PatchMapping("/{memberId}/accept")
    public ResponseEntity<?> acceptJoin(@PathVariable Long tripId, @PathVariable Long memberId) {
        try {
            tripMemberService.acceptJoin(tripId, memberId);
            return ResponseEntity.ok(Map.of("success", true, "message", "참가가 수락되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "거절 및 강퇴 (방장 전용)", description = "참가 신청을 거절하거나 기존 멤버를 강퇴합니다.")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<?> removeMember(@PathVariable Long tripId, @PathVariable Long memberId) {
        try {
            tripMemberService.removeMember(tripId, memberId);
            return ResponseEntity.ok(Map.of("success", true, "message", "멤버가 리스트에서 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Operation(summary = "신청 취소 / 방 나가기", description = "로그인한 유저 본인이 참가 신청을 취소하거나 여행에서 나갑니다.")
    @DeleteMapping("/me")
    public ResponseEntity<?> leaveTrip(@PathVariable Long tripId) {
        try {
            tripMemberService.leaveTrip(tripId);
            return ResponseEntity.ok(Map.of("success", true, "message", "정상적으로 취소/퇴장 되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
