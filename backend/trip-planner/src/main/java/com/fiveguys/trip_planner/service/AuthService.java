package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.LoginRequest;
import com.fiveguys.trip_planner.dto.SignupRequest;
import com.fiveguys.trip_planner.entity.RefreshToken;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.exception.DuplicateEmailException;
import com.fiveguys.trip_planner.exception.InvalidLoginException;
import com.fiveguys.trip_planner.repository.RefreshTokenRepository;
import com.fiveguys.trip_planner.repository.UserRepository;
import com.fiveguys.trip_planner.response.MessageResponse;
import com.fiveguys.trip_planner.response.SignupResponse;
import com.fiveguys.trip_planner.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public SignupResponse signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("이미 사용중인 이메일입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.createUser(
                request.email(),
                encodedPassword,
                request.name(),
                request.phone()
        );

        User saved = userRepository.save(user);

        return new SignupResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getRole(),
                "회원가입 완료"
        );
    }

    public TokenResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidLoginException("이메일 또는 비밀번호가 틀렸습니다."));

        if (user.getPassword() == null) {
            throw new InvalidLoginException("소셜 로그인으로 가입한 계정입니다.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidLoginException("이메일 또는 비밀번호가 틀렸습니다.");
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        RefreshToken refreshTokenEntity = RefreshToken.create(
                user.getId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration()
        );

        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refresh(String refreshTokenValue) {
        if (!jwtTokenProvider.validateToken(refreshTokenValue)) {
            throw new IllegalArgumentException("유효하지 않은 RefreshToken입니다.");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshTokenValue)) {
            throw new IllegalArgumentException("RefreshToken만 재발급에 사용할 수 있습니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshTokenValue);

        RefreshToken savedRefreshToken = refreshTokenRepository.findById(String.valueOf(userId))
                .orElseThrow(() -> new IllegalArgumentException("저장된 RefreshToken이 없습니다. 다시 로그인해주세요."));

        if (!savedRefreshToken.getToken().equals(refreshTokenValue)) {
            throw new IllegalArgumentException("이미 무효화된 RefreshToken입니다. 다시 로그인해주세요.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        String newAccessToken = jwtTokenProvider.createAccessToken(user);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user);

        RefreshToken newRefreshTokenEntity = RefreshToken.create(
                user.getId(),
                newRefreshToken,
                jwtTokenProvider.getRefreshTokenExpiration()
        );

        refreshTokenRepository.save(newRefreshTokenEntity);

        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    public MessageResponse logout(User user) {
        if (user == null) {
            throw new IllegalArgumentException("인증된 사용자만 로그아웃할 수 있습니다.");
        }

        refreshTokenRepository.deleteById(String.valueOf(user.getId()));

        return new MessageResponse("로그아웃 완료");
    }
}