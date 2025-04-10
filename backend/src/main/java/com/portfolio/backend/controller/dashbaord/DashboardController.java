package com.portfolio.backend.controller.dashbaord;

import com.portfolio.backend.domain.dashboard.dto.DashboardDto;
import com.portfolio.backend.domain.dashboard.dto.DashboardUpdateRequest;
import com.portfolio.backend.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 대시보드 관련 API 엔드포인트를 제공하는 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 대시보드 정보 조회 API
     * 모든 사용자가 접근 가능
     *
     * @return 대시보드 정보
     */
    @GetMapping
    public ResponseEntity<DashboardDto> getDashboard() {
        log.debug("대시보드 정보 조회 요청");
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    /**
     * 대시보드 정보 수정 API
     * ADMIN 권한을 가진 사용자만 접근 가능
     *
     * @param request 수정할 정보
     * @param userDetails 인증된 사용자 정보
     * @return 수정된 대시보드 정보
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardDto> updateDashboard(
            @Valid @RequestBody DashboardUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("대시보드 정보 수정 요청: {}", request);
        return ResponseEntity.ok(dashboardService.updateDashboard(request, userDetails.getUsername()));
    }

    /**
     * 초기 대시보드 정보 생성 API
     * ADMIN 권한을 가진 사용자만 접근 가능
     *
     * @param userDetails 인증된 사용자 정보
     * @return 생성된 대시보드 정보
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DashboardDto> initializeDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("대시보드 초기화 요청");
        return ResponseEntity.ok(dashboardService.createInitialDashboard(userDetails.getUsername()));
    }
}
