package com.portfolio.backend.controller.auth;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.common.security.jwt.JwtTokenProvider;
import com.portfolio.backend.service.auth.dto.TokenResponseDto;
import com.portfolio.backend.service.auth.OAuth2Service;
import com.portfolio.backend.service.user.dto.UserDto;
import com.portfolio.backend.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * OAuth2 인증 처리를 위한 컨트롤러
 * 프론트엔드에서 직접 인증 코드를 받아서 처리할 때 사용
 */
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/token")
    public ResponseEntity<TokenResponseDto> githubCallback(@RequestParam String code) {
        // GitHub 로그인 처리 후 사용자 정보 반환
        UserDto userDto = oAuth2Service.processGithubLogin(code);
        
        // JWT 토큰 생성
        User user = User.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .nickname(userDto.getNickname())
                .role(userDto.getRole())
                .build();
        String token = jwtTokenProvider.createToken(user);
        
        // 응답 생성
        TokenResponseDto response = TokenResponseDto.builder()
                .token(token)
                .user(userDto)
                .build();
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/token/refresh")
    public TokenResponseDto validateToken(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        String token = jwtTokenProvider.createToken(user);

        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();

        return TokenResponseDto.builder()
                .token(token)
                .user(userDto)
                .build();
    }
}
