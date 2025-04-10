package com.portfolio.backend.service.dashboard;

import com.portfolio.backend.common.exception.DomainException;
import com.portfolio.backend.common.exception.ResourceNotFoundException;
import com.portfolio.backend.common.exception.UnprocessableEntityException;
import com.portfolio.backend.common.security.UserDetailsImpl;
import com.portfolio.backend.domain.dashboard.entity.Dashboard;
import com.portfolio.backend.domain.dashboard.repository.DashboardRepository;
import com.portfolio.backend.domain.dashboard.value.ExternalLink;
import com.portfolio.backend.domain.user.entity.User;
import com.portfolio.backend.domain.user.repository.UserRepository;
import com.portfolio.backend.service.dashboard.dto.DashboardServiceMapper;
import com.portfolio.backend.service.dashboard.dto.DashboardServiceRequest.Create;
import com.portfolio.backend.service.dashboard.dto.DashboardServiceRequest.Update;
import com.portfolio.backend.service.dashboard.dto.DashboardServiceResponse.Get;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final DashboardServiceMapper dashboardServiceMapper;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Get getDashboard() {

        Optional<Dashboard> response = dashboardRepository.findFirstByOrderByUpdatedAtDesc();

        if(response.isEmpty()) {
            throw new ResourceNotFoundException("대시보드 정보를 찾을 수 없습니다.");
        }

        return dashboardServiceMapper.toGet(response.get());
    }

    @Transactional
    public Long create(Create request, UserDetailsImpl userDetails) {
        Optional<Dashboard> response = dashboardRepository.findFirstByOrderByUpdatedAtDesc();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new DomainException("사용자 정보가 없습니다. 잘못된 접근입니다."));

        if(response.isPresent()) {
            throw new UnprocessableEntityException("대시보드가 이미 존재합니다.");
        }

        List<ExternalLink> externalLinks = request.externalLinks().stream().map(link ->
                new ExternalLink(link.name(), link.url(), link.logoUrl())).toList();

        Dashboard dashboard = dashboardRepository.save(Dashboard.builder()
                .title(request.title())
                .content(request.content())
                .externalLinks(externalLinks)
                .createdBy(user)
                .createdAt(LocalDateTime.now())
                .build());

        return dashboard.getId();
    }

    @Transactional
    public Long updateDashboard(Update request, UserDetailsImpl userDetails) {

        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new DomainException("사용자 정보가 없습니다. 잘못된 접근입니다."));
        Dashboard dashboard = dashboardRepository.findFirstByOrderByUpdatedAtDesc()
                .orElseThrow(() -> new ResourceNotFoundException("대시보드 정보를 찾을 수 없습니다."));

        List<ExternalLink> externalLinks = request.externalLinks().stream().map(link ->
                new ExternalLink(link.name(), link.url(), link.logoUrl())).toList();

        dashboard.update(request.title(), request.content(), externalLinks, user, LocalDateTime.now());

        return dashboard.getId();
    }
}
