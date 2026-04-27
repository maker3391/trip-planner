package com.fiveguys.trip_planner.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequestDto {
    private Long roomId;
    private Long senderId;
    private String content;
}
