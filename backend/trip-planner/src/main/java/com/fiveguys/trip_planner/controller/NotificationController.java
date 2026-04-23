package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.NotificationResponseDto;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@Tag(name = "🔔 알림 서비스 API", description = "실시간 알림 구독 및 미확인 알림 관리 API입니다.")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "실시간 알림 구독 (SSE)", description = "로그인 직후 호출하여 서버와 실시간 알림 통로를 연결합니다. (text/event-stream)")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal User user) {
        return notificationService.subscribe(user.getId());
    }

    @Operation(summary = "미확인 알림 목록 조회", description = "현재 사용자가 아직 읽지 않은 모든 알림을 최신순으로 가져옵니다.")
    @GetMapping
    public List<NotificationResponseDto> getNotifications(@AuthenticationPrincipal User user) {
        return notificationService.getUnreadNotifications(user);
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 클릭했을 때 해당 알림을 읽음 상태로 변경합니다.")
    @PatchMapping("/{id}/read")
    public void readNotification(@PathVariable Long id) {
        notificationService.readNotification(id);
    }
}
