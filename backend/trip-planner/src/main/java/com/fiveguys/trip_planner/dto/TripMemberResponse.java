package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.TripMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "여행 멤버 정보 응답 객체")
@Getter
@Builder
@AllArgsConstructor
public class TripMemberResponse {

    @Schema(description = "참가 고유 ID (TripMember ID)", example = "1")
    private Long memberId;

    @Schema(description = "유저 고유 ID", example = "5")
    private Long userId;

    @Schema(description = "유저 닉네임", example = "여행자")
    private String nickname;

    @Schema(description = "유저 이름", example = "홍길동")
    private String name;

    @Schema(description = "역할 (OWNER, MEMBER, PENDING", example = "PENDING")
    private String role;

    public static TripMemberResponse from(TripMember tripMember) {
        return TripMemberResponse.builder()
                .memberId(tripMember.getId())
                .userId(tripMember.getUser().getId())
                .nickname(tripMember.getUser().getNickname())
                .name(tripMember.getUser().getName())
                .role(tripMember.getRole())
                .build();
    }
}
