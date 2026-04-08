package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;


@Schema(description = "커뮤니티 게시글 등록 및 수정 요청 객체")
@Getter
@Setter
public class CommunityRequest {

    @Schema(description = "카테고리 (예: 맛집, 명소, 숙소)", example = "맛집")
    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @Schema(description = "지역 (시/도 단위)", example = "부산")
    @NotBlank(message = "지역은 필수입니다.")
    private String region;

    @Schema(description = "게시글 제목", example = "해운대 시장 국밥 투어 후기")
    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @Schema(description = "게시글 상세 내용", example = "여기는 국물이 끝내줍니다...")
    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    private String content;

    @Schema(description = "출발지 (선택 사항)", example = "서울역")
    private String departure;

    @Schema(description = "도착지 (선택 사항)", example = "부산역")
    private String arrival;


    // 🔥 핵심 변경
    @Schema(description = "태그 리스트", example = "[\"국밥\", \"부산여행\", \"혼밥\"]")
    private String tags;

    // 🔥 범위 제한
    @Schema(description = "평점 (0~5점)", example = "5", minimum = "0", maximum = "5")
    @Min(value = 0, message = "최소 평점은 0입니다.")
    @Max(value = 5, message = "최대 평점은 5입니다.")
    private Integer rating;

    // 🔥 작성자 ID 추가
    @NotNull(message = "작성자 ID는 필수입니다.")
    private Long userId;
}