package com.fiveguys.trip_planner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class CommunityCommentRequest {

    @NotBlank(message = "댓글 내용은 비어 있을 수 없습니다.")
    @Size(max = 500, message = "댓글은 500자 이하로 작성하세요.")
    private String comment;
}