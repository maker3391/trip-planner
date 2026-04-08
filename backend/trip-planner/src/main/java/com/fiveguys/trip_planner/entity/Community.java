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

    @Column(nullable = false)
    private String authorNickname;

    @Column
    private String departure;
    private String arrival;

    @Column
    private String tags;
    private Integer rating;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Long recommendCount = 0L; // 좋아요 개수

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if(this.viewCount == null) this.viewCount = 0L;
        if(this.recommendCount == null) this.recommendCount = 0L;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(
            String category,
            String region,
            String title,
            String content,
            String departure,
            String arrival,
            String tags,
            Integer rating,
            String authorNickname,
            Long viewCount,
            Long recommendCount
    ) {
        this.category = category;
        this.region = region;
        this.title = title;
        this.content = content;
        this.departure = departure;
        this.arrival = arrival;
        this.tags = tags;
        this.rating = rating;
        this.authorNickname = authorNickname;
        this.viewCount = viewCount;
        this.recommendCount = recommendCount;
    }

    // 🔥 좋아요 증가 메서드
    public void incrementRecommend() {
        this.recommendCount++;
    }

    // 🔥 좋아요 감소 메서드 (선택)
    public void decrementRecommend() {
        if(this.recommendCount > 0) this.recommendCount--;
    }

    // 조회수 증가 메서드
    public void incrementViewCount() {
        this.viewCount++;
    }

}