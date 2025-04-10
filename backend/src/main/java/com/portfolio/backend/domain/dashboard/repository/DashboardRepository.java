package com.portfolio.backend.domain.dashboard.repository;

import com.portfolio.backend.domain.dashboard.entity.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Long> {

    Optional<Dashboard> findFirstByOrderByUpdatedAtDesc();
}
