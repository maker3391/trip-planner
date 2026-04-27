package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.ChatMessageRequestDto;
import com.fiveguys.trip_planner.dto.ChatMessageResponseDto;
import com.fiveguys.trip_planner.dto.ChatRoomRequestDto;
import com.fiveguys.trip_planner.dto.ChatRoomResponseDto;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.service.CSChatService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class CSChatController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final CSChatService CschatService;

    @MessageMapping("/chat/message")
    public void message(ChatMessageRequestDto requestDto) {
        ChatMessageResponseDto responseDto = CschatService.saveMessage(requestDto);

        messagingTemplate.convertAndSend("/sub/chat/room/" + requestDto.getRoomId(), responseDto);
    }

    @PostMapping("/api/cs/room")
    public ResponseEntity<ChatRoomResponseDto> createRoom(@RequestBody ChatRoomRequestDto requestDto) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(!(principal instanceof User)) {
            throw new RuntimeException("로그인 사용자 정보가 없습니다.");
        }
        User currentUser = (User) principal;

        ChatRoomResponseDto responseDto = CschatService.createRoom(requestDto, currentUser);

        messagingTemplate.convertAndSend("/sub/chat/admin/new-room", responseDto);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/api/cs/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getAllRooms() {
        return ResponseEntity.ok(CschatService.findAllRooms());
    }

    @GetMapping("/api/cs/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getChatHistory(@PathVariable Long roomId) {
        return ResponseEntity.ok(CschatService.getChatHistory(roomId));
    }

    @GetMapping("/api/cs/my-rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getMyRooms() {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        if (!(auth.getPrincipal() instanceof User currentUser)) {
            throw new RuntimeException("로그인 사용자 정보가 유효하지 않습니다.");
        }
        return ResponseEntity.ok(CschatService.findRoomsByUser(currentUser));
    }

}
