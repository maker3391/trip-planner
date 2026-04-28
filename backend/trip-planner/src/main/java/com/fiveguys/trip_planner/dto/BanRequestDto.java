package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "회원 제재(정지) 요청 DTO")
public class BanRequestDto {

    @Schema(description = "정지 기간 (일 단위 / 영구 정지는 보통 36500 입력)", example = "7")
    private int duration;

    @Schema(description = "정지 사유 (관리자 메모 및 유저 통보용)", example = "부적절한 닉네임 사용 및 욕설")
    private String reason;
}
