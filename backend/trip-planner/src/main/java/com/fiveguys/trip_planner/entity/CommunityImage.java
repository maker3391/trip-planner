package com.fiveguys.trip_planner.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CommunityImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalName;

    private String contentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] data;

    // 🔥 1. 게시글과의 연관 관계 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id")
    private Community community;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    // 🔥 2. 서비스 레이어에서 필드 세팅을 위한 메서드들
    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    // 🔥 3. 연관 관계 편의 메서드 (중요)
    // 이 메서드를 통해 community_id가 NULL이 아닌 값이 들어가게 됩니다.
    public void setCommunity(Community community) {
        this.community = community;
        // 게시글 엔티티 쪽 리스트에도 이 이미지를 추가해줍니다.
        if (community != null && !community.getImages().contains(this)) {
            community.getImages().add(this);
        }
    }
}