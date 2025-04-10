package com.portfolio.backend.service.dashboard;

import com.portfolio.backend.domain.dashboard.dto.DashboardDto;
import com.portfolio.backend.domain.dashboard.dto.DashboardUpdateRequest;

/**
 * 대시보드 서비스 인터페이스
 * 대시보드 정보 관리에 관련된 비즈니스 로직 정의
 */
public interface DashboardService {

    /**
     * 최신 대시보드 정보 조회
     * 
     * @return 최신 대시보드 정보
     */
    DashboardDto getDashboard();

    /**
     * 대시보드 정보 수정 
     * 
     * @param request 수정할 정보
     * @param username 수정자 아이디
     * @return 수정된 대시보드 정보
     */
    DashboardDto updateDashboard(DashboardUpdateRequest request, String username);

    /**
     * 초기 대시보드 정보 생성 (데이터가 없을 경우)
     * 
     * @param username 생성자 아이디
     * @return 생성된 대시보드 정보
     */
    DashboardDto createInitialDashboard(String username);
}
