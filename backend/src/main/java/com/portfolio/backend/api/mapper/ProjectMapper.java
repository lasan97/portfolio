package com.portfolio.backend.api.mapper;

import com.portfolio.backend.api.dto.ProjectDTO;
import com.portfolio.backend.domain.model.Project;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 프로젝트 모델과 DTO 간의 변환을 담당하는 매퍼 클래스
 */
@Component
public class ProjectMapper {

    /**
     * 도메인 모델을 DTO로 변환합니다.
     * 
     * @param project 프로젝트 모델
     * @return 프로젝트 DTO
     */
    public ProjectDTO toDTO(Project project) {
        return ProjectDTO.builder()
                .id(project.getId())
                .title(project.getTitle())
                .description(project.getDescription())
                .imageUrl(project.getImageUrl())
                .projectUrl(project.getProjectUrl())
                .githubUrl(project.getGithubUrl())
                .technologies(project.getTechnologies())
                .build();
    }

    /**
     * DTO를 도메인 모델로 변환합니다.
     * 
     * @param projectDTO 프로젝트 DTO
     * @return 프로젝트 모델
     */
    public Project toEntity(ProjectDTO projectDTO) {
        return Project.builder()
                .id(projectDTO.getId())
                .title(projectDTO.getTitle())
                .description(projectDTO.getDescription())
                .imageUrl(projectDTO.getImageUrl())
                .projectUrl(projectDTO.getProjectUrl())
                .githubUrl(projectDTO.getGithubUrl())
                .technologies(projectDTO.getTechnologies())
                .build();
    }

    /**
     * 모델 목록을 DTO 목록으로 변환합니다.
     * 
     * @param projects 프로젝트 모델 목록
     * @return 프로젝트 DTO 목록
     */
    public List<ProjectDTO> toDTOList(List<Project> projects) {
        return projects.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
