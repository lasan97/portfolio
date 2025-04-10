package com.portfolio.backend.domain.dashboard.dto;

import com.portfolio.backend.domain.dashboard.Dashboard;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 대시보드 정보 전송 객체
 * API 응답 및 요청에 사용됨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private Long id;
    private String title;
    private String content;
    private String githubUrl;
    private String velogUrl;
    private String linkedinUrl;
    private String otherUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    /**
     * 엔티티를 DTO로 변환
     * 
     * @param dashboard 대시보드 엔티티
     * @return 대시보드 DTO
     */
    public static DashboardDto fromEntity(Dashboard dashboard) {
        return DashboardDto.builder()
                .id(dashboard.getId())
                .title(dashboard.getTitle())
                .content(dashboard.getContent())
                .githubUrl(dashboard.getGithubUrl())
                .velogUrl(dashboard.getVelogUrl())
                .linkedinUrl(dashboard.getLinkedinUrl())
                .otherUrl(dashboard.getOtherUrl())
                .createdAt(dashboard.getCreatedAt())
                .updatedAt(dashboard.getUpdatedAt())
                .createdBy(dashboard.getCreatedBy())
                .updatedBy(dashboard.getUpdatedBy())
                .build();
    }

    /**
     * DTO를 엔티티로 변환
     * 
     * @return 대시보드 엔티티
     */
    public Dashboard toEntity() {
        return Dashboard.builder()
                .id(id)
                .title(title)
                .content(content)
                .githubUrl(githubUrl)
                .velogUrl(velogUrl)
                .linkedinUrl(linkedinUrl)
                .otherUrl(otherUrl)
                .build();
    }
}
