package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.ChatRoom;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomResponseDto {
    private Long id;
    private String title;
    private String userNickname;
    private String status;
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
