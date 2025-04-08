package com.portfolio.backend.common.security.oauth2;

import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.common.security.oauth2.userinfo.OAuth2UserInfo;
import com.portfolio.backend.common.security.oauth2.userinfo.OAuth2UserInfoFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());
        
        String providerId = userInfo.getId();
        String email = userInfo.getEmail();
        String nickname = userInfo.getName();
        String profileImageUrl = userInfo.getImageUrl();
        
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(registrationId, providerId);
        
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.updateProfile(nickname, profileImageUrl);
            user = userRepository.save(user);
        } else {
            user = userRepository.save(User.builder()
                    .provider(registrationId)
                    .providerId(providerId)
                    .email(email)
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .role(User.Role.USER)
                    .build());
        }
        
        return new UserDetailsImpl(user, oAuth2User.getAttributes());
    }
}
