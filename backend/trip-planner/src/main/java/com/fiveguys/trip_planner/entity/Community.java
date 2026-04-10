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
    private Long recommendCount = 0L; // 좋아요(공유수)

    // 🔥 이미지와의 연관 관계 설정
    @Builder.Default
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityImage> images = new ArrayList<>();

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

    // 🔥 비즈니스 로직 메서드들

    // 게시글 수정용
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

    // 추천(공유)수 증가
    public void incrementRecommend() {
        this.recommendCount++;
    }

    // 추천(공유)수 감소
    public void decrementRecommend() {
        if(this.recommendCount > 0) this.recommendCount--;
    }

    /**
     * 🔥 연관 관계 편의 메서드
     * 이미지를 게시글에 안전하게 추가하기 위해 사용합니다.
     */
    public void addImage(CommunityImage image) {
        this.images.add(image);
        if (image.getCommunity() != this) {
            image.setCommunity(this);
        }
    }
}