package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

// =====================================================================
// [요구사항 확인 및 안내]
// 현재 CommunityRequest DTO는 커뮤니티 게시글의 '생성 및 수정'을 담당하는 객체입니다.
// 사이드바의 '조회' 시 사용되는 규칙 1~3(카테고리 우선도, 다중 선택 OR 연산, 전체보기 자동화)은
// 데이터를 읽어오는 조회용 컨트롤러의 파라미터(@RequestParam List<String>)나 검색용 DTO에 적용됩니다.
//
// 게시글을 처음 작성하거나 수정할 때는 프론트엔드(CommunityWritePage) 폼의 기존 형태처럼
// 1게시글 1지역/1카테고리 원칙이 적용되므로, 규칙 5(변수명 및 인수 개수 임의 변경 금지)를 준수하여
// category와 region 필드의 단일 String 타입을 그대로 유지하였습니다.
//
// 규칙 4에 따라 설명은 주석으로 대체하며 전체 코드를 반환합니다.
// =====================================================================

@Schema(description = "커뮤니티 게시글 생성 및 수정 요청 DTO")
@Getter
@Setter
public class CommunityRequest {

    @Schema(description = "게시글 카테고리 (필수)", example = "맛집게시판", allowableValues = {"맛집게시판", "여행플랜 공유", "자유게시판"})
    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @Schema(description = "지역 정보 (필수, 시/도 단위)", example = "부산")
    @NotBlank(message = "지역은 필수입니다.")
    private String region;

    @Schema(description = "게시글 제목 (필수, 최대 200자)", example = "해운대 시장 국밥 투어 후기")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이내로 작성해주세요.")
    private String title;

    @Schema(description = "게시글 본문 내용 (필수)", example = "여기는 국물이 끝내줍니다...")
    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    private String content;

    @Schema(description = "여행 출발지 (선택)", example = "서울역")
    private String departure;

    @Schema(description = "여행 도착지 (선택)", example = "부산역")
    private String arrival;

    @Schema(description = "검색 및 분류용 태그 (선택, 쉼표 구분, 최대 300자)", example = "국밥,부산여행,혼밥")
    @Size(max = 300, message = "태그는 300자 이내로 입력해주세요.")
    private String tags;

    @Schema(description = "사용자 평점 (0~5점 사이)", example = "5", minimum = "0", maximum = "5")
    @Min(value = 0, message = "최소 평점은 0입니다.")
    @Max(value = 5, message = "최대 평점은 5입니다.")
    private Integer rating;

    @Schema(description = "불러올 여행 계획 고유 ID (선택)", example = "5")
    private Long tripPlanId;

    // 🔥 userId는 절대 받지 않음 (서버에서 처리)

    @Schema(description = "업로드된 이미지 ID 리스트 (이미지 업로드 API 선행 호출 필요)", example = "[1, 2, 3]")
    private List<Long> imageIds;
}