package com.fiveguys.trip_planner.response;

import com.fiveguys.trip_planner.entity.Community;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "커뮤니티 게시글 상세 응답 객체")
@Getter
@Builder
@AllArgsConstructor
public class CommunityResponse {

    @Schema(description = "게시글 고유 ID", example = "1024")
    private Long id;

    @Schema(description = "카테고리", example = "맛집")
    private String category;

    @Schema(description = "지역", example = "부산")
    private String region;

    @Schema(description = "게시글 제목", example = "해운대 시장 국밥 투어 후기")
    private String title;

    @Schema(description = "게시글 상세 내용", example = "여기는 국물이 끝내줍니다...")
    private String content;

    @Schema(description = "출발지", example = "서울역")
    private String departure;

    @Schema(description = "도착지", example = "부산역")
    private String arrival;

    @Schema(description = "태그 (쉼표 등으로 구분된 문자열)", example = "국밥,부산여행,혼밥")
    private String tags;

    @Schema(description = "평점 (0~5)", example = "5")
    private Integer rating;

    @Schema(description = "게시글 작성 일시", example = "2026-04-07T15:11:30")
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long viewCount;
    private Long recommendCount;

    // 🔥 작성자 닉네임 추가
    private String authorNickname;

    /**
     * Entity → DTO 변환
     */
    public static CommunityResponse from(Community community) {
        return CommunityResponse.builder()
                .id(community.getId())
                .category(community.getCategory())
                .region(community.getRegion())
                .title(community.getTitle())
                .content(community.getContent())
                .departure(community.getDeparture())
                .arrival(community.getArrival())
                .tags(community.getTags())
                .viewCount(community.getViewCount())
                .recommendCount(community.getRecommendCount())
                .rating(community.getRating())
                .createdAt(community.getCreatedAt())
                .authorNickname(community.getAuthorNickname()) // 🔥 추가
                .build();
    }
}