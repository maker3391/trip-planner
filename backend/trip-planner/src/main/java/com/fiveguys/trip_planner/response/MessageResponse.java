package com.fiveguys.trip_planner.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "단순 확인 메시지 응답 객체")
public class MessageResponse {

    @Schema(description = "처리 결과 혹은 안내 메시지", example = "성공적으로 처리되었습니다.")
    private String message;
}