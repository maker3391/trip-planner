package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    private String message;

    private String type;

    @Column(nullable = false)
    private boolean isRead = false;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column
    private String targetUrl;

    @Builder
    public Notification(User receiver, String message, String type, String targetUrl) {
        this.receiver = receiver;
        this.message = message;
        this.type = type;
        this.targetUrl = targetUrl;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
