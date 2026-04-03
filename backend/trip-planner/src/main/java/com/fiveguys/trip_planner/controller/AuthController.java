package com.fiveguys.trip_planner.controller;

import com.fiveguys.trip_planner.dto.ChangeNicknameRequest;
import com.fiveguys.trip_planner.dto.ChangePasswordRequest;
import com.fiveguys.trip_planner.dto.ChangePhoneRequest;
import com.fiveguys.trip_planner.dto.LoginRequest;
import com.fiveguys.trip_planner.response.MessageResponse;
import com.fiveguys.trip_planner.dto.RefreshTokenRequest;
import com.fiveguys.trip_planner.dto.SignupRequest;
import com.fiveguys.trip_planner.response.SignupResponse;
import com.fiveguys.trip_planner.response.TokenResponse;
import com.fiveguys.trip_planner.response.UserMeResponse;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            throw new IllegalArgumentException("인증된 사용자 정보를 찾을 수 없습니다.");
        }

        return ResponseEntity.ok(
                new UserMeResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getName(),
                        user.getNickname(),
                        user.getPhone(),
                        user.getRole(),
                        user.getStatus()
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.logout(user));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(authService.changePassword(user, request));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<MessageResponse> changeNickname(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangeNicknameRequest request
    ) {
        return ResponseEntity.ok(authService.changeNickname(user, request));
    }

    @PatchMapping("/me/phone")
    public ResponseEntity<MessageResponse> changePhone(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePhoneRequest request
    ) {
        return ResponseEntity.ok(authService.changePhone(user, request));
    }
}