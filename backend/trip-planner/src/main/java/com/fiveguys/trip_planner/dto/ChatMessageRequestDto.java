package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "실시간 채팅 메시지 전송 요청 DTO")
public class ChatMessageRequestDto {

    @Schema(description = "채팅방 고유 ID", example = "1")
    private Long roomId;

    @Schema(description = "메시지 전송자(회원) ID", example = "5")
    private Long senderId;

    @Schema(description = "메시지 내용", example = "안녕하세요, 상담 문의 드립니다.")
    private String content;
}
