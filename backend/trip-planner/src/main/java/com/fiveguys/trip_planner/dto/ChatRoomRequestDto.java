package com.fiveguys.trip_planner.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "1:1 문의 방 생성 요청 DTO")
public class ChatRoomRequestDto {

    @Schema(description = "문의 제목", example = "결제 취소 관련 문의드립니다.")
    private String title;

    @Schema(description = "문의 상세 내용", example = "어제 예약한 상품을 취소하고 싶은데 어떻게 하나요?")
    private String content;
}
