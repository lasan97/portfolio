package com.portfolio.backend.controller.user;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.service.user.UserCreditService;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/credits")
@RequiredArgsConstructor
public class UserCreditController {

    private final UserCreditService userCreditService;

    @PostMapping
    public void charging(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         @Valid @RequestBody UserCreditServiceRequest.Increase request) {
        userCreditService.increase(userDetails.getId(), request);
    }
}
