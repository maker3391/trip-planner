package com.fiveguys.trip_planner.response;

import com.fiveguys.trip_planner.entity.Community;
import com.fiveguys.trip_planner.entity.CommunityImage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @Schema(description = "태그", example = "국밥,부산여행,혼밥")
    private String tags;

    @Schema(description = "평점 (0~5)", example = "5")
    private Integer rating;

    @Schema(description = "게시글 작성 일시")
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long viewCount;

    private Long shareCount;

    private Long likeCount;

    // 🔥 내가 눌렀는지
    private boolean likedByMe;

    // 🔥 작성자 정보 (수정 핵심)
    private Long authorId;
    private String authorNickname;

    // 🔥 이미지 ID 리스트
    @Schema(description = "연결된 이미지 ID 목록")
    private List<Long> imageIds;

    /**
     * 🔥 Entity → DTO 변환 (목록용)
     */
    public static CommunityResponse from(Community community) {

        List<Long> ids = community.getImages() != null
                ? community.getImages().stream()
                .map(CommunityImage::getId)
                .collect(Collectors.toList())
                : List.of();

        return CommunityResponse.builder()
                .id(community.getId())
                .category(community.getCategory())
                .region(community.getRegion())
                .title(community.getTitle())
                .content(community.getContent())
                .departure(community.getDeparture())
                .arrival(community.getArrival())
                .tags(community.getTags() != null ? community.getTags() : "")
                .viewCount(community.getViewCount())
                .shareCount(community.getShareCount())
                .likeCount(community.getLikeCount())
                .likedByMe(false)
                .rating(community.getRating() != null ? community.getRating() : 0)
                .createdAt(community.getCreatedAt())
                .updatedAt(community.getUpdatedAt())

                // 🔥 작성자 정보
                .authorId(
                        community.getAuthor() != null
                                ? community.getAuthor().getId()
                                : null
                )
                .authorNickname(
                        community.getAuthor() != null
                                ? community.getAuthor().getNickname()
                                : "알 수 없음"
                )

                .imageIds(ids)
                .build();
    }

    /**
     * 🔥 Entity → DTO 변환 (상세용)
     */
    public static CommunityResponse from(Community community, boolean likedByMe) {

        List<Long> ids = community.getImages() != null
                ? community.getImages().stream()
                .map(CommunityImage::getId)
                .collect(Collectors.toList())
                : List.of();

        return CommunityResponse.builder()
                .id(community.getId())
                .category(community.getCategory())
                .region(community.getRegion())
                .title(community.getTitle())
                .content(community.getContent())
                .departure(community.getDeparture())
                .arrival(community.getArrival())
                .tags(community.getTags() != null ? community.getTags() : "")
                .viewCount(community.getViewCount())
                .shareCount(community.getShareCount())
                .likeCount(community.getLikeCount())
                .likedByMe(likedByMe)
                .rating(community.getRating() != null ? community.getRating() : 0)
                .createdAt(community.getCreatedAt())
                .updatedAt(community.getUpdatedAt())

                // 🔥 작성자 정보
                .authorId(
                        community.getAuthor() != null
                                ? community.getAuthor().getId()
                                : null
                )
                .authorNickname(
                        community.getAuthor() != null
                                ? community.getAuthor().getNickname()
                                : "알 수 없음"
                )

                .imageIds(ids)
                .build();
    }
}