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

        OAuth2UserInfo userInfo = extractGoogleUserInfo(registrationId, attributes);

        User user = userRepository.findByProviderAndProviderId(userInfo.provider(), userInfo.providerId())
                .map(existingUser -> updateExistingOAuthUser(existingUser, userInfo))
                .orElseGet(() -> handleEmailBasedUserLinkOrCreate(userInfo));

        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())),
                attributes,
                "sub"
        );
    }

    private OAuth2UserInfo extractGoogleUserInfo(String registrationId, Map<String, Object> attributes) {
        if (!"google".equals(registrationId)) {
            throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 제공자입니다.");
        }

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = (String) attributes.get("sub");

        if (email == null || providerId == null) {
            throw new OAuth2AuthenticationException("Google 사용자 정보가 올바르지 않습니다.");
        }

        return new OAuth2UserInfo(email, name, "google", providerId);
    }

    private User updateExistingOAuthUser(User user, OAuth2UserInfo userInfo) {
        user.setEmail(userInfo.email());
        user.setName(userInfo.name());
        return userRepository.save(user);
    }

    private User handleEmailBasedUserLinkOrCreate(OAuth2UserInfo userInfo) {
        return userRepository.findByEmail(userInfo.email())
                .map(existingUser -> connectExistingUser(existingUser, userInfo))
                .orElseGet(() -> createNewOAuthUser(userInfo));
    }

    private User connectExistingUser(User user, OAuth2UserInfo userInfo) {
        user.setProvider(userInfo.provider());
        user.setProviderId(userInfo.providerId());
        user.setName(userInfo.name());
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