package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.PasswordResetConfirmRequestDto;
import com.fiveguys.trip_planner.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "🔑 인증/비밀번호 찾기 API", description = "비밀번호를 잊어버렸을 때 이메일 인증을 통해 재설정하는 API입니다.")
@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @Operation(summary = "비밀번호 재설정 링크 메일 발송", description = "가입된 이메일 주소로 비밀번호를 재설정할 수 있는 1회용 링크(토큰)를 발송합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메일 발송 성공"),
            @ApiResponse(responseCode = "400", description = "가입되지 않은 이메일")
    })
    @PostMapping("/request")
    public ResponseEntity<?> requestReset(@RequestParam String email) {
        passwordResetService.sendResetLink(email);
        return ResponseEntity.ok(Map.of("success", true, "message", "이메일로 재설정 링크를 발송했습니다."));
    }

    @Operation(summary = "새 비밀번호 확정", description = "이메일로 받은 토큰과 새로운 비밀번호를 입력받아 비밀번호를 최종 변경합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 토큰 또는 비밀번호 형식 오류")
    })
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmReset(@Valid @RequestBody PasswordResetConfirmRequestDto request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));
    }
}
