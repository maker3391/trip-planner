package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.ChatMessage;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatMessageResponseDto {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String content;
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
