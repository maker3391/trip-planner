package com.fiveguys.trip_planner.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityRequest {

    @NotBlank(message = "카테고리는 필수입니다.")
    private String category;

    @NotBlank(message = "지역은 필수입니다.")
    private String region;

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    private String content;

    private String departure;
    private String arrival;

    // 🔥 핵심 변경
    private String tags;

    // 🔥 범위 제한
    @Min(value = 0, message = "최소 평점은 0입니다.")
    @Max(value = 5, message = "최대 평점은 5입니다.")
    private Integer rating;
}