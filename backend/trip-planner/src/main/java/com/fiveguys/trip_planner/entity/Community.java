package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    private String departure;
    private String arrival;
    private String tags;
    private Integer rating;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long shareCount = 0L;

    // 🔥 추가 (핵심)
    @Builder.Default
    @Column(nullable = false)
    private Long likeCount = 0L;

    // 🔥 이미지 관계
    @Builder.Default
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityImage> images = new ArrayList<>();

    // 좋아요 관련
    @OneToMany(mappedBy = "community", cascade = CascadeType.REMOVE)
    private List<CommunityLike> likes;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.viewCount == null) this.viewCount = 0L;
        if (this.shareCount == null) this.shareCount = 0L;
        if (this.likeCount == null) this.likeCount = 0L; // 🔥 추가
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================
    // 비즈니스 로직
    // =========================

    public void update(String category, String region, String title, String content,
                       String departure, String arrival, String tags, Integer rating) {
        this.category = category;
        this.region = region;
        this.title = title;
        this.content = content;
        this.departure = departure;
        this.arrival = arrival;
        this.tags = tags;
        this.rating = rating;
    }

    // 조회수 증가
    public void incrementViewCount() {
        this.viewCount++;
    }

    // 공유 증가
    public void incrementShareCount() {
        this.shareCount++;
    }

    // =========================
    // 🔥 좋아요 로직 (추가)
    // =========================

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    /**
     * 🔥 연관 관계 편의 메서드
     */
    public void addImage(CommunityImage image) {
        this.images.add(image);
        if (image.getCommunity() != this) {
            image.setCommunity(this);
        }
    }
}