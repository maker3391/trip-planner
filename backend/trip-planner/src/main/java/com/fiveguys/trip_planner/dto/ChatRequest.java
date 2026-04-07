package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "AI 채팅 요청 객체")
public class ChatRequest {

    @Schema(
            description = "AI에게 보낼 질문이나 요청 메시지",
            example = "부산 2박 3일 먹방 여행 코스 짜줘!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "메시지는 필수입니다.")
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}