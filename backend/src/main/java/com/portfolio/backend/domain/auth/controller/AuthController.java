package com.portfolio.backend.domain.auth.controller;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.common.security.jwt.JwtTokenProvider;
import com.portfolio.backend.domain.auth.dto.TokenResponseDto;
import com.portfolio.backend.domain.user.dto.UserDto;
import com.portfolio.backend.domain.user.entity.User;
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
