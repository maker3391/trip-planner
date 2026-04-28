package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.NotificationResponseDto;
import com.fiveguys.trip_planner.entity.Notification;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(60L * 1000 * 60);
        emitters.put(userId, emitter);

        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        emitter.onError((e) -> emitters.remove(userId));

        sendToClient(userId, "EventStream Created. [userId" + userId + "]");

        return emitter;
    }

    @Transactional
    public void send(User receiver, String message, String type, String targetUrl) {
        Notification notification = Notification.builder()
                .receiver(receiver)
                .message(message)
                .type(type)
                .targetUrl(targetUrl)
                .build();
        notificationRepository.save(notification);

        String eventId = String.valueOf(notification.getId());
        sendToClient(receiver.getId(), Map.of("message", message, "type", type, "notificationId", eventId, "targetUrl", targetUrl));
    }

    private void sendToClient(Long userId, Object data) {
        SseEmitter emitter = emitters.get(userId);
        if(emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("notification")
                        .data(data));
            } catch (IOException exception) {
                emitters.remove(userId);
            }
        }
    }

    @Transactional
    public List<NotificationResponseDto> getUnreadNotifications(User user) {
        List<Notification> notifications = notificationRepository.findAllByReceiverAndIsReadFalseOrderByCreatedAtDesc(user);

        return notifications.stream()
                .map(NotificationResponseDto::fromEntity)
                .toList();
    }

    @Transactional
    public void readNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));

        notification.markAsRead();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getAllNotifications(User user) {
        List<Notification> notifications = notificationRepository
                .findAllByReceiverOrderByCreatedAtDesc(user);
        return notifications.stream()
                .map(NotificationResponseDto::fromEntity)
                .toList();
    }

    @Transactional
    public void deleteNotification(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다."));
        notificationRepository.delete(notification);
    }
}
