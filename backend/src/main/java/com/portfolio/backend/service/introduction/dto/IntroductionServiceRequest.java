package com.portfolio.backend.service.introduction.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class IntroductionServiceRequest {

    private IntroductionServiceRequest() {
        throw new IllegalStateException("유틸리티 클래스는 인스턴스화할 수 없습니다.");
    }

    public record Create(
            @NotBlank(message = "제목은 필수입니다.")
            @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
            String title,

            @NotBlank(message = "내용은 필수입니다.")
            String content,

            @Valid
            List<ExternalLinkDto> externalLinks) {

        public record ExternalLinkDto(
                @NotBlank(message = "이름은 필수입니다.")
                @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
                String name,

                @NotBlank(message = "URL은 필수입니다.")
                @Size(max = 255, message = "URL은 255자를 초과할 수 없습니다.")
                String url,

                String logoUrl) {
        }
    }

    public record Update(
            @NotBlank(message = "제목은 필수입니다.")
            @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.")
            String title,

            @NotBlank(message = "내용은 필수입니다.")
            String content,

            @NotNull(message = "외부 링크 목록은 null일 수 없습니다.")
            @Valid
            List<ExternalLinkDto> externalLinks) {

        public record ExternalLinkDto(
                @NotBlank(message = "이름은 필수입니다.")
                @Size(max = 50, message = "이름은 50자를 초과할 수 없습니다.")
                String name,

                @NotBlank(message = "URL은 필수입니다.")
                @Size(max = 255, message = "URL은 255자를 초과할 수 없습니다.")
                String url,

                String logoUrl) {
        }
    }
}
