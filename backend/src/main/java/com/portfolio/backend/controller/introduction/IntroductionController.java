package com.portfolio.backend.controller.introduction;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.service.introduction.IntroductionService;
import com.portfolio.backend.service.introduction.dto.IntroductionServiceRequest.Create;
import com.portfolio.backend.service.introduction.dto.IntroductionServiceRequest.Update;
import com.portfolio.backend.service.introduction.dto.IntroductionServiceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/introduction")
@RequiredArgsConstructor
public class IntroductionController {

    private final IntroductionService introductionService;

    @GetMapping
    public IntroductionServiceResponse.Get getIntroduction() {
        return introductionService.getIntroduction();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Long updateIntroduction(@Valid @RequestBody Update request,
                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return introductionService.updateIntroduction(request, userDetails);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Long createIntroduction(@Valid @RequestBody Create request,
                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return introductionService.create(request, userDetails);
    }
}
