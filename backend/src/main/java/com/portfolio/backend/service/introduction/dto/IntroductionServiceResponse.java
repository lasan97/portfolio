package com.portfolio.backend.service.introduction.dto;

import com.portfolio.backend.service.common.dto.ServiceBaseResponse;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IntroductionServiceResponse {

    public record Get(
            Long id,
            String title,
            String content,
            List<ExternalLinkDto> externalLinks,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {

        public record UserDto(
                Long id,
                String name,
                String email,
                String avatarUrl
        ) {}

        public record ExternalLinkDto(
                String name,
                String url,
                String logoUrl
        ) {}
    }
}
