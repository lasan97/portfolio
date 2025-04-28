package com.portfolio.backend.controller.user;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.service.user.UserCreditService;
import com.portfolio.backend.service.user.dto.UserCreditServiceRequest;
import com.portfolio.backend.service.user.dto.UserCreditServiceResponse.Get;
import com.portfolio.backend.service.user.dto.UserCreditServiceResponse.GetHistoryPage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/credits")
@RequiredArgsConstructor
public class UserCreditController {

    private final UserCreditService userCreditService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Get getCurrentCredit(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return userCreditService.getCurrentCredit(userDetails.getId());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public void increase(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         @Valid @RequestBody UserCreditServiceRequest.Increase request) {
        userCreditService.increase(userDetails.getId(), request);
    }

    @GetMapping("/histoies/page")
    public Page<GetHistoryPage> getHistoryPage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                               @PageableDefault(sort = "id", direction = Sort.Direction.DESC)Pageable pageable) {
        return userCreditService.getHistoryPage(userDetails.getId(), pageable);
    }
}
