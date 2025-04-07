package com.portfolio.backend.domain.repository;

import com.portfolio.backend.domain.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 프로젝트 관련 데이터 액세스 인터페이스
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
