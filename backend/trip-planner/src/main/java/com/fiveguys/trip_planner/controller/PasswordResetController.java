package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.PasswordResetConfirmRequestDto;
import com.fiveguys.trip_planner.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/password-reset")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<?> requestReset(@RequestParam String email) {
        passwordResetService.sendResetLink(email);
        return ResponseEntity.ok(Map.of("success", true, "message", "이메일로 재설정 링크를 발송했습니다."));
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmReset(@Valid @RequestBody PasswordResetConfirmRequestDto request) {
        String token = request.getToken();
        String newPassword = request.getNewPassword();
        passwordResetService.resetPassword(token, newPassword);
        return ResponseEntity.ok(Map.of("success", true, "message", "비밀번호가 성공적으로 변경되었습니다."));
    }
}
