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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "🔐 인증/사용자 API", description = "회원가입, 로그인, 토큰 갱신 및 내 프로필 관리 API입니다.")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 이름 등을 입력받아 새로운 사용자 계정을 생성합니다.")
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signup(request));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호를 확인하여 Access Token과 Refresh Token을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자의 상세 프로필(ID, 이메일, 닉네임 등)을 가져옵니다. (마이페이지 진입 시 사용)")
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

    @Operation(summary = "토큰 갱신", description = "Access Token이 만료되었을 때, Refresh Token을 사용하여 새로운 Access Token을 발급받습니다.")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @Operation(summary = "로그아웃", description = "현재 사용자의 세션 또는 토큰을 무효화하여 로그아웃 처리합니다.")
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(authService.logout(user));
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호와 새 비밀번호를 입력받아 보안 정보를 업데이트합니다.")
    @PatchMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        return ResponseEntity.ok(authService.changePassword(user, request));
    }

    @Operation(summary = "닉네임 변경", description = "마이페이지에서 사용자의 닉네임을 새로운 이름으로 수정합니다.")
    @PatchMapping("/me/nickname")
    public ResponseEntity<MessageResponse> changeNickname(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangeNicknameRequest request
    ) {
        return ResponseEntity.ok(authService.changeNickname(user, request));
    }

    @Operation(summary = "전화번호 변경", description = "사용자의 연락처 정보를 업데이트합니다.")
    @PatchMapping("/me/phone")
    public ResponseEntity<MessageResponse> changePhone(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ChangePhoneRequest request
    ) {
        return ResponseEntity.ok(authService.changePhone(user, request));
    }
}