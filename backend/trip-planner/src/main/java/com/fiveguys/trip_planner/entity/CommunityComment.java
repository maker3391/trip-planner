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

    // 유저
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 게시글
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    // 댓글 내용
    @Column(nullable = false, length = 500)
    private String comment;

    // 부모 댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommunityComment parent;

    // 자식 댓글
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CommunityComment> children = new ArrayList<>();

    // 생성 시간
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // =========================
    // 🔹 연관관계 메서드
    // =========================
    public void addChild(CommunityComment child) {
        this.children.add(child);
        child.parent = this;
    }

    // =========================
    // 🔹 생성 메서드
    // =========================
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

    // =========================
    // 🔹 수정
    // =========================
    public void updateComment(String comment) {
        this.comment = comment;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }
}