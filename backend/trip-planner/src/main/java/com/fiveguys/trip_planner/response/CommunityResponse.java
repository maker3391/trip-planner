package com.fiveguys.trip_planner.response;

import com.fiveguys.trip_planner.entity.Community;
import com.fiveguys.trip_planner.entity.CommunityImage;
import com.fiveguys.trip_planner.dto.TripPlanResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "커뮤니티 게시글 응답 객체 (상세 및 목록용)")
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

    @Schema(description = "게시글 작성 일시", example = "2026-04-07T15:11:30")
    private LocalDateTime createdAt;


    @Schema(description = "게시글 마지막 수정 일시", example = "2026-04-15T15:30:00")
    private LocalDateTime updatedAt;

    @Schema(description = "조회수", example = "125")
    private Long viewCount;

    @Schema(description = "공유 횟수", example = "12")
    private Long shareCount;

    @Schema(description = "좋아요 총 개수", example = "42")
    private Long likeCount;

    // 🔥 내가 눌렀는지
    @Schema(description = "현재 로그인한 사용자의 좋아요 여부", example = "true")
    private boolean likedByMe;

    // 🔥 작성자 정보 (수정 핵심)
    @Schema(description = "작성자 고유 ID", example = "1")
    private Long authorId;

    @Schema(description = "작성자 닉네임", example = "여행왕수원")
    private String authorNickname;

    @Schema(description = "연결된 여행 계획 상세 정보")
    private TripPlanResponseDto tripPlan;

    // 🔥 이미지 ID 리스트
    @Schema(description = "연결된 이미지 ID 목록 (이미지 조회 API에서 사용)", example = "[1, 2, 5]")
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
                .tripPlan(community.getTripPlan() != null
                        ? TripPlanResponseDto.from(community.getTripPlan())
                        : null)
                .imageIds(ids)
                .build();
    }
}