package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.ChatRequest;
import com.fiveguys.trip_planner.response.ChatResponse;
import com.fiveguys.trip_planner.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "🤖 AI 채팅 API", description = "AI 여행 비서와 대화하며 여행지를 추천받거나 일정을 짜달라고 요청할 수 있는 API입니다.")
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "AI 여행 비서와 채팅",
            description = "사용자의 질문(메시지)을 AI 모델에 전달하고, 최적화된 여행 추천이나 답변을 받아옵니다.")
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.chat(request));
    }
}