package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.entity.RefreshToken;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.RefreshTokenRepository;
import com.fiveguys.trip_planner.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String provider = (String) oAuth2User.getAttributes().get("provider");
        String providerId = (String) oAuth2User.getAttributes().get("providerId");

        if (provider == null || provider.isBlank() || providerId == null || providerId.isBlank()) {
            response.sendRedirect("http://localhost:5173/login?error=oauth2_user_not_found");
            return;
        }

        User user = userRepository.findByProviderAndProviderId(provider, providerId)
                .orElse(null);

        if (user == null) {
            response.sendRedirect("http://localhost:5173/login?error=user_not_found");
            return;
        }

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        RefreshToken refreshTokenEntity = RefreshToken.create(
                user.getId(),
                refreshToken,
                jwtTokenProvider.getRefreshTokenExpiration()
        );

        refreshTokenRepository.save(refreshTokenEntity);

        String redirectUrl =
                "http://localhost:5173/oauth2/callback" +
                        "?accessToken=" + URLEncoder.encode(accessToken, StandardCharsets.UTF_8) +
                        "&refreshToken=" + URLEncoder.encode(refreshToken, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}