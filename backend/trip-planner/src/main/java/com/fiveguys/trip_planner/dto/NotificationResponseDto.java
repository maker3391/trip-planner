package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "알림 응답 데이터 모델")
public class NotificationResponseDto {

    @Schema(description = "알림 고유 ID", example = "10")
    private Long id;

    @Schema(description = "알림 메시지", example = "수원님이 여행 참가 신청을 하셨습니다.")
    private String message;

    @Schema(description = "알림 타입", example = "TRIP_JOIN_REQUEST")
    private String type;

    @Schema(description = "읽음 여부", example = "false")
    private boolean isRead;

    @Schema(description = "알림 생성 시간")
    private LocalDateTime createdAt;

    private String targetUrl;

    public static NotificationResponseDto fromEntity(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .targetUrl(notification.getTargetUrl())
                .build();
    }
}
