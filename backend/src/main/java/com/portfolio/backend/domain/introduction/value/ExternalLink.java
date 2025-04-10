package com.portfolio.backend.domain.introduction.value;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Embeddable
public class ExternalLink {

    private String name;
    private String url;
    private String logoUrl;

    public ExternalLink(String name, String url, String logoUrl) {
        this.name = name;
        this.url = url;
        this.logoUrl = logoUrl;
    }
}
