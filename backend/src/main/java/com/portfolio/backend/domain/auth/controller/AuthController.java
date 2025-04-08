package com.portfolio.backend.domain.auth.controller;

import com.portfolio.backend.domain.auth.dto.TokenResponseDto;
import com.portfolio.backend.domain.user.dto.UserDto;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.global.dto.ApiResponse;
import com.portfolio.backend.global.security.UserDetailsImpl;
import com.portfolio.backend.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/token")
    public ApiResponse<TokenResponseDto> validateToken(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();
        String token = jwtTokenProvider.createToken(user);
        
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
        
        TokenResponseDto tokenResponseDto = TokenResponseDto.builder()
                .token(token)
                .user(userDto)
                .build();
        
        return ApiResponse.success("토큰이 성공적으로 발급되었습니다.", tokenResponseDto);
    }
}
