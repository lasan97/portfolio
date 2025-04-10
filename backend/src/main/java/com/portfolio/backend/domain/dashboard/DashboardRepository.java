package com.portfolio.backend.domain.dashboard;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 대시보드 정보에 대한 데이터 접근 인터페이스
 */
@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    /**
     * 가장 최근에 업데이트된 대시보드 정보를 조회
     * @return 최신 대시보드 정보
     */
    Optional<Dashboard> findFirstByOrderByUpdatedAtDesc();
}
