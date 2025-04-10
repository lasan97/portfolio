package com.portfolio.backend.service.introduction.dto;

import com.portfolio.backend.domain.introduction.entity.Introduction;
import com.portfolio.backend.domain.introduction.value.ExternalLink;
import com.portfolio.backend.service.introduction.dto.IntroductionServiceResponse.Get;
import com.portfolio.backend.service.introduction.dto.IntroductionServiceResponse.Get.ExternalLinkDto;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IntroductionServiceMapper {

    public Get toGet(Introduction introduction) {
        return new Get(
                introduction.getId(),
                introduction.getTitle(),
                introduction.getContent(),
                mapExternalLinks(introduction.getExternalLinks()),
                introduction.getCreatedAt(),
                introduction.getUpdatedAt()
        );
    }

    private List<ExternalLinkDto> mapExternalLinks(List<ExternalLink> externalLinks) {
        return externalLinks.stream()
                .map(link -> new ExternalLinkDto(
                        link.getName(),
                        link.getUrl(),
                        link.getLogoUrl()
                ))
                .toList();
    }
}
