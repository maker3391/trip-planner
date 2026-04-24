package com.fiveguys.trip_planner.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {

        String errorMessage = "소셜 로그인 중 문제가 발생했습니다. 다시 시도해주세요.";

        if (exception instanceof OAuth2AuthenticationException oauthException
                && oauthException.getError() != null
                && oauthException.getError().getDescription() != null
                && !oauthException.getError().getDescription().isBlank()) {
            errorMessage = oauthException.getError().getDescription();
        } else if (exception.getCause() != null
                && exception.getCause().getMessage() != null
                && !exception.getCause().getMessage().isBlank()) {
            errorMessage = exception.getCause().getMessage();
        } else if (exception.getMessage() != null
                && !exception.getMessage().isBlank()) {
            errorMessage = exception.getMessage();
        }

        String redirectUrl =
                "http://localhost:5173/login?error=oauth2&message=" +
                        URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);

        response.sendRedirect(redirectUrl);
    }
}