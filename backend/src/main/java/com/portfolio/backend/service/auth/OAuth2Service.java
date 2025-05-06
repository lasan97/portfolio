package com.portfolio.backend.service.auth;

import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.common.integration.oauth2.github.GithubIntegration;
import com.portfolio.backend.common.integration.oauth2.github.dto.GithubResponse;
import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.common.security.jwt.JwtTokenProvider;
import com.portfolio.backend.common.event.EventPublisher;
import com.portfolio.backend.domain.user.entity.Oauth2ProviderType;
import com.portfolio.backend.domain.user.entity.RoleType;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.auth.dto.OAuth2ServiceResponse.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;
    private final GithubIntegration githubIntegration;
    private final JwtTokenProvider jwtTokenProvider;
    private final EventPublisher eventPublisher;

    @Transactional
    public TokenResponse processGithubLogin(String code) {
        // 1. 인증 코드로 액세스 토큰 요청
        String accessToken = githubIntegration.getAccessToken(code);
        
        // 2. 액세스 토큰으로 사용자 정보 요청
        GithubResponse.UserDto userInfo = githubIntegration.getUserInfo(accessToken);
        String email = githubIntegration.getUserPrimaryEmail(accessToken);

        // 3. 사용자 정보로 회원가입/로그인 처리
        User user = saveOrUpdateUser(userInfo, email);

        // 4. JWT 토큰 생성
        String token = jwtTokenProvider.createToken(user);

        return new TokenResponse(token);
    }
    
    /**
     * 사용자 정보로 회원가입/로그인 처리
     */
    private User saveOrUpdateUser(GithubResponse.UserDto userInfo, String email) {
        String providerId = String.valueOf(userInfo.id());
        String nickname = userInfo.name();
        if (nickname == null) {
            nickname = userInfo.login();
        }
        String profileImageUrl = userInfo.avatarUrl().toString();
        
        Optional<User> userOptional = userRepository.findByProviderAndProviderId(Oauth2ProviderType.GITHUB, providerId);
        
        if (userOptional.isPresent()) {
            // 기존 사용자인 경우 정보 업데이트
            return userOptional.get();
        } else {
            // 신규 사용자인 경우 회원가입 처리
            // 최초 가입자는 어드민
            User newUser = User.builder()
                    .provider(Oauth2ProviderType.GITHUB)
                    .providerId(providerId)
                    .email(email != null ? email : "github_" + providerId + "@example.com")
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .role(userRepository.count() > 0 ? RoleType.USER : RoleType.ADMIN)
                    .build();
            User user = userRepository.save(newUser);

            eventPublisher.publishDomainEventsFrom(user);

            return user;
        }
    }

    public TokenResponse refresh(UserDetailsImpl userDetails) {

        User user = userRepository.findById(userDetails.getId()).orElseThrow(() -> new ResourceNotFoundException("토큰 정보가 잘못 되었습니다."));

        String token = jwtTokenProvider.createToken(user);

        return new TokenResponse(token);
    }
}
