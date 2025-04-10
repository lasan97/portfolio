package com.portfolio.backend.service.dashboard;

import com.portfolio.backend.domain.dashboard.Dashboard;
import com.portfolio.backend.domain.dashboard.DashboardRepository;
import com.portfolio.backend.domain.dashboard.dto.DashboardDto;
import com.portfolio.backend.domain.dashboard.dto.DashboardUpdateRequest;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 대시보드 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final DashboardRepository dashboardRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardDto getDashboard() {
        Dashboard dashboard = dashboardRepository.findFirstByOrderByUpdatedAtDesc()
                .orElse(createDefaultDashboard());
        return DashboardDto.fromEntity(dashboard);
    }

    @Override
    @Transactional
    public DashboardDto updateDashboard(DashboardUpdateRequest request, String username) {
        Dashboard dashboard = dashboardRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new ResourceNotFoundException("대시보드 정보를 찾을 수 없습니다."));

        dashboard.setTitle(request.getTitle());
        dashboard.setContent(request.getContent());
        dashboard.setGithubUrl(request.getGithubUrl());
        dashboard.setVelogUrl(request.getVelogUrl());
        dashboard.setLinkedinUrl(request.getLinkedinUrl());
        dashboard.setOtherUrl(request.getOtherUrl());
        dashboard.setUpdatedBy(username);
        dashboard.setUpdatedAt(LocalDateTime.now());

        Dashboard savedDashboard = dashboardRepository.save(dashboard);
        log.info("대시보드 정보가 업데이트되었습니다. ID: {}, 사용자: {}", savedDashboard.getId(), username);
        return DashboardDto.fromEntity(savedDashboard);
    }

    @Override
    @Transactional
    public DashboardDto createInitialDashboard(String username) {
        Dashboard dashboard = createDefaultDashboard();
        dashboard.setCreatedBy(username);
        dashboard.setUpdatedBy(username);
        
        Dashboard savedDashboard = dashboardRepository.save(dashboard);
        log.info("초기 대시보드 정보가 생성되었습니다. ID: {}, 사용자: {}", savedDashboard.getId(), username);
        return DashboardDto.fromEntity(savedDashboard);
    }

    /**
     * 기본 대시보드 정보 생성
     * 
     * @return 기본 대시보드 엔티티
     */
    private Dashboard createDefaultDashboard() {
        Dashboard dashboard = new Dashboard();
        dashboard.setTitle("포트폴리오에 오신 것을 환영합니다");
        dashboard.setContent("# 안녕하세요!\n\n저의 포트폴리오에 방문해 주셔서 감사합니다. 저에 대한 소개와 작업물을 이곳에서 확인하실 수 있습니다.");
        dashboard.setGithubUrl("https://github.com");
        dashboard.setVelogUrl("https://velog.io");
        dashboard.setCreatedAt(LocalDateTime.now());
        dashboard.setUpdatedAt(LocalDateTime.now());
        return dashboard;
    }
}
