package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.UpdateMyInfoRequest;
import com.fiveguys.trip_planner.dto.LoginRequest;
import com.fiveguys.trip_planner.dto.SignupRequest;
import com.fiveguys.trip_planner.entity.RefreshToken;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.exception.DuplicateEmailException;
import com.fiveguys.trip_planner.exception.DuplicateNicknameException;
import com.fiveguys.trip_planner.exception.DuplicatePhoneException;
import com.fiveguys.trip_planner.exception.InvalidLoginException;
import com.fiveguys.trip_planner.repository.RefreshTokenRepository;
import com.fiveguys.trip_planner.repository.UserRepository;
import com.fiveguys.trip_planner.response.MessageResponse;
import com.fiveguys.trip_planner.response.SignupResponse;
import com.fiveguys.trip_planner.response.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public SignupResponse signup(SignupRequest request) {

        String normalizedNickname = normalizeNickname(request.nickname());
        String normalizedPhone = normalizePhone(request.phone());

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("이미 사용중인 이메일입니다.");
        }

        if (userRepository.existsByNickname(normalizedNickname)) {
            throw new DuplicateNicknameException("이미 사용중인 닉네임입니다.");
        }

        if (normalizedPhone != null && userRepository.existsByPhone(normalizedPhone)) {
            throw new DuplicatePhoneException("이미 사용중인 전화번호입니다.");
        }

        String encodedPassword = passwordEncoder.encode(request.password());

        User user = User.createUser(
                request.email(),
                encodedPassword,
                request.name(),
                normalizedNickname,
                normalizedPhone
        );

        User saved = userRepository.save(user);

        return new SignupResponse(
                saved.getId(),
                saved.getEmail(),
                saved.getName(),
                saved.getNickname(),
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

    @Transactional
    public MessageResponse updateMe(User user, UpdateMyInfoRequest request) {
        validateAuthenticatedUser(user);

        String newName = request.name() == null ? "" : request.name().trim();
        String newNickname = normalizeNickname(request.nickname());
        String newPhone = normalizePhone(request.phone());

        boolean hasPasswordInput =
                (request.currentPassword() != null && !request.currentPassword().isBlank()) ||
                        (request.newPassword() != null && !request.newPassword().isBlank());

        if (newName.isBlank()) {
            throw new IllegalArgumentException("이름은 비어 있을 수 없습니다.");
        }

        if (newName.equals(user.getName())
                && newNickname.equals(user.getNickname())
                && equalsNullable(newPhone, user.getPhone())
                && !hasPasswordInput) {
            throw new IllegalArgumentException("변경된 내용이 없습니다.");
        }

        if (!newNickname.equals(user.getNickname())
                && userRepository.existsByNicknameAndIdNot(newNickname, user.getId())) {
            throw new DuplicateNicknameException("이미 사용중인 닉네임입니다.");
        }

        if (!equalsNullable(newPhone, user.getPhone())
                && newPhone != null
                && userRepository.existsByPhoneAndIdNot(newPhone, user.getId())) {
            throw new DuplicatePhoneException("이미 사용중인 전화번호입니다.");
        }

        if (hasPasswordInput) {
            if (user.getPassword() == null) {
                throw new IllegalArgumentException("소셜 로그인 계정은 비밀번호를 변경할 수 없습니다.");
            }

            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
            }

            if (request.newPassword() == null || request.newPassword().isBlank()) {
                throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
            }

            if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
                throw new IllegalArgumentException("새 비밀번호는 현재 비밀번호와 달라야 합니다.");
            }
        }

        user.setName(newName);
        user.setNickname(newNickname);
        user.setPhone(newPhone);

        if (hasPasswordInput) {
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        userRepository.save(user);

        return new MessageResponse("회원정보 수정 완료");
    }

    private void validateAuthenticatedUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("인증된 사용자만 요청할 수 있습니다.");
        }
    }

    private String normalizeNickname(String nickname) {
        String normalized = nickname == null ? null : nickname.trim();

        if (normalized == null || normalized.isBlank()) {
            throw new IllegalArgumentException("닉네임은 비어 있을 수 없습니다.");
        }

        return normalized;
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        String digitsOnly = phone.replaceAll("\\D", "");

        if (!digitsOnly.matches("^01[0-9]\\d{7,8}$")) {
            throw new IllegalArgumentException("올바른 phone 형식이어야 합니다.");
        }

        if (digitsOnly.length() == 10) {
            return digitsOnly.replaceFirst("(\\d{3})(\\d{3})(\\d{4})", "$1-$2-$3");
        }

        return digitsOnly.replaceFirst("(\\d{3})(\\d{4})(\\d{4})", "$1-$2-$3");
    }

    private boolean equalsNullable(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }
}