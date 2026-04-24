package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "community_comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 외래키 연관관계: 이 필드 하나로 항상 최신 User 정보(닉네임 포함)에 접근 가능합니다.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ❌ userName 필드 삭제! (User 테이블의 nickname을 항상 참조할 것이므로 중복 저장 금지)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @Column(nullable = false, length = 500)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommunityComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommunityComment> children = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(insertable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addChild(CommunityComment child) {
        this.children.add(child);
        child.parent = this;
    }

    // 🔥 생성 메서드 수정: userName 파라미터 및 세팅 제거
    public static CommunityComment create(User user, Community community, String comment) {
        return CommunityComment.builder()
                .user(user)
                .community(community)
                .comment(comment)
                .build();
    }

    public static CommunityComment createReply(User user, Community community, String comment, CommunityComment parent) {
        CommunityComment reply = CommunityComment.builder()
                .user(user)
                .community(community)
                .comment(comment)
                .parent(parent)
                .build();

        parent.addChild(reply);
        return reply;
    }

    public void updateComment(String comment) {
        this.comment = comment;
    }

    public void deleteComment() {
        this.isDeleted = true;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }
}