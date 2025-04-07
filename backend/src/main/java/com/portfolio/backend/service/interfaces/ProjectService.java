package com.portfolio.backend.service.interfaces;

import com.portfolio.backend.domain.model.Project;
import java.util.List;
import java.util.Optional;

/**
 * 프로젝트 서비스 인터페이스
 * 서비스 계층의 추상화를 통해 유지보수성과 테스트 용이성을 높입니다.
 */
public interface ProjectService {

    /**
     * 모든 프로젝트를 가져옵니다.
     * 
     * @return 프로젝트 목록
     */
    List<Project> getAllProjects();

    /**
     * ID로 프로젝트를 조회합니다.
     * 
     * @param id 프로젝트 ID
     * @return 프로젝트 정보
     */
    Optional<Project> getProjectById(Long id);

    /**
     * 새 프로젝트를 생성합니다.
     * 
     * @param project 프로젝트 정보
     * @return 생성된 프로젝트
     */
    Project createProject(Project project);

    /**
     * 기존 프로젝트를 업데이트합니다.
     * 
     * @param id 업데이트할 프로젝트 ID
     * @param project 업데이트할 프로젝트 정보
     * @return 업데이트된 프로젝트
     */
    Project updateProject(Long id, Project project);

    /**
     * 프로젝트를 삭제합니다.
     * 
     * @param id 삭제할 프로젝트 ID
     */
    void deleteProject(Long id);
}
