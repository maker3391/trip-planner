package com.fiveguys.trip_planner.dto;

import com.fiveguys.trip_planner.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "관리자용 회원 정보 응답 DTO")
public class AdminUserResponseDto {

    @Schema(description = "회원 고유 ID", example = "1")
    private Long id;

    @Schema(description = "회원 닉네임", example = "여행왕")
    private String nickname;

    @Schema(description = "회원 이메일", example = "namsuwon@example.com")
    private String email;

    @Schema(description = "회원 권한 (USER, ADMIN)", example = "USER")
    private String role;

    @Schema(description = "계정 상태 (ACTIVE, DELETE)", example = "ACTIVE")
    private String status;

    @Schema(description = "제재 종료 일시 (정지 중이 아닐 경우 null)", example = "2026-05-04T15:30:00")
    private LocalDateTime bannedUntil;

    @Schema(description = "제재 사유 (정지 중이 아닐 경우 null)", example = "부적절한 게시글 도배")
    private String banReason;

    public static AdminUserResponseDto from(User user) {
        return AdminUserResponseDto.builder()
                .id(user.getId())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .bannedUntil(user.getBannedUntil())
                .banReason(user.getBanReason())
                .build();
    }

}
