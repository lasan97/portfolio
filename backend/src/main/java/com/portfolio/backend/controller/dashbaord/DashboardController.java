package com.portfolio.backend.controller.dashbaord;

import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.service.dashboard.DashboardService;
import com.portfolio.backend.service.dashboard.dto.DashboardServiceRequest.Create;
import com.portfolio.backend.service.dashboard.dto.DashboardServiceRequest.Update;
import com.portfolio.backend.service.dashboard.dto.DashboardServiceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardServiceResponse.Get getDashboard() {
        return dashboardService.getDashboard();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Long updateDashboard(@Valid @RequestBody Update request,
                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return dashboardService.updateDashboard(request, userDetails);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Long createDashboard(@Valid @RequestBody Create request,
                                @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return dashboardService.create(request, userDetails);
    }
}
