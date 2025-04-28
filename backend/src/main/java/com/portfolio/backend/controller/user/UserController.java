package com.portfolio.backend.controller.user;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.service.user.UserService;
import com.portfolio.backend.service.user.dto.UserServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("isAuthenticated()")
    public UserServiceResponse.Profile getCurrentUser(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		return userService.getUserProfile(userDetails.getUser().id());
    }
}
