package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// =====================================================================
// [요구사항 확인 및 안내]
// 백엔드의 엔티티 클래스(Community)입니다.
// 규칙 1, 2, 3(카테고리 우선도, 다중 선택 OR 연산, 자동 전환)은
// DB 테이블 스키마 자체를 변경하는 것이 아니라 데이터를 조회(Query/Filter)하는
// Repository(QueryDSL 등) 또는 Service 계층에서 List<String> 형태의
// 파라미터를 받아 처리해야 하는 비즈니스 로직에 해당합니다.
//
// 따라서 이 엔티티 클래스 자체는 기존의 단일 region 구조(String)를 그대로
// 유지해도 무방하며, (게시글 하나가 여러 지역을 가지는 것이 아니라면) 수정이 필요 없습니다.
// 규칙 4와 5에 따라 주석으로만 안내하고 코드는 원본 그대로 반환합니다.
// =====================================================================

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

    @Builder.Default
    @Column(nullable = false)
    private String region = "전체"; // ✅ 객체 생성 시 기본값 할당

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    // 🔥 LAZY 유지 (중요)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Builder.Default
    @OneToMany(mappedBy = "community", cascade = CascadeType.REMOVE)
    private List<CommunityComment> comments = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_plan_id")
    private TripPlan tripPlan;

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

    @Builder.Default
    @Column(nullable = false)
    private Long likeCount = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long commentCount = 0L;

    // 🔥 리스트는 항상 초기화 (NPE 방지)
    @Builder.Default
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommunityImage> images = new ArrayList<>();

    // 🔥 여기가 중요 (초기화 + cascade 정리)
    @Builder.Default
    @OneToMany(mappedBy = "community", cascade = CascadeType.REMOVE)
    private List<CommunityLike> likes = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        if (this.viewCount == null) this.viewCount = 0L;
        if (this.shareCount == null) this.shareCount = 0L;
        if (this.likeCount == null) this.likeCount = 0L;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // =========================
    // 비즈니스 로직
    // =========================

    public void update(String category, String region, String title, String content,
                       String departure, String arrival, String tags, Integer rating, TripPlan tripPlan) {
        this.category = category;
        this.region = region;
        this.title = title;
        this.content = content;
        this.departure = departure;
        this.arrival = arrival;
        this.tags = tags;
        this.rating = rating;
        this.tripPlan = tripPlan;
    }

    public void incrementViewCount() {
        if (this.viewCount == null) {
            this.viewCount = 1L;
        } else {
            this.viewCount++;
        }
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) this.commentCount--;
    }

    public void incrementShareCount() {
        this.shareCount++;
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public void addImage(CommunityImage image) {
        this.images.add(image);
        if (image.getCommunity() != this) {
            image.setCommunity(this);
        }
    }

    public void removeImage(CommunityImage image) {
        this.images.remove(image);
        image.setCommunity(null);
    }

    public void addComment(CommunityComment comment) {
        this.comments.add(comment);
        if (comment.getCommunity() != this) {
            comment.setCommunity(this);
        }
    }
}