package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.ChatMessageRequestDto;
import com.fiveguys.trip_planner.dto.ChatMessageResponseDto;
import com.fiveguys.trip_planner.dto.ChatRoomRequestDto;
import com.fiveguys.trip_planner.dto.ChatRoomResponseDto;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.service.CSChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "🎧 고객센터 API", description = "1:1 문의 및 실시간 상담 관리")
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

    @Operation(summary = "문의 방 생성", description = "새로운 1:1 상담 문의 방을 생성합니다. 생성 시 관리자에게 실시간 알림을 전송합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 방 생성 성공"),
            @ApiResponse(responseCode = "401", description = "로그인 정보 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
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

    @Operation(summary = "전체 문의 목록 조회(관리자)", description = "관리자 권한으로 접수된 모든 1:1 문의 방 목록을 가져옵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "접근 권한 부족")
    })
    @GetMapping("/api/cs/rooms")
    public ResponseEntity<List<ChatRoomResponseDto>> getAllRooms() {
        return ResponseEntity.ok(CschatService.findAllRooms());
    }

    @Operation(summary = "채팅 내역 조회", description = "특정 문의 방의 전체 메시지 내역을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방 번호")
    })
    @GetMapping("/api/cs/room/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponseDto>> getChatHistory(@PathVariable Long roomId) {
        return ResponseEntity.ok(CschatService.getChatHistory(roomId));
    }

    @Operation(summary = "내 문의 목록 조회", description = "현재 로그인한 유저가 본인이 작성한 문의 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
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

    @Operation(summary = "상담 종료", description = "특정 상담 방의 상태를 종료(CLOSED)로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "종료 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 방 번호")
    })
    @PatchMapping("/api/cs/room/{roomId}/close")
    public ResponseEntity<?> closeRoom(@PathVariable("roomId") Long roomId) {
        CschatService.closeRoom(roomId);
        return ResponseEntity.ok().body("상담이 종료되었습니다.");
    }

    @Operation(summary = "상담 내역 삭제", description = "유저의 상담 목록에서 해당 내역을 삭제(숨김) 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PatchMapping("/api/cs/room/{roomId}/delete")
    public ResponseEntity<?> deleteRoom(@PathVariable("roomId") Long roomId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("인증 정보가 없습니다.");
        }

        if (!(auth.getPrincipal() instanceof User currentUser)) {
            throw new RuntimeException("로그인 사용자 정보가 유효하지 않습니다.");
        }

        CschatService.deleteRoomByUser(roomId, currentUser);
        return ResponseEntity.ok().body("상담 내역이 삭제되었습니다.");
    }
}
