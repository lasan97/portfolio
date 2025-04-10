package com.portfolio.backend.domain.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * 대시보드 정보 업데이트 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardUpdateRequest {
    
    @NotBlank(message = "제목은 필수 입력 항목입니다")
    private String title;
    
    @NotBlank(message = "내용은 필수 입력 항목입니다")
    private String content;
    
    private String githubUrl;
    private String velogUrl;
    private String linkedinUrl;
    private String otherUrl;
}
