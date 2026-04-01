package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.exception.DuplicateEmailException;
import com.fiveguys.trip_planner.exception.InvalidLoginException;
import com.fiveguys.trip_planner.dto.LoginRequest;
import com.fiveguys.trip_planner.response.LoginResponse;
import com.fiveguys.trip_planner.dto.SignupRequest;
import com.fiveguys.trip_planner.response.SignupResponse;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidLoginException("이메일 또는 비밀번호가 틀렸습니다."));

        if (user.getPassword() == null) {
            throw new InvalidLoginException("소셜 로그인으로 가입한 계정입니다.");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidLoginException("이메일 또는 비밀번호가 틀렸습니다.");
        }

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                "로그인 성공"
        );
    }
}
