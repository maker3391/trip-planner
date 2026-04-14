package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Schema(description = "커뮤니티 게시글 등록 및 수정 요청 객체")
@Getter
@Setter
public class CommunityRequest {

    @Schema(description = "카테고리 (예: 맛집게시판, 여행플랜 공유 등)", example = "맛집게시판")
    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @Schema(description = "지역 (시/도 단위)", example = "부산")
    @NotBlank(message = "지역은 필수입니다.")
    private String region;

    @Schema(description = "게시글 제목", example = "해운대 시장 국밥 투어 후기")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이내로 작성해주세요.")
    private String title;

    @Schema(description = "게시글 상세 내용", example = "여기는 국물이 끝내줍니다...")
    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    private String content;

    @Schema(description = "출발지 (선택 사항)", example = "서울역")
    private String departure;

    @Schema(description = "도착지 (선택 사항)", example = "부산역")
    private String arrival;

    @Schema(description = "태그 (쉼표로 구분된 문자열)", example = "국밥,부산여행,혼밥")
    @Size(max = 300, message = "태그는 300자 이내로 입력해주세요.")
    private String tags;

    @Schema(description = "평점 (0~5점)", example = "5", minimum = "0", maximum = "5")
    @Min(value = 0, message = "최소 평점은 0입니다.")
    @Max(value = 5, message = "최대 평점은 5입니다.")
    private Integer rating;

    // 🔥 userId는 절대 받지 않음 (서버에서 처리)

    @Schema(description = "업로드된 이미지 ID 리스트", example = "[1, 2, 3]")
    private List<Long> imageIds;
}