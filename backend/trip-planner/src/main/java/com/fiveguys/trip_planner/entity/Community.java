package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    // ✈️ 여행 플랜 전용
    private String departure;
    private String arrival;

    // 태그 (현재는 문자열 유지)
    private String tags;

    // ⭐ 평점 (선택적)
    private Integer rating;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 🔥 생성 시 자동 시간
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 🔥 수정 메서드 (중요)
    public void update(
            String category,
            String region,
            String title,
            String content,
            String departure,
            String arrival,
            String tags,
            Integer rating
    ) {
        this.category = category;
        this.region = region;
        this.title = title;
        this.content = content;
        this.departure = departure;
        this.arrival = arrival;
        this.tags = tags;
        this.rating = rating;
    }
}