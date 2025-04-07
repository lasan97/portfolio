package com.portfolio.backend.api.controller;

import com.portfolio.backend.api.dto.ProjectDTO;
import com.portfolio.backend.api.mapper.ProjectMapper;
import com.portfolio.backend.domain.model.Project;
import com.portfolio.backend.service.interfaces.ProjectService;
import com.portfolio.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 프로젝트 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    /**
     * 모든 프로젝트 목록을 조회합니다.
     * 
     * @return 프로젝트 목록
     */
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projectMapper.toDTOList(projects));
    }

    /**
     * ID로 프로젝트를 조회합니다.
     * 
     * @param id 프로젝트 ID
     * @return 프로젝트 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
        
        return ResponseEntity.ok(projectMapper.toDTO(project));
    }

    /**
     * 새 프로젝트를 생성합니다.
     * 
     * @param projectDTO 프로젝트 정보
     * @return 생성된 프로젝트
     */
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO) {
        Project project = projectMapper.toEntity(projectDTO);
        Project savedProject = projectService.createProject(project);
        
        return new ResponseEntity<>(projectMapper.toDTO(savedProject), HttpStatus.CREATED);
    }

    /**
     * 기존 프로젝트를 업데이트합니다.
     * 
     * @param id 업데이트할 프로젝트 ID
     * @param projectDTO 업데이트할 프로젝트 정보
     * @return 업데이트된 프로젝트
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        Project project = projectMapper.toEntity(projectDTO);
        Project updatedProject = projectService.updateProject(id, project);
        
        return ResponseEntity.ok(projectMapper.toDTO(updatedProject));
    }

    /**
     * 프로젝트를 삭제합니다.
     * 
     * @param id 삭제할 프로젝트 ID
     * @return 성공 메시지
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }
}
