package com.portfolio.backend.controller.auth;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.service.auth.OAuth2Service;
import com.portfolio.backend.service.auth.dto.OAuth2ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
public class OAuth2Controller {

    private final OAuth2Service oAuth2Service;

    @PostMapping("/token")
    public OAuth2ServiceResponse.TokenResponse getToken(@RequestParam String code) {
        return oAuth2Service.processGithubLogin(code);
    }

    @GetMapping("/token/refresh")
    public OAuth2ServiceResponse.TokenResponse refresh(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return oAuth2Service.refresh(userDetails);
    }
}
