package com.fiveguys.trip_planner.service;

import com.fiveguys.trip_planner.dto.OAuth2UserInfo;
import com.fiveguys.trip_planner.entity.User;
import com.fiveguys.trip_planner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oauth2User = delegate.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oauth2User.getAttributes();

        OAuth2UserInfo userInfo = extractOAuth2UserInfo(registrationId, attributes);

        User user = userRepository.findByProviderAndProviderId(userInfo.provider(), userInfo.providerId())
                .map(existingUser -> updateExistingOAuthUser(existingUser, userInfo))
                .orElseGet(() -> handleOAuthUserCreateOrLink(userInfo));

        Map<String, Object> normalizedAttributes = new HashMap<>(attributes);
        normalizedAttributes.put("email", userInfo.email());
        normalizedAttributes.put("name", userInfo.name());
        normalizedAttributes.put("provider", userInfo.provider());
        normalizedAttributes.put("providerId", userInfo.providerId());

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                normalizedAttributes,
                getNameAttributeKey(registrationId)
        );
    }

    private OAuth2UserInfo extractOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "google" -> extractGoogleUserInfo(attributes);
            case "kakao" -> extractKakaoUserInfo(attributes);
            default -> throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다.");
        };
    }

    private OAuth2UserInfo extractGoogleUserInfo(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");

        if (email == null || email.isBlank() || providerId == null || providerId.isBlank()) {
            throw new OAuth2AuthenticationException("Google 사용자 정보가 올바르지 않습니다.");
        }

        if (name == null || name.isBlank()) {
            name = email;
        }

        return new OAuth2UserInfo(email, name, "google", providerId);
    }

    @SuppressWarnings("unchecked")
    private OAuth2UserInfo extractKakaoUserInfo(Map<String, Object> attributes) {
        Object idValue = attributes.get("id");

        if (idValue == null) {
            throw new OAuth2AuthenticationException("Kakao 사용자 식별값이 없습니다.");
        }

        String providerId = String.valueOf(idValue);

        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        String nickname = null;

        if (properties != null) {
            nickname = (String) properties.get("nickname");
        }

        if (nickname == null || nickname.isBlank()) {
            nickname = "kakao_" + providerId;
        }

        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        String email = null;

        if (kakaoAccount != null) {
            email = (String) kakaoAccount.get("email");
        }

        return new OAuth2UserInfo(email, nickname, "kakao", providerId);
    }

    private String getNameAttributeKey(String registrationId) {
        return switch (registrationId) {
            case "google" -> "sub";
            case "kakao" -> "id";
            default -> throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다.");
        };
    }

    private User updateExistingOAuthUser(User user, OAuth2UserInfo userInfo) {
        if (userInfo.email() != null && !userInfo.email().isBlank()) {
            user.setEmail(userInfo.email());
        }
        user.setName(userInfo.name());
        return userRepository.save(user);
    }

    private User handleOAuthUserCreateOrLink(OAuth2UserInfo userInfo) {
        if (userInfo.email() != null && !userInfo.email().isBlank()) {
            return userRepository.findByEmail(userInfo.email())
                    .map(existingUser -> connectExistingUser(existingUser, userInfo))
                    .orElseGet(() -> createNewOAuthUser(userInfo));
        }

        return createNewOAuthUser(userInfo);
    }

    private User connectExistingUser(User user, OAuth2UserInfo userInfo) {
        user.setProvider(userInfo.provider());
        user.setProviderId(userInfo.providerId());
        user.setName(userInfo.name());

        if (userInfo.email() != null && !userInfo.email().isBlank()) {
            user.setEmail(userInfo.email());
        }

        return userRepository.save(user);
    }

    private User createNewOAuthUser(OAuth2UserInfo userInfo) {
        User newUser = User.createOAuthUser(
                userInfo.email(),
                userInfo.name(),
                userInfo.provider(),
                userInfo.providerId()
        );
        return userRepository.save(newUser);
    }
}