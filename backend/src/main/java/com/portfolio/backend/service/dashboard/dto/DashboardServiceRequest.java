package com.portfolio.backend.service.dashboard.dto;

import com.portfolio.backend.service.common.dto.ServiceBaseRequest;
import jakarta.validation.Valid;
import lombok.*;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DashboardServiceRequest {

    public record Create(
            @NotBlank(message = "제목은 필수 입력 항목입니다")
            String title,
            @NotBlank(message = "내용은 필수 입력 항목입니다")
            String content,
            @Valid
            List<ServiceBaseRequest.ExternalLink> externalLinks
    ) {}

    public record Update(
            @NotBlank(message = "제목은 필수 입력 항목입니다")
            String title,
            @NotBlank(message = "내용은 필수 입력 항목입니다")
            String content,
            @Valid
            List<ServiceBaseRequest.ExternalLink> externalLinks
    ) {}
}
