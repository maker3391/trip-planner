package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "실시간 채팅 메시지 응답 DTO")
public class ChatMessageResponseDto {

    @Schema(description = "메시지 고유 ID", example = "101")
    private Long id;

    @Schema(description = "채팅방 고유 ID", example = "1")
    private Long roomId;

    @Schema(description = "전송자 고유 ID", example = "5")
    private Long senderId;

    @Schema(description = "전송자 닉네임", example = "여행왕")
    private String senderNickname;

    @Schema(description = "메시지 내용", example = "상담원 연결을 기다려주세요.")
    private String content;

    @Schema(description = "메시지 전송 시간", example = "2026-04-27T15:45:00")
    private String createdAt;

    public static ChatMessageResponseDto from(ChatMessage message) {
        return ChatMessageResponseDto.builder()
                .id(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSender().getId())
                .senderNickname(message.getSender().getNickname())
                .content(message.getContent())
                .createdAt(message.getCreatedAt().toString())
                .build();
    }
}
