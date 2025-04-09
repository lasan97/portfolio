package com.portfolio.backend.domain.user.controller;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.domain.user.dto.UserDto;
import com.portfolio.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserDto getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		return userService.getUserProfile(userDetails.getUser().getId());
    }
}
