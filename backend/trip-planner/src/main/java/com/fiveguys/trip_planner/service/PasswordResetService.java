package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendResetLink(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입되지 않은 이메일입니다."));

        validateLocalAccountForPasswordReset(user);

        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("reset_token:" + token, email, 15, TimeUnit.MINUTES);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[TPlanner] 비밀번호 재설정 안내입니다.");
            helper.setFrom("tndnjs981102@gmail.com", "TPlanner");

            String resetLink = "http://localhost:5173/reset-password?token=" + token;

            String content = "<h3>안녕하세요, TPlanner입니다.</h3>" +
                    "<p>아래 링크를 클릭하여 비밀번호를 재설정해 주세요.</p>" +
                    "<a href='" + resetLink + "'>비밀번호 재설정하러 가기</a>" +
                    "<p>본 링크는 15분 후 만료됩니다.</p>";

            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException("이메일 발송 중 오류가 발생했습니다. 다시 시도해주세요.", e);
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = redisTemplate.opsForValue().get("reset_token:" + token);

        if (email == null) {
            throw new IllegalArgumentException("만료되었거나 유효하지 않은 토큰입니다.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        validateLocalAccountForPasswordReset(user);

        user.setPassword(passwordEncoder.encode(newPassword));

        redisTemplate.delete("reset_token:" + token);
    }

    private void validateLocalAccountForPasswordReset(User user) {
        String provider = user.getProvider();

        if (provider == null || provider.isBlank()) {
            return;
        }

        throw new IllegalArgumentException(buildProviderResetMessage(provider));
    }

    private String buildProviderResetMessage(String provider) {
        return switch (provider) {
            case "google" -> "Google 로그인 계정은 비밀번호 재설정을 사용할 수 없습니다.";
            case "kakao" -> "Kakao 로그인 계정은 비밀번호 재설정을 사용할 수 없습니다.";
            default -> "소셜 로그인 계정은 비밀번호 재설정을 사용할 수 없습니다.";
        };
    }
}