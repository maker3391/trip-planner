package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.AdminUserResponseDto;
import com.fiveguys.trip_planner.dto.BanRequestDto;
import com.fiveguys.trip_planner.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "🛡️ 관리자 API", description = "회원 관리 및 서비스 제재 관리")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @Operation(summary = "전체 회원 목록 조회", description = "관리자가 시스템의 모든 회원 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponseDto>> getAllUsers() {
        List<AdminUserResponseDto> responseList = userService.findAllUser().stream()
                .map(AdminUserResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @Operation(summary = "유저 정지 처리", description = "특정 유저에게 정지 기간과 사유를 부여하여 서비스 이용을 제한합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정지 처리 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터"),
            @ApiResponse(responseCode = "404", description = "해당 유저를 찾을 수 없음")
    })
    @PostMapping("/user/{userId}/ban")
    public ResponseEntity<String> banUser(
            @PathVariable Long userId,
            @RequestBody BanRequestDto requestDto) {
        userService.banUser(userId, requestDto.getDuration(), requestDto.getReason());
        return ResponseEntity.ok("유저 정지가 완료되었습니다.");
    }
}
