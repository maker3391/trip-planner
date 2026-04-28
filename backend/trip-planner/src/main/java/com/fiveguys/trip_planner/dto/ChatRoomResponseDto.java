package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "1:1 상담 방 정보 응답 DTO")
public class ChatRoomResponseDto {

    @Schema(description = "상담 방 고유 ID", example = "1")
    private Long id;

    @Schema(description = "상담 제목", example = "결제 취소 관련 문의드립니다.")
    private String title;

    @Schema(description = "문의 작성자 닉네임", example = "여행왕")
    private String userNickname;

    @Schema(description = "상담 상태 (예: OPEN, CLOSED)", example = "OPEN")
    private String status;

    @Schema(description = "상담 생성 일시", example = "2026-04-27T15:00:00")
    private LocalDateTime createdAt;

    public static ChatRoomResponseDto from(ChatRoom room) {
        return ChatRoomResponseDto.builder()
                .id(room.getId())
                .title(room.getTitle())
                .userNickname(room.getUser().getNickname())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
