package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.ChatMessageRequestDto;
import com.fiveguys.trip_planner.dto.ChatMessageResponseDto;
import com.fiveguys.trip_planner.dto.ChatRoomRequestDto;
import com.fiveguys.trip_planner.dto.ChatRoomResponseDto;
import com.fiveguys.trip_planner.entity.ChatMessage;
import com.fiveguys.trip_planner.entity.ChatRoom;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.ChatMessageRepository;
import com.fiveguys.trip_planner.repository.ChatRoomRepository;
import com.fiveguys.trip_planner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CSChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageResponseDto saveMessage(ChatMessageRequestDto requestDto) {
        ChatRoom room = chatRoomRepository.findById(requestDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 채팅방입니다."));

        User sender = userRepository.findById(requestDto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        ChatMessage message = ChatMessage.builder()
                .chatRoom(room)
                .sender(sender)
                .content(requestDto.getContent())
                .build();

        chatMessageRepository.save(message);

        return ChatMessageResponseDto.from(message);
    }

    @Transactional
    public ChatRoomResponseDto createRoom(ChatRoomRequestDto requestDto, User user) {
        ChatRoom chatRoom = ChatRoom.builder()
                .user(user)
                .title(requestDto.getTitle())
                .status("WAITING")
                .build();
        chatRoomRepository.save(chatRoom);

        ChatMessage firstMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(user)
                .content(requestDto.getContent())
                .build();
        chatMessageRepository.save(firstMessage);

        return ChatRoomResponseDto.from(chatRoom);
    }

    public List<ChatRoomResponseDto> findAllRooms() {
        return chatRoomRepository.findAll().stream()
                .map(ChatRoomResponseDto::from)
                .collect(Collectors.toList());
    }

    public List<ChatMessageResponseDto> getChatHistory(Long roomId) {
        return chatMessageRepository.findByChatRoomIdOrderByCreatedAtAsc(roomId).stream()
                .map(ChatMessageResponseDto::from)
                .collect(Collectors.toList());
    }
}
