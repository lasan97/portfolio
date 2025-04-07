package com.portfolio.backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 프로젝트 관련 DTO 클래스
 * API 계층과 도메인 계층의 분리를 위해 사용합니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String projectUrl;
    private String githubUrl;
    private String technologies;
}
